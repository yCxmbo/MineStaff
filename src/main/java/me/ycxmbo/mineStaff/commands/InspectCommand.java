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

    public InspectCommand(MineStaff plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player staff)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!staff.hasPermission("minestaff.inspect")) {
            staff.sendMessage(ChatColor.RED + "You do not have permission.");
            return true;
        }

        if (args.length != 1) {
            staff.sendMessage(ChatColor.RED + "Usage: /staffinspect <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            staff.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        plugin.getInspectorGUI().openInventoryView(staff, target);
        return true;
    }
}
