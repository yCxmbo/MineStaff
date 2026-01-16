package me.ycxmbo.mineStaff.warnings;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * GUI for viewing player warnings
 */
public class WarningsGUI {
    private final MineStaff plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");

    public WarningsGUI(MineStaff plugin) {
        this.plugin = plugin;
    }

    /**
     * Open warnings GUI for a target player
     */
    public void open(Player viewer, Player target) {
        List<Warning> warnings = plugin.getWarningManager().getWarnings(target.getUniqueId());
        List<Warning> activeWarnings = plugin.getWarningManager().getActiveWarnings(target.getUniqueId());

        Inventory inv = Bukkit.createInventory(null, 54, "§6Warnings: §e" + target.getName());

        // Info item
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName("§e" + target.getName() + "'s Warnings");
        List<String> infoLore = new ArrayList<>();
        infoLore.add("§7Total Warnings: §f" + warnings.size());
        infoLore.add("§7Active Warnings: §a" + activeWarnings.size());
        infoLore.add("");
        infoLore.add("§8Click warnings to see details");
        infoMeta.setLore(infoLore);
        info.setItemMeta(infoMeta);
        inv.setItem(4, info);

        // Display warnings
        int slot = 9;
        for (Warning warning : warnings) {
            if (slot >= 54) break;

            ItemStack item;
            if (warning.isActive()) {
                item = new ItemStack(Material.RED_CONCRETE);
            } else {
                item = new ItemStack(Material.GRAY_CONCRETE);
            }

            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(warning.isActive() ? "§c§lWarning #" + warning.getId() : "§7Warning #" + warning.getId());

            List<String> lore = new ArrayList<>();
            lore.add("§7Reason: §f" + warning.getReason());
            lore.add("§7Severity: " + getSeverityColor(warning.getSeverity()) + warning.getSeverity());
            lore.add("§7Issued by: §e" + warning.getIssuerName());
            lore.add("§7Date: §f" + dateFormat.format(new Date(warning.getTimestamp())));

            if (warning.getExpiresAt() > 0) {
                if (warning.hasExpired()) {
                    lore.add("§7Status: §cExpired");
                } else {
                    long remaining = warning.getTimeRemaining();
                    lore.add("§7Expires in: §e" + formatDuration(remaining));
                }
            } else {
                lore.add("§7Duration: §fPermanent");
            }

            lore.add("");
            if (warning.isActive()) {
                lore.add("§a✓ Active");
            } else {
                lore.add("§7✗ Inactive");
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }

        viewer.openInventory(inv);
    }

    private String getSeverityColor(String severity) {
        return switch (severity.toUpperCase()) {
            case "LOW" -> "§a";
            case "MEDIUM" -> "§e";
            case "HIGH" -> "§6";
            case "SEVERE" -> "§c";
            default -> "§7";
        };
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "d " + (hours % 24) + "h";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m";
        } else {
            return seconds + "s";
        }
    }
}
