package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class InspectorGUIListener implements Listener {

    private final MineStaff plugin;

    public InspectorGUIListener(MineStaff plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInspectorClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player staff)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        var meta = clicked.getItemMeta();
        if (!meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "ender_view"), PersistentDataType.STRING))
            return;

        event.setCancelled(true);
        String targetName = meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "ender_view"), PersistentDataType.STRING);
        Player target = Bukkit.getPlayerExact(targetName);

        if (target == null) {
            staff.sendMessage(ChatColor.RED + "Player is offline.");
            return;
        }

        staff.openInventory(target.getEnderChest());
        staff.sendMessage(ChatColor.LIGHT_PURPLE + "Opening " + target.getName() + "'s Ender Chest...");
    }
}
