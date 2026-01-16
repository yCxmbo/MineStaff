package me.ycxmbo.mineStaff.util;

import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Manages customizable sounds for various plugin features
 */
public class SoundManager {
    private final JavaPlugin plugin;
    private final FileConfiguration config;
    private final Map<String, SoundConfig> sounds = new HashMap<>();

    public SoundManager(JavaPlugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
        loadSounds();
    }

    private void loadSounds() {
        // Load all sound configurations
        sounds.put("staffmode.enable", loadSound("sounds.staffmode.enable", "ENTITY_PLAYER_LEVELUP", 1.0f, 1.0f));
        sounds.put("staffmode.disable", loadSound("sounds.staffmode.disable", "ENTITY_EXPERIENCE_ORB_PICKUP", 1.0f, 1.0f));
        sounds.put("vanish.enable", loadSound("sounds.vanish.enable", "ENTITY_BAT_TAKEOFF", 1.0f, 0.8f));
        sounds.put("vanish.disable", loadSound("sounds.vanish.disable", "ENTITY_BAT_AMBIENT", 1.0f, 1.2f));
        sounds.put("freeze.apply", loadSound("sounds.freeze.apply", "BLOCK_GLASS_BREAK", 1.0f, 0.5f));
        sounds.put("freeze.remove", loadSound("sounds.freeze.remove", "ENTITY_EXPERIENCE_ORB_PICKUP", 1.0f, 1.5f));
        sounds.put("teleport.success", loadSound("sounds.teleport.success", "ENTITY_ENDERMAN_TELEPORT", 1.0f, 1.0f));
        sounds.put("teleport.fail", loadSound("sounds.teleport.fail", "ENTITY_VILLAGER_NO", 1.0f, 1.0f));
        sounds.put("alert.normal", loadSound("sounds.alert.normal", "ENTITY_EXPERIENCE_ORB_PICKUP", 1.0f, 1.0f));
        sounds.put("alert.urgent", loadSound("sounds.alert.urgent", "ENTITY_WARDEN_SONIC_BOOM", 1.0f, 1.0f));
        sounds.put("report.filed", loadSound("sounds.report.filed", "ENTITY_VILLAGER_YES", 1.0f, 1.0f));
        sounds.put("report.claimed", loadSound("sounds.report.claimed", "BLOCK_NOTE_BLOCK_BELL", 1.0f, 1.0f));
        sounds.put("report.closed", loadSound("sounds.report.closed", "ENTITY_EXPERIENCE_ORB_PICKUP", 1.0f, 1.0f));
        sounds.put("infraction.added", loadSound("sounds.infraction.added", "BLOCK_ANVIL_LAND", 1.0f, 1.5f));
        sounds.put("cps.start", loadSound("sounds.cps.start", "BLOCK_NOTE_BLOCK_PLING", 1.0f, 1.5f));
        sounds.put("cps.complete", loadSound("sounds.cps.complete", "BLOCK_NOTE_BLOCK_PLING", 1.0f, 2.0f));
        sounds.put("backup.request", loadSound("sounds.backup.request", "ENTITY_WARDEN_SONIC_BOOM", 1.0f, 1.0f));
        sounds.put("backup.sent", loadSound("sounds.backup.sent", "ENTITY_PLAYER_LEVELUP", 1.0f, 1.0f));
        sounds.put("staffchat.mention", loadSound("sounds.staffchat.mention", "ENTITY_EXPERIENCE_ORB_PICKUP", 1.0f, 1.5f));
        sounds.put("staffchat.message", loadSound("sounds.staffchat.message", "ENTITY_ITEM_PICKUP", 0.5f, 1.0f));
        sounds.put("warning.issued", loadSound("sounds.warning.issued", "ENTITY_LIGHTNING_BOLT_THUNDER", 0.5f, 1.5f));
        sounds.put("warning.received", loadSound("sounds.warning.received", "ENTITY_LIGHTNING_BOLT_IMPACT", 1.0f, 1.0f));
        sounds.put("duty.start", loadSound("sounds.duty.start", "ENTITY_ENDER_DRAGON_GROWL", 0.8f, 1.0f));
        sounds.put("duty.end", loadSound("sounds.duty.end", "ENTITY_EXPERIENCE_ORB_PICKUP", 1.0f, 1.0f));
    }

    private SoundConfig loadSound(String path, String defaultSound, float defaultVolume, float defaultPitch) {
        boolean enabled = config.getBoolean(path + ".enabled", true);
        String soundName = config.getString(path + ".sound", defaultSound);
        float volume = (float) config.getDouble(path + ".volume", defaultVolume);
        float pitch = (float) config.getDouble(path + ".pitch", defaultPitch);

        Sound sound = null;
        try {
            sound = Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound '" + soundName + "' at " + path + ", using default: " + defaultSound);
            try {
                sound = Sound.valueOf(defaultSound.toUpperCase());
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().severe("Default sound '" + defaultSound + "' is also invalid! Sound will not play.");
                return new SoundConfig(null, volume, pitch, false);
            }
        }

        return new SoundConfig(sound, volume, pitch, enabled);
    }

    /**
     * Play a sound to a player
     *
     * @param player The player to play the sound to
     * @param soundKey The sound key (e.g., "staffmode.enable")
     */
    public void playSound(Player player, String soundKey) {
        if (player == null) return;

        SoundConfig config = sounds.get(soundKey);
        if (config == null || !config.enabled || config.sound == null) {
            return;
        }

        try {
            player.playSound(player.getLocation(), config.sound, config.volume, config.pitch);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to play sound " + soundKey + " to " + player.getName(), e);
        }
    }

    /**
     * Play a sound to all online players with a specific permission
     *
     * @param permission The permission to check
     * @param soundKey The sound key to play
     */
    public void playSoundToPermission(String permission, String soundKey) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.hasPermission(permission)) {
                playSound(player, soundKey);
            }
        }
    }

    /**
     * Reload all sound configurations
     */
    public void reload() {
        sounds.clear();
        loadSounds();
        plugin.getLogger().info("Sound configurations reloaded.");
    }

    /**
     * Check if a sound is enabled
     *
     * @param soundKey The sound key
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled(String soundKey) {
        SoundConfig config = sounds.get(soundKey);
        return config != null && config.enabled;
    }

    /**
     * Internal sound configuration
     */
    private static class SoundConfig {
        final Sound sound;
        final float volume;
        final float pitch;
        final boolean enabled;

        SoundConfig(Sound sound, float volume, float pitch, boolean enabled) {
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
            this.enabled = enabled;
        }
    }
}
