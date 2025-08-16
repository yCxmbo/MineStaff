package me.ycxmbo.mineStaff.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CPSCheckManager {
    private static class Session {
        final UUID staff;
        final long start = System.currentTimeMillis();
        volatile int clicks = 0;
        Session(UUID staff) { this.staff = staff; }
    }

    // key = target uuid
    private final Map<UUID, Session> active = new ConcurrentHashMap<>();

    public boolean begin(Player staff, Player target) {
        return active.putIfAbsent(target.getUniqueId(), new Session(staff.getUniqueId())) == null;
    }

    public void tickClick(Player target) {
        Session s = active.get(target.getUniqueId());
        if (s != null) s.clicks++;
    }

    public boolean isChecking(Player target) {
        return active.containsKey(target.getUniqueId());
    }

    /** finish after 10s (200 ticks) and message staff + target */
    public void finishLater(Player staff, Player target) {
        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("MineStaff"), () -> {
            Session s = active.remove(target.getUniqueId());
            if (s == null) return;
            double cps = s.clicks / 10.0;
            if (staff != null && staff.isOnline()) {
                staff.sendMessage("§a[CPS] §f" + target.getName() + ": §e" + String.format(java.util.Locale.US, "%.2f", cps) + " CPS");
            }
            if (target != null && target.isOnline()) {
                target.sendMessage("§eA staff member measured your CPS: §f" + String.format(java.util.Locale.US, "%.2f", cps));
            }
        }, 200L);
    }
}
