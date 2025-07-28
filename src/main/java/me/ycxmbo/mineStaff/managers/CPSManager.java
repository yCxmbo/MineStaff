package me.ycxmbo.mineStaff.managers;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CPSManager {

    private final MineStaff plugin;
    private final Map<UUID, Integer> clickCounts = new HashMap<>();
    private final Map<UUID, Boolean> activeTests = new HashMap<>();

    private final int testDurationSeconds = 5;

    public CPSManager(MineStaff plugin) {
        this.plugin = plugin;
    }

    public boolean isTesting(Player player) {
        return activeTests.getOrDefault(player.getUniqueId(), false);
    }

    public void startTest(Player staff, Player target) {
        UUID targetUUID = target.getUniqueId();
        if (isTesting(target)) {
            staff.sendMessage("§cThat player is already undergoing a CPS test.");
            return;
        }

        activeTests.put(targetUUID, true);
        clickCounts.put(targetUUID, 0);

        staff.sendMessage("§aStarted CPS test on " + target.getName() + ". Test will last " + testDurationSeconds + " seconds.");
        target.sendMessage("§eYou are undergoing a CPS test. Please click as fast as you can!");

        // Schedule test end
        new BukkitRunnable() {
            @Override
            public void run() {
                endTest(staff, target);
            }
        }.runTaskLater(plugin, testDurationSeconds * 20L); // 20 ticks per second
    }

    public void recordClick(Player player) {
        if (!isTesting(player)) return;

        UUID uuid = player.getUniqueId();
        clickCounts.put(uuid, clickCounts.getOrDefault(uuid, 0) + 1);
    }

    private void endTest(Player staff, Player target) {
        UUID uuid = target.getUniqueId();
        if (!isTesting(target)) return;

        int clicks = clickCounts.getOrDefault(uuid, 0);
        double cps = clicks / (double) testDurationSeconds;

        staff.sendMessage("§6CPS Test finished for " + target.getName() + ": §c" + String.format("%.2f CPS", cps));
        target.sendMessage("§eYour CPS test is finished. Your CPS was: §c" + String.format("%.2f CPS", cps));

        // Clean up
        activeTests.remove(uuid);
        clickCounts.remove(uuid);
    }
}
