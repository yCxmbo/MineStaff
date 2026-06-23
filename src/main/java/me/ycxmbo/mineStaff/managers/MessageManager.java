package me.ycxmbo.mineStaff.managers;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class MessageManager {

    private final MineStaff plugin;
    private YamlConfiguration messages;
    private final File messagesFile;

    public MessageManager(MineStaff plugin) {
        this.plugin = plugin;
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        load();
    }

    public void reload() {
        load();
    }

    private void load() {
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);

        // Merge any new keys from the bundled default without overwriting existing values
        var defaults = plugin.getResource("messages.yml");
        if (defaults != null) {
            YamlConfiguration bundled = YamlConfiguration.loadConfiguration(
                    new java.io.InputStreamReader(defaults, java.nio.charset.StandardCharsets.UTF_8));
            messages.setDefaults(bundled);
            // Persist any new keys
            boolean dirty = false;
            for (String key : bundled.getKeys(true)) {
                if (!messages.isSet(key)) {
                    messages.set(key, bundled.get(key));
                    dirty = true;
                }
            }
            if (dirty) {
                try {
                    messages.save(messagesFile);
                } catch (IOException e) {
                    plugin.getLogger().log(Level.WARNING, "Could not save updated messages.yml", e);
                }
            }
        }
    }

    /** Exposes the underlying YamlConfiguration for direct key lookups. */
    public YamlConfiguration messages() {
        return messages;
    }

    /** Returns the raw (un-prefixed) message with color codes translated. */
    public String getRaw(String key, String fallback) {
        String value = messages.getString(key, fallback);
        if (value == null) value = fallback;
        return ChatColor.translateAlternateColorCodes('&', value);
    }

    /** Returns the prefix + message with color codes translated. */
    public String get(String key, String fallback) {
        String prefix = getRaw("prefix", "");
        return prefix + getRaw(key, fallback);
    }

    /** Convenience overload that uses an empty string as fallback. */
    public String get(String key) {
        return get(key, "");
    }

    /** Returns the prefix alone, color-translated. */
    public String getPrefix() {
        return getRaw("prefix", "");
    }
}
