package me.ycxmbo.mineStaff.listeners;

import com.gcreeper123.godseye.api.events.GodsEyeFlagEvent;
import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.util.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;

public class AlertListener implements Listener {

    private final MineStaff plugin;
    private final ConfigManager config;

    // Track player names for name change detection
    private final Map<UUID, String> lastKnownNames = new HashMap<>();

    public AlertListener(MineStaff plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!config.getConfig().getBoolean("alerts.skin_or_name_change")) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String currentName = player.getName();

        if (lastKnownNames.containsKey(uuid)) {
            String previousName = lastKnownNames.get(uuid);
            if (!previousName.equals(currentName)) {
                broadcastAlert(config.getMessage("name_skin_change", "&e%player%'s skin or name has changed!")
                        .replace("%player%", currentName));
            }
        }

        lastKnownNames.put(uuid, currentName);
    }

    // Alert for block place
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!config.getConfig().getBoolean("alerts.block_place")) return;

        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();

        String message = config.getMessage("alert_messages.block_place", "&e%player% placed a block at &7%x%, %y%, %z%")
                .replace("%player%", player.getName())
                .replace("%x%", String.valueOf(loc.getBlockX()))
                .replace("%y%", String.valueOf(loc.getBlockY()))
                .replace("%z%", String.valueOf(loc.getBlockZ()));

        broadcastAlert(message);
    }

    // Alert for block break
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!config.getConfig().getBoolean("alerts.block_break")) return;

        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();

        String message = config.getMessage("alert_messages.block_break", "&c%player% broke a block at &7%x%, %y%, %z%")
                .replace("%player%", player.getName())
                .replace("%x%", String.valueOf(loc.getBlockX()))
                .replace("%y%", String.valueOf(loc.getBlockY()))
                .replace("%z%", String.valueOf(loc.getBlockZ()));

        broadcastAlert(message);
    }

    // Alert for item drop
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (!config.getConfig().getBoolean("alerts.item_drop")) return;

        Player player = event.getPlayer();
        Location loc = player.getLocation();

        String message = config.getMessage("alert_messages.item_drop", "&d%player% dropped an item at &7%x%, %y%, %z%")
                .replace("%player%", player.getName())
                .replace("%x%", String.valueOf(loc.getBlockX()))
                .replace("%y%", String.valueOf(loc.getBlockY()))
                .replace("%z%", String.valueOf(loc.getBlockZ()));

        broadcastAlert(message);
    }

    // GodEyes AntiCheat Integration
    @EventHandler
    public void onGodsEyeFlag(GodsEyeFlagEvent event) {
        if (!config.getConfig().getBoolean("alerts.godseye_events")) return;

        Player player = event.getPlayer();
        String cheatType = event.getDetection().getName();

        String message = config.getMessage("godseye_detected", "&4GodEyes Alert: &c%player% flagged for &f%cheat%&c.")
                .replace("%player%", player.getName())
                .replace("%cheat%", cheatType);

        broadcastAlert(message);
    }

    // LiteBans integration example (uncomment and implement your LiteBans listener if you have LiteBans API)
    /*
    @EventHandler
    public void onLiteBansPunish(LiteBansEvent event) {
        if (!config.getConfig().getBoolean("alerts.litebans_events")) return;

        String message = config.getMessage("litebans_action", "&c%player% was banned or muted via LiteBans!")
                .replace("%player%", event.getPlayerName())
                .replace("%reason%", event.getReason())
                .replace("%actor%", event.getActor());

        broadcastAlert(message);
    }
    */

    private void broadcastAlert(String message) {
        String prefix = config.getMessage("alert_prefix", "&c[MineStaff Alert] &f");
        String fullMessage = ChatColor.translateAlternateColorCodes('&', prefix + message);

        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("staffmode.alerts"))
                .forEach(p -> p.sendMessage(fullMessage));
    }
}
