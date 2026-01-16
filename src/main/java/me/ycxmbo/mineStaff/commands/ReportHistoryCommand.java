package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ReportManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReportHistoryCommand implements CommandExecutor {
    private final MineStaff plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm");

    public ReportHistoryCommand(MineStaff plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("staffmode.reports.history")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no_permission", "You don't have permission."));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(Component.text("Usage:", NamedTextColor.RED));
            sender.sendMessage(Component.text("  /reporthistory <player> [filed|against|all]", NamedTextColor.GRAY));
            sender.sendMessage(Component.text("  /reporthistory <player> gui", NamedTextColor.GRAY));
            return true;
        }

        String targetName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
            return true;
        }

        // Check for GUI mode
        if (args.length > 1 && args[1].equalsIgnoreCase("gui")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use GUI mode.");
                return true;
            }
            plugin.getReportHistoryGUI().open(player, target);
            return true;
        }

        // Determine mode
        String mode = args.length > 1 ? args[1].toLowerCase() : "all";

        switch (mode) {
            case "filed":
            case "by":
                showReportsFiled(sender, target);
                break;
            case "against":
            case "about":
                showReportsAgainst(sender, target);
                break;
            case "all":
            default:
                showAllReports(sender, target);
                break;
        }

        return true;
    }

    private void showReportsFiled(CommandSender sender, OfflinePlayer target) {
        List<ReportManager.Report> reports = plugin.getReportManager().getAll().stream()
                .filter(r -> r.reporter.equals(target.getUniqueId()))
                .sorted(Comparator.comparingLong(r -> -r.created))
                .collect(Collectors.toList());

        sender.sendMessage(Component.text("═══════════════════════════════════", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Reports Filed by ", NamedTextColor.YELLOW)
                .append(Component.text(target.getName(), NamedTextColor.GOLD)));
        sender.sendMessage(Component.text("Total: " + reports.size(), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("═══════════════════════════════════", NamedTextColor.GOLD));

        if (reports.isEmpty()) {
            sender.sendMessage(Component.text("No reports found.", NamedTextColor.GRAY));
            return;
        }

        int count = 0;
        for (ReportManager.Report report : reports) {
            if (count++ >= 10) {
                sender.sendMessage(Component.text("... and " + (reports.size() - 10) + " more. Use GUI for full view.", NamedTextColor.DARK_GRAY));
                break;
            }
            displayReport(sender, report);
        }
    }

    private void showReportsAgainst(CommandSender sender, OfflinePlayer target) {
        List<ReportManager.Report> reports = plugin.getReportManager().getAll().stream()
                .filter(r -> r.target.equals(target.getUniqueId()))
                .sorted(Comparator.comparingLong(r -> -r.created))
                .collect(Collectors.toList());

        sender.sendMessage(Component.text("═══════════════════════════════════", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Reports Against ", NamedTextColor.YELLOW)
                .append(Component.text(target.getName(), NamedTextColor.GOLD)));
        sender.sendMessage(Component.text("Total: " + reports.size(), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("═══════════════════════════════════", NamedTextColor.GOLD));

        if (reports.isEmpty()) {
            sender.sendMessage(Component.text("No reports found.", NamedTextColor.GRAY));
            return;
        }

        int count = 0;
        for (ReportManager.Report report : reports) {
            if (count++ >= 10) {
                sender.sendMessage(Component.text("... and " + (reports.size() - 10) + " more. Use GUI for full view.", NamedTextColor.DARK_GRAY));
                break;
            }
            displayReport(sender, report);
        }
    }

    private void showAllReports(CommandSender sender, OfflinePlayer target) {
        List<ReportManager.Report> filed = plugin.getReportManager().getAll().stream()
                .filter(r -> r.reporter.equals(target.getUniqueId()))
                .collect(Collectors.toList());

        List<ReportManager.Report> against = plugin.getReportManager().getAll().stream()
                .filter(r -> r.target.equals(target.getUniqueId()))
                .collect(Collectors.toList());

        sender.sendMessage(Component.text("═══════════════════════════════════", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Report History: ", NamedTextColor.YELLOW)
                .append(Component.text(target.getName(), NamedTextColor.GOLD)));
        sender.sendMessage(Component.text("Filed: ", NamedTextColor.GRAY)
                .append(Component.text(filed.size(), NamedTextColor.GREEN))
                .append(Component.text(" | Against: ", NamedTextColor.GRAY))
                .append(Component.text(against.size(), NamedTextColor.RED)));
        sender.sendMessage(Component.text("═══════════════════════════════════", NamedTextColor.GOLD));

        // Show against reports first (more important)
        if (!against.isEmpty()) {
            sender.sendMessage(Component.text("\nReports Against:", NamedTextColor.RED, TextDecoration.BOLD));
            int count = 0;
            for (ReportManager.Report report : against.stream()
                    .sorted(Comparator.comparingLong(r -> -r.created))
                    .limit(5)
                    .collect(Collectors.toList())) {
                displayReport(sender, report);
            }
            if (against.size() > 5) {
                sender.sendMessage(Component.text("... and " + (against.size() - 5) + " more", NamedTextColor.DARK_GRAY));
            }
        }

        if (!filed.isEmpty()) {
            sender.sendMessage(Component.text("\nReports Filed:", NamedTextColor.GREEN, TextDecoration.BOLD));
            for (ReportManager.Report report : filed.stream()
                    .sorted(Comparator.comparingLong(r -> -r.created))
                    .limit(5)
                    .collect(Collectors.toList())) {
                displayReport(sender, report);
            }
            if (filed.size() > 5) {
                sender.sendMessage(Component.text("... and " + (filed.size() - 5) + " more", NamedTextColor.DARK_GRAY));
            }
        }

        if (filed.isEmpty() && against.isEmpty()) {
            sender.sendMessage(Component.text("No reports found.", NamedTextColor.GRAY));
        }

        // Suggest GUI for full view
        if (sender instanceof Player && (filed.size() + against.size() > 10)) {
            sender.sendMessage(Component.text(""));
            sender.sendMessage(Component.text("[Click for Full History]", NamedTextColor.GREEN, TextDecoration.UNDERLINED)
                    .clickEvent(ClickEvent.runCommand("/reporthistory " + target.getName() + " gui"))
                    .hoverEvent(HoverEvent.showText(Component.text("Open GUI with all reports"))));
        }
    }

    private void displayReport(CommandSender sender, ReportManager.Report report) {
        NamedTextColor statusColor = getStatusColor(report.status);

        String reporterName = Bukkit.getOfflinePlayer(report.reporter).getName();
        String targetName = Bukkit.getOfflinePlayer(report.target).getName();

        Component message = Component.text()
                .append(Component.text("  ", NamedTextColor.DARK_GRAY))
                .append(Component.text("[" + report.status + "]", statusColor))
                .append(Component.text(" ", NamedTextColor.DARK_GRAY))
                .append(Component.text(reporterName, NamedTextColor.YELLOW))
                .append(Component.text(" → ", NamedTextColor.DARK_GRAY))
                .append(Component.text(targetName, NamedTextColor.RED))
                .build();

        Component details = Component.text()
                .append(Component.text("    ", NamedTextColor.DARK_GRAY))
                .append(Component.text(report.reason, NamedTextColor.WHITE))
                .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                .append(Component.text(dateFormat.format(new Date(report.created)), NamedTextColor.GRAY))
                .build();

        sender.sendMessage(message);
        sender.sendMessage(details);
    }

    private NamedTextColor getStatusColor(String status) {
        return switch (status.toUpperCase()) {
            case "OPEN" -> NamedTextColor.RED;
            case "CLAIMED" -> NamedTextColor.YELLOW;
            case "CLOSED" -> NamedTextColor.GREEN;
            case "NEEDS_INFO" -> NamedTextColor.GOLD;
            default -> NamedTextColor.GRAY;
        };
    }
}
