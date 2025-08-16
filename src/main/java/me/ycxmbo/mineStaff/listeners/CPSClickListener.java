package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.CPSCheckManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;

public class CPSClickListener implements Listener {
    private final CPSCheckManager cps;

    public CPSClickListener(MineStaff plugin) {
        this.cps = plugin.getCPSManager();
    }

    @EventHandler
    public void onSwing(PlayerAnimationEvent e) {
        if (e.getAnimationType() != PlayerAnimationType.ARM_SWING) return; // left click animation
        Player p = e.getPlayer();
        if (cps.isChecking(p)) cps.tickClick(p);
    }
}
