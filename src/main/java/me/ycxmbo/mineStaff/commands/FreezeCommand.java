package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FreezeCommand implements CommandExecutor {

    private final MineStaff plugin;
    private final StaffDataManager staffManager;

    public FreezeCommand(MineStaff plugin) {
        this.plugin = plugin;
        this.staffManager = plugin.getStaffDataManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player staff)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (!staff.hasPermission("staffmode.freeze")) {
            staff.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (!staffManager.isInStaffMode(staff)) {
            staff.sendMessage(ChatColor.RED + "You must be in staff mode to use this command.");
            return true;
        }

        if (args.length != 1) {
            staff.sendMessage(ChatColor.RED + "Usage: /freeze <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            staff.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        if (staffManager.isFrozen(target)) {
            staffManager.unfreezePlayer(target);
            target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.unfreeze_notify", "&aYou have been unfrozen.")));
            staff.sendMessage(ChatColor.GREEN + "Unfroze " + target.getName());
        } else {
            staffManager.freezePlayer(target);
            target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.freeze_notify", "&eYou have been frozen by a staff member.")));
            staff.sendMessage(ChatColor.RED + "Froze " + target.getName());
        }

        return true;
    }
}
