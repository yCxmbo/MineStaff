package me.ycxmbo.mineStaff.tools;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ToolManager {
    public static final Material TELEPORT_TOOL = Material.COMPASS;
    public static final Material FREEZE_TOOL = Material.BLAZE_ROD;
    public static final Material INSPECT_TOOL = Material.BOOK;
    public static final Material VANISH_TOOL = Material.GHAST_TEAR;

    private final MineStaff plugin;
    public ToolManager(MineStaff plugin) { this.plugin = plugin; }

    public void giveStaffTools(Player p) {
        p.getInventory().clear();
        p.getInventory().addItem(named(TELEPORT_TOOL, ChatColor.AQUA + "Teleport Tool", "Right-click: teleport to block you look at", "Sneak+Right-click: up to max range"));
        p.getInventory().addItem(named(FREEZE_TOOL, ChatColor.RED + "Freeze Tool", "Right-click a player to toggle freeze"));
        p.getInventory().addItem(named(INSPECT_TOOL, ChatColor.GREEN + "Inspect Tool", "Right-click a player to open inspector"));
        p.getInventory().addItem(named(VANISH_TOOL, ChatColor.LIGHT_PURPLE + "Vanish Toggle", "Right-click to toggle vanish"));
    }

    private ItemStack named(Material m, String name, String... lore) {
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        it.setItemMeta(meta);
        return it;
    }
}
