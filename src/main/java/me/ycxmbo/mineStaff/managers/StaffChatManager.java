package me.ycxmbo.mineStaff.managers;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class StaffChatManager {
    private final Set<UUID> toggled = new HashSet<>();
    private final MineStaff plugin;

    public StaffChatManager(MineStaff plugin) { this.plugin = plugin; }

    public boolean isToggled(Player p) { return toggled.contains(p.getUniqueId()); }
    public void setToggled(Player p, boolean on) {
        if (on) toggled.add(p.getUniqueId()); else toggled.remove(p.getUniqueId());
    }

    private String legacyFormat(String name, String message) {
        String fmt = plugin.getConfigManager().getConfig().getString("staffchat.format_legacy",
                "&8[&dStaff&8] &b{name}&7: &f{message}");
        String line = fmt.replace("{name}", name).replace("{message}", message);
        return ChatColor.translateAlternateColorCodes('&', line);
    }

    /** Normal broadcast initiated locally; forwards to proxy if enabled. */
    public void broadcast(Player sender, String message) {
        String rendered = legacyFormat(sender.getName(), message);
        String soundName = plugin.getConfigManager().getConfig().getString("staffchat.mention_sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
        org.bukkit.Sound snd = org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        try { snd = org.bukkit.Sound.valueOf(soundName); } catch (IllegalArgumentException ignored) {}
        for (Player pl : Bukkit.getOnlinePlayers()) {
            if (!pl.hasPermission("staffmode.chat")) continue;
            pl.sendMessage(rendered);
            // mention: @name
            if (message.toLowerCase().contains("@" + pl.getName().toLowerCase())) {
                try { pl.playSound(pl.getLocation(), snd, 0.6f, 1.2f); } catch (Throwable ignored) {}
            }
        }
        if (plugin.getConfigManager().getConfig().getBoolean("staffchat.console_log", true)) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.stripColor(rendered));
        }
        if (plugin.getConfigManager().getConfig().getBoolean("staffchat.cross_server", true)) {
            try { plugin.getProxyMessenger().sendStaffChat(sender.getUniqueId(), sender.getName(), message); } catch (Throwable ignored) {}
            try { plugin.getRedisBridge().publishStaffChat(sender.getName(), message); } catch (Throwable ignored) {}
        }
        try { plugin.getDiscordBridge().sendStaffChat(sender.getName(), message); } catch (Throwable ignored) {}
    }

    /** Broadcast originating from network; do not forward again. */
    public void broadcastLocal(String name, String message) {
        String rendered = legacyFormat(name, message);
        for (Player pl : Bukkit.getOnlinePlayers()) {
            if (pl.hasPermission("staffmode.chat")) pl.sendMessage(rendered);
        }
        if (plugin.getConfigManager().getConfig().getBoolean("staffchat.console_log", true)) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.stripColor(rendered));
        }
    }
}
