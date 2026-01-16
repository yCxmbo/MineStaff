package me.ycxmbo.mineStaff.managers;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player follow mode for staff members
 */
public class FollowManager {
    private final MineStaff plugin;
    private final Map<UUID, UUID> following = new HashMap<>(); // follower -> target
    private final Map<UUID, BukkitTask> tasks = new HashMap<>();
    private static final long FOLLOW_INTERVAL_TICKS = 20L; // 1 second

    public FollowManager(MineStaff plugin) {
        this.plugin = plugin;
    }

    /**
     * Start following a player
     */
    public boolean startFollowing(Player follower, Player target) {
        if (follower.equals(target)) {
            return false;
        }

        // Stop existing follow if any
        stopFollowing(follower);

        following.put(follower.getUniqueId(), target.getUniqueId());

        // Create repeating task to teleport
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Player currentTarget = Bukkit.getPlayer(target.getUniqueId());
            Player currentFollower = Bukkit.getPlayer(follower.getUniqueId());

            // Stop if either player is offline
            if (currentTarget == null || currentFollower == null) {
                stopFollowing(follower);
                return;
            }

            // Stop if follower is no longer in staff mode
            if (!plugin.getStaffDataManager().isStaffMode(currentFollower)) {
                stopFollowing(currentFollower);
                if (currentFollower.isOnline()) {
                    currentFollower.sendMessage("§cFollow mode disabled - you left staff mode.");
                }
                return;
            }

            // Teleport to target
            Location targetLoc = currentTarget.getLocation();
            currentFollower.teleport(targetLoc);

        }, 0L, FOLLOW_INTERVAL_TICKS);

        tasks.put(follower.getUniqueId(), task);
        return true;
    }

    /**
     * Stop following
     */
    public void stopFollowing(Player follower) {
        UUID followerId = follower.getUniqueId();

        if (tasks.containsKey(followerId)) {
            tasks.get(followerId).cancel();
            tasks.remove(followerId);
        }

        following.remove(followerId);
    }

    /**
     * Check if a player is currently following someone
     */
    public boolean isFollowing(Player follower) {
        return following.containsKey(follower.getUniqueId());
    }

    /**
     * Get who a player is following
     */
    public Player getFollowingTarget(Player follower) {
        UUID targetId = following.get(follower.getUniqueId());
        if (targetId == null) return null;
        return Bukkit.getPlayer(targetId);
    }

    /**
     * Stop all active follows
     */
    public void stopAll() {
        for (BukkitTask task : tasks.values()) {
            task.cancel();
        }
        tasks.clear();
        following.clear();
    }
}
