package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ReportManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class ReportCommand implements CommandExecutor {
    private final ReportManager reports;

    public ReportCommand(MineStaff plugin) { this.reports = new ReportManager(plugin); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Only players."); return true; }
        if (args.length < 2) { p.sendMessage(ChatColor.YELLOW + "Usage: /report <player> <reason>"); return true; }
        OfflinePlayer t = Bukkit.getOfflinePlayer(args[0]);
        String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        reports.add(new ReportManager.Report(p.getUniqueId(), t.getUniqueId(), reason));
        p.sendMessage(ChatColor.GREEN + "Report submitted.");
        return true;
    }
}
