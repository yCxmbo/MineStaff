package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.migration.DataMigrationTool;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Command to migrate data between YAML and SQL storage
 */
public class MigrateCommand implements CommandExecutor {
    private final MineStaff plugin;
    private boolean isRunning = false;

    public MigrateCommand(MineStaff plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("staffmode.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (isRunning) {
            sender.sendMessage("§cA migration is already in progress!");
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
            sender.sendMessage("§cInvalid direction! Use 'yaml-to-sql' or 'sql-to-yaml'");
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

        // Run migration asynchronously
        new BukkitRunnable() {
            @Override
            public void run() {
                DataMigrationTool.MigrationResult result;

                if (direction.equals("yaml-to-sql")) {
                    result = migrationTool.migrateYamlToSql(sender::sendMessage);
                } else {
                    result = migrationTool.migrateSqlToYaml(sender::sendMessage);
                }

                // Send summary
                sender.sendMessage("§e========================================");
                if (result.success) {
                    sender.sendMessage("§a§lMigration Completed Successfully!");
                    sender.sendMessage("§7");
                    sender.sendMessage("§7Statistics:");
                    for (DataMigrationTool.MigrationStats stat : result.stats.values()) {
                        sender.sendMessage(String.format("  §f%s: §a%d migrated",
                                stat.dataType, stat.migrated));
                    }
                } else {
                    sender.sendMessage("§c§lMigration Failed!");
                    sender.sendMessage("§7Error: §f" + result.message);
                }
                sender.sendMessage("§e========================================");

                isRunning = false;
            }
        }.runTaskAsynchronously(plugin);

        return true;
    }
}
