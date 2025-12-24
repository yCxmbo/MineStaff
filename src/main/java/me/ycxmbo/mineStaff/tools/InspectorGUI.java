package me.ycxmbo.mineStaff.tools;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class InspectorGUI {
    private final MineStaff plugin;
    private static final Map<UUID, UUID> viewerToTarget = new HashMap<>();

    public InspectorGUI(MineStaff plugin) { this.plugin = plugin; }

    public void open(Player viewer, Player target) {
        viewerToTarget.put(viewer.getUniqueId(), target.getUniqueId());

        String title = ChatColor.DARK_AQUA + "Inspector: " + ChatColor.AQUA + target.getName();
        Inventory inv = Bukkit.createInventory(viewer, 27, title);

        inv.setItem(10, named(new ItemStack(Material.PAPER), ChatColor.AQUA + "Info",
                ChatColor.GRAY + "Name: " + target.getName(),
                ChatColor.GRAY + "Ping: " + target.getPing() + "ms",
                ChatColor.GRAY + "Health: " + (int) target.getHealth() + "/" + (int) target.getMaxHealth(),
                ChatColor.GRAY + "Effects: " + target.getActivePotionEffects().size()));

        inv.setItem(12, named(new ItemStack(Material.CHEST), ChatColor.GOLD + "Inventory",
                ChatColor.GRAY + "Open the player's inventory"));

        inv.setItem(14, named(new ItemStack(Material.ENDER_CHEST), ChatColor.LIGHT_PURPLE + "Ender Chest",
                ChatColor.GRAY + "Open the player's ender chest"));

        inv.setItem(16, named(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Close",
                ChatColor.GRAY + "Close this menu"));

        viewer.openInventory(inv);
    }

    private ItemStack named(ItemStack base, String name, String... lore) {
        ItemStack it = base.clone();
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
        it.setItemMeta(meta);
        return it;
    }

    public static UUID extractTargetFromItem(ItemStack item) {
        if (item == null || item.getItemMeta() == null || item.getItemMeta().getLore() == null) return null;
        for (String line : item.getItemMeta().getLore()) {
            if (line == null) continue;
            String plain = ChatColor.stripColor(line).trim();
            if (plain.toUpperCase(Locale.ROOT).startsWith("TARGET:")) {
                String raw = plain.substring("TARGET:".length()).trim();
                try { return UUID.fromString(raw); } catch (Exception ignored) {}
            }
        }
        return null;
    }

    public static UUID extractTargetFromTitle(String title) {
        if (title == null) return null;
        String plain = ChatColor.stripColor(title);
        int open = plain.lastIndexOf('[');
        int close = plain.lastIndexOf(']');
        if (open == -1 || close == -1 || close <= open) return null;
        String raw = plain.substring(open + 1, close).trim();
        try { return UUID.fromString(raw); } catch (Exception ex) { return null; }
    }

    public static UUID getTargetForViewer(UUID viewerId) {
        return viewerToTarget.get(viewerId);
    }

    public static void clearTargetForViewer(UUID viewerId) {
        viewerToTarget.remove(viewerId);
    }
}
