package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.util.CPSManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CPSCheckCommand implements CommandExecutor {
    private final CPSManager cps;

    public CPSCheckCommand(MineStaff plugin) { this.cps = plugin.getCPSManager(); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(ChatColor.RED + "Only players.");
            return true;
        }
        if (!p.hasPermission("staffmode.cpscheck")) {
            p.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (args.length < 2) {
            p.sendMessage(ChatColor.YELLOW + "Usage: /cpscheck <player> <seconds>");
            return true;
        }
        Player t = Bukkit.getPlayerExact(args[0]);
        if (t == null) {
            p.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }
        int seconds;
        try { seconds = Integer.parseInt(args[1]); }
        catch (Exception e) { p.sendMessage(ChatColor.RED + "Seconds must be a number."); return true; }
        cps.startTest(p, t, seconds);
        return true;
    }
}
