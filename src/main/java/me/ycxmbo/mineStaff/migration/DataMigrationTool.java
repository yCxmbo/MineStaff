package me.ycxmbo.mineStaff.migration;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.evidence.EvidenceManager;
import me.ycxmbo.mineStaff.managers.InfractionManager;
import me.ycxmbo.mineStaff.managers.ReportManager;
import me.ycxmbo.mineStaff.notes.PlayerNotesManager;
import me.ycxmbo.mineStaff.storage.SqlStorage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

/**
 * Tool for migrating data between YAML and SQL storage
 */
public class DataMigrationTool {
    private final MineStaff plugin;
    private final Map<String, MigrationStats> stats = new HashMap<>();

    public DataMigrationTool(MineStaff plugin) {
        this.plugin = plugin;
    }

    /**
     * Migrate all data from YAML to SQL
     */
    public MigrationResult migrateYamlToSql(Consumer<String> progressCallback) {
        SqlStorage sqlStorage = plugin.getStorage();
        if (sqlStorage == null) {
            return new MigrationResult(false, "SQL storage is not initialized. Set storage.mode to sqlite or mysql in config.yml");
        }

        progressCallback.accept("§eStarting YAML to SQL migration...");
        progressCallback.accept("§7Creating backup before migration...");

        // Create backup before migration
        boolean backupSuccess = plugin.getBackupManager().createBackup();
        if (!backupSuccess) {
            return new MigrationResult(false, "Backup failed! Migration aborted for safety.");
        }

        progressCallback.accept("§aBackup created successfully.");
        stats.clear();

        try {
            // Migrate reports
            progressCallback.accept("§bMigrating reports...");
            migrateReportsYamlToSql(progressCallback);

            // Migrate infractions
            progressCallback.accept("§bMigrating infractions...");
            migrateInfractionsYamlToSql(progressCallback);

            // Migrate notes
            progressCallback.accept("§bMigrating notes...");
            migrateNotesYamlToSql(progressCallback);

            // Migrate evidence
            progressCallback.accept("§bMigrating evidence...");
            migrateEvidenceYamlToSql(progressCallback);

            progressCallback.accept("§a§lMigration completed successfully!");
            progressCallback.accept("§e§lIMPORTANT: Update storage.mode in config.yml to 'sqlite' or 'mysql' to use SQL storage.");
            progressCallback.accept("§e§lThen restart the server for changes to take effect.");
            return new MigrationResult(true, "All data migrated successfully", stats);

        } catch (Exception e) {
            progressCallback.accept("§c§lERROR: " + e.getMessage());
            e.printStackTrace();
            return new MigrationResult(false, "Migration failed: " + e.getMessage());
        }
    }

    /**
     * Migrate all data from SQL to YAML
     */
    public MigrationResult migrateSqlToYaml(Consumer<String> progressCallback) {
        SqlStorage sqlStorage = plugin.getStorage();
        if (sqlStorage == null) {
            return new MigrationResult(false, "SQL storage is not initialized. Cannot export from SQL.");
        }

        progressCallback.accept("§eStarting SQL to YAML migration...");
        progressCallback.accept("§7Creating backup before migration...");

        // Create backup
        boolean backupSuccess = plugin.getBackupManager().createBackup();
        if (!backupSuccess) {
            return new MigrationResult(false, "Backup failed! Migration aborted for safety.");
        }

        progressCallback.accept("§aBackup created successfully.");
        stats.clear();

        try {
            // Migrate reports
            progressCallback.accept("§bMigrating reports...");
            migrateReportsSqlToYaml(progressCallback);

            // Migrate infractions
            progressCallback.accept("§bMigrating infractions...");
            migrateInfractionsSqlToYaml(progressCallback);

            // Migrate notes
            progressCallback.accept("§bMigrating notes...");
            migrateNotesSqlToYaml(progressCallback);

            // Migrate evidence
            progressCallback.accept("§bMigrating evidence...");
            migrateEvidenceSqlToYaml(progressCallback);

            progressCallback.accept("§a§lMigration completed successfully!");
            progressCallback.accept("§e§lIMPORTANT: Update storage.mode to 'yaml' in config.yml to use YAML storage.");
            progressCallback.accept("§e§lThen restart the server for changes to take effect.");
            return new MigrationResult(true, "All data migrated successfully", stats);

        } catch (Exception e) {
            progressCallback.accept("§c§lERROR: " + e.getMessage());
            e.printStackTrace();
            return new MigrationResult(false, "Migration failed: " + e.getMessage());
        }
    }

