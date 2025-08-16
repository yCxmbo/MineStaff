package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.gui.StaffListGUI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class StaffListGUIListener implements Listener {

    private boolean isStaffList(String title) {
        return title != null && ChatColor.stripColor(title).equalsIgnoreCase(
                ChatColor.stripColor(StaffListGUI.TITLE)
        );
    }

    @EventHandler
    public void onListClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (!isStaffList(e.getView().getTitle())) return;

        // Absolutely read-only:
        e.setCancelled(true);

        // Also block hotbar swaps
        if (e.getClick() == ClickType.NUMBER_KEY) e.setCancelled(true);

        // (Optional) if you want click actions like TP to player on left click,
        // handle them here WITHOUT letting items move. Example:
        // if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.PLAYER_HEAD) { ... }
    }

    @EventHandler
    public void onListDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (!isStaffList(e.getView().getTitle())) return;
        e.setCancelled(true);
    }
}
