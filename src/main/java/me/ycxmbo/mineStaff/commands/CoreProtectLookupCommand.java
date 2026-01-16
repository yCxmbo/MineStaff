package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.integrations.CoreProtectIntegration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Command for CoreProtect block logging lookups
 */
public class CoreProtectLookupCommand implements CommandExecutor {
    private final MineStaff plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd HH:mm:ss");

    public CoreProtectLookupCommand(MineStaff plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (!player.hasPermission("staffmode.coreprotect")) {
            player.sendMessage("§cYou don't have permission to use CoreProtect lookups.");
            return true;
        }

        if (!plugin.getCoreProtectIntegration().isEnabled()) {
            player.sendMessage("§cCoreProtect integration is not available.");
            return true;
        }

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "block", "here" -> handleBlockLookup(player, args);
            case "player", "user" -> handlePlayerLookup(player, args);
            case "nearby", "area" -> handleNearbyLookup(player, args);
            case "rollback", "rb" -> handleRollback(player, args);
            case "restore", "rs" -> handleRestore(player, args);
            default -> showHelp(player);
        }

        return true;
    }

    private void showHelp(Player player) {
        player.sendMessage("§6§l=== CoreProtect Lookup Commands ===");
        player.sendMessage("§e/co block [time] [radius] - §7Look up block at target");
        player.sendMessage("§e/co player <name> [time] [radius] - §7Look up player actions");
        player.sendMessage("§e/co nearby [time] [radius] - §7Look up nearby changes");
        player.sendMessage("§e/co rollback <player> [time] [radius] - §7Rollback player changes");
        player.sendMessage("§e/co restore <player> [time] [radius] - §7Restore rolled back changes");
        player.sendMessage("§7");
        player.sendMessage("§7Time format: 5m, 2h, 3d (default: 1h)");
        player.sendMessage("§7Radius: blocks from center (default: 5)");
    }

    private void handleBlockLookup(Player player, String[] args) {
        Block target = player.getTargetBlockExact(100);
        if (target == null || target.getType() == Material.AIR) {
            player.sendMessage("§cYou must be looking at a block!");
            return;
        }

        int timeInSeconds = args.length > 1 ? parseTime(args[1]) : 3600; // Default 1 hour
        int radius = args.length > 2 ? parseRadius(args[2]) : 0; // Exact block by default

        player.sendMessage("§7Looking up block changes at " + target.getType().name() + "...");

        List<CoreProtectIntegration.BlockChange> changes = plugin.getCoreProtectIntegration()
                .lookupBlock(target.getLocation(), timeInSeconds, radius);

        if (changes.isEmpty()) {
            player.sendMessage("§7No changes found in the last " + formatTime(timeInSeconds) + ".");
            return;
        }

        player.sendMessage("§6§l=== Block Changes (showing " + Math.min(10, changes.size()) + "/" + changes.size() + ") ===");
        for (int i = 0; i < Math.min(10, changes.size()); i++) {
            CoreProtectIntegration.BlockChange change = changes.get(i);
            displayChange(player, change);
        }

        if (changes.size() > 10) {
            player.sendMessage("§7... and " + (changes.size() - 10) + " more changes.");
        }
    }

    private void handlePlayerLookup(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /co player <name> [time] [radius]");
            return;
        }

        String targetPlayer = args[1];
        int timeInSeconds = args.length > 2 ? parseTime(args[2]) : 3600;
        int radius = args.length > 3 ? parseRadius(args[3]) : 10;

        player.sendMessage("§7Looking up actions by §f" + targetPlayer + "§7...");

        List<CoreProtectIntegration.BlockChange> changes = plugin.getCoreProtectIntegration()
                .lookupPlayer(targetPlayer, timeInSeconds, player.getLocation(), radius);

        if (changes.isEmpty()) {
            player.sendMessage("§7No changes found by " + targetPlayer + " in the last " + formatTime(timeInSeconds) + ".");
            return;
        }

        player.sendMessage("§6§l=== " + targetPlayer + "'s Changes (showing " + Math.min(10, changes.size()) + "/" + changes.size() + ") ===");
        for (int i = 0; i < Math.min(10, changes.size()); i++) {
            CoreProtectIntegration.BlockChange change = changes.get(i);
            displayChange(player, change);
        }

        if (changes.size() > 10) {
            player.sendMessage("§7... and " + (changes.size() - 10) + " more changes.");
        }
    }

    private void handleNearbyLookup(Player player, String[] args) {
        int timeInSeconds = args.length > 1 ? parseTime(args[1]) : 3600;
        int radius = args.length > 2 ? parseRadius(args[2]) : 5;

        player.sendMessage("§7Looking up nearby changes (radius: " + radius + ")...");

        List<CoreProtectIntegration.BlockChange> changes = plugin.getCoreProtectIntegration()
                .lookupBlock(player.getLocation(), timeInSeconds, radius);

        if (changes.isEmpty()) {
            player.sendMessage("§7No changes found nearby in the last " + formatTime(timeInSeconds) + ".");
            return;
        }

        player.sendMessage("§6§l=== Nearby Changes (showing " + Math.min(10, changes.size()) + "/" + changes.size() + ") ===");
        for (int i = 0; i < Math.min(10, changes.size()); i++) {
            CoreProtectIntegration.BlockChange change = changes.get(i);
            displayChange(player, change);
        }

        if (changes.size() > 10) {
            player.sendMessage("§7... and " + (changes.size() - 10) + " more changes.");
        }
    }

    private void handleRollback(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /co rollback <player> [time] [radius]");
            return;
        }

        if (!player.hasPermission("staffmode.coreprotect.rollback")) {
            player.sendMessage("§cYou don't have permission to perform rollbacks.");
            return;
        }

        String targetPlayer = args[1];
        int timeInSeconds = args.length > 2 ? parseTime(args[2]) : 3600;
        int radius = args.length > 3 ? parseRadius(args[3]) : 10;

        player.sendMessage("§e§lWARNING: §7Rolling back changes by §f" + targetPlayer +
                "§7 within §f" + radius + " blocks §7from the last §f" + formatTime(timeInSeconds) + "§7...");

        CoreProtectIntegration.RollbackResult result = plugin.getCoreProtectIntegration()
                .rollback(targetPlayer, timeInSeconds, player.getLocation(), radius);

        if (result.success) {
            player.sendMessage("§a§l[CoreProtect] §r" + result.message);
            plugin.getSoundManager().playSound(player, "infraction.added");

            // Log to audit
            plugin.getAuditLogger().log(java.util.Map.of(
                    "type", "coreprotect_rollback",
                    "staff", player.getName(),
                    "target", targetPlayer,
                    "time", String.valueOf(timeInSeconds),
                    "radius", String.valueOf(radius),
                    "blocks", String.valueOf(result.affectedBlocks)
            ));
        } else {
            player.sendMessage("§c§l[CoreProtect] §r" + result.message);
        }
    }

    private void handleRestore(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /co restore <player> [time] [radius]");
            return;
        }

        if (!player.hasPermission("staffmode.coreprotect.rollback")) {
            player.sendMessage("§cYou don't have permission to perform restores.");
            return;
        }

        String targetPlayer = args[1];
        int timeInSeconds = args.length > 2 ? parseTime(args[2]) : 3600;
        int radius = args.length > 3 ? parseRadius(args[3]) : 10;

        player.sendMessage("§e§lWARNING: §7Restoring changes by §f" + targetPlayer +
                "§7 within §f" + radius + " blocks §7from the last §f" + formatTime(timeInSeconds) + "§7...");

        CoreProtectIntegration.RollbackResult result = plugin.getCoreProtectIntegration()
                .restore(targetPlayer, timeInSeconds, player.getLocation(), radius);

        if (result.success) {
            player.sendMessage("§a§l[CoreProtect] §r" + result.message);
            plugin.getSoundManager().playSound(player, "infraction.added");

            // Log to audit
            plugin.getAuditLogger().log(java.util.Map.of(
                    "type", "coreprotect_restore",
                    "staff", player.getName(),
                    "target", targetPlayer,
                    "time", String.valueOf(timeInSeconds),
                    "radius", String.valueOf(radius),
                    "blocks", String.valueOf(result.affectedBlocks)
            ));
        } else {
            player.sendMessage("§c§l[CoreProtect] §r" + result.message);
        }
    }

    private void displayChange(Player player, CoreProtectIntegration.BlockChange change) {
        if (change == null) return;

        String color = switch (change.action) {
            case "broke" -> "§c";
            case "placed" -> "§a";
            case "interacted" -> "§e";
            default -> "§7";
        };

        player.sendMessage(String.format("%s%s §f%s %s%s §7at §f%s §7(%s)",
                color,
                change.getFormattedAge(),
                change.player,
                color,
                change.action,
                change.blockType,
                change.getLocationString()));
    }

    private int parseTime(String input) {
        try {
            if (input.endsWith("s")) {
                return Integer.parseInt(input.substring(0, input.length() - 1));
            } else if (input.endsWith("m")) {
                return Integer.parseInt(input.substring(0, input.length() - 1)) * 60;
            } else if (input.endsWith("h")) {
                return Integer.parseInt(input.substring(0, input.length() - 1)) * 3600;
            } else if (input.endsWith("d")) {
                return Integer.parseInt(input.substring(0, input.length() - 1)) * 86400;
            } else {
                return Integer.parseInt(input); // Assume seconds
            }
        } catch (NumberFormatException e) {
            return 3600; // Default 1 hour
        }
    }

    private int parseRadius(String input) {
        try {
            return Math.max(0, Math.min(100, Integer.parseInt(input))); // Clamp between 0-100
        } catch (NumberFormatException e) {
            return 5; // Default radius
        }
    }

    private String formatTime(int seconds) {
        if (seconds < 60) return seconds + "s";
        if (seconds < 3600) return (seconds / 60) + "m";
        if (seconds < 86400) return (seconds / 3600) + "h";
        return (seconds / 86400) + "d";
    }
}
