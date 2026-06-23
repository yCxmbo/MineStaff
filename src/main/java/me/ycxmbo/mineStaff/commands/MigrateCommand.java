package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ConfigManager;
import me.ycxmbo.mineStaff.migration.DataMigrationTool;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class MigrateCommand implements CommandExecutor {
    private final MineStaff plugin;
    private boolean isRunning = false;

    public MigrateCommand(MineStaff plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        if (!sender.hasPermission("staffmode.admin")) {
            sender.sendMessage(cfg.getMessage("migrate_no_permission", "No permission."));
            return true;
        }
        if (isRunning) {
            sender.sendMessage(cfg.getMessage("migrate_in_progress", "A migration is already in progress!"));
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage("§c§lMineStaff Data Migration Tool");
            sender.sendMessage("§7");
            sender.sendMessage("§eUsage:");
            sender.sendMessage("  §f/migrate yaml-to-sql §7- Migrate all data from YAML to SQL");
            sender.sendMessage("  §f/migrate sql-to-yaml §7- Migrate all data from SQL to YAML");
            sender.sendMessage("§7");
            sender.sendMessage("§c§lWARNING: §7A backup will be created automatically before migration.");
            sender.sendMessage("§7After migration, update storage.mode in config.yml and restart.");
            return true;
        }

        String direction = args[0].toLowerCase();
        if (!direction.equals("yaml-to-sql") && !direction.equals("sql-to-yaml")) {
            sender.sendMessage(cfg.getMessage("migrate_invalid_direction", "Invalid direction! Use 'yaml-to-sql' or 'sql-to-yaml'"));
            return true;
        }

        DataMigrationTool migrationTool = new DataMigrationTool(plugin);
        sender.sendMessage("§e========================================");
        sender.sendMessage("§6§l    MineStaff Data Migration");
        sender.sendMessage("§e========================================");
        sender.sendMessage("§7Direction: §f" + direction.toUpperCase().replace("-", " → "));
        sender.sendMessage("§7");
        sender.sendMessage("§cWARNING: This will modify your data files!");
        sender.sendMessage("§7A backup will be created automatically.");
        sender.sendMessage("§7");

        isRunning = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                DataMigrationTool.MigrationResult result = direction.equals("yaml-to-sql")
                        ? migrationTool.migrateYamlToSql(sender::sendMessage)
                        : migrationTool.migrateSqlToYaml(sender::sendMessage);

                sender.sendMessage("§e========================================");
                if (result.success) {
                    sender.sendMessage(cfg.getMessage("migrate_completed", "Migration completed successfully!"));
                    sender.sendMessage("§7Statistics:");
                    for (DataMigrationTool.MigrationStats stat : result.stats.values()) {
                        sender.sendMessage(String.format("  §f%s: §a%d migrated", stat.dataType, stat.migrated));
                    }
                } else {
                    sender.sendMessage(cfg.getMessage("migrate_failed", "Migration failed: {error}").replace("{error}", result.message));
                }
                sender.sendMessage("§e========================================");
                isRunning = false;
            }
        }.runTaskAsynchronously(plugin);
        return true;
    }
}
