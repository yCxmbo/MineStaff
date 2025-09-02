package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class ActivityListener implements Listener {
    private final MineStaff plugin;
    public ActivityListener(MineStaff plugin) { this.plugin = plugin; }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockY() != e.getTo().getBlockY() || e.getFrom().getBlockZ() != e.getTo().getBlockZ()) {
            plugin.getActivityTracker().mark(e.getPlayer());
        }
    }
}
