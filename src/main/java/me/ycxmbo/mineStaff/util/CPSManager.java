package me.ycxmbo.mineStaff.util;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CPSManager {
    private final MineStaff plugin;
    private final Map<UUID, Integer> clicks = new HashMap<>();

    public CPSManager(MineStaff plugin) { this.plugin = plugin; }

    public void recordClick(Player p) {
        clicks.put(p.getUniqueId(), clicks.getOrDefault(p.getUniqueId(), 0) + 1);
    }

    public void startTest(Player staff, Player target, int seconds) {
        clicks.remove(target.getUniqueId());
        staff.sendMessage("Starting CPS test for " + target.getName() + " (" + seconds + "s)");
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            int c = clicks.getOrDefault(target.getUniqueId(), 0);
            double cps = (double) c / seconds;
            staff.sendMessage("Result: " + target.getName() + " ~ " + String.format("%.2f", cps) + " CPS");
        }, 20L * seconds);
    }
}
