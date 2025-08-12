package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ConfigManager;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import me.ycxmbo.mineStaff.managers.StaffLoginManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

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
            // Enable staff mode
            staffManager.enableStaffMode(p);
            staffManager.rememberGamemode(p);
            p.setGameMode(GameMode.CREATIVE);
            plugin.getToolManager().giveStaffTools(p);
            p.sendMessage(config.getMessage("staffmode_enabled", "Staff mode enabled."));
            plugin.getActionLogger().logCommand(p, "StaffMode ON");
        } else {
            // Disable staff mode
            staffManager.disableStaffMode(p);

            // Restore previous gamemode if we have it
            GameMode prev = staffManager.popPreviousGamemode(p);
            if (prev != null) {
                p.setGameMode(prev);
            }

            // Optionally teleport to spawn on exit
            boolean tpOnExit = config.getConfig().getBoolean("teleport-to-spawn-on-exit", true);
            if (tpOnExit) {
                Plugin essentials = Bukkit.getPluginManager().getPlugin("Essentials");
                if (essentials != null && essentials.isEnabled()) {
                    // Use Essentials(X) /spawn if present
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + p.getName());
                } else {
                    // Vanilla world spawn
                    Location spawn = p.getWorld().getSpawnLocation();
                    p.teleport(spawn);
                }
            }

            p.sendMessage(config.getMessage("staffmode_disabled", "Staff mode disabled."));
            plugin.getActionLogger().logCommand(p, "StaffMode OFF");
        }

        return true;
    }
}
