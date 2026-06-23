package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.analytics.StaffAnalyticsManager;
import me.ycxmbo.mineStaff.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class StaffStatsCommand implements CommandExecutor {
    private final MineStaff plugin;

    public StaffStatsCommand(MineStaff plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        if (!sender.hasPermission("staffmode.stats")) {
            sender.sendMessage(cfg.getMessage("no_permission", "No permission."));
            return true;
        }

        StaffAnalyticsManager an = plugin.getStaffAnalyticsManager();
        if (an == null) {
            sender.sendMessage(cfg.getMessage("staffstats_unavailable", "Analytics are unavailable."));
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("gui")) {
            if (!(sender instanceof Player p)) { sender.sendMessage(cfg.getMessage("staffstats_gui_only", "Only players can open the GUI.")); return true; }
            plugin.getStaffStatsGUI().open(p);
            return true;
        }

        if (args.length >= 1) {
            OfflinePlayer off = Bukkit.getOfflinePlayer(args[0]);
            UUID id = off.getUniqueId();
            String name = off.getName() != null ? off.getName() : args[0];
            boolean onDuty = an.isOnDutyNow(id);
            String dutyTime = StaffAnalyticsManager.formatDuration(an.getDutySeconds(id))
                    + (onDuty ? " §a(on duty)" : "");
            sender.sendMessage(cfg.getMessage("staffstats_header", "Stats for {name}").replace("{name}", name));
            sender.sendMessage(cfg.getMessage("staffstats_duty_time", "  Duty Time » {time}").replace("{time}", dutyTime));
            sender.sendMessage(cfg.getMessage("staffstats_sessions", "  Sessions  » {count}").replace("{count}", String.valueOf(an.getStat(id, "sessions"))));
            sender.sendMessage(cfg.getMessage("staffstats_warnings", "  Warnings  » {count}").replace("{count}", String.valueOf(an.getStat(id, "warnings"))));
            sender.sendMessage(cfg.getMessage("staffstats_punishments", "  Punishments » {count}").replace("{count}", String.valueOf(an.getStat(id, "punishments"))));
            sender.sendMessage(cfg.getMessage("staffstats_reports", "  Reports   » {count}").replace("{count}", String.valueOf(an.getStat(id, "reports"))));
            return true;
        }

        // Leaderboard
        List<UUID> tracked = an.getTracked();
        tracked.sort(Comparator.comparingLong(an::getDutySeconds).reversed());
        sender.sendMessage(cfg.getMessage("staffstats_leaderboard_header", "═══ Staff Activity Leaderboard ═══"));
        if (tracked.isEmpty()) {
            sender.sendMessage(cfg.getMessage("staffstats_no_data", "No data recorded yet."));
            return true;
        }
        int rank = 1;
        for (UUID id : tracked) {
            if (rank > 10) break;
            sender.sendMessage(cfg.getMessage("staffstats_leaderboard_entry", "{rank}. {name} — {time}")
                    .replace("{rank}", String.valueOf(rank))
                    .replace("{name}", an.getName(id))
                    .replace("{time}", StaffAnalyticsManager.formatDuration(an.getDutySeconds(id))));
            rank++;
        }
        if (sender instanceof Player) {
            sender.sendMessage(cfg.getMessage("staffstats_gui_tip", "Tip: use /staffstats gui for the interactive view."));
        }
        return true;
    }
}
