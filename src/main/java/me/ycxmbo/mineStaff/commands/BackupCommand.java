package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BackupCommand implements CommandExecutor {
    private final MineStaff plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");

    public BackupCommand(MineStaff plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("staffmode.backup.manage")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no_permission", "You don't have permission."));
            return true;
        }

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
            case "make":
                handleCreate(sender);
                break;
            case "list":
                handleList(sender);
                break;
            case "restore":
                handleRestore(sender, args);
                break;
            case "auto":
            case "toggle":
                handleToggle(sender);
                break;
            default:
                showHelp(sender);
                break;
        }

        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(Component.text("═══════════════════════════════", NamedTextColor.GOLD));
        sender.sendMessage(Component.text(" Backup Management", NamedTextColor.YELLOW, TextDecoration.BOLD));
        sender.sendMessage(Component.text("═══════════════════════════════", NamedTextColor.GOLD));
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("/backupdata create", NamedTextColor.YELLOW)
                .append(Component.text(" - Create backup now", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/backupdata list", NamedTextColor.YELLOW)
                .append(Component.text(" - List all backups", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/backupdata restore <file>", NamedTextColor.YELLOW)
                .append(Component.text(" - Restore from backup", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/backupdata auto", NamedTextColor.YELLOW)
                .append(Component.text(" - Toggle automatic backups", NamedTextColor.GRAY)));
    }

    private void handleCreate(CommandSender sender) {
        sender.sendMessage(Component.text("Creating backup...", NamedTextColor.YELLOW));

        new BukkitRunnable() {
            @Override
            public void run() {
                boolean success = plugin.getBackupManager().createBackup();

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (success) {
                            sender.sendMessage(Component.text("✓ Backup created successfully!", NamedTextColor.GREEN));
                        } else {
                            sender.sendMessage(Component.text("✗ Backup failed! Check console for details.", NamedTextColor.RED));
                        }
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);
    }

    private void handleList(CommandSender sender) {
        List<File> backups = plugin.getBackupManager().listBackups();

        sender.sendMessage(Component.text("═══════════════════════════════", NamedTextColor.GOLD));
        sender.sendMessage(Component.text(" Available Backups (" + backups.size() + ")", NamedTextColor.YELLOW, TextDecoration.BOLD));
        sender.sendMessage(Component.text("═══════════════════════════════", NamedTextColor.GOLD));

        if (backups.isEmpty()) {
            sender.sendMessage(Component.text("No backups found.", NamedTextColor.GRAY));
            return;
        }

        int count = 0;
        for (File backup : backups) {
            if (count++ >= 10) {
                sender.sendMessage(Component.text("... and " + (backups.size() - 10) + " more", NamedTextColor.DARK_GRAY));
                break;
            }

            long size = backup.length() / 1024; // KB
            String date = dateFormat.format(new Date(backup.lastModified()));

            Component line = Component.text()
                    .append(Component.text("  • ", NamedTextColor.DARK_GRAY))
                    .append(Component.text(backup.getName(), NamedTextColor.YELLOW))
                    .append(Component.text(" (" + size + " KB)", NamedTextColor.GRAY))
                    .clickEvent(ClickEvent.suggestCommand("/backupdata restore " + backup.getName()))
                    .hoverEvent(HoverEvent.showText(
                            Component.text("Created: " + date, NamedTextColor.WHITE)
                                    .append(Component.text("\n\nClick to restore", NamedTextColor.GRAY))))
                    .build();

            sender.sendMessage(line);
        }

        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("Click a backup to restore it", NamedTextColor.DARK_GRAY, TextDecoration.ITALIC));
    }

    private void handleRestore(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /backupdata restore <filename>", NamedTextColor.RED));
            return;
        }

        String filename = args[1];
        File backupFolder = new File(plugin.getDataFolder(), "backups");
        File backupFile = new File(backupFolder, filename);

        if (!backupFile.exists()) {
            sender.sendMessage(Component.text("Backup file not found: " + filename, NamedTextColor.RED));
            return;
        }

        sender.sendMessage(Component.text("⚠ WARNING", NamedTextColor.RED, TextDecoration.BOLD));
        sender.sendMessage(Component.text("Restoring will overwrite current data!", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("[CONFIRM RESTORE]", NamedTextColor.GREEN, TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.runCommand("/backupdata confirmrestore " + filename))
                .hoverEvent(HoverEvent.showText(Component.text("Click to confirm restoration"))));
        sender.sendMessage(Component.text("[CANCEL]", NamedTextColor.RED, TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.runCommand("/backupdata list"))
                .hoverEvent(HoverEvent.showText(Component.text("Cancel and go back"))));
    }

    private void handleToggle(CommandSender sender) {
        boolean currentlyEnabled = plugin.getConfig().getBoolean("backup.enabled", true);
        boolean newState = !currentlyEnabled;

        plugin.getConfig().set("backup.enabled", newState);
        plugin.saveConfig();

        if (newState) {
            plugin.getBackupManager().startAutomaticBackups();
            sender.sendMessage(Component.text("✓ Automatic backups enabled!", NamedTextColor.GREEN));
        } else {
            plugin.getBackupManager().stopAutomaticBackups();
            sender.sendMessage(Component.text("✗ Automatic backups disabled.", NamedTextColor.YELLOW));
        }
    }
}
