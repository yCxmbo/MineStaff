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
    private final StaffDataManager staff;
    private final MineStaff plugin;

    public FreezeCommand(MineStaff plugin) {
        this.plugin = plugin;
        this.staff = plugin.getStaffDataManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        var cfg = plugin.getConfigManager();
        if (!(sender instanceof Player p)) {
            sender.sendMessage(cfg.getMessage("only_players", "Only players can use this."));
            return true;
        }
        if (!(p.hasPermission("staffmode.freeze.use") || p.hasPermission("staffmode.freeze"))) {
            p.sendMessage(cfg.getMessage("no_permission", "You don't have permission."));
            return true;
        }
        if (args.length < 1) {
            p.sendMessage(cfg.getMessage("freeze_usage", "Usage: /freeze <player> [seconds]"));
            return true;
        }
        Player t = Bukkit.getPlayerExact(args[0]);
        if (t == null) {
            p.sendMessage(cfg.getMessage("player_not_found", "Player not found."));
            return true;
        }
        int seconds = 0;
        if (args.length >= 2) {
            try { seconds = Math.max(0, Integer.parseInt(args[1])); } catch (NumberFormatException ignored) {}
        } else {
            seconds = plugin.getConfigManager().getConfig().getInt("freeze.default_seconds", 0);
        }
        boolean result = plugin.getFreezeService().toggle(p, t, null, me.ycxmbo.mineStaff.api.MineStaffAPI.ToggleCause.COMMAND, seconds);
        plugin.getActionLogger().logCommand(p, "Freeze " + t.getName() + " -> " + result + (seconds > 0 && result ? (" for " + seconds + "s") : ""));
        return true;
    }
}
