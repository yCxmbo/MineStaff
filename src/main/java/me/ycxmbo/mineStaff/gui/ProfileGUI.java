package me.ycxmbo.mineStaff.gui;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.InfractionManager;
import me.ycxmbo.mineStaff.managers.ReportManager;
import me.ycxmbo.mineStaff.notes.PlayerNotesManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public class ProfileGUI {
    private final MineStaff plugin;
    private final PlayerNotesManager notes;

    public ProfileGUI(MineStaff plugin) { this.plugin = plugin; this.notes = plugin.getPlayerNotesManager(); }

    public void open(Player viewer, UUID targetId) {
        OfflinePlayer off = Bukkit.getOfflinePlayer(targetId);
        String name = off.getName() != null ? off.getName() : targetId.toString();
        Inventory inv = Bukkit.createInventory(viewer, 27, ChatColor.DARK_AQUA + "Profile: " + ChatColor.AQUA + name);

        // Summary item
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta im = info.getItemMeta();
        im.setDisplayName(ChatColor.AQUA + "Summary");
        int reports = plugin.getReportManager().all().stream().filter(r -> r.target.equals(targetId)).toArray().length;
        int infractions = plugin.getInfractionManager().get(targetId).size();
        List<PlayerNotesManager.Note> noteList = notes.get(targetId);
        im.setLore(java.util.List.of(
                ChatColor.GRAY + "Reports: " + reports,
                ChatColor.GRAY + "Infractions: " + infractions,
                ChatColor.GRAY + "Notes: " + noteList.size()
        ));
        info.setItemMeta(im);
        inv.setItem(10, info);

        // Inventory button
        ItemStack invBtn = new ItemStack(Material.CHEST);
        var invMeta = invBtn.getItemMeta();
        invMeta.setDisplayName(ChatColor.GOLD + "View Inventory");
        invMeta.setLore(java.util.List.of(ChatColor.DARK_GRAY + "TARGET:" + targetId, ChatColor.DARK_GRAY + "TYPE:INV"));
        invBtn.setItemMeta(invMeta);
        inv.setItem(12, invBtn);

        // Ender chest button
        ItemStack ecBtn = new ItemStack(Material.ENDER_CHEST);
        var ecMeta = ecBtn.getItemMeta();
        ecMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "View Ender Chest");
        ecMeta.setLore(java.util.List.of(ChatColor.DARK_GRAY + "TARGET:" + targetId, ChatColor.DARK_GRAY + "TYPE:EC"));
        ecBtn.setItemMeta(ecMeta);
        inv.setItem(14, ecBtn);

        // History button (CoreProtect/Prism)
        ItemStack hist = new ItemStack(Material.BRUSH);
        var hm = hist.getItemMeta();
        hm.setDisplayName(ChatColor.GREEN + "View History");
        hm.setLore(java.util.List.of(ChatColor.DARK_GRAY + "TARGET:" + targetId, ChatColor.DARK_GRAY + "TYPE:HISTORY"));
        hist.setItemMeta(hm);
        inv.setItem(15, hist);

        // Close
        ItemStack close = new ItemStack(Material.BARRIER);
        var cm = close.getItemMeta(); cm.setDisplayName(ChatColor.RED + "Close"); close.setItemMeta(cm);
        inv.setItem(16, close);

        viewer.openInventory(inv);
    }
}
