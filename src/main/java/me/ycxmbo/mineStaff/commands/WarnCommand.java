package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.warnings.Warning;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WarnCommand implements CommandExecutor {
    private final MineStaff plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");

    public WarnCommand(MineStaff plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("staffmode.warn")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no_permission", "You don't have permission."));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage:", NamedTextColor.RED));
            sender.sendMessage(Component.text("  /warn <player> <reason> [severity] [duration]", NamedTextColor.GRAY));
            sender.sendMessage(Component.text("  /warn list <player>", NamedTextColor.GRAY));
            sender.sendMessage(Component.text("  /warn remove <id>", NamedTextColor.GRAY));
            sender.sendMessage(Component.text("  /warn clear <player>", NamedTextColor.GRAY));
            sender.sendMessage(Component.text("  /warn gui <player>", NamedTextColor.GRAY));
            sender.sendMessage(Component.text("Severity: LOW, MEDIUM, HIGH, SEVERE", NamedTextColor.DARK_GRAY));
            sender.sendMessage(Component.text("Duration: 1h, 3d, 7d, perm", NamedTextColor.DARK_GRAY));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "list":
                return handleList(sender, args);
            case "remove":
                return handleRemove(sender, args);
            case "clear":
                return handleClear(sender, args);
            case "gui":
                return handleGUI(sender, args);
            default:
                return handleIssue(sender, args);
        }
    }

    private boolean handleIssue(CommandSender sender, String[] args) {
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
            return true;
        }

        // Build reason
        StringBuilder reasonBuilder = new StringBuilder();
        int reasonStart = 1;
        String severity = "MEDIUM";
        long duration = 0; // Permanent by default

        // Check for optional severity and duration
        for (int i = 1; i < args.length; i++) {
            String arg = args[i].toUpperCase();
            if (arg.equals("LOW") || arg.equals("MEDIUM") || arg.equals("HIGH") || arg.equals("SEVERE")) {
                severity = arg;
                reasonStart = i + 1;
            } else if (parseDuration(arg) >= 0) {
                duration = parseDuration(arg);
                continue;
            } else {
                if (i >= reasonStart) {
                    reasonBuilder.append(args[i]).append(" ");
                }
            }
        }

        String reason = reasonBuilder.toString().trim();
        if (reason.isEmpty()) {
            sender.sendMessage(Component.text("You must provide a reason!", NamedTextColor.RED));
            return true;
        }

        // Issue warning
        String issuerName = sender instanceof Player ? sender.getName() : "Console";
        java.util.UUID issuerUuid = sender instanceof Player ? ((Player) sender).getUniqueId() : new java.util.UUID(0, 0);

        Warning warning = plugin.getWarningManager().issueWarning(
                target.getUniqueId(), target.getName(),
                issuerUuid, issuerName,
                reason, severity, duration
        );

        // Notify issuer
        sender.sendMessage(Component.text("Warning issued to ", NamedTextColor.GREEN)
                .append(Component.text(target.getName(), NamedTextColor.YELLOW))
                .append(Component.text(" (ID: #" + warning.getId() + ")", NamedTextColor.GRAY)));

        // Notify target
        target.sendMessage(Component.text("═══════════════════════════════════", NamedTextColor.RED));
        target.sendMessage(Component.text("⚠ WARNING ⚠", NamedTextColor.RED).decorate(net.kyori.adventure.text.format.TextDecoration.BOLD));
        target.sendMessage(Component.text(""));
        target.sendMessage(Component.text("Reason: ", NamedTextColor.GRAY)
                .append(Component.text(reason, NamedTextColor.WHITE)));
        target.sendMessage(Component.text("Severity: ", NamedTextColor.GRAY)
                .append(Component.text(severity, getSeverityColor(severity))));
        target.sendMessage(Component.text("Issued by: ", NamedTextColor.GRAY)
                .append(Component.text(issuerName, NamedTextColor.YELLOW)));

        List<Warning> activeWarnings = plugin.getWarningManager().getActiveWarnings(target.getUniqueId());
        target.sendMessage(Component.text(""));
        target.sendMessage(Component.text("Active warnings: ", NamedTextColor.GRAY)
                .append(Component.text(activeWarnings.size(), NamedTextColor.RED)));
        target.sendMessage(Component.text("═══════════════════════════════════", NamedTextColor.RED));

        // Play sound
        plugin.getSoundManager().playSound(target, "warning.received");

        // Notify staff
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("staffmode.alerts") && !staff.equals(sender)) {
                staff.sendMessage(Component.text("[Staff] ", NamedTextColor.DARK_GRAY)
                        .append(Component.text(issuerName, NamedTextColor.YELLOW))
                        .append(Component.text(" warned ", NamedTextColor.GRAY))
                        .append(Component.text(target.getName(), NamedTextColor.YELLOW))
                        .append(Component.text(": " + reason, NamedTextColor.WHITE)));
            }
        }

        return true;
    }

    private boolean handleList(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /warn list <player>", NamedTextColor.RED));
            return true;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
            return true;
        }

        List<Warning> warnings = plugin.getWarningManager().getWarnings(target.getUniqueId());
        List<Warning> activeWarnings = plugin.getWarningManager().getActiveWarnings(target.getUniqueId());

        sender.sendMessage(Component.text("═════════════════════════════════", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Warnings for ", NamedTextColor.YELLOW)
                .append(Component.text(target.getName(), NamedTextColor.GOLD)));
        sender.sendMessage(Component.text("Active: ", NamedTextColor.GRAY)
                .append(Component.text(activeWarnings.size(), NamedTextColor.GREEN))
                .append(Component.text(" | Total: ", NamedTextColor.GRAY))
                .append(Component.text(warnings.size(), NamedTextColor.YELLOW)));
        sender.sendMessage(Component.text("═════════════════════════════════", NamedTextColor.GOLD));

        if (warnings.isEmpty()) {
            sender.sendMessage(Component.text("No warnings found.", NamedTextColor.GRAY));
            return true;
        }

        for (Warning warning : warnings) {
            String status = warning.isActive() ? "§a✓" : "§7✗";
            sender.sendMessage(Component.text(status + " #" + warning.getId() + " | " + warning.getSeverity() + " | " + warning.getReason())
                    .color(warning.isActive() ? NamedTextColor.WHITE : NamedTextColor.DARK_GRAY));
            sender.sendMessage(Component.text("  By: " + warning.getIssuerName() + " | " + dateFormat.format(new Date(warning.getTimestamp())))
                    .color(NamedTextColor.DARK_GRAY));
        }

        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /warn remove <id>", NamedTextColor.RED));
            return true;
        }

        int id;
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Invalid warning ID!", NamedTextColor.RED));
            return true;
        }

        if (plugin.getWarningManager().removeWarning(id)) {
            sender.sendMessage(Component.text("Warning #" + id + " removed.", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("Warning not found!", NamedTextColor.RED));
        }

        return true;
    }

    private boolean handleClear(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /warn clear <player>", NamedTextColor.RED));
            return true;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
            return true;
        }

        int removed = plugin.getWarningManager().clearWarnings(target.getUniqueId());
        sender.sendMessage(Component.text("Cleared " + removed + " warning(s) for " + target.getName(), NamedTextColor.GREEN));

        return true;
    }

    private boolean handleGUI(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /warn gui <player>", NamedTextColor.RED));
            return true;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
            return true;
        }

        // Open warnings GUI
        plugin.getWarningsGUI().open(player, target);

        return true;
    }

    private long parseDuration(String duration) {
        duration = duration.toLowerCase();
        if (duration.equals("perm") || duration.equals("permanent")) {
            return 0;
        }

        try {
            char unit = duration.charAt(duration.length() - 1);
            int amount = Integer.parseInt(duration.substring(0, duration.length() - 1));

            return switch (unit) {
                case 'h' -> TimeUnit.HOURS.toMillis(amount);
                case 'd' -> TimeUnit.DAYS.toMillis(amount);
                case 'w' -> TimeUnit.DAYS.toMillis(amount * 7L);
                case 'm' -> TimeUnit.DAYS.toMillis(amount * 30L);
                default -> -1;
            };
        } catch (Exception e) {
            return -1;
        }
    }

    private NamedTextColor getSeverityColor(String severity) {
        return switch (severity.toUpperCase()) {
            case "LOW" -> NamedTextColor.GREEN;
            case "MEDIUM" -> NamedTextColor.YELLOW;
            case "HIGH" -> NamedTextColor.GOLD;
            case "SEVERE" -> NamedTextColor.RED;
            default -> NamedTextColor.GRAY;
        };
    }
}
