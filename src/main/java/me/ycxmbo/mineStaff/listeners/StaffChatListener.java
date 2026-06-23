package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.StaffChatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class StaffChatListener implements Listener {
    private final StaffChatManager scm;
    private final MineStaff plugin;

    public StaffChatListener(MineStaff plugin) {
        this.plugin = plugin;
        this.scm = plugin.getStaffChatManager();
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        String message = e.getMessage();
        String prefix = plugin.getConfigManager().getStaffchatPrefix();

        // If toggled, route message to staff only
        if (scm.isToggled(p)) {
            e.setCancelled(true);
            if (!p.hasPermission("staffmode.chat")) {
                p.sendMessage(plugin.getConfigManager().getMessage("staffchat_lost_permission", "&c⚠ You no longer have permission for staff chat."));
                scm.setToggled(p, false);
                return;
            }
            scm.broadcast(p, message);
            return;
        }

        // Allow one-off staff chat messages using the configured prefix
        if (message.startsWith(prefix)) {
            e.setCancelled(true);
            if (!p.hasPermission("staffmode.chat")) {
                p.sendMessage(plugin.getConfigManager().getMessage("staffchat_lost_permission", "&c⚠ You no longer have permission for staff chat."));
                return;
            }
            String trimmed = message.substring(prefix.length()).trim();
            if (!trimmed.isEmpty()) {
                scm.broadcast(p, trimmed);
            }
        }
    }
}
