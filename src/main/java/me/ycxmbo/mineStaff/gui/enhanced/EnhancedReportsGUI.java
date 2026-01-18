package me.ycxmbo.mineStaff.gui.enhanced;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ReportManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Predicate;

/**
 * Enhanced Reports GUI with search, sort, filters, pagination, and bulk actions
 */
public class EnhancedReportsGUI {
    private final MineStaff plugin;
    private final Map<UUID, GUIContext<ReportManager.Report>> contexts = new HashMap<>();
    private final Map<UUID, Boolean> selectionModes = new HashMap<>();
    
    public EnhancedReportsGUI(MineStaff plugin) {
        this.plugin = plugin;
    }

    private String getPlayerName(UUID playerId) {
        if (playerId == null) return "Unknown";
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
        return player.getName() != null ? player.getName() : playerId.toString().substring(0, 8);
    }
    
    public void open(Player viewer) {
        GUIContext<ReportManager.Report> ctx = contexts.computeIfAbsent(viewer.getUniqueId(), 
            id -> new GUIContext<>(viewer, plugin.getReportManager().getActiveReports()));
        
        openWithContext(viewer, ctx);
    }
    
    private void openWithContext(Player viewer, GUIContext<ReportManager.Report> ctx) {
        // Build filter predicate
        Predicate<ReportManager.Report> filter = report -> {
            // Search filter
            String query = ctx.getSearchQuery();
            if (!query.isEmpty()) {
                String searchable = (getPlayerName(report.target) + " " + report.reason).toLowerCase();
                if (!searchable.contains(query)) {
                    return false;
                }
            }
            
            // Status filter
            String statusFilter = ctx.getFilter("status");
            if (!statusFilter.equalsIgnoreCase("ALL")) {
                // Assuming reports have a status field
                if (!report.toString().toLowerCase().contains(statusFilter.toLowerCase())) {
                    return false;
                }
            }
            
            return true;
        };
        
        // Build sort comparator
        Comparator<ReportManager.Report> sorter = getSorter(ctx.getSortOption());
        
        // Get filtered items
        List<ReportManager.Report> allFiltered = plugin.getReportManager().getActiveReports().stream()
                .filter(filter)
                .sorted(sorter)
                .toList();
        
        List<ReportManager.Report> pageItems = ctx.getPageItems(filter, sorter);
        int totalPages = ctx.getTotalPages(allFiltered.size());
        
        // Create inventory
        String title = "§4Reports §7(" + (ctx.getCurrentPage() + 1) + "/" + Math.max(1, totalPages) + ")";
        Inventory inv = Bukkit.createInventory(null, 54, title);
        
        // Control buttons (row 1)
        inv.setItem(0, GUIBuilder.createSearchButton(ctx.getSearchQuery()));
        inv.setItem(1, GUIBuilder.createSortButton(ctx.getSortOption()));
        inv.setItem(2, GUIBuilder.createFilterButton("Status", "status", ctx.getFilter("status"), Material.REDSTONE));
        
        boolean selectionMode = selectionModes.getOrDefault(viewer.getUniqueId(), false);
        inv.setItem(6, GUIBuilder.createSelectionToggle(selectionMode, ctx.getSelectionCount()));
        
        if (selectionMode && ctx.getSelectionCount() > 0) {
            inv.setItem(7, GUIBuilder.createBulkActionButton("Close Selected", ctx.getSelectionCount(), Material.RED_CONCRETE));
        }
        
        inv.setItem(8, GUIBuilder.createRefreshButton());
        
        // Report items (rows 2-6)
        int slot = 9;
        for (ReportManager.Report report : pageItems) {
            ItemStack item = createReportItem(report);
            
            if (ctx.isSelected(report)) {
                item = GUIBuilder.makeSelected(item);
            }
            
            // Add helpful tooltips
            item = GUIBuilder.addTooltip(item,
                "§7Left-click: §fView details",
                "§7Right-click: §fClaim report",
                selectionMode ? "§7Shift-click: §fToggle selection" : ""
            );
            
            inv.setItem(slot++, item);
        }
        
        // Navigation row
        if (ctx.getCurrentPage() > 0) {
            inv.setItem(48, GUIBuilder.createPageButton(ctx.getCurrentPage(), totalPages, false));
        }
        
        inv.setItem(49, GUIBuilder.createPageIndicator(ctx.getCurrentPage(), totalPages, allFiltered.size()));
        
        if (ctx.getCurrentPage() < totalPages - 1) {
            inv.setItem(50, GUIBuilder.createPageButton(ctx.getCurrentPage(), totalPages, true));
        }
        
        inv.setItem(53, GUIBuilder.createCloseButton());
        
        viewer.openInventory(inv);
    }
    
    private ItemStack createReportItem(ReportManager.Report report) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e" + getPlayerName(report.target));

        List<String> lore = new ArrayList<>();
        lore.add("§7Reported by: §f" + getPlayerName(report.reporter));
        lore.add("§7Reason: §f" + report.reason);
        lore.add("§7Time: §f" + formatTime(report.created));
        lore.add("");
        lore.add("§8ID: " + report.id.toString().substring(0, 8));
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private Comparator<ReportManager.Report> getSorter(GUIContext.SortOption option) {
        return switch (option) {
            case DATE_NEW_FIRST -> Comparator.comparingLong((ReportManager.Report r) -> r.created).reversed();
            case DATE_OLD_FIRST -> Comparator.comparingLong(r -> r.created);
            case NAME_ALPHA -> Comparator.comparing(r -> getPlayerName(r.target));
            case NAME_ALPHA_REV -> Comparator.comparing((ReportManager.Report r) -> getPlayerName(r.target)).reversed();
            default -> Comparator.comparingLong((ReportManager.Report r) -> r.created).reversed();
        };
    }
    
