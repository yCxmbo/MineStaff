package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.gui.ProfileGUI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProfileCommand implements CommandExecutor {
    private final MineStaff plugin;
    private final ProfileGUI gui;

    public ProfileCommand(MineStaff plugin) {
        this.plugin = plugin;
        this.gui = new ProfileGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage(plugin.getConfigManager().getMessage("only_players", "Only players can use this.")); return true; }
        if (!p.hasPermission("staffmode.profile") && !p.hasPermission("staffmode.inspect")) { p.sendMessage(plugin.getConfigManager().getMessage("no_permission", "No permission.")); return true; }
        if (args.length < 1) { p.sendMessage(plugin.getConfigManager().getMessage("profile_usage", "Usage: /profile <player>")); return true; }
        OfflinePlayer off = Bukkit.getOfflinePlayer(args[0]);
        gui.open(p, off.getUniqueId());
        return true;
    }
}
