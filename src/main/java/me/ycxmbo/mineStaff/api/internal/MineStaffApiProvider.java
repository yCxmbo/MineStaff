package me.ycxmbo.mineStaff.api.internal;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.api.MineStaffAPI;
import me.ycxmbo.mineStaff.api.events.FreezeToggleEvent;
import me.ycxmbo.mineStaff.api.events.StaffModeToggleEvent;
import me.ycxmbo.mineStaff.api.events.VanishToggleEvent;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.UUID;

public class MineStaffApiProvider implements MineStaffAPI {

    private final MineStaff plugin;
    private final StaffDataManager data;

    // optional cached reflective methods
    private Method mSetStaffMode;     // setStaffMode(Player, boolean)
    private Method mEnterStaffMode;   // enter/enable/start/activate(Player)
    private Method mExitStaffMode;    // exit/disable/stop/deactivate(Player)
    private boolean lookedUp = false;

    public MineStaffApiProvider(MineStaff plugin) {
        this.plugin = plugin;
        this.data = plugin.getStaffDataManager();
    }

    // ---------- interface reads ----------
    @Override public boolean isStaffMode(Player player) { return data.isStaffMode(player); }
    @Override public boolean isStaffMode(UUID playerId) {
        Player p = playerId == null ? null : Bukkit.getPlayer(playerId);
        return p != null && data.isStaffMode(p);
    }

    @Override public boolean isVanished(Player player) { return data.isVanished(player); }
    @Override public boolean isVanished(UUID playerId) {
        Player p = playerId == null ? null : Bukkit.getPlayer(playerId);
        return p != null && data.isVanished(p);
    }

    @Override public boolean isFrozen(Player player) { return data.isFrozen(player); }
    @Override public boolean isFrozen(UUID playerId) {
        Player p = playerId == null ? null : Bukkit.getPlayer(playerId);
        return p != null && data.isFrozen(p);
    }

    @Override
    public StaffSnapshot snapshot(Player player) {
        return new StaffSnapshot(
                player.getUniqueId(),
                data.isStaffMode(player),
                data.isVanished(player),
                data.isFrozen(player)
        );
    }

    // ---------- interface writes ----------
    @Override
    public boolean setStaffMode(Player player, boolean enabled, ToggleCause cause) {
        boolean before = data.isStaffMode(player);
        boolean after = applyStaffMode(player, enabled);
        if (before != after) {
            Bukkit.getPluginManager().callEvent(new StaffModeToggleEvent(player, after, cause));
        }
        return after;
    }

    @Override
    public boolean setVanish(Player player, boolean enabled, ToggleCause cause) {
        boolean before = data.isVanished(player);
        try {
            data.setVanished(player, enabled); // your manager usually returns void
        } catch (Throwable t) {
            plugin.getLogger().warning("[API] setVanish failed: " + t.getMessage());
        }
        boolean after = data.isVanished(player);
        if (before != after) {
            Bukkit.getPluginManager().callEvent(new VanishToggleEvent(player, after, cause));
        }
        return after;
    }

    @Override
    public boolean setFrozen(Player target, boolean enabled, ToggleCause cause) {
        boolean before = data.isFrozen(target);
        try {
            data.setFrozen(target, enabled);   // your manager usually returns void
        } catch (Throwable t) {
            plugin.getLogger().warning("[API] setFrozen failed: " + t.getMessage());
        }
        boolean after = data.isFrozen(target);
        if (before != after) {
            Bukkit.getPluginManager().callEvent(new FreezeToggleEvent(target, after, cause));
        }
        return after;
    }

    // ---------- helpers ----------
    private void lookupOnce() {
        if (lookedUp) return;
        lookedUp = true;
        Class<?> c = data.getClass();
        try { mSetStaffMode = c.getMethod("setStaffMode", Player.class, boolean.class); } catch (NoSuchMethodException ignored) {}
        // enable/enter
        for (String n : new String[]{"enterStaffMode","enableStaffMode","startStaffMode","activateStaffMode"}) {
            if (mEnterStaffMode != null) break;
            try { mEnterStaffMode = c.getMethod(n, Player.class); } catch (NoSuchMethodException ignored) {}
        }
        // disable/exit
        for (String n : new String[]{"exitStaffMode","disableStaffMode","stopStaffMode","deactivateStaffMode"}) {
            if (mExitStaffMode != null) break;
            try { mExitStaffMode = c.getMethod(n, Player.class); } catch (NoSuchMethodException ignored) {}
        }
    }

    /** Try direct setter; otherwise try enable/exit methods; always return current manager state. */
    private boolean applyStaffMode(Player p, boolean enabled) {
        lookupOnce();
        try {
            if (mSetStaffMode != null) {
                mSetStaffMode.invoke(data, p, enabled);
            } else if (enabled && mEnterStaffMode != null) {
                mEnterStaffMode.invoke(data, p);
            } else if (!enabled && mExitStaffMode != null) {
                mExitStaffMode.invoke(data, p);
            } else {
                plugin.getLogger().warning("[API] No staff-mode method found in StaffDataManager; state unchanged.");
            }
        } catch (Throwable t) {
            plugin.getLogger().warning("[API] Failed to toggle staff mode: " + t.getMessage());
        }
        return data.isStaffMode(p);
    }
}
