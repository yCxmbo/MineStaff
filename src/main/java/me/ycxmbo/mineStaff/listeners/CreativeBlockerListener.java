package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCreativeEvent;

public class CreativeBlockerListener implements Listener {
    private final StaffDataManager data;

    public CreativeBlockerListener(MineStaff plugin) {
        this.data = plugin.getStaffDataManager();
    }

    @EventHandler
    public void onCreativePickup(InventoryCreativeEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (data.isStaffMode(p)) {
            // Cancel any attempt to pull from the Creative tabs
            e.setCancelled(true);
        }
    }
}