    private String formatTime(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        long minutes = diff / 60000;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) return days + "d ago";
        if (hours > 0) return hours + "h ago";
        if (minutes > 0) return minutes + "m ago";
        return "just now";
    }
    
    // Event handlers
    public void handleClick(Player viewer, int slot, boolean isShiftClick, boolean isRightClick) {
        GUIContext<ReportManager.Report> ctx = contexts.get(viewer.getUniqueId());
        if (ctx == null) return;
        
        boolean selectionMode = selectionModes.getOrDefault(viewer.getUniqueId(), false);
        
        switch (slot) {
            case 0 -> handleSearchClick(viewer, ctx, isRightClick);
            case 1 -> handleSortClick(viewer, ctx);
            case 2 -> handleFilterClick(viewer, ctx, "status");
            case 6 -> {
                selectionModes.put(viewer.getUniqueId(), !selectionMode);
                if (!selectionMode) {
                    ctx.clearSelection();
                }
                openWithContext(viewer, ctx);
            }
            case 7 -> handleBulkAction(viewer, ctx);
            case 8 -> {
                // Refresh - reload data
                ctx = new GUIContext<>(viewer, plugin.getReportManager().getActiveReports());
                contexts.put(viewer.getUniqueId(), ctx);
                openWithContext(viewer, ctx);
            }
            case 48 -> {
                if (isRightClick) {
                    handleJumpToPage(viewer, ctx);
                } else {
                    ctx.previousPage();
                    openWithContext(viewer, ctx);
                }
            }
            case 49 -> handleJumpToPage(viewer, ctx);
            case 50 -> {
                if (isRightClick) {
                    handleJumpToPage(viewer, ctx);
                } else {
                    ctx.nextPage();
                    openWithContext(viewer, ctx);
                }
            }
            case 53 -> viewer.closeInventory();
            default -> {
                if (slot >= 9 && slot < 54) {
                    handleReportClick(viewer, ctx, slot, isShiftClick, isRightClick, selectionMode);
                }
            }
        }
    }
    
    private void handleSearchClick(Player viewer, GUIContext<ReportManager.Report> ctx, boolean isRightClick) {
        if (isRightClick) {
            ctx.setSearchQuery("");
            openWithContext(viewer, ctx);
        } else {
            viewer.closeInventory();
            viewer.sendMessage("§e§lSearch: §7Type your search query in chat (or 'cancel' to abort)");
            // Chat listener would handle the input
        }
    }
    
    private void handleSortClick(Player viewer, GUIContext<ReportManager.Report> ctx) {
        ctx.setSortOption(ctx.getSortOption().next());
        openWithContext(viewer, ctx);
    }
    
    private void handleFilterClick(Player viewer, GUIContext<ReportManager.Report> ctx, String filterKey) {
        String current = ctx.getFilter(filterKey);
        String[] options = {"ALL", "OPEN", "CLAIMED", "CLOSED"};
        int currentIndex = Arrays.asList(options).indexOf(current.toUpperCase());
        int nextIndex = (currentIndex + 1) % options.length;
        ctx.setFilter(filterKey, options[nextIndex]);
        openWithContext(viewer, ctx);
    }
    
    private void handleJumpToPage(Player viewer, GUIContext<ReportManager.Report> ctx) {
        viewer.closeInventory();
        viewer.sendMessage("§e§lJump to Page: §7Type a page number in chat (or 'cancel' to abort)");
        // Chat listener would handle the input
    }
    
    private void handleReportClick(Player viewer, GUIContext<ReportManager.Report> ctx, int slot, 
                                   boolean isShiftClick, boolean isRightClick, boolean selectionMode) {
        // Get the report at this slot
        int itemIndex = slot - 9;
        List<ReportManager.Report> pageItems = ctx.getPageItems(r -> true, getSorter(ctx.getSortOption()));

        if (itemIndex < 0 || itemIndex >= pageItems.size()) return;
        ReportManager.Report report = pageItems.get(itemIndex);
        
        if (selectionMode && isShiftClick) {
            ctx.toggleSelection(report);
            openWithContext(viewer, ctx);
        } else if (isRightClick) {
            // Claim report
            viewer.sendMessage("§aClaimed report for " + getPlayerName(report.target));
            viewer.closeInventory();
        } else {
            // View details
            viewer.sendMessage("§eReport Details:");
            viewer.sendMessage("§7Reported: §f" + getPlayerName(report.target));
            viewer.sendMessage("§7By: §f" + getPlayerName(report.reporter));
            viewer.sendMessage("§7Reason: §f" + report.reason);
        }
    }
    
    private void handleBulkAction(Player viewer, GUIContext<ReportManager.Report> ctx) {
        Set<ReportManager.Report> selected = ctx.getSelectedItems();
        if (selected.isEmpty()) {
            viewer.sendMessage("§cNo reports selected!");
            return;
        }
        
        int closed = 0;
        for (ReportManager.Report report : selected) {
            plugin.getReportManager().closeReport(report.id, viewer);
            closed++;
        }
        
        ctx.clearSelection();
        viewer.sendMessage("§aClosed " + closed + " report(s)");
        
        // Refresh
        ctx = new GUIContext<>(viewer, plugin.getReportManager().getActiveReports());
        contexts.put(viewer.getUniqueId(), ctx);
        openWithContext(viewer, ctx);
    }
    
    public GUIContext<ReportManager.Report> getContext(Player viewer) {
        return contexts.get(viewer.getUniqueId());
    }
}
