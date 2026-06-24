package me.ycxmbo.mineStaff.tools;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ConfigManager;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** Responsible for giving/refreshing staff tools and their materials. */
public class ToolManager {

    // Fallback materials used when config value is absent or invalid
    public static final Material TELEPORT_TOOL = Material.COMPASS;
    public static final Material FREEZE_TOOL   = Material.BLAZE_ROD;
    public static final Material INSPECT_TOOL  = Material.BOOK;
    public static final Material RANDOMTP_TOOL = Material.FEATHER;
    public static final Material CPS_TOOL      = Material.CLOCK;

    private final MineStaff plugin;

    public ToolManager(MineStaff plugin) {
        this.plugin = plugin;
    }

    public void giveStaffTools(Player p) {
        ConfigManager cfg = plugin.getConfigManager();
        StaffDataManager data = plugin.getStaffDataManager();
        FileConfiguration c = cfg.getConfig();

        int tpSlot  = c.getInt("tools.slots.teleport", 0);
        int frSlot  = c.getInt("tools.slots.freeze", 1);
        int inSlot  = c.getInt("tools.slots.inspect", 2);
        int cpSlot  = c.getInt("tools.slots.cps", 3);
        int vaSlot  = c.getInt("tools.slots.vanish", 8);
        int rtSlot  = c.getInt("tools.slots.randomtp", 4);

        p.getInventory().setItem(tpSlot, named(material(c, "teleport", TELEPORT_TOOL), toolName(c, "teleport", "&bTeleport")));
        p.getInventory().setItem(frSlot, named(material(c, "freeze",   FREEZE_TOOL),   toolName(c, "freeze",   "&cFreeze")));
        p.getInventory().setItem(inSlot, named(material(c, "inspect",  INSPECT_TOOL),  toolName(c, "inspect",  "&6Inspect")));

        if (p.hasPermission("staffmode.cpscheck")) {
            p.getInventory().setItem(cpSlot, named(material(c, "cps", CPS_TOOL), toolName(c, "cps", "&eCPS Check")));
        }
        if (p.hasPermission("staffmode.randomtp")) {
            p.getInventory().setItem(rtSlot, named(material(c, "randomtp", RANDOMTP_TOOL), toolName(c, "randomtp", "&bRandom TP")));
        }

        boolean vanished = data.isVanished(p);
        Material dye = vanished ? Material.LIME_DYE : Material.LIGHT_GRAY_DYE;
        String vanishName = vanished
                ? color(c.getString("tools.names.vanish_on",  "&dVanish &aON"))
                : color(c.getString("tools.names.vanish_off", "&dVanish &7OFF"));
        p.getInventory().setItem(vaSlot, named(dye, vanishName));

        p.updateInventory();
    }

    public void updateVanishDye(Player p, boolean vanished) {
        FileConfiguration c = plugin.getConfigManager().getConfig();
        int vaSlot = c.getInt("tools.slots.vanish", 8);
        Material dye = vanished ? Material.LIME_DYE : Material.LIGHT_GRAY_DYE;
        String vanishName = vanished
                ? color(c.getString("tools.names.vanish_on",  "&dVanish &aON"))
                : color(c.getString("tools.names.vanish_off", "&dVanish &7OFF"));
        p.getInventory().setItem(vaSlot, named(dye, vanishName));
        p.updateInventory();
    }

    private Material material(FileConfiguration c, String key, Material fallback) {
        String raw = c.getString("tools.materials." + key, null);
        if (raw == null) return fallback;
        try {
            Material m = Material.valueOf(raw.toUpperCase());
            return m == Material.AIR ? fallback : m;
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("tools.materials." + key + " has unknown material '" + raw + "', using default.");
            return fallback;
        }
    }

    private String toolName(FileConfiguration c, String key, String fallback) {
        return color(c.getString("tools.names." + key, fallback));
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
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
