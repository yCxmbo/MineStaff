package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CrossServerTeleportCommand implements CommandExecutor {
    private final MineStaff plugin;

    public CrossServerTeleportCommand(MineStaff plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        if (!(sender instanceof Player player)) {
            sender.sendMessage(cfg.getMessage("only_players", "Only players can use this."));
            return true;
        }
        if (!player.hasPermission("staffmode.teleport.crossserver")) {
            player.sendMessage(cfg.getMessage("csteleport_no_permission", "No permission."));
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(cfg.getMessage("csteleport_usage", "Usage: /csteleport <player>").replace("{}", ""));
            return true;
        }

        var crossServerTeleport = plugin.getCrossServerTeleport();
        if (crossServerTeleport == null) {
            player.sendMessage(cfg.getMessage("csteleport_disabled", "Cross-server features are not enabled."));
            return true;
        }

        crossServerTeleport.teleportToPlayer(player, args[0]);
        return true;
    }
}
