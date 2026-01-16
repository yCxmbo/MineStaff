package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class ReportHistoryGUIListener implements Listener {
    private final MineStaff plugin;

    public ReportHistoryGUIListener(MineStaff plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (!title.startsWith("§6Reports: §e")) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        // Extract player name from title
        String[] parts = title.split("§e");
        if (parts.length < 2) return;
        String playerName = parts[1].split(" ")[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

        int slot = event.getSlot();
        String currentMode = plugin.getReportHistoryGUI().getCurrentMode(player);
        int currentPage = plugin.getReportHistoryGUI().getCurrentPage(player);

        // Mode selectors
        if (slot == 0) { // Filed mode
            plugin.getReportHistoryGUI().open(player, target, 0, "filed");
            return;
        }
        if (slot == 1) { // Against mode
            plugin.getReportHistoryGUI().open(player, target, 0, "against");
            return;
        }
        if (slot == 2) { // All mode
            plugin.getReportHistoryGUI().open(player, target, 0, "all");
            return;
        }

        // Navigation
        if (slot == 48) { // Previous page
            plugin.getReportHistoryGUI().open(player, target, currentPage - 1, currentMode);
            return;
        }
        if (slot == 50) { // Next page
            plugin.getReportHistoryGUI().open(player, target, currentPage + 1, currentMode);
            return;
        }

        // Close button
        if (slot == 49) {
            player.closeInventory();
            return;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (title.startsWith("§6Reports: §e")) {
            plugin.getReportHistoryGUI().cleanup(player);
        }
    }
}
