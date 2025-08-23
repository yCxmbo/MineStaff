package me.ycxmbo.mineStaff.api;

import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public interface MineStaffAPI {
    boolean isStaffMode(Player player);
    boolean isStaffMode(UUID playerId);

    boolean isVanished(Player player);
    boolean isVanished(UUID playerId);

    boolean isFrozen(Player player);
    boolean isFrozen(UUID playerId);

    /** Toggle/set; returns final state. */
    boolean setStaffMode(Player player, boolean enabled, ToggleCause cause);
    boolean setVanish(Player player, boolean enabled, ToggleCause cause);
    boolean setFrozen(Player target, boolean enabled, ToggleCause cause);

    StaffSnapshot snapshot(Player player);

    enum ToggleCause { COMMAND, TOOL, API, OTHER }

    record StaffSnapshot(UUID uuid, boolean staffMode, boolean vanished, boolean frozen) {}

    static Optional<MineStaffAPI> get() {
        var plugin = org.bukkit.Bukkit.getPluginManager().getPlugin("MineStaff");
        if (plugin == null || !plugin.isEnabled()) return Optional.empty();
        var reg = org.bukkit.Bukkit.getServicesManager().getRegistration(MineStaffAPI.class);
        return reg == null ? Optional.empty() : Optional.of(reg.getProvider());
    }
}
