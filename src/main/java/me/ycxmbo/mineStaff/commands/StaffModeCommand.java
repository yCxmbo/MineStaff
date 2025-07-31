package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import me.ycxmbo.mineStaff.managers.StaffLoginManager;
import me.ycxmbo.mineStaff.util.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StaffModeCommand implements CommandExecutor {

    private final MineStaff plugin;
    private final StaffDataManager staffManager;
    private final StaffLoginManager loginManager;
    private final ConfigManager configManager;
    private final Map<UUID, Long> staffModeCooldowns = new HashMap<>();
    private final long staffModeCooldown = 20 * 10; // 10 seconds

    public StaffModeCommand(MineStaff plugin) {
        this.plugin = plugin;
        this.staffManager = plugin.getStaffDataManager();
        this.loginManager = plugin.getStaffLoginManager();
        this.configManager = plugin.getConfigManager();
    }

    public boolean isOnStaffModeCooldown(Player player) {
        return staffModeCooldowns.containsKey(player.getUniqueId()) &&
                System.currentTimeMillis() < staffModeCooldowns.get(player.getUniqueId());
    }

    public void setStaffModeCooldown(Player player) {
        staffModeCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + staffModeCooldown);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + configManager.getMessage("only_players", "Only players can use this command."));
            return true;
        }

        if (!player.hasPermission("staffmode.toggle")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to toggle Staff Mode.");
            return true;
        }

        if (!loginManager.hasPassword(player)) {
            player.sendMessage(ChatColor.RED + configManager.getMessage("no_password_set", "You must set a staff login password first using /stafflogin set <password>"));
            return true;
        }

        if (!loginManager.isLoggedIn(player)) {
            player.sendMessage(ChatColor.RED + configManager.getMessage("must_login", "You must log in first using /stafflogin <password> before toggling Staff Mode."));
            return true;
        }

        if (isOnStaffModeCooldown(player)) {
            player.sendMessage(ChatColor.RED + "Please wait before toggling Staff Mode again.");
            return true;
        }

        boolean enabled;
        if (staffManager.isInStaffMode(player)) {
            // Disable staff mode
            staffManager.disableStaffMode(player);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', configManager.getMessage("staffmode_disabled", "&cStaff Mode disabled.")));
            enabled = false;
        } else {
            // Enable staff mode
            staffManager.enableStaffMode(player);
            plugin.getToolManager().giveStaffTools(player);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', configManager.getMessage("staffmode_enabled", "&aStaff Mode enabled.")));
            enabled = true;
        }

        setStaffModeCooldown(player);

        plugin.getLogger().info("Player " + player.getName() + " toggled vanish.");
        // Log staff mode toggle
        plugin.getActionLogger().logCommand(player, "/staffmode " + (enabled ? "enabled" : "disabled"));

        return true;
    }
}
