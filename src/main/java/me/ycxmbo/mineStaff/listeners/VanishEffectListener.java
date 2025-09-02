package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.api.events.VanishToggleEvent;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import me.ycxmbo.mineStaff.tools.ToolManager;
import me.ycxmbo.mineStaff.util.SoundUtil;
import me.ycxmbo.mineStaff.util.VanishUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Applies hide/show effects and persists state when vanish toggles.
 */
public class VanishEffectListener implements Listener {
    private final MineStaff plugin;
    private final StaffDataManager data;

    public VanishEffectListener(MineStaff plugin) {
        this.plugin = plugin;
        this.data = plugin.getStaffDataManager();
    }

    @EventHandler
    public void onVanish(VanishToggleEvent e) {
        // Apply visual + gameplay effects
        VanishUtil.applyVanish(e.getPlayer(), e.isEnabled());
        // Persist to store
        plugin.getVanishStore().setVanished(e.getPlayer().getUniqueId(), e.isEnabled());
        // Update tool dye indicator if player is in staff mode
        try { plugin.getToolManager().updateVanishDye(e.getPlayer(), e.isEnabled()); } catch (Throwable ignored) {}
        // Sounds
        if (e.isEnabled()) SoundUtil.playVanishOn(e.getPlayer()); else SoundUtil.playVanishOff(e.getPlayer());
    }
}

