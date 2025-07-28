package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class StaffAlertListener implements Listener {

    private final MineStaff plugin;

    public StaffAlertListener(MineStaff plugin) {
        this.plugin = plugin;
    }

    private void sendStaffAlert(String message) {
        String prefix = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfigManager().getConfig().getString("alerts.alert_prefix", "&6[Staff Alert] &f"));
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("staffmode.alerts"))
                .forEach(p -> p.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', message)));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("alerts.block_break", true)) return;

        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();
        String msg = plugin.getConfigManager().getConfig().getString("alerts.message.block_break",
                        "&c%player% broke a block at &7%x%, %y%, %z%")
                .replace("%player%", player.getName())
                .replace("%x%", String.valueOf(loc.getBlockX()))
                .replace("%y%", String.valueOf(loc.getBlockY()))
                .replace("%z%", String.valueOf(loc.getBlockZ()));

        sendStaffAlert(msg);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("alerts.block_place", true)) return;

        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();
        String msg = plugin.getConfigManager().getConfig().getString("alerts.message.block_place",
                        "&e%player% placed a block at &7%x%, %y%, %z%")
                .replace("%player%", player.getName())
                .replace("%x%", String.valueOf(loc.getBlockX()))
                .replace("%y%", String.valueOf(loc.getBlockY()))
                .replace("%z%", String.valueOf(loc.getBlockZ()));

        sendStaffAlert(msg);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("alerts.item_drop", true)) return;

        Player player = event.getPlayer();
        Location loc = player.getLocation();
        String msg = plugin.getConfigManager().getConfig().getString("alerts.message.item_drop",
                        "&d%player% dropped an item at &7%x%, %y%, %z%")
                .replace("%player%", player.getName())
                .replace("%x%", String.valueOf(loc.getBlockX()))
                .replace("%y%", String.valueOf(loc.getBlockY()))
                .replace("%z%", String.valueOf(loc.getBlockZ()));

        sendStaffAlert(msg);
    }
}
