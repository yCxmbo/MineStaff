package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ConfigManager;
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
    private final ConfigManager config;

    public StaffModeCommand(MineStaff plugin) {
        this.plugin = plugin;
        this.staffManager = plugin.getStaffDataManager();
        this.loginManager = plugin.getStaffLoginManager();
        this.config = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(ChatColor.RED + "Only players.");
            return true;
        }
        if (!p.hasPermission("staffmode.toggle")) {
            p.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (config.isLoginRequired() && !loginManager.isLoggedIn(p)) {
            p.sendMessage(config.getMessage("login_required", "You must login first."));
            return true;
        }

        boolean enable = !staffManager.isStaffMode(p);
        if (enable) {
            staffManager.enableStaffMode(p);
            plugin.getToolManager().giveStaffTools(p);
            p.sendMessage(config.getMessage("staffmode_enabled", "Staff mode enabled."));
        } else {
            staffManager.disableStaffMode(p);
            p.sendMessage(config.getMessage("staffmode_disabled", "Staff mode disabled."));
        }
        plugin.getActionLogger().logCommand(p, "StaffMode " + (enable ? "ON" : "OFF"));
        return true;
    }
}
