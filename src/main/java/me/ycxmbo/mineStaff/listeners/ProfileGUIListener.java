package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.offline.OfflineInventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ProfileGUIListener implements Listener {
    private final OfflineInventoryManager offInv;

    public ProfileGUIListener(OfflineInventoryManager offInv) { this.offInv = offInv; }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String title = e.getView().getTitle(); if (title == null) return;
        if (!ChatColor.stripColor(title).startsWith("Profile:")) return;
        e.setCancelled(true);
        ItemStack it = e.getCurrentItem(); if (it == null || it.getItemMeta() == null || it.getItemMeta().getLore() == null) return;
        UUID target = null; String type = null;
        for (String l : it.getItemMeta().getLore()) {
            String s = ChatColor.stripColor(l);
            if (s.startsWith("TARGET:")) try { target = java.util.UUID.fromString(s.substring(7).trim()); } catch (Exception ignored) {}
            if (s.startsWith("TYPE:")) type = s.substring(5).trim();
        }
        if (target == null || type == null) return;
        OfflinePlayer off = Bukkit.getOfflinePlayer(target);
        if ("INV".equalsIgnoreCase(type)) offInv.openInventory(p, off);
        if ("EC".equalsIgnoreCase(type)) offInv.openEnderChest(p, off);
        if ("HISTORY".equalsIgnoreCase(type)) {
            if (!me.ycxmbo.mineStaff.bridge.HistoryBridge.openHistory(me.ycxmbo.mineStaff.MineStaff.getInstance(), p, off.getName() != null ? off.getName() : off.getUniqueId().toString())) {
                p.sendMessage(ChatColor.RED + "No history plugin detected (CoreProtect/Prism).");
            }
        }
    }
}
