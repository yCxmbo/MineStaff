package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InspectCommand implements CommandExecutor {
    private final MineStaff plugin;
    public InspectCommand(MineStaff plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        var cfg = plugin.getConfigManager();
        if (!(sender instanceof Player p)) {
            sender.sendMessage(cfg.getMessage("only_players", "Only players can use this."));
            return true;
        }
        if (!p.hasPermission("staffmode.inspect")) {
            p.sendMessage(cfg.getMessage("no_permission", "You don't have permission."));
            return true;
        }
        if (args.length < 1) {
            p.sendMessage(cfg.getMessage("inspect_usage", "Usage: /inspect <player>"));
            return true;
        }
        Player t = Bukkit.getPlayerExact(args[0]);
        if (t == null) {
            p.sendMessage(cfg.getMessage("player_not_found", "No player with that name is online."));
            return true;
        }
        plugin.getInspectorGUI().open(p, t);
        return true;
    }
}
