package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import me.ycxmbo.mineStaff.util.AlertFormatter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class FreezeQuitListener implements Listener {
    private final MineStaff plugin;
    private final StaffDataManager staff;

    public FreezeQuitListener(MineStaff plugin) {
        this.plugin = plugin;
        this.staff = plugin.getStaffDataManager();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (!staff.isFrozen(e.getPlayer())) return;
        var cfg = plugin.getConfigManager().getConfig();
        if (!cfg.getBoolean("freeze.logout_flag_enabled", true)) return;
        String content = e.getPlayer().getName() + " logged out while frozen.";
        AlertFormatter.broadcast(plugin, content, e.getPlayer().getName());
        try { plugin.getAuditLogger().log(java.util.Map.of(
                "type","freeze_logout",
                "player", e.getPlayer().getUniqueId().toString()
        )); } catch (Throwable ignored) {}
    }
}

