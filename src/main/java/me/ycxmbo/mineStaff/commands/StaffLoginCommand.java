package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.StaffLoginManager;
import me.ycxmbo.mineStaff.util.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffLoginCommand implements CommandExecutor {

    private final MineStaff plugin;
    private final StaffLoginManager loginManager;
    private final ConfigManager configManager;

    public StaffLoginCommand(MineStaff plugin) {
        this.plugin = plugin;
        this.loginManager = plugin.getStaffLoginManager();
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + configManager.getMessage("only_players", "Only players can use this command."));
            return true;
        }

        if (!player.hasPermission("staffmode.use")) {
            player.sendMessage(ChatColor.RED + configManager.getMessage("no_permission", "You do not have permission."));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + configManager.getMessage("stafflogin_usage", "Usage: /stafflogin set <password> or /stafflogin <password>"));
            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + configManager.getMessage("password_required", "Please provide a password."));
                return true;
            }
            String newPassword = args[1];
            loginManager.setPassword(player, newPassword);
            player.sendMessage(ChatColor.GREEN + configManager.getMessage("password_set", "Staff login password set successfully."));
            return true;
        }

        // Attempt login with given password
        String inputPassword = args[0];
        if (!loginManager.hasPassword(player)) {
            player.sendMessage(ChatColor.RED + configManager.getMessage("no_password_set", "You must set a staff login password first using /stafflogin set <password>"));
            return true;
        }

        if (loginManager.isLoggedIn(player)) {
            player.sendMessage(ChatColor.GREEN + configManager.getMessage("already_logged_in", "You are already logged in."));
            return true;
        }

        if (loginManager.checkPassword(player, inputPassword)) {
            player.sendMessage(ChatColor.GREEN + configManager.getMessage("login_success", "Staff login successful! You can now use staff mode."));
        } else {
            player.sendMessage(ChatColor.RED + configManager.getMessage("login_failed", "Incorrect password."));
        }
        return true;
    }
}
