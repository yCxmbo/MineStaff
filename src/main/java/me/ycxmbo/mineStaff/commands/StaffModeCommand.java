package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import me.ycxmbo.mineStaff.managers.StaffLoginManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffModeCommand implements CommandExecutor {

    private final MineStaff plugin;
    private final StaffDataManager staffManager;
    private final StaffLoginManager loginManager;

    public StaffModeCommand(MineStaff plugin) {
        this.plugin = plugin;
        this.staffManager = plugin.getStaffDataManager();
        this.loginManager = plugin.getStaffLoginManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + plugin.getConfig().getString("messages.only_players", "Only players can use this command."));
            return true;
        }

        if (!player.hasPermission("staffmode.use")) {
            player.sendMessage(ChatColor.RED + plugin.getConfig().getString("messages.no_permission", "You do not have permission."));
            return true;
        }

        // Check if player has set a password
        if (!loginManager.hasPassword(player)) {
            player.sendMessage(ChatColor.RED + "You must set a staff login password first using /stafflogin set <password>");
            return true;
        }

        // Check if logged in
        if (!loginManager.isLoggedIn(player)) {
            player.sendMessage(ChatColor.RED + "You must log in first using /stafflogin <password> before toggling Staff Mode.");
            return true;
        }

        boolean enabled;
        if (staffManager.isInStaffMode(player)) {
            // Disable staff mode
            staffManager.disableStaffMode(player);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.staffmode_disabled", "&cStaff Mode disabled.")));
            enabled = false;
        } else {
            // Enable staff mode
            staffManager.enableStaffMode(player);
            plugin.getToolManager().giveStaffTools(player);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.staffmode_enabled", "&aStaff Mode enabled.")));
            enabled = true;
        }

        // Log staff mode toggle
        plugin.getActionLogger().logCommand(player, "/staffmode " + (enabled ? "enabled" : "disabled"));

        return true;
    }
}
