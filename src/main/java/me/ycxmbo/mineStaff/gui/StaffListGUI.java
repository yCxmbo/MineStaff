package me.ycxmbo.mineStaff.gui;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class StaffListGUI {

    private final MineStaff plugin;
    public static final String TITLE = ChatColor.DARK_AQUA + "Staff List";

    public StaffListGUI(MineStaff plugin) {
        this.plugin = plugin;
    }

    public void open(Player viewer) {
        // 6 rows leaves room for many staff
        Inventory inv = Bukkit.createInventory(viewer, 54, TITLE);

        // Build a snapshot of online players with staff perms
        List<Player> staffOnline = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("staffmode.toggle")) staffOnline.add(p);
        }

        int slot = 10;
        for (Player sp : staffOnline) {
            inv.setItem(slot, headFor(sp));
            slot++;
            // wrap each row leaving borders
            if (slot % 9 == 8) slot += 3;
            if (slot >= 54) break;
        }

        // Simple border decoration (optional)
        for (int i = 0; i < 54; i++) {
            if (inv.getItem(i) != null) continue;
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, border());
            }
        }

        viewer.openInventory(inv);
    }

    private ItemStack headFor(Player p) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(p);
        meta.setDisplayName(ChatColor.AQUA + p.getName());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Ping: " + p.getPing() + "ms");
        lore.add(ChatColor.GRAY + "World: " + p.getWorld().getName());
        lore.add(ChatColor.DARK_GRAY + "UUID:" + p.getUniqueId()); // hidden key for listener if needed
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        skull.setItemMeta(meta);
        return skull;
    }

    private ItemStack border() {
        ItemStack it = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        var m = it.getItemMeta();
        m.setDisplayName(" ");
        it.setItemMeta(m);
        return it;
    }
}
