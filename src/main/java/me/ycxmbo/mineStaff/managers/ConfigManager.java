package me.ycxmbo.mineStaff.managers;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final MineStaff plugin;
    private FileConfiguration config;
    private File staffAccountsFile;
    private YamlConfiguration staffAccounts;

    private final Map<String, String> defaults = new HashMap<>();

    public ConfigManager(MineStaff plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        loadDefaults();
        ensureDefaults();
        setupStaffAccounts();
    }

    private void loadDefaults() {
        defaults.put("messages.prefix", "&8[&aMineStaff&8]&r ");
        defaults.put("messages.no_permission", "&cYou don't have permission.");
        defaults.put("messages.only_players", "&cOnly players can use this.");
        defaults.put("messages.staffmode_enabled", "&aStaff Mode enabled.");
        defaults.put("messages.staffmode_disabled", "&cStaff Mode disabled.");
        defaults.put("messages.login_required", "&eYou must /stafflogin before using staff tools.");
        defaults.put("messages.login_success", "&aLogin successful.");
        defaults.put("messages.login_failure", "&cIncorrect password.");
        defaults.put("messages.password_set", "&aPassword set.");
        defaults.put("options.require_login", "true");
        defaults.put("options.staffchat_prefix", "@");
    }

    private void ensureDefaults() {
        for (Map.Entry<String, String> e : defaults.entrySet()) {
            if (!config.isSet(e.getKey())) {
                config.set(e.getKey(), e.getValue());
            }
        }
        plugin.saveConfig();
    }

    private void setupStaffAccounts() {
        staffAccountsFile = new File(plugin.getDataFolder(), "staffaccounts.yml");
        if (!staffAccountsFile.exists()) {
            try {
                staffAccountsFile.getParentFile().mkdirs();
                staffAccountsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        staffAccounts = YamlConfiguration.loadConfiguration(staffAccountsFile);
    }

    public String getMessage(String path, String def) {
        String prefix = ChatColor.translateAlternateColorCodes('&', config.getString("messages.prefix", ""));
        String raw = ChatColor.translateAlternateColorCodes('&', config.getString("messages." + path, def));
        return prefix + raw;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public boolean isLoginRequired() {
        return config.getBoolean("options.require_login", true);
    }

    public String getStaffchatPrefix() {
        return config.getString("options.staffchat_prefix", "@");
    }

    public YamlConfiguration getStaffAccounts() {
        return staffAccounts;
    }

    public void saveStaffAccounts() {
        try {
            staffAccounts.save(staffAccountsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        setupStaffAccounts();
        ensureDefaults();
    }
}
