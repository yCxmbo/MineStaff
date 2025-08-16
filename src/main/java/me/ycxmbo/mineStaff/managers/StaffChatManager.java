package me.ycxmbo.mineStaff.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class StaffChatManager {
    private final Set<UUID> toggled = new HashSet<>();

    public boolean isToggled(Player p) { return toggled.contains(p.getUniqueId()); }
    public void setToggled(Player p, boolean on) {
        if (on) toggled.add(p.getUniqueId()); else toggled.remove(p.getUniqueId());
    }

    /** Send a staff chat line to all online players with permission. */
    public void broadcast(Player sender, String message) {
        String line = ChatColor.DARK_GRAY + "["
                + ChatColor.LIGHT_PURPLE + "Staff"
                + ChatColor.DARK_GRAY + "] "
                + ChatColor.AQUA + sender.getName()
                + ChatColor.GRAY + ": "
                + ChatColor.WHITE + message;
        for (Player pl : Bukkit.getOnlinePlayers()) {
            if (pl.hasPermission("staffmode.chat")) pl.sendMessage(line);
        }
        // Optional: also log to console
        Bukkit.getConsoleSender().sendMessage(ChatColor.stripColor(line));
    }
}
