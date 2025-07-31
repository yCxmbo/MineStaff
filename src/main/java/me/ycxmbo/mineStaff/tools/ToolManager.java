package me.ycxmbo.mineStaff.tools;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.util.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class ToolManager {

    private final MineStaff plugin;
    private final ConfigManager configManager;
    private final Map<String, Integer> toolSlots = new HashMap<>();

    public ToolManager(MineStaff plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        loadSlotsFromConfig();
    }

    private void loadSlotsFromConfig() {
        String inspectMaterial = configManager.getMessage("tool-materials.inspect", "CHEST");
        Material material = Material.getMaterial(inspectMaterial.toUpperCase());
        if (material == null) {
            plugin.getLogger().severe("Invalid material in config for 'tool-materials.inspect': " + inspectMaterial);
            material = Material.CHEST; // Fallback
        }
        toolSlots.put("inspect", configManager.getInt("tool-slots.inspect", 0));
        toolSlots.put("teleport", configManager.getInt("tool-slots.teleport", 1));
        toolSlots.put("freeze", configManager.getInt("tool-slots.freeze", 7));
        toolSlots.put("vanish", configManager.getInt("tool-slots.vanish", 8));
    }

    public void reloadSlots() {
        loadSlotsFromConfig();
    }

    public void giveStaffTools(Player player) {
        reloadSlots();
        if (!plugin.getStaffLoginManager().isLoggedIn(player)) {
            player.getInventory().clear();
            return;
        }

        player.getInventory().setItem(toolSlots.get("inspect"), getInspectTool());
        player.getInventory().setItem(toolSlots.get("teleport"), getTeleportTool());
        player.getInventory().setItem(toolSlots.get("freeze"), getFreezeTool());
        player.getInventory().setItem(toolSlots.get("vanish"), getVanishTool(player));
    }

    public ItemStack getInspectTool() {
        return createTool(Material.CHEST, configManager.getMessage("tool-names.inspect", "Inspect Player"));
    }

    public ItemStack getTeleportTool() {
        return createTool(Material.CLOCK, configManager.getMessage("tool-names.teleport", "Random Teleport"));
    }

    public ItemStack getFreezeTool() {
        return createTool(Material.ICE, configManager.getMessage("tool-names.freeze", "Freeze Player"));
    }

    public ItemStack getVanishTool(Player player) {
        Material material = player.hasPotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY) ?
                Material.LIME_DYE : Material.LIGHT_GRAY_DYE;
        return createTool(material, configManager.getMessage("tool-names.vanish", "Toggle Vanish"));
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
