package me.ycxmbo.mineStaff.tools;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class InspectorGUI {
    private final MineStaff plugin;
    private final String title = ChatColor.DARK_GREEN + "Inspector";

    public InspectorGUI(MineStaff plugin) {
        this.plugin = plugin;
    }

    public String getTitle() { return title; }

    public void open(Player staff, Player target) {
        Inventory inv = Bukkit.createInventory(staff, 27, title + " - " + target.getName());
        inv.setItem(10, named(Material.PLAYER_HEAD, ChatColor.GOLD + "Profile"));
        inv.setItem(12, named(Material.CHEST, ChatColor.GREEN + "Inventory"));
        inv.setItem(14, named(Material.ENDER_CHEST, ChatColor.DARK_PURPLE + "Ender Chest"));
        inv.setItem(16, named(Material.CLOCK, ChatColor.AQUA + "Stats"));
        staff.openInventory(inv);
    }

    public void openOffline(Player staff, OfflinePlayer target) {
        Inventory inv = Bukkit.createInventory(staff, 27, title + " - " + target.getName());
        inv.setItem(10, named(Material.PLAYER_HEAD, ChatColor.GOLD + "Profile (offline)"));
        inv.setItem(16, named(Material.CLOCK, ChatColor.AQUA + "Last Seen"));
        staff.openInventory(inv);
    }

    private ItemStack named(Material m, String name) {
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Click to view"));
        it.setItemMeta(meta);
        return it;
    }
}
