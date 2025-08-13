package me.ycxmbo.mineStaff.gui;

import me.ycxmbo.mineStaff.managers.ReportManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ReportsGUI {
    private final ReportManager reports;

    public ReportsGUI(ReportManager reports) { this.reports = reports; }

    public void open(Player viewer) {
        Inventory inv = Bukkit.createInventory(viewer, 54, ChatColor.RED + "Reports");
        List<ReportManager.Report> list = reports.all();
        for (ReportManager.Report r : list) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(r.target);
            ItemStack it = new ItemStack(Material.PAPER);
            ItemMeta meta = it.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + (target.getName() != null ? target.getName() : r.target.toString()));
            meta.setLore(java.util.List.of(
                    ChatColor.GRAY + "Reason: " + r.reason,
                    ChatColor.GRAY + "Status: " + r.status + (r.claimedBy != null ? " by " + Bukkit.getOfflinePlayer(r.claimedBy).getName() : ""),
                    ChatColor.DARK_GRAY + "ID: " + r.id
            ));
            it.setItemMeta(meta);
            inv.addItem(it);
        }
        viewer.openInventory(inv);
    }
}
