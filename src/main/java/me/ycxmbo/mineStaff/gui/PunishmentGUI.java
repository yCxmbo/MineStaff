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
import java.util.Map;
import java.util.UUID;

/**
 * Quick-action punishment menu. Punishment templates are configured under
 * {@code punishments.templates}; quick unban/unmute/kick buttons are always
 * present. Target data is encoded into item lore for the click listener.
 */
public class PunishmentGUI {
    public static final String TITLE_PREFIX = "Punish: ";

    private final MineStaff plugin;

    public PunishmentGUI(MineStaff plugin) {
        this.plugin = plugin;
    }

    public void open(Player viewer, String targetName, UUID targetUuid) {
        Inventory inv = Bukkit.createInventory(viewer, 54, ChatColor.DARK_RED + TITLE_PREFIX + targetName);

        List<Map<?, ?>> templates = plugin.getConfig().getMapList("punishments.templates");
        int slot = 0;
        for (Map<?, ?> t : templates) {
            if (slot >= 45) break;
            String name = str(t.get("name"), "Punishment");
            String type = str(t.get("type"), "ban").toLowerCase();
            String duration = str(t.get("duration"), "perm");
            String reason = str(t.get("reason"), name);

            Material mat = switch (type) {
                case "mute" -> Material.BOOK;
                case "kick" -> Material.IRON_BOOTS;
                default -> Material.BARRIER;
            };
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Type: " + ChatColor.WHITE + type.toUpperCase());
            lore.add(ChatColor.GRAY + "Duration: " + ChatColor.WHITE + duration);
            lore.add(ChatColor.GRAY + "Reason: " + ChatColor.WHITE + reason);
            lore.add(ChatColor.DARK_GRAY + "Click to apply");
            lore.add(ChatColor.DARK_GRAY + "ACTION:" + type);
            lore.add(ChatColor.DARK_GRAY + "DUR:" + duration);
            lore.add(ChatColor.DARK_GRAY + "REASON:" + reason);
            lore.add(ChatColor.DARK_GRAY + "TARGET:" + targetUuid);
            lore.add(ChatColor.DARK_GRAY + "TNAME:" + targetName);
            inv.setItem(slot++, named(mat, ChatColor.RED + name, lore));
        }

        inv.setItem(48, quick(Material.LIME_DYE, ChatColor.GREEN + "Unmute", "unmute", targetUuid, targetName));
        inv.setItem(49, quick(Material.IRON_BOOTS, ChatColor.YELLOW + "Kick (generic)", "kick", targetUuid, targetName));
        inv.setItem(50, quick(Material.GREEN_WOOL, ChatColor.GREEN + "Unban", "unban", targetUuid, targetName));

        inv.setItem(53, named(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Close", List.of()));

        viewer.openInventory(inv);
    }

    private ItemStack quick(Material mat, String name, String action, UUID target, String targetName) {
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_GRAY + "Click to apply");
        lore.add(ChatColor.DARK_GRAY + "ACTION:" + action);
        lore.add(ChatColor.DARK_GRAY + "DUR:perm");
        lore.add(ChatColor.DARK_GRAY + "REASON:Staff action");
        lore.add(ChatColor.DARK_GRAY + "TARGET:" + target);
        lore.add(ChatColor.DARK_GRAY + "TNAME:" + targetName);
        return named(mat, name, lore);
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

    private static String str(Object o, String def) {
        return o == null ? def : String.valueOf(o);
    }
}
