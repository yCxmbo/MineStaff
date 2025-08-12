package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.util.AlertFormatter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class AlertListener implements Listener {
    private final MineStaff plugin;

    public AlertListener(MineStaff plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        String full = e.getMessage();
        String lower = full.toLowerCase();
        if (!(lower.startsWith("/ban") || lower.startsWith("/mute")
                || lower.startsWith("/kick") || lower.startsWith("/warn"))) {
            return;
        }

        String[] parts = full.split("\\s+");
        String target = (parts.length > 1) ? parts[1] : null;

        String content = e.getPlayer().getName() + " ran: " + full;
        AlertFormatter.broadcast(plugin, content, target);
    }
}
