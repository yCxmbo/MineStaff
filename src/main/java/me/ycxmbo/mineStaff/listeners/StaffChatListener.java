package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.StaffChatManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class StaffChatListener implements Listener {
    private final StaffChatManager scm;

    public StaffChatListener(MineStaff plugin) {
        this.scm = plugin.getStaffChatManager();
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        // If toggled, route message to staff only
        if (scm.isToggled(p)) {
            e.setCancelled(true);
            if (!p.hasPermission("staffmode.chat")) {
                p.sendMessage(ChatColor.RED + "You lost permission for staff chat.");
                scm.setToggled(p, false);
                return;
            }
            scm.broadcast(p, e.getMessage());
        }
    }
}
