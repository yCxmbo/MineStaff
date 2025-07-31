package me.ycxmbo.mineStaff.tools;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class InspectorGUI {

    public void openInspectorGUI(Player viewer, Player target) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Inspecting " + target.getName());

        // Add target inventory
        ItemStack[] contents = target.getInventory().getContents();
        for (int i = 0; i < contents.length && i < 36; i++) {
            if (contents[i] != null) {
                inv.setItem(i, contents[i]);
            }
        }

        // Armor
        inv.setItem(45, target.getInventory().getHelmet());
        inv.setItem(46, target.getInventory().getChestplate());
        inv.setItem(47, target.getInventory().getLeggings());
        inv.setItem(48, target.getInventory().getBoots());

        // Health
        ItemStack health = new ItemStack(Material.REDSTONE);
        ItemMeta healthMeta = health.getItemMeta();
        if (healthMeta != null) {
            double hp = target.getHealth();
            double max = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            healthMeta.setDisplayName(ChatColor.RED + "Health: " + hp + "/" + max);
            health.setItemMeta(healthMeta);
        } else {
            MineStaff.getInstance().getLogger().severe("Failed to create health item meta!");
        }
        inv.setItem(50, health);

        // Effects
        ItemStack effects = new ItemStack(Material.BREWING_STAND);
        ItemMeta effectsMeta = effects.getItemMeta();
        if (effectsMeta != null) {
            StringBuilder effectList = new StringBuilder();
            target.getActivePotionEffects().forEach(effect -> {
                effectList.append(ChatColor.GRAY).append("- ")
                        .append(effect.getType().getName())
                        .append(" (").append(effect.getAmplifier() + 1).append(")\n");
            });
            effectsMeta.setDisplayName(ChatColor.AQUA + "Potion Effects");
            effectsMeta.setLore(List.of(effectList.toString().split("\n")));
            effects.setItemMeta(effectsMeta);
        } else {
            MineStaff.getInstance().getLogger().severe("Failed to create effects item meta!");
        }
        inv.setItem(51, effects);

        // Ender Chest
        ItemStack ender = new ItemStack(Material.ENDER_CHEST);
        ItemMeta enderMeta = ender.getItemMeta();
        if (enderMeta != null) {
            String description = MineStaff.getInstance().getConfigManager().getMessage("tool-descriptions.ender_chest", "Click to view Ender Chest");
            enderMeta.setDisplayName(ChatColor.LIGHT_PURPLE + description);
            enderMeta.getPersistentDataContainer().set(
                    new NamespacedKey(MineStaff.getInstance(), "ender_chest"), 
                    new ConfigManager().getConfig().getNamespace(), 
                    "ender_chest");
            ender.setItemMeta(enderMeta);
        } else {
            MineStaff.getInstance().getLogger().severe("Failed to create ender chest item meta!");
        }
        inv.setItem(52, ender);

        viewer.openInventory(inv);
    }
}