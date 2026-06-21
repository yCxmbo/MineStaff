package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.gui.StaffStatsGUI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/** Keeps the read-only staff analytics GUI from being interacted with. */
public class StaffStatsGUIListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getView() == null || e.getView().getTitle() == null) return;
        if (ChatColor.stripColor(e.getView().getTitle()).equalsIgnoreCase(StaffStatsGUI.TITLE)) {
            e.setCancelled(true);
        }
    }
}
