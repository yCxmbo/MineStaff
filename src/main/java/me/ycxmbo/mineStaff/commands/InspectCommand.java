package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InspectCommand implements CommandExecutor {
    private final MineStaff plugin;
    public InspectCommand(MineStaff plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(ChatColor.RED + "Only players.");
            return true;
        }
        if (!p.hasPermission("staffmode.inspect")) {
            p.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (args.length < 1) {
            p.sendMessage(ChatColor.YELLOW + "Usage: /inspect <player>");
            return true;
        }
        Player t = Bukkit.getPlayerExact(args[0]);
        if (t == null) {
            p.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }
        plugin.getInspectorGUI().open(p, t);
        return true;
    }
}
