package me.ycxmbo.mineStaff.backup;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Manages automated backups of plugin data
 */
public class BackupManager {
    private final MineStaff plugin;
    private final File backupFolder;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private BukkitRunnable backupTask;

    public BackupManager(MineStaff plugin) {
        this.plugin = plugin;
        this.backupFolder = new File(plugin.getDataFolder(), "backups");
        if (!backupFolder.exists()) {
            backupFolder.mkdirs();
        }
    }

    /**
     * Start automatic backups
     */
    public void startAutomaticBackups() {
        boolean enabled = plugin.getConfig().getBoolean("backup.enabled", true);
        if (!enabled) {
            plugin.getLogger().info("Automated backups are disabled.");
            return;
        }

        int intervalHours = plugin.getConfig().getInt("backup.interval_hours", 24);
        long intervalTicks = intervalHours * 60 * 60 * 20L; // Convert hours to ticks

        if (backupTask != null && !backupTask.isCancelled()) {
            backupTask.cancel();
        }

        backupTask = new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getLogger().info("Starting automatic backup...");
                boolean success = createBackup();
                if (success) {
                    plugin.getLogger().info("Automatic backup completed successfully!");
                    cleanOldBackups();
                } else {
                    plugin.getLogger().warning("Automatic backup failed!");
                }
            }
        };

        // Run first backup after 1 minute, then repeat
        backupTask.runTaskTimerAsynchronously(plugin, 1200L, intervalTicks);

        plugin.getLogger().info("Automatic backups enabled. Interval: " + intervalHours + " hours");
    }

    /**
     * Stop automatic backups
     */
    public void stopAutomaticBackups() {
        if (backupTask != null && !backupTask.isCancelled()) {
            backupTask.cancel();
            plugin.getLogger().info("Automatic backups stopped.");
        }
    }

    /**
     * Create a backup manually
     */
    public boolean createBackup() {
        String timestamp = dateFormat.format(new Date());
        File backupFile = new File(backupFolder, "backup-" + timestamp + ".zip");

        try {
            // Get files to backup
            List<File> filesToBackup = getFilesToBackup();

            if (filesToBackup.isEmpty()) {
                plugin.getLogger().warning("No files found to backup!");
                return false;
            }

            // Create ZIP file
            try (FileOutputStream fos = new FileOutputStream(backupFile);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                for (File file : filesToBackup) {
                    if (!file.exists() || !file.isFile()) continue;

                    String relativePath = plugin.getDataFolder().toPath()
                            .relativize(file.toPath())
                            .toString();

                    ZipEntry zipEntry = new ZipEntry(relativePath);
                    zos.putNextEntry(zipEntry);

                    try (FileInputStream fis = new FileInputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fis.read(buffer)) > 0) {
                            zos.write(buffer, 0, length);
                        }
                    }

                    zos.closeEntry();
                }
            }

            long size = backupFile.length() / 1024; // Size in KB
            plugin.getLogger().info("Backup created: " + backupFile.getName() + " (" + size + " KB)");
            return true;

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create backup: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get list of files to backup
     */
    private List<File> getFilesToBackup() {
        List<File> files = new ArrayList<>();
        File dataFolder = plugin.getDataFolder();

        // Config file
        files.add(new File(dataFolder, "config.yml"));

        // Data files
        files.add(new File(dataFolder, "reports.yml"));
        files.add(new File(dataFolder, "infractions.yml"));
        files.add(new File(dataFolder, "warnings.yml"));
        files.add(new File(dataFolder, "notes.yml"));
        files.add(new File(dataFolder, "evidence.yml"));
        files.add(new File(dataFolder, "staffaccounts.yml"));
        files.add(new File(dataFolder, "vanish.yml"));
        files.add(new File(dataFolder, "rollbacks.yml"));
        files.add(new File(dataFolder, "offlineinv.yml"));

        // SQLite database if it exists
        files.add(new File(dataFolder, "minestaff.db"));

        // Filter out non-existent files
        return files.stream()
                .filter(File::exists)
                .filter(File::isFile)
                .toList();
    }

    /**
     * Clean up old backups based on retention policy
     */
    private void cleanOldBackups() {
        int maxBackups = plugin.getConfig().getInt("backup.max_backups", 7);
        int maxAgeDays = plugin.getConfig().getInt("backup.max_age_days", 30);

        File[] backupFiles = backupFolder.listFiles((dir, name) -> name.startsWith("backup-") && name.endsWith(".zip"));
        if (backupFiles == null || backupFiles.length == 0) return;

        // Sort by modification time (newest first)
        Arrays.sort(backupFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

        long currentTime = System.currentTimeMillis();
        long maxAgeMillis = maxAgeDays * 24L * 60 * 60 * 1000;

        int deleted = 0;

        // Delete old backups
        for (int i = 0; i < backupFiles.length; i++) {
            File backup = backupFiles[i];

            // Keep first 'maxBackups' regardless of age
            if (i < maxBackups) {
                // But still check if it's older than max age
                if (currentTime - backup.lastModified() <= maxAgeMillis) {
                    continue;
                }
            }

            // Delete this backup
            if (backup.delete()) {
                deleted++;
                plugin.getLogger().info("Deleted old backup: " + backup.getName());
            }
        }

        if (deleted > 0) {
            plugin.getLogger().info("Cleaned up " + deleted + " old backup(s).");
        }
    }

    /**
     * Restore from a backup file
     */
    public boolean restoreBackup(File backupFile) {
        if (!backupFile.exists() || !backupFile.isFile()) {
            plugin.getLogger().warning("Backup file not found: " + backupFile.getName());
            return false;
        }

        try {
            plugin.getLogger().info("Restoring from backup: " + backupFile.getName());

            // Create temporary restore folder
            File restoreFolder = new File(backupFolder, "restore-temp");
            if (restoreFolder.exists()) {
                deleteDirectory(restoreFolder);
            }
            restoreFolder.mkdirs();

            // Extract ZIP
            try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(new FileInputStream(backupFile))) {
                ZipEntry entry;
                byte[] buffer = new byte[1024];

                while ((entry = zis.getNextEntry()) != null) {
                    File newFile = new File(restoreFolder, entry.getName());
                    newFile.getParentFile().mkdirs();

                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int length;
                        while ((length = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                    }
                }
            }

            // Copy files from restore folder to data folder
            copyDirectory(restoreFolder, plugin.getDataFolder());

            // Clean up
            deleteDirectory(restoreFolder);

            plugin.getLogger().info("Backup restored successfully!");
            plugin.getLogger().warning("Server restart recommended to apply all changes.");

            return true;

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to restore backup: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * List all available backups
     */
    public List<File> listBackups() {
        File[] backupFiles = backupFolder.listFiles((dir, name) -> name.startsWith("backup-") && name.endsWith(".zip"));
        if (backupFiles == null) return new ArrayList<>();

        List<File> backups = Arrays.asList(backupFiles);
        backups.sort((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified())); // Newest first
        return backups;
    }

    // Utility methods

    private void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) return;

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    private void copyDirectory(File source, File destination) throws IOException {
        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdirs();
            }

            String[] files = source.list();
            if (files != null) {
                for (String file : files) {
                    copyDirectory(new File(source, file), new File(destination, file));
                }
            }
        } else {
            try (InputStream in = new FileInputStream(source);
                 OutputStream out = new FileOutputStream(destination)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }
        }
    }
}
