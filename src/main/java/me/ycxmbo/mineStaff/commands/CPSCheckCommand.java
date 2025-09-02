package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.CPSCheckManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CPSCheckCommand implements CommandExecutor {
    private final MineStaff plugin;

    public CPSCheckCommand(MineStaff plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Only players."); return true; }
        if (!p.hasPermission("staffmode.cpscheck")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
        if (args.length < 1) { p.sendMessage(ChatColor.YELLOW + "Usage: /cpscheck <player>"); return true; }

        OfflinePlayer off = Bukkit.getOfflinePlayer(args[0]);
        if (!off.isOnline()) { p.sendMessage(ChatColor.RED + "Target must be online."); return true; }
        Player target = off.getPlayer();

        CPSCheckManager cps = plugin.getCPSManager();
        if (cps.isChecking(target)) {
            p.sendMessage(ChatColor.RED + "A CPS test is already running for " + target.getName() + ".");
            return true;
        }
        if (cps.begin(p, target)) {
            int secs = plugin.getConfigManager().getConfig().getInt("cps.duration_seconds", 10);
            p.sendMessage(ChatColor.GREEN + "Started " + secs + "s CPS test on " + target.getName() + ".");
            target.sendMessage(ChatColor.YELLOW + "A staff member is measuring your CPS for " + secs + " seconds.");
            cps.finishLater(p, target);
        }
        return true;
    }
}
