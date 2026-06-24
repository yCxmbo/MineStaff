package me.ycxmbo.mineStaff.gui;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Surfaces per-tool cooldown / rate-limit settings in an editable GUI. Each
 * tool's millisecond cooldown is read from and written back to config.yml.
 * Left/right click adjusts by 250ms; shift-click adjusts by 1000ms.
 */
public class CooldownConfigGUI {
    public static final String TITLE = "Tool Cooldowns";

    /** Tool display rows: {displayName, configPath, material, defaultValue}. */
    static final Object[][] TOOLS = {
            {"Teleport", "options.teleport_cooldown_ms", Material.COMPASS, 1500},
            {"Freeze", "freeze.cooldown_ms", Material.BLAZE_ROD, 500},
            {"CPS Check", "cps.cooldown_ms", Material.CLOCK, 2000},
            {"Random TP", "randomtp.cooldown_ms", Material.FEATHER, 2000},
    };

    private final MineStaff plugin;

    public CooldownConfigGUI(MineStaff plugin) {
        this.plugin = plugin;
    }

    public void open(Player viewer) {
        Inventory inv = Bukkit.createInventory(viewer, 27, ChatColor.DARK_GREEN + TITLE);
        int slot = 10;
        for (Object[] tool : TOOLS) {
            String name = (String) tool[0];
            String path = (String) tool[1];
            Material mat = (Material) tool[2];
            int def = (Integer) tool[3];
            int current = plugin.getConfig().getInt(path, def);
            inv.setItem(slot++, item(mat, name, path, current));
        }
        inv.setItem(26, named(Material.BARRIER, ChatColor.RED + "Close", List.of()));
        viewer.openInventory(inv);
    }

    private ItemStack item(Material mat, String name, String path, int current) {
        int smallStep = plugin.getConfig().getInt("cooldowns.small_step_ms", 250);
        int largeStep = plugin.getConfig().getInt("cooldowns.large_step_ms", 1000);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Cooldown: " + ChatColor.WHITE + current + "ms"
                + ChatColor.DARK_GRAY + " (" + String.format("%.2f", current / 1000.0) + "s)");
        lore.add("");
        lore.add(ChatColor.GREEN + "Left-click " + ChatColor.GRAY + "+" + smallStep + "ms  " + ChatColor.DARK_GRAY + "(shift +" + largeStep + "ms)");
        lore.add(ChatColor.RED + "Right-click " + ChatColor.GRAY + "-" + smallStep + "ms  " + ChatColor.DARK_GRAY + "(shift -" + largeStep + "ms)");
        lore.add(ChatColor.DARK_GRAY + "PATH:" + path);
        return named(mat, ChatColor.AQUA + name, lore);
    }

    private ItemStack named(Material mat, String name, List<String> lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta im = it.getItemMeta();
        if (im != null) {
            im.setDisplayName(name);
            if (lore != null && !lore.isEmpty()) im.setLore(lore);
            it.setItemMeta(im);
        }
        return it;
    }
}
