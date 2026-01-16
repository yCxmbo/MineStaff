package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.channels.StaffChannel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ChannelChatListener implements Listener {
    private final MineStaff plugin;

    public ChannelChatListener(MineStaff plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // Check if player is in channel mode
        if (!plugin.getChannelManager().isInChannelMode(player)) {
            return;
        }

        // Cancel the event to prevent normal chat
        event.setCancelled(true);

        // Get active channel
        StaffChannel channel = plugin.getChannelManager().getActiveChannel(player);
        if (channel == null) {
            player.sendMessage("§cNo active channel! Use /channel join <name>");
            return;
        }

        // Send message to channel
        String message = event.getMessage();
        plugin.getChannelManager().sendMessage(player, channel, message);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Cleanup when player leaves
        plugin.getChannelManager().leaveChannel(event.getPlayer());
    }
}
