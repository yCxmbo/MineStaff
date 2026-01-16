package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command for cross-server teleportation
 */
public class CrossServerTeleportCommand implements CommandExecutor {
    private final MineStaff plugin;
    
    public CrossServerTeleportCommand(MineStaff plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        
        if (!player.hasPermission("staffmode.teleport.crossserver")) {
            player.sendMessage("§cYou don't have permission to use cross-server teleport!");
            return true;
        }
        
        if (args.length == 0) {
            player.sendMessage("§cUsage: /csteleport <player>");
            return true;
        }
        
        String targetPlayer = args[0];
        
        var crossServerTeleport = plugin.getCrossServerTeleport();
        if (crossServerTeleport == null) {
            player.sendMessage("§cCross-server features are not enabled!");
            return true;
        }
        
        crossServerTeleport.teleportToPlayer(player, targetPlayer);
        return true;
    }
}
