package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.util.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class AlertListener implements Listener {

    private final MineStaff plugin;
    private final ConfigManager configManager;

    public AlertListener(MineStaff plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!configManager.getBoolean("alerts.skin_or_name_change", true)) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String currentName = player.getName();

        if (plugin.getLastKnownNames().containsKey(uuid)) {
            String previousName = plugin.getLastKnownNames().get(uuid);
            if (!previousName.equals(currentName)) {
                broadcastAlert(configManager.getMessage("name_skin_change", "&e%player%'s skin or name has changed!")
                        .replace("%player%", currentName));
            }
        }

        plugin.getLastKnownNames().put(uuid, currentName);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!configManager.getBoolean("alerts.block_place", true)) return;

        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();
        String blockType = event.getBlock().getType().toString();

        String message = configManager.getMessage("alert_messages.block_place", "&e%player% placed a %block% at &7%x%, %y%, %z%")
                .replace("%player%", player.getName())
                .replace("%block%", blockType)
                .replace("%x%", String.valueOf(loc.getBlockX()))
                .replace("%y%", String.valueOf(loc.getBlockY()))
                .replace("%z%", String.valueOf(loc.getBlockZ()));

        broadcastAlert(message);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!configManager.getBoolean("alerts.block_break", true)) return;

        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();
        String blockType = event.getBlock().getType().toString();

        String color = configManager.getMessage("alert-colors.block_break", "&c");
        String message = color + player.getName() + " broke a block...";

        broadcastAlert(message);
    }