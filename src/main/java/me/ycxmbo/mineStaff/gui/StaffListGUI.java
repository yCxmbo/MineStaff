package me.ycxmbo.mineStaff.gui;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

public class StaffListGUI {
    private final MineStaff plugin;
    private final StaffDataManager data;

    public StaffListGUI(MineStaff plugin) { this.plugin = plugin; this.data = plugin.getStaffDataManager(); }

    public void open(Player viewer) {
        Inventory inv = Bukkit.createInventory(viewer, 54, ChatColor.DARK_AQUA + "Staff Online");
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!data.isStaffMode(p)) continue;
            ItemStack it = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = it.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + p.getName());
            it.setItemMeta(meta);
            inv.addItem(it);
        }
        viewer.openInventory(inv);
    }
}
