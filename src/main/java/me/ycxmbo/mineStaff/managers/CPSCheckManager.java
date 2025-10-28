package me.ycxmbo.mineStaff.managers;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.api.events.CPSCheckFinishEvent;
import me.ycxmbo.mineStaff.api.events.CPSCheckStartEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CPSCheckManager {
    private final MineStaff plugin;

    private static class Session {
        final UUID staff;
        final long start = System.currentTimeMillis();
        volatile int clicks = 0;
        Session(UUID staff) { this.staff = staff; }
    }

    // key = target uuid
    private final Map<UUID, Session> active = new ConcurrentHashMap<>();

    public CPSCheckManager(MineStaff plugin) { this.plugin = plugin; }

    public boolean begin(Player staff, Player target) {
        Session session = new Session(staff.getUniqueId());
        Session existing = active.putIfAbsent(target.getUniqueId(), session);
        if (existing == null) {
            Bukkit.getPluginManager().callEvent(new CPSCheckStartEvent(staff, target));
            return true;
        }
        return false;
    }

    public void tickClick(Player target) {
        Session s = active.get(target.getUniqueId());
        if (s != null) s.clicks++;
    }

    public boolean isChecking(Player target) {
        return active.containsKey(target.getUniqueId());
    }

    /** finish after configured seconds and message staff + target */
    public void finishLater(Player staff, Player target) {
        int seconds = plugin.getConfigManager().getConfig().getInt("cps.duration_seconds", 10);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Session s = active.remove(target.getUniqueId());
            if (s == null) return;
            double cps = seconds <= 0 ? s.clicks : (s.clicks / (double) seconds);
            Bukkit.getPluginManager().callEvent(new CPSCheckFinishEvent(staff, target, cps));
            if (staff != null && staff.isOnline()) {
                staff.sendMessage(ChatColor.GREEN + "[CPS] " + ChatColor.WHITE + target.getName() + ": " + ChatColor.YELLOW + String.format(java.util.Locale.US, "%.2f", cps) + " CPS");
            }
            if (target != null && target.isOnline()) {
                target.sendMessage(ChatColor.YELLOW + "A staff member measured your CPS: " + ChatColor.WHITE + String.format(java.util.Locale.US, "%.2f", cps));
            }
        }, Math.max(1, seconds) * 20L);
    }
}
