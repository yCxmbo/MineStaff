package me.ycxmbo.mineStaff.managers;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.api.MineStaffAPI;
import me.ycxmbo.mineStaff.util.VanishUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class StaffDutyManager {
    private final MineStaff plugin;
    private final StaffDataManager data;
    private final Set<UUID> duty = new HashSet<>();

    public StaffDutyManager(MineStaff plugin) {
        this.plugin = plugin;
        this.data = plugin.getStaffDataManager();
    }

    public boolean isOnDuty(Player p) { return duty.contains(p.getUniqueId()); }

    public void enterDuty(Player p) {
        if (isOnDuty(p)) return;
        duty.add(p.getUniqueId());
        // Turn on staff mode via API for consistency and events
        MineStaffAPI.get().ifPresent(api -> api.setStaffMode(p, true, MineStaffAPI.ToggleCause.COMMAND));
        // Ensure vanish if configured
        if (!data.isVanished(p)) {
            data.setVanished(p, true);
            Bukkit.getPluginManager().callEvent(new me.ycxmbo.mineStaff.api.events.VanishToggleEvent(p, true, MineStaffAPI.ToggleCause.API));
        }
        // Give tools
        plugin.getToolManager().giveStaffTools(p);
        // Fly on
        try { p.setAllowFlight(true); p.setFlying(true); } catch (Throwable ignored) {}
        try {
            java.util.List<String> nodes = plugin.getConfigManager().getConfig().getStringList("luckperms.duty_temp_perms");
            if (nodes != null && !nodes.isEmpty()) me.ycxmbo.mineStaff.luckperms.LuckPermsBridge.addTempPerms(p, nodes);
        } catch (Throwable ignored) {}
    }

    public void exitDuty(Player p) {
        if (!isOnDuty(p)) return;
        duty.remove(p.getUniqueId());
        // Turn off staff mode via API
        MineStaffAPI.get().ifPresent(api -> api.setStaffMode(p, false, MineStaffAPI.ToggleCause.COMMAND));
        // Optionally unvanish
        if (data.isVanished(p)) {
            data.setVanished(p, false);
            Bukkit.getPluginManager().callEvent(new me.ycxmbo.mineStaff.api.events.VanishToggleEvent(p, false, MineStaffAPI.ToggleCause.API));
        }
        // Disable flight if not allowed otherwise
        if (!p.hasPermission("essentials.fly") && !p.isOp()) {
            try { p.setFlying(false); p.setAllowFlight(false); } catch (Throwable ignored) {}
        }
        // Tools will be restored by staff mode exit inventory restore
        VanishUtil.applyVanish(p, false);
        try {
            java.util.List<String> nodes = plugin.getConfigManager().getConfig().getStringList("luckperms.duty_temp_perms");
            if (nodes != null && !nodes.isEmpty()) me.ycxmbo.mineStaff.luckperms.LuckPermsBridge.removePerms(p, nodes);
        } catch (Throwable ignored) {}
    }
}
