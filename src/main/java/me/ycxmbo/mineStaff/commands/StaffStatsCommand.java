package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.analytics.StaffAnalyticsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * {@code /staffstats} &mdash; view staff activity analytics: a duty-time
 * leaderboard, an individual breakdown, or the interactive GUI.
 */
public class StaffStatsCommand implements CommandExecutor {
    private final MineStaff plugin;

    public StaffStatsCommand(MineStaff plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("staffmode.stats")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no_permission", "You don't have permission."));
            return true;
        }

        StaffAnalyticsManager an = plugin.getStaffAnalyticsManager();
        if (an == null) {
            sender.sendMessage(ChatColor.RED + "Analytics are unavailable.");
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("gui")) {
            if (!(sender instanceof Player p)) { sender.sendMessage(ChatColor.RED + "Only players can open the GUI."); return true; }
            plugin.getStaffStatsGUI().open(p);
            return true;
        }

        if (args.length >= 1) {
            OfflinePlayer off = Bukkit.getOfflinePlayer(args[0]);
            UUID id = off.getUniqueId();
            String name = off.getName() != null ? off.getName() : args[0];
            sender.sendMessage(ChatColor.GOLD + "Staff stats: " + ChatColor.YELLOW + name);
            sender.sendMessage(ChatColor.GRAY + "  Duty time: " + ChatColor.WHITE + StaffAnalyticsManager.formatDuration(an.getDutySeconds(id))
                    + (an.isOnDutyNow(id) ? ChatColor.GREEN + " (on duty)" : ""));
            sender.sendMessage(ChatColor.GRAY + "  Sessions: " + ChatColor.WHITE + an.getStat(id, "sessions"));
            sender.sendMessage(ChatColor.GRAY + "  Warnings issued: " + ChatColor.WHITE + an.getStat(id, "warnings"));
            sender.sendMessage(ChatColor.GRAY + "  Punishments issued: " + ChatColor.WHITE + an.getStat(id, "punishments"));
            sender.sendMessage(ChatColor.GRAY + "  Reports handled: " + ChatColor.WHITE + an.getStat(id, "reports"));
            return true;
        }

        // Leaderboard
        List<UUID> tracked = an.getTracked();
        tracked.sort(Comparator.comparingLong(an::getDutySeconds).reversed());
        sender.sendMessage(ChatColor.GOLD + "═══ Staff Activity Leaderboard ═══");
        if (tracked.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "No data recorded yet.");
            return true;
        }
        int rank = 1;
        for (UUID id : tracked) {
            if (rank > 10) break;
            sender.sendMessage(ChatColor.YELLOW + "" + rank + ". " + ChatColor.WHITE + an.getName(id)
                    + ChatColor.GRAY + " - " + StaffAnalyticsManager.formatDuration(an.getDutySeconds(id))
                    + ChatColor.DARK_GRAY + " | W:" + an.getStat(id, "warnings")
                    + " P:" + an.getStat(id, "punishments")
                    + " R:" + an.getStat(id, "reports"));
            rank++;
        }
        if (sender instanceof Player) {
            sender.sendMessage(ChatColor.DARK_GRAY + "Tip: /staffstats gui for the interactive view.");
        }
        return true;
    }
}
