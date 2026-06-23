package me.ycxmbo.mineStaff.crossserver;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Handles cross-server teleportation for staff members
 */
public class CrossServerTeleport {
    private final MineStaff plugin;
    private final String serverName;
    private final Map<UUID, PendingTeleport> pendingTeleports = new HashMap<>();
    
    private static class PendingTeleport {
        final String targetServer;
        final String targetPlayer;
        final long requestTime;
        
        PendingTeleport(String targetServer, String targetPlayer) {
            this.targetServer = targetServer;
            this.targetPlayer = targetPlayer;
            this.requestTime = System.currentTimeMillis();
        }
    }
    
    public CrossServerTeleport(MineStaff plugin) {
        this.plugin = plugin;
        this.serverName = plugin.getConfig().getString("server-name", "unknown");
        
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            pendingTeleports.entrySet().removeIf(entry -> 
                now - entry.getValue().requestTime > 60000);
        }, 1200L, 1200L);
    }
    
    public void teleportToPlayer(Player staff, String targetPlayerName) {
        if (!staff.hasPermission("staffmode.teleport.crossserver")) {
            staff.sendMessage(plugin.getConfigManager().getMessage("csteleport_no_permission", "&c✖ You don't have permission to use cross-server teleport."));
            return;
        }

        Player localPlayer = Bukkit.getPlayerExact(targetPlayerName);
        if (localPlayer != null) {
            staff.teleport(localPlayer.getLocation());
            staff.sendMessage(plugin.getConfigManager().getMessage("csteleport_success", "&a✔ Teleported to &f{target}&a.")
                    .replace("{target}", targetPlayerName));
            return;
        }

        queryPlayerLocation(staff, targetPlayerName);
    }

    private void queryPlayerLocation(Player staff, String targetPlayerName) {
        staff.sendMessage(plugin.getConfigManager().getMessage("csteleport_searching", "&7⌛ Searching for &f{target} &7across the network...")
                .replace("{target}", targetPlayerName));

        var redisBridge = plugin.getRedisBridge();
        if (redisBridge == null) {
            staff.sendMessage(plugin.getConfigManager().getMessage("csteleport_disabled", "&c✖ Cross-server features are not enabled on this server."));
            return;
        }

        String channel = "minestaff:teleport:query";
        String message = serverName + "|" + staff.getUniqueId() + "|" + staff.getName() + "|" + targetPlayerName;

        redisBridge.publish(channel, message);

        staff.sendMessage(plugin.getConfigManager().getMessage("csteleport_waiting", "&e⌛ Awaiting network response..."));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!pendingTeleports.containsKey(staff.getUniqueId())) {
                staff.sendMessage(plugin.getConfigManager().getMessage("csteleport_not_found", "&c✖ Player &f{target} &cis not online on any server.")
                        .replace("{target}", targetPlayerName));
            }
        }, 60L);
    }
    
    public void handleTeleportQuery(String originServer, UUID staffId, String staffName, String targetPlayerName) {
        Player target = Bukkit.getPlayerExact(targetPlayerName);
        if (target == null) return;
        
        String channel = "minestaff:teleport:response";
        String message = serverName + "|" + originServer + "|" + staffId + "|" + staffName + "|" + targetPlayerName;
        
        plugin.getRedisBridge().publish(channel, message);
        
        plugin.getLogger().info("Cross-server teleport: " + staffName + " is teleporting to " + targetPlayerName + " on " + serverName);
    }
    
    public void handleTeleportResponse(String targetServer, UUID staffId, String staffName, String targetPlayerName) {
        if (!targetServer.equals(serverName)) return;
        
        Player staff = Bukkit.getPlayer(staffId);
        if (staff == null) return;
        
        pendingTeleports.put(staffId, new PendingTeleport(targetServer, targetPlayerName));

        staff.sendMessage(plugin.getConfigManager().getMessage("csteleport_found", "&a✔ Found &f{target} &aon &e{server}&a.")
                .replace("{target}", targetPlayerName)
                .replace("{server}", targetServer));
        staff.sendMessage(plugin.getConfigManager().getMessage("csteleport_initiating", "&7⌛ Initiating transfer..."));

        sendPlayerToServer(staff, targetServer, targetPlayerName);
    }
    
    private void sendPlayerToServer(Player player, String server, String targetPlayer) {
        com.google.common.io.ByteArrayDataOutput out = com.google.common.io.ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());

        String channel = "minestaff:teleport:pending";
        String message = server + "|" + player.getUniqueId() + "|" + targetPlayer;
        plugin.getRedisBridge().publish(channel, message);

        player.sendMessage(plugin.getConfigManager().getMessage("csteleport_switching", "&a⌛ Connecting to &e{server}&a...")
                .replace("{server}", server));
    }
    
    public void handlePlayerJoin(Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            checkPendingTeleport(player);
        }, 20L);
    }
    
    private void checkPendingTeleport(Player player) {
        // Implementation would check Redis for pending teleports
        // Simplified for now
    }
}
