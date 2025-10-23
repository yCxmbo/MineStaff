package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.util.AlertFormatter;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class StaffAlertListener implements Listener {
    private final MineStaff plugin;
    public StaffAlertListener(MineStaff plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        FileConfiguration cfg = plugin.getConfigManager().getConfig();
        if (!cfg.getBoolean("alerts.notify_on_join", true)) return;

        Player player = e.getPlayer();
        String template = cfg.getString("alerts.join_template", "{player} joined the server.");
        String message = format(template, player);

        boolean includeTarget = cfg.getBoolean("alerts.join_include_tp", true);
        String target = includeTarget ? player.getName() : null;

        AlertFormatter.broadcast(plugin, message, target);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        FileConfiguration cfg = plugin.getConfigManager().getConfig();
        if (!cfg.getBoolean("alerts.notify_on_quit", false)) return;

        Player player = e.getPlayer();
        String template = cfg.getString("alerts.quit_template", "{player} left the server.");
        String message = format(template, player);

        boolean includeTarget = cfg.getBoolean("alerts.quit_include_tp", false);
        String target = includeTarget ? player.getName() : null;

        AlertFormatter.broadcast(plugin, message, target);
    }

    private String format(String template, Player player) {
        if (template == null || template.isBlank()) {
            template = "{player}";
        }
        Location loc = player.getLocation();
        String world = loc.getWorld() != null ? loc.getWorld().getName() : "unknown";
        return template
                .replace("{player}", player.getName())
                .replace("{display}", player.getDisplayName())
                .replace("{uuid}", player.getUniqueId().toString())
                .replace("{world}", world)
                .replace("{x}", String.valueOf(loc.getBlockX()))
                .replace("{y}", String.valueOf(loc.getBlockY()))
                .replace("{z}", String.valueOf(loc.getBlockZ()));
    }
}
