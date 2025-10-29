package me.ycxmbo.mineStaff.gui;

import me.ycxmbo.mineStaff.managers.InfractionManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public class InfractionsGUI {
    private final InfractionManager infractions;

    public InfractionsGUI(InfractionManager infractions) { this.infractions = infractions; }

    public void open(Player viewer, UUID target) {
        if (!infractions.isSqlBacked()) infractions.reload();
        List<InfractionManager.Infraction> list = infractions.get(target);
        Inventory inv = Bukkit.createInventory(viewer, 54, ChatColor.GOLD + "Infractions");
        for (InfractionManager.Infraction i : list) {
            ItemStack it = new ItemStack(Material.BOOK);
            ItemMeta meta = it.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + i.type);
            meta.setLore(java.util.List.of(ChatColor.GRAY + i.reason));
            it.setItemMeta(meta);
            inv.addItem(it);
        }
        viewer.openInventory(inv);
    }
}
