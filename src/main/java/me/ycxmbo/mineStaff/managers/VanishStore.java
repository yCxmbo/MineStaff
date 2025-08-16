package me.ycxmbo.mineStaff.managers;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Persists vanish state across restarts in vanish.yml
 * API kept tiny on purpose: setVanished(), isVanished(), save().
 */
public class VanishStore {
    private final MineStaff plugin;
    private final File file;
    private final YamlConfiguration yaml;

    public VanishStore(MineStaff plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "vanish.yml");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException ignored) {}
        }
        this.yaml = YamlConfiguration.loadConfiguration(file);
    }

    public boolean isVanished(UUID uuid) {
        return yaml.getBoolean("players." + uuid + ".vanished", false);
    }

    /** Set and immediately write to disk (also returns the value for convenience). */
    public boolean setVanished(UUID uuid, boolean value) {
        yaml.set("players." + uuid + ".vanished", value);
        save();
        return value;
    }

    public void save() {
        try { yaml.save(file); } catch (IOException e) { plugin.getLogger().warning("Failed to save vanish.yml: " + e.getMessage()); }
    }
}
