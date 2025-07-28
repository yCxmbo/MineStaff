package me.ycxmbo.mineStaff.util;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final MineStaff plugin;
    private final FileConfiguration config;

    public ConfigManager(MineStaff plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public String getMessage(String path, String def) {
        return config.getString("messages." + path, def);
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
