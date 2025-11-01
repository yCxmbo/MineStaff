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
    private YamlConfiguration localeYaml;

    private final Map<String, String> defaults = new HashMap<>();

    public ConfigManager(MineStaff plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        loadDefaults();
        ensureDefaults();
        loadLocale();
        setupStaffAccounts();
    }

    private void loadDefaults() {
        defaults.put("config_version", "2");
        defaults.put("messages.prefix", "&8[&aMineStaff&8]&r ");
        defaults.put("messages.no_permission", "&cYou don't have permission.");
        defaults.put("messages.only_players", "&cOnly players can use this.");
        defaults.put("messages.staffmode_enabled", "&aStaff Mode enabled.");
        defaults.put("messages.staffmode_disabled", "&cStaff Mode disabled.");
        defaults.put("messages.login_required", "&eYou must /stafflogin before using staff tools.");
        defaults.put("messages.login_success", "&aLogin successful.");
        defaults.put("messages.login_failure", "&cIncorrect password.");
        defaults.put("messages.password_set", "&aPassword set.");
        defaults.put("messages.staff_login_disabled", "&cStaff login is disabled.");
        defaults.put("messages.cps_started", "&aStarted {seconds}s CPS test on {target}.");
        defaults.put("messages.cps_target_notify", "&eA staff member is measuring your CPS for {seconds} seconds.");
        defaults.put("messages.cps_tool_cooldown", "&cCPS checker cooldown: {seconds}s");
        defaults.put("messages.cps_already_running", "&cA CPS test is already running for {target}.");
        defaults.put("options.staff_login_enabled", "true");
        defaults.put("options.require_login", "true");
        defaults.put("options.staffmode_gamemode", "CREATIVE");
        defaults.put("options.staffchat_prefix", "@");
        defaults.put("tools.slots.teleport", "0");
        defaults.put("tools.slots.freeze", "1");
        defaults.put("tools.slots.inspect", "2");
        defaults.put("tools.slots.vanish", "8");
        defaults.put("tools.slots.cps", "3");
        defaults.put("tools.slots.randomtp", "4");
        defaults.put("alerts.use_minimessage", "true");
        defaults.put("alerts.notify_on_join", "true");
        defaults.put("alerts.notify_on_quit", "false");
        defaults.put("alerts.join_template", "{player} joined at {world} ({x}, {y}, {z}).");
        defaults.put("alerts.quit_template", "{player} left from {world} ({x}, {y}, {z}).");
        defaults.put("alerts.join_include_tp", "true");
        defaults.put("alerts.quit_include_tp", "false");
        defaults.put("alerts.cross_server", "true");
        defaults.put("reports.cross_server", "true");
        defaults.put("staffchat.cross_server", "true");
        defaults.put("staffchat.console_log", "true");
        defaults.put("staffchat.format_legacy", "&8[&dStaff&8] &b{name}&7: &f{message}");
        defaults.put("cps.duration_seconds", "10");
        defaults.put("cps.cooldown_ms", "2000");

        // Freeze options
        defaults.put("freeze.cooldown_ms", "500");
        defaults.put("freeze.default_seconds", "0");
        defaults.put("freeze.shift_seconds", "0");
        defaults.put("freeze.logout_flag_enabled", "true");
        defaults.put("freeze.visual_cage.enabled", "true");
        defaults.put("freeze.visual_cage.particle", "SNOWFLAKE");
        defaults.put("freeze.visual_cage.radius", "0.7");

        // Teleport options
        defaults.put("options.teleport_to_player", "true");

        // Reports
        defaults.put("reports.claim_timeout_seconds", "0");
        defaults.put("reports.notify_reporter_on_claim", "true");
        defaults.put("reports.notify_reporter_on_close", "true");
        defaults.put("reports.notify_reporter_on_needs_info", "true");
        defaults.put("reports.default_category", "GENERAL");
        defaults.put("reports.default_priority", "MEDIUM");
        defaults.put("redis.channels.alerts", "minestaff:alerts");
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
        String rawMsg = null;
        if (localeYaml != null && localeYaml.isSet("messages." + path)) {
            rawMsg = localeYaml.getString("messages." + path, def);
        }
        if (rawMsg == null) rawMsg = config.getString("messages." + path, def);
        String raw = ChatColor.translateAlternateColorCodes('&', rawMsg);
        return prefix + raw;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public boolean isStaffLoginEnabled() {
        // Support legacy locations while preferring the current options.* layout.
        if (config.isSet("options.staff_login_enabled")) {
            return config.getBoolean("options.staff_login_enabled");
        }
        return config.getBoolean("staff_login_enabled", true);
    }

    public boolean isLoginRequired() {
        boolean requireLogin = config.getBoolean(
                config.isSet("options.require_login") ? "options.require_login" : "require_login",
                true
        );
        return isStaffLoginEnabled() && requireLogin;
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
        loadLocale();
        setupStaffAccounts();
        ensureDefaults();
    }

    public int getToolSlot(String key, int def) {
        return config.getInt("tools.slots." + key, def);
    }

    private void loadLocale() {
        try {
            String loc = config.getString("options.locale", null);
            if (loc == null || loc.isBlank()) { localeYaml = null; return; }
            java.io.File f = new java.io.File(plugin.getDataFolder(), "lang/" + loc + ".yml");
            if (!f.exists()) { localeYaml = null; return; }
            localeYaml = YamlConfiguration.loadConfiguration(f);
        } catch (Throwable t) {
            localeYaml = null;
        }
    }
}
