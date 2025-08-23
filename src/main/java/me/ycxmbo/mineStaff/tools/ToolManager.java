package me.ycxmbo.mineStaff.tools;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ConfigManager;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** Responsible for giving/refreshing staff tools and their materials. */
public class ToolManager {

    public static final Material TELEPORT_TOOL = Material.COMPASS;
    public static final Material FREEZE_TOOL   = Material.BLAZE_ROD;
    public static final Material INSPECT_TOOL  = Material.BOOK;

    private final MineStaff plugin;

    public ToolManager(MineStaff plugin) {
        this.plugin = plugin;
    }

    public void giveStaffTools(Player p) {
        ConfigManager cfg = plugin.getConfigManager();
        StaffDataManager data = plugin.getStaffDataManager();

        int tpSlot  = cfg.getConfig().getInt("tools.slots.teleport", 0);
        int frSlot  = cfg.getConfig().getInt("tools.slots.freeze", 1);
        int inSlot  = cfg.getConfig().getInt("tools.slots.inspect", 2);
        int vaSlot  = cfg.getConfig().getInt("tools.slots.vanish", 8);

        p.getInventory().setItem(tpSlot,  named(TELEPORT_TOOL, ChatColor.AQUA + "Teleport"));
        p.getInventory().setItem(frSlot,  named(FREEZE_TOOL,   ChatColor.RED + "Freeze"));
        p.getInventory().setItem(inSlot,  named(INSPECT_TOOL,  ChatColor.GOLD + "Inspect"));

        boolean vanished = data.isVanished(p);
        Material dye = vanished ? Material.LIME_DYE : Material.LIGHT_GRAY_DYE;
        p.getInventory().setItem(vaSlot,  named(dye, ChatColor.LIGHT_PURPLE + "Vanish " + (vanished ? ChatColor.GREEN + "ON" : ChatColor.GRAY + "OFF")));

        p.updateInventory();
    }

    public void updateVanishDye(Player p, boolean vanished) {
        int vaSlot  = plugin.getConfigManager().getConfig().getInt("tools.slots.vanish", 8);
        Material dye = vanished ? Material.LIME_DYE : Material.LIGHT_GRAY_DYE;
        p.getInventory().setItem(vaSlot, named(dye, ChatColor.LIGHT_PURPLE + "Vanish " + (vanished ? ChatColor.GREEN + "ON" : ChatColor.GRAY + "OFF")));
        p.updateInventory();
    }

    private ItemStack named(Material mat, String name) {
        ItemStack it = new ItemStack(mat);
        ItemMeta im = it.getItemMeta();
        if (im != null) {
            im.setDisplayName(name);
            im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
            it.setItemMeta(im);
        }
        return it;
    }
}