    // ========== YAML to SQL Migration Methods ==========

    private void migrateReportsYamlToSql(Consumer<String> progress) {
        File file = new File(plugin.getDataFolder(), "reports.yml");
        if (!file.exists()) {
            progress.accept("  §7No reports.yml found, skipping.");
            stats.put("reports", new MigrationStats("Reports", 0, 0));
            return;
        }

        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        int count = 0;

        if (yaml.isConfigurationSection("reports")) {
            for (String key : yaml.getConfigurationSection("reports").getKeys(false)) {
                String base = "reports." + key;
                try {
                    UUID id = UUID.fromString(key);
                    UUID reporter = UUID.fromString(yaml.getString(base + ".reporter"));
                    UUID target = UUID.fromString(yaml.getString(base + ".target"));
                    String reason = yaml.getString(base + ".reason", "No reason");
                    long created = yaml.getLong(base + ".created", 0L);
                    String status = yaml.getString(base + ".status", "OPEN");
                    String claimedStr = yaml.getString(base + ".claimedBy");
                    UUID claimedBy = claimedStr == null ? null : UUID.fromString(claimedStr);
                    String category = yaml.getString(base + ".category", "GENERAL");
                    String priority = yaml.getString(base + ".priority", "MEDIUM");
                    long dueBy = yaml.getLong(base + ".dueBy", 0L);
                    long claimedAt = yaml.getLong(base + ".claimedAt", 0L);

                    ReportManager.Report report = new ReportManager.Report(
                            id, reporter, target, reason, created, status, claimedBy,
                            category, priority, dueBy, claimedAt
                    );
                    plugin.getStorage().upsertReport(report);
                    count++;
                } catch (Exception e) {
                    progress.accept("  §cWarning: Failed to migrate report " + key + ": " + e.getMessage());
                }
            }
        }

        stats.put("reports", new MigrationStats("Reports", count, 0));
        progress.accept("  §aMigrated " + count + " reports.");
    }

    private void migrateInfractionsYamlToSql(Consumer<String> progress) {
        File file = new File(plugin.getDataFolder(), "infractions.yml");
        if (!file.exists()) {
            progress.accept("  §7No infractions.yml found, skipping.");
            stats.put("infractions", new MigrationStats("Infractions", 0, 0));
            return;
        }

        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        int count = 0;

        for (String key : yaml.getKeys(false)) {
            try {
                String player = yaml.getString(key + ".player");
                long timestamp = yaml.getLong(key + ".timestamp");
                String staff = yaml.getString(key + ".staff");
                String type = yaml.getString(key + ".type");
                String reason = yaml.getString(key + ".reason");

                plugin.getStorage().addInfraction(
                        UUID.fromString(player),
                        timestamp,
                        staff,
                        type,
                        reason
                );
                count++;
            } catch (Exception e) {
                progress.accept("  §cWarning: Failed to migrate infraction " + key + ": " + e.getMessage());
            }
        }

        stats.put("infractions", new MigrationStats("Infractions", count, 0));
        progress.accept("  §aMigrated " + count + " infractions.");
    }

