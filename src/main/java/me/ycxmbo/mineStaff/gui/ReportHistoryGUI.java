package me.ycxmbo.mineStaff.gui;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ReportManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * GUI for viewing player report history
 */
public class ReportHistoryGUI {
    private final MineStaff plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
    private final Map<UUID, Integer> viewerPages = new HashMap<>();
    private final Map<UUID, String> viewerModes = new HashMap<>(); // "filed", "against", or "all"

    public ReportHistoryGUI(MineStaff plugin) {
        this.plugin = plugin;
    }

    public void open(Player viewer, OfflinePlayer target) {
        open(viewer, target, 0, "all");
    }

    public void open(Player viewer, OfflinePlayer target, int page, String mode) {
        viewerPages.put(viewer.getUniqueId(), page);
        viewerModes.put(viewer.getUniqueId(), mode);

        List<ReportManager.Report> reports = getReports(target, mode);
        reports.sort(Comparator.comparingLong(r -> -r.created)); // Newest first

        int totalPages = (int) Math.ceil(reports.size() / 45.0);
        page = Math.max(0, Math.min(page, totalPages - 1));

        Inventory inv = Bukkit.createInventory(null, 54, "§6Reports: §e" + target.getName() + " §7(" + (page + 1) + "/" + Math.max(1, totalPages) + ")");

        // Info header
        ItemStack info = createInfoItem(target, reports, mode);
        inv.setItem(4, info);

        // Mode selector
        inv.setItem(0, createModeButton("§aFiled by Player", "filed", mode, Material.GREEN_CONCRETE));
        inv.setItem(1, createModeButton("§cReports Against", "against", mode, Material.RED_CONCRETE));
        inv.setItem(2, createModeButton("§eAll Reports", "all", mode, Material.YELLOW_CONCRETE));

        // Display reports
        int startIndex = page * 45;
        int endIndex = Math.min(startIndex + 45, reports.size());

        int slot = 9;
        for (int i = startIndex; i < endIndex; i++) {
            ReportManager.Report report = reports.get(i);
            inv.setItem(slot++, createReportItem(report));
        }

        // Navigation
        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prev.getItemMeta();
            prevMeta.setDisplayName("§a← Previous Page");
            prev.setItemMeta(prevMeta);
            inv.setItem(48, prev);
        }

        if (page < totalPages - 1) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.setDisplayName("§aNext Page →");
            next.setItemMeta(nextMeta);
            inv.setItem(50, next);
        }

        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName("§cClose");
        close.setItemMeta(closeMeta);
        inv.setItem(49, close);

        viewer.openInventory(inv);
    }

    private List<ReportManager.Report> getReports(OfflinePlayer target, String mode) {
        List<ReportManager.Report> all = plugin.getReportManager().getAll();

        return switch (mode) {
            case "filed" -> all.stream()
                    .filter(r -> r.reporter.equals(target.getUniqueId()))
                    .collect(Collectors.toList());
            case "against" -> all.stream()
                    .filter(r -> r.target.equals(target.getUniqueId()))
                    .collect(Collectors.toList());
            default -> all.stream()
                    .filter(r -> r.reporter.equals(target.getUniqueId()) || r.target.equals(target.getUniqueId()))
                    .collect(Collectors.toList());
        };
    }

    private ItemStack createInfoItem(OfflinePlayer target, List<ReportManager.Report> reports, String mode) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e" + target.getName() + "'s Report History");

        List<String> lore = new ArrayList<>();
        lore.add("§7Total Reports: §f" + reports.size());

        long open = reports.stream().filter(r -> "OPEN".equals(r.status)).count();
        long claimed = reports.stream().filter(r -> "CLAIMED".equals(r.status)).count();
        long closed = reports.stream().filter(r -> "CLOSED".equals(r.status)).count();

        lore.add("§7Open: §c" + open);
        lore.add("§7Claimed: §e" + claimed);
        lore.add("§7Closed: §a" + closed);
        lore.add("");
        lore.add("§7Mode: §f" + mode.substring(0, 1).toUpperCase() + mode.substring(1));
        lore.add("");
        lore.add("§8Click mode buttons to filter");

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createModeButton(String name, String modeValue, String currentMode, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        List<String> lore = new ArrayList<>();
        if (modeValue.equals(currentMode)) {
            lore.add("§a§l✓ Currently Selected");
        } else {
            lore.add("§7Click to view");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createReportItem(ReportManager.Report report) {
        Material material = switch (report.status.toUpperCase()) {
            case "OPEN" -> Material.RED_CONCRETE;
            case "CLAIMED" -> Material.YELLOW_CONCRETE;
            case "CLOSED" -> Material.GREEN_CONCRETE;
            case "NEEDS_INFO" -> Material.ORANGE_CONCRETE;
            default -> Material.GRAY_CONCRETE;
        };

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String reporterName = Bukkit.getOfflinePlayer(report.reporter).getName();
        String targetName = Bukkit.getOfflinePlayer(report.target).getName();

        meta.setDisplayName("§f" + reporterName + " §7→ §c" + targetName);

        List<String> lore = new ArrayList<>();
        lore.add("§7Reason: §f" + report.reason);
        lore.add("§7Status: " + getStatusColor(report.status) + report.status);
        lore.add("§7Category: §e" + report.category);
        lore.add("§7Priority: " + getPriorityColor(report.priority) + report.priority);
        lore.add("§7Date: §f" + dateFormat.format(new Date(report.created)));

        if (report.claimedBy != null) {
            String claimedByName = Bukkit.getOfflinePlayer(report.claimedBy).getName();
            lore.add("§7Claimed by: §e" + claimedByName);
        }

        lore.add("");
        lore.add("§7ID: §8" + report.id.toString().substring(0, 8) + "...");

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private String getStatusColor(String status) {
        return switch (status.toUpperCase()) {
            case "OPEN" -> "§c";
            case "CLAIMED" -> "§e";
            case "CLOSED" -> "§a";
            case "NEEDS_INFO" -> "§6";
            default -> "§7";
        };
    }

    private String getPriorityColor(String priority) {
        return switch (priority.toUpperCase()) {
            case "LOW" -> "§a";
            case "MEDIUM" -> "§e";
            case "HIGH" -> "§6";
            case "CRITICAL" -> "§c";
            default -> "§7";
        };
    }

    public Integer getCurrentPage(Player viewer) {
        return viewerPages.getOrDefault(viewer.getUniqueId(), 0);
    }

    public String getCurrentMode(Player viewer) {
        return viewerModes.getOrDefault(viewer.getUniqueId(), "all");
    }

    public void cleanup(Player viewer) {
        viewerPages.remove(viewer.getUniqueId());
        viewerModes.remove(viewer.getUniqueId());
    }
}
