package me.ycxmbo.mineStaff.gui;

import me.ycxmbo.mineStaff.managers.ReportManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ReportsGUI {
    private final ReportManager reports;
    private static final Map<java.util.UUID, Filter> filters = new HashMap<>();

    private static class Filter {
        String status = "ALL";     // ALL/OPEN/CLAIMED/CLOSED
        String category = "ALL";   // ALL or category
        String priority = "ALL";   // ALL/LOW/MEDIUM/HIGH/CRITICAL
    }

    public ReportsGUI(ReportManager reports) { this.reports = reports; }

    public void open(Player viewer) {
        Inventory inv = Bukkit.createInventory(viewer, 54, ChatColor.RED + "Reports");
        Filter f = filters.computeIfAbsent(viewer.getUniqueId(), k -> new Filter());
        // Filter controls top row
        inv.setItem(0, control(Material.NAME_TAG, ChatColor.AQUA + "Status: " + f.status));
        inv.setItem(1, control(Material.CHEST, ChatColor.GOLD + "Category: " + f.category));
        inv.setItem(2, control(Material.REDSTONE_TORCH, ChatColor.RED + "Priority: " + f.priority));
        inv.setItem(8, control(Material.SUNFLOWER, ChatColor.YELLOW + "Refresh"));

        List<ReportManager.Report> list = reports.all();
        List<ReportManager.Report> filtered = new ArrayList<>();
        for (ReportManager.Report r : list) {
            if (!f.status.equalsIgnoreCase("ALL")) {
                if (!r.status.equalsIgnoreCase(f.status)) continue;
            }
            if (!f.category.equalsIgnoreCase("ALL")) {
                if (r.category == null || !r.category.equalsIgnoreCase(f.category)) continue;
            }
            if (!f.priority.equalsIgnoreCase("ALL")) {
                if (r.priority == null || !r.priority.equalsIgnoreCase(f.priority)) continue;
            }
            filtered.add(r);
        }
        int index = 9; // start after control row
        for (ReportManager.Report r : filtered) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(r.target);
            ItemStack it = new ItemStack(Material.PAPER);
            ItemMeta meta = it.getItemMeta();
            boolean overdue = (r.dueBy > 0 && System.currentTimeMillis() > r.dueBy && !"CLOSED".equalsIgnoreCase(r.status));
            String name = (target.getName() != null ? target.getName() : r.target.toString());
            meta.setDisplayName((overdue ? ChatColor.RED + "[OVERDUE] " : ChatColor.YELLOW.toString()) + name);
            int evid = 0;
            try { evid = me.ycxmbo.mineStaff.MineStaff.getInstance().getEvidenceManager().count(r.id); } catch (Throwable ignored) {}
            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add(ChatColor.GRAY + "Reason: " + r.reason);
            lore.add(ChatColor.GRAY + "Status: " + r.status + (r.claimedBy != null ? " by " + Bukkit.getOfflinePlayer(r.claimedBy).getName() : ""));
            lore.add(ChatColor.GRAY + "Category: " + (r.category == null ? "GENERAL" : r.category));
            lore.add(ChatColor.GRAY + "Priority: " + (r.priority == null ? "MEDIUM" : r.priority));
            if (r.dueBy > 0) {
                long diff = r.dueBy - System.currentTimeMillis();
                String when = (diff >= 0 ? formatDuration(diff) + " left" : formatDuration(-diff) + " overdue");
                lore.add((diff >= 0 ? ChatColor.GREEN : ChatColor.RED) + "SLA: " + when);
            }
            lore.add(ChatColor.GRAY + "Evidence: " + evid);
            lore.add(ChatColor.DARK_GRAY + "ID: " + r.id);
            meta.setLore(lore);
            it.setItemMeta(meta);
            if (index < inv.getSize()) {
                inv.setItem(index++, it);
            } else {
                inv.addItem(it);
            }
        }
        viewer.openInventory(inv);
    }

    private ItemStack control(Material mat, String name) {
        ItemStack it = new ItemStack(mat);
        ItemMeta im = it.getItemMeta();
        if (im != null) { im.setDisplayName(name); it.setItemMeta(im); }
        return it;
    }

    private String formatDuration(long ms) {
        long s = ms / 1000L;
        long h = s / 3600; s %= 3600;
        long m = s / 60; s %= 60;
        if (h > 0) return h + "h " + m + "m";
        if (m > 0) return m + "m " + s + "s";
        return s + "s";
    }

    // Called by listener
    public static void cycleStatus(Player p) {
        Filter f = filters.computeIfAbsent(p.getUniqueId(), k -> new Filter());
        java.util.List<String> order = java.util.List.of("ALL","OPEN","CLAIMED","CLOSED");
        int i = order.indexOf(f.status.toUpperCase(java.util.Locale.ROOT));
        f.status = order.get((i + 1) % order.size());
    }
    public static void cyclePriority(Player p) {
        Filter f = filters.computeIfAbsent(p.getUniqueId(), k -> new Filter());
        java.util.List<String> order = java.util.List.of("ALL","LOW","MEDIUM","HIGH","CRITICAL");
        int i = order.indexOf(f.priority.toUpperCase(java.util.Locale.ROOT));
        f.priority = order.get((i + 1) % order.size());
    }
    public static void cycleCategory(Player p) {
        Filter f = filters.computeIfAbsent(p.getUniqueId(), k -> new Filter());
        java.util.List<String> cats = new java.util.ArrayList<>();
        cats.add("ALL");
        // Load from config or default
        try {
            var cfg = me.ycxmbo.mineStaff.MineStaff.getInstance().getConfigManager().getConfig();
            java.util.List<String> cl = cfg.getStringList("reports.categories");
            if (cl == null || cl.isEmpty()) cl = java.util.Arrays.asList("GENERAL","CHEATING","CHAT","GRIEFING","BUGABUSE");
            cats.addAll(cl);
        } catch (Throwable t) { cats.addAll(java.util.Arrays.asList("GENERAL","CHEATING","CHAT","GRIEFING","BUGABUSE")); }
        int i = cats.indexOf(f.category.toUpperCase(java.util.Locale.ROOT));
        f.category = cats.get((i + 1) % cats.size());
    }
}
