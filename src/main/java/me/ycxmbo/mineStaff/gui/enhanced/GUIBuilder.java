package me.ycxmbo.mineStaff.gui.enhanced;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility for building enhanced GUI controls
 */
public class GUIBuilder {
    
    /**
     * Creates a search button
     */
    public static ItemStack createSearchButton(String currentQuery) {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e§lSearch");
        
        List<String> lore = new ArrayList<>();
        if (currentQuery == null || currentQuery.isEmpty()) {
            lore.add("§7Click to search");
            lore.add("§7Type in chat to search");
        } else {
            lore.add("§7Current: §f" + currentQuery);
            lore.add("§7Right-click to clear");
            lore.add("§7Left-click to change");
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Creates a sort button
     */
    public static ItemStack createSortButton(GUIContext.SortOption currentSort) {
        ItemStack item = new ItemStack(Material.HOPPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6§lSort By");
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Current: §e" + currentSort.getDisplay());
        lore.add("");
        lore.add("§7Click to cycle sort options:");
        for (GUIContext.SortOption option : GUIContext.SortOption.values()) {
            String prefix = option == currentSort ? "§a▶ " : "§8  ";
            lore.add(prefix + option.getDisplay());
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Creates a filter button
     */
    public static ItemStack createFilterButton(String name, String filterKey, String currentValue, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§b" + name);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Current: §f" + currentValue);
        lore.add("§7Click to cycle options");
        
        if (!currentValue.equalsIgnoreCase("ALL")) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            lore.add("");
            lore.add("§a✓ Active Filter");
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Creates a pagination control with page numbers
     */
    public static ItemStack createPageButton(int currentPage, int totalPages, boolean isNext) {
        ItemStack item = new ItemStack(isNext ? Material.ARROW : Material.SPECTRAL_ARROW);
        ItemMeta meta = item.getItemMeta();
        
        if (isNext) {
            meta.setDisplayName("§aNext Page →");
        } else {
            meta.setDisplayName("§a← Previous Page");
        }
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Page " + (currentPage + 1) + "/" + totalPages);
        lore.add("");
        lore.add("§7Left-click: " + (isNext ? "Next" : "Previous"));
        lore.add("§7Right-click: Jump to page");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Creates a page indicator showing current page
     */
    public static ItemStack createPageIndicator(int currentPage, int totalPages, int totalItems) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Page " + (currentPage + 1) + " of " + totalPages);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Total Items: §f" + totalItems);
        lore.add("");
        lore.add("§7Click to jump to page");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Creates a bulk action button
     */
    public static ItemStack createBulkActionButton(String actionName, int selectedCount, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§c§l" + actionName);
        
        List<String> lore = new ArrayList<>();
        if (selectedCount > 0) {
            lore.add("§7Selected: §e" + selectedCount + " item(s)");
            lore.add("");
            lore.add("§aClick to " + actionName.toLowerCase());
        } else {
            lore.add("§7No items selected");
            lore.add("§7Shift-click items to select");
        }
        
        meta.setLore(lore);
        if (selectedCount == 0) {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Creates a selection mode toggle
     */
    public static ItemStack createSelectionToggle(boolean selectionMode, int selectedCount) {
        ItemStack item = new ItemStack(selectionMode ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(selectionMode ? "§a§lSelection Mode: ON" : "§7Selection Mode: OFF");
        
        List<String> lore = new ArrayList<>();
        if (selectionMode) {
            lore.add("§7Selected: §e" + selectedCount + " item(s)");
            lore.add("");
            lore.add("§7Shift-click items to select/deselect");
            lore.add("§7Click to disable selection mode");
        } else {
            lore.add("§7Click to enable selection mode");
            lore.add("§7Allows bulk actions on items");
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Creates a refresh button
     */
    public static ItemStack createRefreshButton() {
        ItemStack item = new ItemStack(Material.SUNFLOWER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e§lRefresh");
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Click to reload data");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Creates a close button
     */
    public static ItemStack createCloseButton() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§c§lClose");
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Click to close this menu");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Adds tooltip to an item
     */
    public static ItemStack addTooltip(ItemStack item, String... tooltipLines) {
        if (item == null || tooltipLines.length == 0) return item;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add("");
        lore.add("§7§o" + "─".repeat(20));
        lore.addAll(Arrays.asList(tooltipLines));
        lore.add("§7§o" + "─".repeat(20));
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Makes an item show as selected
     */
    public static ItemStack makeSelected(ItemStack item) {
        if (item == null) return item;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add(0, "§a§l✓ SELECTED");
        lore.add(1, "");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
