package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class StaffAlertListener implements Listener {
    private final MineStaff plugin;
    public StaffAlertListener(MineStaff plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        // Placeholder for alert broadcasting / integrations
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        // Placeholder
    }
}
