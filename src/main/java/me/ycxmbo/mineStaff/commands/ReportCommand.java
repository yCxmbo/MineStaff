package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ReportManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ReportCommand implements CommandExecutor {
    private final ReportManager reports;

    public ReportCommand(MineStaff plugin) {
        this.reports = plugin.getReportManager();   // reuse the singleton
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Only players."); return true; }
        if (args.length < 2) {
            p.sendMessage(ChatColor.YELLOW + "Usage: /report <player> <reason>");
            return true;
        }

        OfflinePlayer t = Bukkit.getOfflinePlayer(args[0]);
        String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        // Use the manager's add() which generates the ID internally
        UUID id = reports.add(p.getUniqueId(), t.getUniqueId(), reason);

        p.sendMessage(ChatColor.GREEN + "Report submitted. ID: " + ChatColor.YELLOW + id);
        return true;
    }
}

