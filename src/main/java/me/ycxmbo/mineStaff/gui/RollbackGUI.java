package me.ycxmbo.mineStaff.gui;

import me.ycxmbo.mineStaff.managers.RollbackManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class RollbackGUI {
    private final RollbackManager rb;
    private final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public RollbackGUI(RollbackManager rb) { this.rb = rb; }

    public void open(Player viewer, UUID target) {
        Map<Long, RollbackManager.Snapshot> snaps = rb.getSnapshots(target);
        Inventory inv = Bukkit.createInventory(viewer, 54, ChatColor.AQUA + "Rollbacks");
        for (Map.Entry<Long, RollbackManager.Snapshot> e : snaps.entrySet()) {
            ItemStack it = new ItemStack(Material.CLOCK);
            ItemMeta meta = it.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + fmt.format(new Date(e.getKey())));
            meta.setLore(java.util.List.of(
                    ChatColor.GRAY + "Click to restore",
                    ChatColor.DARK_GRAY + "TS:" + e.getKey(),
                    ChatColor.DARK_GRAY + "TARGET:" + target
            ));
            it.setItemMeta(meta);
            inv.addItem(it);
        }
        viewer.openInventory(inv);
    }
}
