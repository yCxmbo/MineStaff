package me.ycxmbo.mineStaff.tools;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class ToolManager {

    private final MineStaff plugin;
    private final Map<String, Integer> toolSlots = new HashMap<>();

    public ToolManager(MineStaff plugin) {
        this.plugin = plugin;
        loadSlotsFromConfig();
    }

    private void loadSlotsFromConfig() {
        toolSlots.put("inspect", plugin.getConfig().getInt("tool-slots.inspect", 0));
        toolSlots.put("teleport", plugin.getConfig().getInt("tool-slots.teleport", 1));
        toolSlots.put("freeze", plugin.getConfig().getInt("tool-slots.freeze", 7));
        toolSlots.put("vanish", plugin.getConfig().getInt("tool-slots.vanish", 8));
    }

    public void reloadSlots() {
        loadSlotsFromConfig();
    }

    public void giveStaffTools(Player player) {
        reloadSlots();
        if (!plugin.getStaffLoginManager().isLoggedIn(player)) {
            // If not logged in, don't give tools
            player.getInventory().clear();
            return;
        }

        player.getInventory().setItem(toolSlots.get("inspect"), getInspectTool());
        player.getInventory().setItem(toolSlots.get("teleport"), getTeleportTool());
        player.getInventory().setItem(toolSlots.get("freeze"), getFreezeTool());

        boolean vanished = player.hasPotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY);
        player.getInventory().setItem(toolSlots.get("vanish"), getVanishTool(vanished));
    }

    public ItemStack getInspectTool() {
        return createTool(Material.CHEST, "&bInspect Player");
    }

    public ItemStack getTeleportTool() {
        return createTool(Material.CLOCK, "&aRandom Teleport");
    }

    public ItemStack getFreezeTool() {
        return createTool(Material.ICE, "&cFreeze Player");
    }

    public ItemStack getVanishTool(boolean vanished) {
        Material material = vanished ? Material.LIME_DYE : Material.LIGHT_GRAY_DYE;
        return createTool(material, "&eToggle Vanish");
    }

    private ItemStack createTool(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
            item.setItemMeta(meta);
        }
        return item;
    }
}
