package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.automod.AutoModManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Runs incoming chat through {@link AutoModManager}. Blocked messages are
 * cancelled on the (async) chat thread; consequences are applied back on the
 * main thread.
 */
public class ChatFilterListener implements Listener {
    private final MineStaff plugin;

    public ChatFilterListener(MineStaff plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        AutoModManager am = plugin.getAutoModManager();
        if (am == null || !am.isEnabled()) return;

        AutoModManager.Violation v = am.inspect(e.getPlayer(), e.getMessage());
        if (v == null) return;

        e.setCancelled(true);
        final var player = e.getPlayer();
        Bukkit.getScheduler().runTask(plugin, () -> am.punish(player, v));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        AutoModManager am = plugin.getAutoModManager();
        if (am != null) am.clear(e.getPlayer().getUniqueId());
    }
}
