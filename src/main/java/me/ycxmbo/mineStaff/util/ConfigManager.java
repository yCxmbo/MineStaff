package me.ycxmbo.mineStaff.util;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final MineStaff plugin;
    private FileConfiguration config;

    public ConfigManager(MineStaff plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public String getMessage(String path, String def) {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages." + path, def));
    }

    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