    private void migrateNotesYamlToSql(Consumer<String> progress) {
        File file = new File(plugin.getDataFolder(), "notes.yml");
        if (!file.exists()) {
            progress.accept("  §7No notes.yml found, skipping.");
            stats.put("notes", new MigrationStats("Notes", 0, 0));
            return;
        }

        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        int count = 0;

        // Notes are stored as "players.{uuid}: [list of 'ts|staff|text']"
        if (yaml.isConfigurationSection("players")) {
            for (String playerUuid : yaml.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID player = UUID.fromString(playerUuid);
                    List<String> noteList = yaml.getStringList("players." + playerUuid);

                    for (String noteStr : noteList) {
                        String[] parts = noteStr.split("\\|", 3);
                        if (parts.length < 3) continue;

                        long ts = Long.parseLong(parts[0]);
                        UUID staff = UUID.fromString(parts[1]);
                        String text = parts[2];

                        plugin.getStorage().addNote(player, staff, text, ts);
                        count++;
                    }
                } catch (Exception e) {
                    progress.accept("  §cWarning: Failed to migrate notes for " + playerUuid + ": " + e.getMessage());
                }
            }
        }

        stats.put("notes", new MigrationStats("Notes", count, 0));
        progress.accept("  §aMigrated " + count + " notes.");
    }

    private void migrateEvidenceYamlToSql(Consumer<String> progress) {
        File file = new File(plugin.getDataFolder(), "evidence.yml");
        if (!file.exists()) {
            progress.accept("  §7No evidence.yml found, skipping.");
            stats.put("evidence", new MigrationStats("Evidence", 0, 0));
            return;
        }

        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        int count = 0;

        // Evidence is stored as "reports.{reportId}.{evidenceId}.{ts|staff|url}"
        if (yaml.isConfigurationSection("reports")) {
            for (String reportUuid : yaml.getConfigurationSection("reports").getKeys(false)) {
                try {
                    UUID reportId = UUID.fromString(reportUuid);
                    String reportPath = "reports." + reportUuid;

                    if (yaml.isConfigurationSection(reportPath)) {
                        for (String evidenceId : yaml.getConfigurationSection(reportPath).getKeys(false)) {
                            String evidencePath = reportPath + "." + evidenceId;
                            long ts = yaml.getLong(evidencePath + ".ts");
                            UUID staff = UUID.fromString(yaml.getString(evidencePath + ".staff"));
                            String url = yaml.getString(evidencePath + ".url");

                            plugin.getStorage().addEvidence(reportId, staff, url, ts);
                            count++;
                        }
                    }
                } catch (Exception e) {
                    progress.accept("  §cWarning: Failed to migrate evidence for report " + reportUuid + ": " + e.getMessage());
                }
            }
        }

        stats.put("evidence", new MigrationStats("Evidence", count, 0));
        progress.accept("  §aMigrated " + count + " evidence entries.");
    }

    // ========== SQL to YAML Migration Methods ==========

    private void migrateReportsSqlToYaml(Consumer<String> progress) {
        List<ReportManager.Report> reports = plugin.getStorage().listReports();

        File file = new File(plugin.getDataFolder(), "reports.yml");
        FileConfiguration yaml = new YamlConfiguration();

        for (ReportManager.Report report : reports) {
            String path = "reports." + report.id.toString();
            yaml.set(path + ".reporter", report.reporter.toString());
            yaml.set(path + ".target", report.target.toString());
            yaml.set(path + ".reason", report.reason);
            yaml.set(path + ".created", report.created);
            yaml.set(path + ".status", report.status);
            if (report.claimedBy != null) {
                yaml.set(path + ".claimedBy", report.claimedBy.toString());
            }
            yaml.set(path + ".category", report.category);
            yaml.set(path + ".priority", report.priority);
            yaml.set(path + ".dueBy", report.dueBy);
            yaml.set(path + ".claimedAt", report.claimedAt);
        }

        try {
            yaml.save(file);
            stats.put("reports", new MigrationStats("Reports", reports.size(), 0));
            progress.accept("  §aMigrated " + reports.size() + " reports.");
        } catch (Exception e) {
            progress.accept("  §cERROR saving reports.yml: " + e.getMessage());
        }
    }

    private void migrateInfractionsSqlToYaml(Consumer<String> progress) {
        List<InfractionManager.Infraction> infractions = plugin.getStorage().getAllInfractions();

        File file = new File(plugin.getDataFolder(), "infractions.yml");
        FileConfiguration yaml = new YamlConfiguration();

        int index = 0;
        for (InfractionManager.Infraction infraction : infractions) {
            String path = String.valueOf(index++);
            yaml.set(path + ".player", infraction.player);
            yaml.set(path + ".timestamp", infraction.timestamp);
            yaml.set(path + ".staff", infraction.staff);
            yaml.set(path + ".type", infraction.type);
            yaml.set(path + ".reason", infraction.reason);
        }

        try {
            yaml.save(file);
            stats.put("infractions", new MigrationStats("Infractions", infractions.size(), 0));
            progress.accept("  §aMigrated " + infractions.size() + " infractions.");
        } catch (Exception e) {
            progress.accept("  §cERROR saving infractions.yml: " + e.getMessage());
        }
    }

    private void migrateNotesSqlToYaml(Consumer<String> progress) {
        Map<UUID, List<PlayerNotesManager.Note>> allNotes = plugin.getStorage().getAllNotes();

        File file = new File(plugin.getDataFolder(), "notes.yml");
        FileConfiguration yaml = new YamlConfiguration();

        int totalCount = 0;
        for (Map.Entry<UUID, List<PlayerNotesManager.Note>> entry : allNotes.entrySet()) {
            String playerPath = "players." + entry.getKey().toString();
            List<String> noteStrings = new ArrayList<>();

            for (PlayerNotesManager.Note note : entry.getValue()) {
                // Format: "ts|staff|text"
                String noteStr = note.ts + "|" + note.staff.toString() + "|" + note.text;
                noteStrings.add(noteStr);
                totalCount++;
            }

            yaml.set(playerPath, noteStrings);
        }

        try {
            yaml.save(file);
            stats.put("notes", new MigrationStats("Notes", totalCount, 0));
            progress.accept("  §aMigrated " + totalCount + " notes.");
        } catch (Exception e) {
            progress.accept("  §cERROR saving notes.yml: " + e.getMessage());
        }
    }

    private void migrateEvidenceSqlToYaml(Consumer<String> progress) {
        Map<UUID, List<EvidenceManager.Evidence>> allEvidence = plugin.getStorage().getAllEvidence();

        File file = new File(plugin.getDataFolder(), "evidence.yml");
        FileConfiguration yaml = new YamlConfiguration();

        int totalCount = 0;
        for (Map.Entry<UUID, List<EvidenceManager.Evidence>> entry : allEvidence.entrySet()) {
            UUID reportId = entry.getKey();

            for (EvidenceManager.Evidence evidence : entry.getValue()) {
                String path = "reports." + reportId.toString() + "." + evidence.id.toString();
                yaml.set(path + ".ts", evidence.ts);
                yaml.set(path + ".staff", evidence.staff.toString());
                yaml.set(path + ".url", evidence.url);
                totalCount++;
            }
        }

        try {
            yaml.save(file);
            stats.put("evidence", new MigrationStats("Evidence", totalCount, 0));
            progress.accept("  §aMigrated " + totalCount + " evidence entries.");
        } catch (Exception e) {
            progress.accept("  §cERROR saving evidence.yml: " + e.getMessage());
        }
    }

    // ========== Helper Classes ==========

    public static class MigrationResult {
        public final boolean success;
        public final String message;
        public final Map<String, MigrationStats> stats;

        public MigrationResult(boolean success, String message) {
            this(success, message, Collections.emptyMap());
        }

        public MigrationResult(boolean success, String message, Map<String, MigrationStats> stats) {
            this.success = success;
            this.message = message;
            this.stats = new HashMap<>(stats);
        }
    }

    public static class MigrationStats {
        public final String dataType;
        public final int migrated;
        public final int failed;

        public MigrationStats(String dataType, int migrated, int failed) {
            this.dataType = dataType;
            this.migrated = migrated;
            this.failed = failed;
        }
    }
}
