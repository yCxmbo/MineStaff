package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.StaffLoginManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffLoginCommand implements CommandExecutor {

    private final MineStaff plugin;
    private final StaffLoginManager loginManager;

    public StaffLoginCommand(MineStaff plugin) {
        this.plugin = plugin;
        this.loginManager = plugin.getStaffLoginManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("staffmode.use")) {
            player.sendMessage(ChatColor.RED + "You do not have permission.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "Usage:");
            player.sendMessage(ChatColor.YELLOW + "/stafflogin set <password> - Set your staff login password.");
            player.sendMessage(ChatColor.YELLOW + "/stafflogin <password> - Login to staff mode.");
            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Please provide a password.");
                return true;
            }
            String newPassword = args[1];
            loginManager.setPassword(player, newPassword);
            player.sendMessage(ChatColor.GREEN + "Staff login password set successfully.");
            return true;
        }

        // Attempt login with given password
        String inputPassword = args[0];
        if (!loginManager.hasPassword(player)) {
            player.sendMessage(ChatColor.RED + "You do not have a password set. Use /stafflogin set <password> first.");
            return true;
        }

        if (loginManager.isLoggedIn(player)) {
            player.sendMessage(ChatColor.GREEN + "You are already logged in.");
            return true;
        }

        if (loginManager.checkPassword(player, inputPassword)) {
            player.sendMessage(ChatColor.GREEN + "Staff login successful! You can now use staff mode.");
        } else {
            player.sendMessage(ChatColor.RED + "Incorrect password.");
        }
        return true;
    }
}
