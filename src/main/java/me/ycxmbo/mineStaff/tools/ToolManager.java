package me.ycxmbo.mineStaff.tools;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
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

    private final MineStaff plugin;
    public ToolManager(MineStaff plugin) { this.plugin = plugin; }

    public void giveStaffTools(Player p) {
        int tpSlot = plugin.getConfigManager().getToolSlot("teleport", 0);
        int frSlot = plugin.getConfigManager().getToolSlot("freeze", 1);
        int inSlot = plugin.getConfigManager().getToolSlot("inspect", 2);
        int vaSlot = plugin.getConfigManager().getToolSlot("vanish", 8);

        p.getInventory().setItem(tpSlot, named(TELEPORT_TOOL, ChatColor.AQUA + "Teleport Tool",
                "Right-click: teleport to block you look at", "Sneak+Right-click: up to max range"));

        p.getInventory().setItem(frSlot, named(FREEZE_TOOL, ChatColor.RED + "Freeze Tool",
                "Right-click a player to toggle freeze"));

        p.getInventory().setItem(inSlot, named(INSPECT_TOOL, ChatColor.GREEN + "Inspect Tool",
                "Right-click a player to open inspector"));

        // vanish tool (dye) – initial appearance reflects current vanish state
        boolean vanished = plugin.getStaffDataManager().isVanished(p);
        p.getInventory().setItem(vaSlot, makeVanishItem(vanished));
    }

    public void updateVanishTool(Player p, boolean vanished) {
        int vaSlot = plugin.getConfigManager().getToolSlot("vanish", 8);
        p.getInventory().setItem(vaSlot, makeVanishItem(vanished));
        p.updateInventory();
    }

    private ItemStack makeVanishItem(boolean vanished) {
        Material m = vanished ? Material.LIME_DYE : Material.LIGHT_GRAY_DYE;
        String name = vanished ? ChatColor.LIGHT_PURPLE + "Vanish: ON" : ChatColor.GRAY + "Vanish: OFF";
        String lore = vanished ? "Right-click to disable vanish" : "Right-click to enable vanish";
        return named(m, name, lore);
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
