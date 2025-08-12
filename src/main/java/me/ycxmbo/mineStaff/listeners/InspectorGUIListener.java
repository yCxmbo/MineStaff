package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.tools.InspectorGUI;
import me.ycxmbo.mineStaff.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InspectorGUIListener implements Listener {
    private final MineStaff plugin;
    private final InspectorGUI gui;

    public InspectorGUIListener(MineStaff plugin) {
        this.plugin = plugin;
        this.gui = plugin.getInspectorGUI();
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player staff)) return;
        Inventory inv = e.getInventory();
        String title = e.getView().getTitle();
        if (title == null || !title.startsWith(gui.getTitle())) return;

        e.setCancelled(true);
        ItemStack current = e.getCurrentItem();
        if (current == null || current.getType() == Material.AIR) return;

        String[] parts = title.split(" - ", 2);
        if (parts.length < 2) return;
        String targetName = parts[1];
        Player online = Bukkit.getPlayerExact(targetName);
        if (online != null) {
            switch (current.getType()) {
                case PLAYER_HEAD -> {
                    staff.sendMessage(ChatColor.GOLD + "UUID: " + online.getUniqueId());
                    SoundUtil.playInspectSound(staff);
                }
                case CHEST -> {
                    staff.openInventory(online.getInventory());
                    SoundUtil.playInspectSound(staff);
                }
                case ENDER_CHEST -> {
                    staff.openInventory(online.getEnderChest());
                    SoundUtil.playInspectSound(staff);
                }
                case CLOCK -> {
                    staff.sendMessage(ChatColor.AQUA + "Health: " + online.getHealth() + " / " + online.getMaxHealth());
                    SoundUtil.playInspectSound(staff);
                }
                default -> {
                    SoundUtil.playFailSound(staff);
                }
            }
        } else {
            OfflinePlayer off = Bukkit.getOfflinePlayer(targetName);
            staff.sendMessage(ChatColor.YELLOW + "Offline profile: " + off.getName());
            SoundUtil.playInspectSound(staff);
        }
    }
}
