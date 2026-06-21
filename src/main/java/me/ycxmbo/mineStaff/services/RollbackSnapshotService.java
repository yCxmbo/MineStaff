package me.ycxmbo.mineStaff.services;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.RollbackManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

/**
 * Periodically snapshots online players' inventories into the
 * {@link RollbackManager}, complementing the existing on-death snapshots.
 *
 * <p>Driven entirely by config under {@code rollback.periodic.*}. Snapshots are
 * subject to the same per-player retention cap as death snapshots.</p>
 */
public class RollbackSnapshotService {
    private final MineStaff plugin;
    private BukkitTask task;

    public RollbackSnapshotService(MineStaff plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        if (!plugin.getConfig().getBoolean("rollback.periodic.enabled", false)) {
            return;
        }
        long intervalSeconds = Math.max(30L, plugin.getConfig().getLong("rollback.periodic.interval-seconds", 300L));
        long ticks = intervalSeconds * 20L;
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::snapshotAll, ticks, ticks);
        plugin.getLogger().info("Periodic inventory snapshots enabled (every " + intervalSeconds + "s).");
    }

    public void stop() {
        if (task != null) {
            try { task.cancel(); } catch (Throwable ignored) {}
            task = null;
        }
    }

    private void snapshotAll() {
        boolean skipEmpty = plugin.getConfig().getBoolean("rollback.periodic.skip-empty", true);
        String bypassPerm = "staffmode.rollback.snapshot.bypass";
        for (Player p : Bukkit.getOnlinePlayers()) {
            try {
                if (p.hasPermission(bypassPerm)) continue;
                ItemStack[] inv = p.getInventory().getContents();
                ItemStack[] ec = p.getEnderChest().getContents();
                if (skipEmpty && isEmpty(inv) && isEmpty(ec)) continue;
                plugin.getRollbackManager().saveSnapshot(p.getUniqueId(),
                        new RollbackManager.Snapshot(inv, ec));
            } catch (Throwable t) {
                plugin.getLogger().warning("Failed to snapshot inventory for " + p.getName() + ": " + t.getMessage());
            }
        }
    }

    private boolean isEmpty(ItemStack[] arr) {
        if (arr == null) return true;
        for (ItemStack i : arr) {
            if (i != null && i.getType() != Material.AIR) return false;
        }
        return true;
    }
}
