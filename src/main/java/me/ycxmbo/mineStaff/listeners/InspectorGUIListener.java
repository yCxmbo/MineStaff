package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.tools.InspectorGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class InspectorGUIListener implements Listener {
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

        UUID targetId = InspectorGUI.getTargetForViewer(viewer.getUniqueId());
        if (targetId == null) { viewer.sendMessage(ChatColor.RED + "Unable to resolve target."); return; }

        OfflinePlayer off = Bukkit.getOfflinePlayer(targetId);
        if (!off.isOnline()) { viewer.sendMessage(ChatColor.RED + "Target is not online."); return; }
        Player target = off.getPlayer();

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).toLowerCase();

        if (name.contains("inventory")) { viewer.openInventory(target.getInventory()); return; }
        if (name.contains("ender chest")) { viewer.openInventory(target.getEnderChest()); return; }
        if (name.contains("close")) {
            InspectorGUI.clearTargetForViewer(viewer.getUniqueId());
            viewer.closeInventory();
            return;
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player viewer)) return;
        String title = e.getView() == null ? null : e.getView().getTitle();
        if (title == null) return;

        String stripped = ChatColor.stripColor(title);
        if (stripped != null && stripped.startsWith("Inspector:")) {
            InspectorGUI.clearTargetForViewer(viewer.getUniqueId());
        }
    }
}
