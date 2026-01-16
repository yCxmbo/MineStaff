package me.ycxmbo.mineStaff.gui;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.tickets.StaffTicket;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * GUI for viewing and managing staff tickets
 */
public class StaffTicketsGUI {
    private final MineStaff plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm");
    private final Map<UUID, Integer> viewerPages = new HashMap<>();
    private final Map<UUID, String> viewerFilters = new HashMap<>();

    public StaffTicketsGUI(MineStaff plugin) {
        this.plugin = plugin;
    }

    public void open(Player viewer, int page, String filter) {
        viewerPages.put(viewer.getUniqueId(), page);
        viewerFilters.put(viewer.getUniqueId(), filter);

        List<StaffTicket> tickets = getFilteredTickets(viewer, filter);

        int totalPages = (int) Math.ceil(tickets.size() / 45.0);
        page = Math.max(0, Math.min(page, Math.max(0, totalPages - 1)));

        Inventory inv = Bukkit.createInventory(null, 54, "§6Staff Tickets §7(" + filter.toUpperCase() + " - " + (page + 1) + "/" + Math.max(1, totalPages) + ")");

        // Filter buttons
        inv.setItem(0, createFilterButton("§aOpen", "open", filter, Material.LIME_CONCRETE));
        inv.setItem(1, createFilterButton("§eClaimed", "claimed", filter, Material.YELLOW_CONCRETE));
        inv.setItem(2, createFilterButton("§9My Tickets", "mine", filter, Material.BLUE_CONCRETE));
        inv.setItem(3, createFilterButton("§aResolved", "resolved", filter, Material.GREEN_CONCRETE));
        inv.setItem(4, createFilterButton("§7All", "all", filter, Material.GRAY_CONCRETE));

        // Stats info
        Map<String, Integer> stats = plugin.getStaffTicketManager().getTicketStats();
        ItemStack statsItem = new ItemStack(Material.PAPER);
        ItemMeta statsMeta = statsItem.getItemMeta();
        statsMeta.setDisplayName("§6Ticket Statistics");
        List<String> statsLore = new ArrayList<>();
        statsLore.add("§7Total: §f" + stats.get("total"));
        statsLore.add("§7Open: §c" + stats.get("open"));
        statsLore.add("§7Claimed: §e" + stats.get("claimed"));
        statsLore.add("§7Resolved: §a" + stats.get("resolved"));
        statsLore.add("§7Closed: §8" + stats.get("closed"));
        statsMeta.setLore(statsLore);
        statsItem.setItemMeta(statsMeta);
        inv.setItem(8, statsItem);

        // Display tickets
        int startIndex = page * 45;
        int endIndex = Math.min(startIndex + 45, tickets.size());

        int slot = 9;
        for (int i = startIndex; i < endIndex; i++) {
            StaffTicket ticket = tickets.get(i);
            inv.setItem(slot++, createTicketItem(ticket));
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

    private List<StaffTicket> getFilteredTickets(Player viewer, String filter) {
        return switch (filter) {
            case "mine" -> plugin.getStaffTicketManager().getTicketsByCreator(viewer.getUniqueId());
            case "claimed" -> plugin.getStaffTicketManager().getTicketsByClaimed(viewer.getUniqueId());
            case "resolved" -> plugin.getStaffTicketManager().getTicketsByStatus("RESOLVED");
            case "open" -> plugin.getStaffTicketManager().getOpenTickets();
            default -> plugin.getStaffTicketManager().getAllTickets();
        };
    }

    private ItemStack createFilterButton(String name, String filterValue, String currentFilter, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        List<String> lore = new ArrayList<>();
        if (filterValue.equals(currentFilter)) {
            lore.add("§a§l✓ Currently Selected");
        } else {
            lore.add("§7Click to view");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createTicketItem(StaffTicket ticket) {
        Material material = switch (ticket.status.toUpperCase()) {
            case "OPEN" -> Material.RED_CONCRETE;
            case "CLAIMED" -> Material.YELLOW_CONCRETE;
            case "RESOLVED" -> Material.GREEN_CONCRETE;
            case "CLOSED" -> Material.GRAY_CONCRETE;
            default -> Material.WHITE_CONCRETE;
        };

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§f" + ticket.subject);

        List<String> lore = new ArrayList<>();
        lore.add("§7ID: §8" + ticket.id.toString().substring(0, 8) + "...");
        lore.add("§7Created by: §f" + ticket.createdByName);
        lore.add("§7Date: §f" + dateFormat.format(new Date(ticket.created)));
        lore.add("§7Status: " + getStatusColor(ticket.status) + ticket.status);
        lore.add("§7Category: §e" + ticket.category);
        lore.add("§7Priority: " + getPriorityColor(ticket.priority) + ticket.priority);

        if (ticket.claimedBy != null) {
            lore.add("§7Claimed by: §f" + ticket.claimedByName);
        }

        if (ticket.comments.size() > 0) {
            lore.add("§7Comments: §f" + ticket.comments.size());
        }

        lore.add("");
        lore.add("§7Click to view details");

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private String getStatusColor(String status) {
        return switch (status.toUpperCase()) {
            case "OPEN" -> "§c";
            case "CLAIMED" -> "§e";
            case "RESOLVED" -> "§a";
            case "CLOSED" -> "§8";
            default -> "§7";
        };
    }

    private String getPriorityColor(String priority) {
        return switch (priority.toUpperCase()) {
            case "LOW" -> "§a";
            case "MEDIUM" -> "§e";
            case "HIGH" -> "§6";
            case "URGENT" -> "§c";
            default -> "§7";
        };
    }

    public Integer getCurrentPage(Player viewer) {
        return viewerPages.getOrDefault(viewer.getUniqueId(), 0);
    }

    public String getCurrentFilter(Player viewer) {
        return viewerFilters.getOrDefault(viewer.getUniqueId(), "open");
    }

    public void cleanup(Player viewer) {
        viewerPages.remove(viewer.getUniqueId());
        viewerFilters.remove(viewer.getUniqueId());
    }
}
