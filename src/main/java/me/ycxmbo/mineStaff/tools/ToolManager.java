package me.ycxmbo.mineStaff.tools;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.*;
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
        reloadSlots(); // Reload slots from config each time
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

    /**
     * Plays tool-specific sound and particle effects from config.
     *
     * @param player   Player to affect
     * @param toolKey  Config key under tools.effects (e.g., "freeze", "vanish")
     */
    public void playToolEffects(Player player, String toolKey) {
        String path = "tools.effects." + toolKey;
        String soundName = plugin.getConfig().getString(path + ".sound");
        String particleName = plugin.getConfig().getString(path + ".particle");

        if (soundName != null && !soundName.isEmpty()) {
            try {
                Sound sound = Sound.valueOf(soundName.toUpperCase());
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("[MineStaff] Invalid sound in config: " + soundName);
            }
        }

        if (particleName != null && !particleName.isEmpty()) {
            try {
                Particle particle = Particle.valueOf(particleName.toUpperCase());
                player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1, 0), 20);
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("[MineStaff] Invalid particle in config: " + particleName);
            }
        }
    }
}
