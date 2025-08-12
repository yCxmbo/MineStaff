package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class StaffChatListener implements Listener {
    private final MineStaff plugin;
    public StaffChatListener(MineStaff plugin) { this.plugin = plugin; }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (!p.hasPermission("staffmode.chat")) return;

        String prefix = plugin.getConfigManager().getStaffchatPrefix();
        boolean toggled = plugin.getStaffChatCommand().isToggled(p);
        String msg = e.getMessage();

        if (toggled || msg.startsWith(prefix)) {
            if (msg.startsWith(prefix)) msg = msg.substring(prefix.length()).trim();
            e.setCancelled(true);
            String formatted = ChatColor.DARK_AQUA + "[StaffChat] " + p.getName() + ": " + ChatColor.WHITE + msg;
            for (Player pl : Bukkit.getOnlinePlayers()) {
                if (pl.hasPermission("staffmode.chat")) pl.sendMessage(formatted);
            }
        }
    }
}
