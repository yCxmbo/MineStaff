package me.ycxmbo.mineStaff.gui.enhanced;

import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Predicate;

/**
 * Holds state for an enhanced GUI session
 */
public class GUIContext<T> {
    private final UUID viewerId;
    private final List<T> allItems;
    
    private String searchQuery = "";
    private int currentPage = 0;
    private int itemsPerPage = 45;
    private SortOption sortOption = SortOption.DEFAULT;
    private final Map<String, String> filters = new HashMap<>();
    private final Set<T> selectedItems = new HashSet<>();
    
    public GUIContext(Player viewer, List<T> items) {
        this.viewerId = viewer.getUniqueId();
        this.allItems = new ArrayList<>(items);
    }
    
    public UUID getViewerId() { return viewerId; }
    
    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String query) {
        this.searchQuery = query == null ? "" : query.toLowerCase();
        this.currentPage = 0;
    }
    
    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int page) { this.currentPage = Math.max(0, page); }
    public void nextPage() { currentPage++; }
    public void previousPage() { currentPage = Math.max(0, currentPage - 1); }
    public void jumpToPage(int page) { setCurrentPage(page); }
    
    public int getItemsPerPage() { return itemsPerPage; }
    public void setItemsPerPage(int items) { this.itemsPerPage = Math.max(1, Math.min(45, items)); }
    
    public SortOption getSortOption() { return sortOption; }
    public void setSortOption(SortOption option) {
        this.sortOption = option;
        this.currentPage = 0;
    }
    
    public void setFilter(String key, String value) {
        if (value == null || value.isEmpty() || value.equalsIgnoreCase("ALL")) {
            filters.remove(key);
        } else {
            filters.put(key, value);
        }
        this.currentPage = 0;
    }
    
    public String getFilter(String key) {
        return filters.getOrDefault(key, "ALL");
    }
    
    public Map<String, String> getAllFilters() { return new HashMap<>(filters); }
    
    public boolean hasActiveFilters() { return !filters.isEmpty(); }
    
    public void toggleSelection(T item) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item);
        } else {
            selectedItems.add(item);
        }
    }
    
    public void selectAll(List<T> items) { selectedItems.addAll(items); }
    public void clearSelection() { selectedItems.clear(); }
    public Set<T> getSelectedItems() { return new HashSet<>(selectedItems); }
    public boolean isSelected(T item) { return selectedItems.contains(item); }
    public int getSelectionCount() { return selectedItems.size(); }
    
    public List<T> getPageItems(Predicate<T> filter, Comparator<T> sortComparator) {
        List<T> filtered = allItems.stream()
                .filter(filter)
                .toList();
        
        List<T> sorted = new ArrayList<>(filtered);
        if (sortComparator != null) {
            sorted.sort(sortComparator);
        }
        
        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, sorted.size());
        
        if (start >= sorted.size()) {
            return Collections.emptyList();
        }
        
        return sorted.subList(start, end);
    }
    
    public int getTotalPages(int totalItems) {
        return (int) Math.ceil((double) totalItems / itemsPerPage);
    }
    
    public enum SortOption {
        DEFAULT("Default"),
        DATE_NEW_FIRST("Newest First"),
        DATE_OLD_FIRST("Oldest First"),
        PRIORITY_HIGH_FIRST("High Priority First"),
        PRIORITY_LOW_FIRST("Low Priority First"),
        STATUS_ALPHA("Status A-Z"),
        NAME_ALPHA("Name A-Z"),
        NAME_ALPHA_REV("Name Z-A");
        
        private final String display;
        
        SortOption(String display) { this.display = display; }
        
        public String getDisplay() { return display; }
        
        public SortOption next() {
            SortOption[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }
}
