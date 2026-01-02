package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.tools.InspectorGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class InspectorGUIListener implements Listener {
    private static final String INV_VIEW_PREFIX = ChatColor.GOLD + "Inspecting: " + ChatColor.YELLOW;
    private static final String EC_VIEW_PREFIX = ChatColor.LIGHT_PURPLE + "Inspecting EC: " + ChatColor.YELLOW;

    private final MineStaff plugin;
    public InspectorGUIListener(MineStaff plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player viewer)) return;
        String title = e.getView() == null ? null : e.getView().getTitle();
        if (title == null) return;

        String stripped = ChatColor.stripColor(title);
        if (stripped == null || !stripped.startsWith("Inspector:")) return;

        e.setCancelled(true);
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getItemMeta() == null || clicked.getItemMeta().getDisplayName() == null) return;

        UUID targetId = InspectorGUI.extractTargetFromItem(clicked);
        if (targetId == null) targetId = InspectorGUI.extractTargetFromTitle(title);
        if (targetId == null) { viewer.sendMessage(ChatColor.RED + "Unable to resolve target."); return; }

        OfflinePlayer off = Bukkit.getOfflinePlayer(targetId);
        if (!off.isOnline()) { viewer.sendMessage(ChatColor.RED + "Target is not online."); return; }
        Player target = off.getPlayer();

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).toLowerCase();

        if (name.contains("inventory")) {
            // Create a read-only copy of the inventory
            Inventory copy = Bukkit.createInventory(viewer, 54, INV_VIEW_PREFIX + target.getName());
            copy.setContents(target.getInventory().getContents());
            viewer.openInventory(copy);
            return;
        }
        if (name.contains("ender chest")) {
            // Create a read-only copy of the ender chest
            Inventory copy = Bukkit.createInventory(viewer, 27, EC_VIEW_PREFIX + target.getName());
            copy.setContents(target.getEnderChest().getContents());
            viewer.openInventory(copy);
            return;
        }
        if (name.contains("close")) { viewer.closeInventory(); return; }
    }

    @EventHandler
    public void onInspectClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String title = e.getView() == null ? null : e.getView().getTitle();
        if (title == null) return;

        String stripped = ChatColor.stripColor(title);
        if (stripped.startsWith("Inspecting: ") || stripped.startsWith("Inspecting EC: ")) {
            e.setCancelled(true);
            if (e.getClick() == ClickType.NUMBER_KEY) {
                int hotbar = e.getHotbarButton();
                if (hotbar >= 0 && hotbar < 9) {
                    ItemStack hb = p.getInventory().getItem(hotbar);
                    if (hb != null) e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onInspectDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        String title = e.getView() == null ? null : e.getView().getTitle();
        if (title == null) return;

        String stripped = ChatColor.stripColor(title);
        if (stripped.startsWith("Inspecting: ") || stripped.startsWith("Inspecting EC: ")) {
            e.setCancelled(true);
        }
    }
}
