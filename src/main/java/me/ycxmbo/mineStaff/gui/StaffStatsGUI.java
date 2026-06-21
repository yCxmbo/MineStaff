package me.ycxmbo.mineStaff.gui;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.analytics.StaffAnalyticsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Read-only leaderboard GUI showing each tracked staff member as a player head
 * with their activity metrics in the lore, ranked by duty time.
 */
public class StaffStatsGUI {
    public static final String TITLE = "Staff Activity";

    private final MineStaff plugin;

    public StaffStatsGUI(MineStaff plugin) {
        this.plugin = plugin;
    }

    public void open(Player viewer) {
        StaffAnalyticsManager an = plugin.getStaffAnalyticsManager();
        Inventory inv = Bukkit.createInventory(viewer, 54, ChatColor.DARK_AQUA + TITLE);

        List<UUID> tracked = an.getTracked();
        tracked.sort(Comparator.comparingLong(an::getDutySeconds).reversed());

        int slot = 0;
        for (UUID id : tracked) {
            if (slot >= 54) break;
            inv.setItem(slot++, head(an, id));
        }
        viewer.openInventory(inv);
    }

    private ItemStack head(StaffAnalyticsManager an, UUID id) {
        ItemStack it = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = it.getItemMeta();
        if (meta instanceof SkullMeta skull) {
            try {
                OfflinePlayer off = Bukkit.getOfflinePlayer(id);
                skull.setOwningPlayer(off);
            } catch (Throwable ignored) {}
        }
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + an.getName(id));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Duty time: " + ChatColor.WHITE + StaffAnalyticsManager.formatDuration(an.getDutySeconds(id))
                    + (an.isOnDutyNow(id) ? ChatColor.GREEN + " (on duty)" : ""));
            lore.add(ChatColor.GRAY + "Sessions: " + ChatColor.WHITE + an.getStat(id, "sessions"));
            lore.add(ChatColor.GRAY + "Warnings: " + ChatColor.WHITE + an.getStat(id, "warnings"));
            lore.add(ChatColor.GRAY + "Punishments: " + ChatColor.WHITE + an.getStat(id, "punishments"));
            lore.add(ChatColor.GRAY + "Reports handled: " + ChatColor.WHITE + an.getStat(id, "reports"));
            meta.setLore(lore);
            it.setItemMeta(meta);
        }
        return it;
    }
}
