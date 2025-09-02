package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/** Ensures staff in Staff Mode remain in configured gamemode across teleports/world changes/respawn. */
public class StaffGameModeGuardListener implements Listener {
    private final MineStaff plugin;
    private final StaffDataManager data;

    public StaffGameModeGuardListener(MineStaff plugin) {
        this.plugin = plugin;
        this.data = plugin.getStaffDataManager();
    }

    private void ensureMode(Player p) {
        if (!data.isStaffMode(p)) return;
        String gm = plugin.getConfigManager().getConfig().getString("options.staffmode_gamemode", "CREATIVE");
        try {
            GameMode mode = GameMode.valueOf(gm.toUpperCase());
            if (p.getGameMode() != mode) p.setGameMode(mode);
        } catch (IllegalArgumentException ignored) {}
    }

    @EventHandler public void onTeleport(PlayerTeleportEvent e) { ensureMode(e.getPlayer()); }
    @EventHandler public void onWorld(PlayerChangedWorldEvent e) { ensureMode(e.getPlayer()); }
    @EventHandler public void onRespawn(PlayerRespawnEvent e) { ensureMode(e.getPlayer()); }
}

