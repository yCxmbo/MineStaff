package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.tickets.StaffTicket;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for staff tickets GUI interactions
 */
public class StaffTicketsGUIListener implements Listener {
    private final MineStaff plugin;

    public StaffTicketsGUIListener(MineStaff plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (!title.startsWith("§6Staff Tickets")) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        int slot = event.getSlot();
        int currentPage = plugin.getStaffTicketsGUI().getCurrentPage(player);
        String currentFilter = plugin.getStaffTicketsGUI().getCurrentFilter(player);

        // Filter buttons
        if (slot == 0) { // Open
            plugin.getStaffTicketsGUI().open(player, 0, "open");
            return;
        }
        if (slot == 1) { // Claimed
            plugin.getStaffTicketsGUI().open(player, 0, "claimed");
            return;
        }
        if (slot == 2) { // Mine
            plugin.getStaffTicketsGUI().open(player, 0, "mine");
            return;
        }
        if (slot == 3) { // Resolved
            plugin.getStaffTicketsGUI().open(player, 0, "resolved");
            return;
        }
        if (slot == 4) { // All
            plugin.getStaffTicketsGUI().open(player, 0, "all");
            return;
        }

        // Navigation
        if (slot == 48) { // Previous page
            plugin.getStaffTicketsGUI().open(player, currentPage - 1, currentFilter);
            return;
        }
        if (slot == 50) { // Next page
            plugin.getStaffTicketsGUI().open(player, currentPage + 1, currentFilter);
            return;
        }

        // Close button
        if (slot == 49) {
            player.closeInventory();
            return;
        }

        // Ticket items (slots 9-53)
        if (slot >= 9 && slot < 54 && clicked.hasItemMeta() && clicked.getItemMeta().hasLore()) {
            // Extract ticket ID from lore
            String lore = clicked.getItemMeta().getLore().get(0); // First line has ID
            if (lore.startsWith("§7ID: §8")) {
                String shortId = lore.substring(8, 16); // Extract short ID
                player.closeInventory();
                player.performCommand("ticket view " + shortId);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (title.startsWith("§6Staff Tickets")) {
            plugin.getStaffTicketsGUI().cleanup(player);
        }
    }
}
