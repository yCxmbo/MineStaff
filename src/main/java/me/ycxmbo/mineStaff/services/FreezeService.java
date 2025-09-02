package me.ycxmbo.mineStaff.services;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.api.MineStaffAPI;
import me.ycxmbo.mineStaff.api.events.FreezeToggleEvent;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class FreezeService {
    private final MineStaff plugin;
    private final StaffDataManager data;

    private final Map<UUID, BukkitTask> timers = new HashMap<>();
    private BukkitTask particleTask;

    public FreezeService(MineStaff plugin) {
        this.plugin = plugin;
        this.data = plugin.getStaffDataManager();
        startParticles();
    }

    public boolean toggle(Player actor, Player target, Boolean newState, MineStaffAPI.ToggleCause cause, int durationSeconds) {
        if (target.hasPermission("staffmode.freeze.bypass")) {
            if (actor != null) actor.sendMessage(ChatColor.RED + "Target cannot be frozen.");
            return false;
        }
        boolean state = (newState != null ? newState : !data.isFrozen(target));
        data.setFrozen(target, state);

        // Fire API event
        Bukkit.getPluginManager().callEvent(new FreezeToggleEvent(target, state, cause));

        String msg = ChatColor.YELLOW + "Player " + target.getName() + " " + (state ? "frozen." : "unfrozen.");
        if (actor != null) actor.sendMessage(msg);
        if (state) target.sendMessage(ChatColor.RED + "You have been frozen by staff. Do not log out.");

        try { plugin.getDiscordBridge().sendAlert("Freeze: " + (actor == null ? "Console" : actor.getName()) + " -> " + target.getName() + " = " + state); } catch (Throwable ignored) {}
        try { plugin.getAuditLogger().log(java.util.Map.of(
                "type","freeze","actor", String.valueOf(actor == null ? null : actor.getUniqueId()),
                "target", target.getUniqueId().toString(),
                "state", String.valueOf(state),
                "cause", String.valueOf(cause)
        )); } catch (Throwable ignored) {}

        // Timed freeze handling
        cancelTimer(target.getUniqueId());
        if (state && durationSeconds > 0) {
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try { toggle(null, target, false, MineStaffAPI.ToggleCause.OTHER, 0); } catch (Throwable ignored) {}
            }, durationSeconds * 20L);
            timers.put(target.getUniqueId(), task);
        }
        return state;
    }

    public void cancelTimer(UUID target) {
        BukkitTask t = timers.remove(target);
        if (t != null) t.cancel();
    }

    private void startParticles() {
        if (particleTask != null) return;
        particleTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            var cfg = plugin.getConfigManager().getConfig();
            boolean enabled = cfg.getBoolean("freeze.visual_cage.enabled", true);
            String particleName = cfg.getString("freeze.visual_cage.particle", "SNOWFLAKE");
            Particle particle = Particle.SNOWFLAKE;
            try { particle = Particle.valueOf(particleName); } catch (IllegalArgumentException ignored) {}
            double r = cfg.getDouble("freeze.visual_cage.radius", 0.7);
            int points = 16;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!data.isFrozen(p)) continue;
                if (!enabled) continue;
                var loc = p.getLocation().add(0, 1, 0);
                for (int i = 0; i < points; i++) {
                    double ang = (2 * Math.PI * i) / points;
                    p.getWorld().spawnParticle(particle, loc.getX() + r * Math.cos(ang), loc.getY(), loc.getZ() + r * Math.sin(ang), 1, 0, 0, 0, 0);
                }
                p.sendActionBar(ChatColor.RED + "You are frozen.");
            }
        }, 20L, 40L); // every 2 seconds
    }

    public void stop() {
        for (BukkitTask t : timers.values()) try { t.cancel(); } catch (Throwable ignored) {}
        timers.clear();
        if (particleTask != null) try { particleTask.cancel(); } catch (Throwable ignored) {}
        particleTask = null;
    }
}

