package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.gui.ProfileGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProfileCommand implements CommandExecutor {
    private final ProfileGUI gui;
    public ProfileCommand(MineStaff plugin) { this.gui = new ProfileGUI(plugin); }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Only players."); return true; }
        if (!p.hasPermission("staffmode.profile") && !p.hasPermission("staffmode.inspect")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
        if (args.length < 1) { p.sendMessage(ChatColor.YELLOW + "Usage: /profile <player>"); return true; }
        OfflinePlayer off = Bukkit.getOfflinePlayer(args[0]);
        gui.open(p, off.getUniqueId());
        return true;
    }
}

