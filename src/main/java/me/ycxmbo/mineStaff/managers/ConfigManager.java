package me.ycxmbo.mineStaff.managers;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ConfigManager {
    private final MineStaff plugin;
    private FileConfiguration config;
    private File staffAccountsFile;
    private YamlConfiguration staffAccounts;
    private YamlConfiguration localeYaml;

    private final Map<String, Object> defaults = new HashMap<>();

    public ConfigManager(MineStaff plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        loadDefaults();
        ensureDefaults();
        loadLocale();
        setupStaffAccounts();
    }

    private void loadDefaults() {
        defaults.put("config_version", 2);
        defaults.put("options.staff_login_enabled", true);
        defaults.put("options.require_login", true);
        defaults.put("options.staffmode_gamemode", "CREATIVE");
        defaults.put("options.staffchat_prefix", "@");
        defaults.put("tools.slots.teleport", 0);
        defaults.put("tools.slots.freeze", 1);
        defaults.put("tools.slots.inspect", 2);
        defaults.put("tools.slots.vanish", 8);
        defaults.put("tools.slots.cps", 3);
        defaults.put("tools.slots.randomtp", 4);
        defaults.put("alerts.use_minimessage", true);
        defaults.put("alerts.notify_on_join", true);
        defaults.put("alerts.notify_on_quit", false);
        defaults.put("alerts.join_template", "{player} joined at {world} ({x}, {y}, {z}).");
        defaults.put("alerts.quit_template", "{player} left from {world} ({x}, {y}, {z}).");
        defaults.put("alerts.join_include_tp", true);
        defaults.put("alerts.quit_include_tp", false);
        defaults.put("alerts.cross_server", true);
        defaults.put("reports.cross_server", true);
        defaults.put("staffchat.cross_server", true);
        defaults.put("staffchat.console_log", true);
        defaults.put("staffchat.format_legacy", "&8[&dStaff&8] &b{name}&7: &f{message}");
        defaults.put("cps.duration_seconds", 10);
        defaults.put("cps.cooldown_ms", 2000);

        // Freeze options
        defaults.put("freeze.cooldown_ms", 500);
        defaults.put("freeze.default_seconds", 0);
        defaults.put("freeze.shift_seconds", 0);
        defaults.put("freeze.logout_flag_enabled", true);
        defaults.put("freeze.visual_cage.enabled", true);
        defaults.put("freeze.visual_cage.particle", "SNOWFLAKE");
        defaults.put("freeze.visual_cage.radius", 0.7);

        // Teleport options
        defaults.put("options.teleport_to_player", true);

        // Reports
        defaults.put("reports.claim_timeout_seconds", 0);
        defaults.put("reports.notify_reporter_on_claim", true);
        defaults.put("reports.notify_reporter_on_close", true);
        defaults.put("reports.notify_reporter_on_needs_info", true);
        defaults.put("reports.default_category", "GENERAL");
        defaults.put("reports.default_priority", "MEDIUM");
        defaults.put("redis.channels.alerts", "minestaff:alerts");

        // Tool materials
        defaults.put("tools.materials.teleport", "COMPASS");
        defaults.put("tools.materials.freeze",   "BLAZE_ROD");
        defaults.put("tools.materials.inspect",  "BOOK");
        defaults.put("tools.materials.cps",      "CLOCK");
        defaults.put("tools.materials.randomtp", "FEATHER");

        // Tool display names
        defaults.put("tools.names.teleport",  "&bTeleport");
        defaults.put("tools.names.freeze",    "&cFreeze");
        defaults.put("tools.names.inspect",   "&6Inspect");
        defaults.put("tools.names.cps",       "&eCPS Check");
        defaults.put("tools.names.randomtp",  "&bRandom TP");
        defaults.put("tools.names.vanish_on",  "&dVanish &aON");
        defaults.put("tools.names.vanish_off", "&dVanish &7OFF");

        // Staff mode protections
        defaults.put("staffmode.protections.cancel_damage",         true);
        defaults.put("staffmode.protections.cancel_mob_targeting",  true);
        defaults.put("staffmode.protections.cancel_block_place",    true);
        defaults.put("staffmode.protections.cancel_block_break",    true);
        defaults.put("staffmode.protections.cancel_item_drop",      true);
        defaults.put("staffmode.protections.cancel_item_pickup",    true);

        // Vanish
        defaults.put("vanish.persist", true);

        // Follow mode
        defaults.put("follow.interval_ticks", 20);

        // Notes limit
        defaults.put("notes.max_per_player", 0);

        // Security thresholds
        defaults.put("security.max_failed_attempts",      5);
        defaults.put("security.lockout_duration_minutes", 5);

        // Freeze visual cage extras
        defaults.put("freeze.visual_cage.particle_count", 16);
        defaults.put("freeze.visual_cage.period_ticks",   40);
        defaults.put("freeze.action_bar", "&cYou are frozen.");

        // Report SLA defaults
        defaults.put("reports.sla_seconds.CRITICAL", 3600);
        defaults.put("reports.sla_seconds.HIGH",     7200);
        defaults.put("reports.sla_seconds.MEDIUM",  21600);
        defaults.put("reports.sla_seconds.LOW",     86400);

        // Backup startup delay
        defaults.put("backup.startup_delay_seconds", 60);

        // Channels remote suffix
        defaults.put("channels.remote_suffix", " §7[Remote]");

        // Cross-server teleport
        defaults.put("crossserver.timeout_ms",               60000);
        defaults.put("crossserver.cleanup_interval_ticks",   1200);
        defaults.put("crossserver.response_timeout_ticks",   60);

        // Redis teleport channels
        defaults.put("redis.channels.teleport_query",    "minestaff:teleport:query");
        defaults.put("redis.channels.teleport_response", "minestaff:teleport:response");
        defaults.put("redis.channels.teleport_pending",  "minestaff:teleport:pending");

        // Cooldown GUI step sizes
        defaults.put("cooldowns.small_step_ms", 250);
        defaults.put("cooldowns.large_step_ms", 1000);
    }

    private void ensureDefaults() {
        for (Map.Entry<String, Object> e : defaults.entrySet()) {
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
                plugin.getLogger().log(Level.WARNING, "Could not create staffaccounts.yml", e);
            }
        }
        staffAccounts = YamlConfiguration.loadConfiguration(staffAccountsFile);
    }

    /**
     * Returns a message by key. Lookup order:
     *  1. messages.yml (via MessageManager)
     *  2. legacy locale file
     *  3. legacy messages.* section in config.yml
     *  4. supplied default value
     */
    public String getMessage(String path, String def) {
        // 1. messages.yml
        MessageManager mm = plugin.getMessageManager();
        if (mm != null) {
            String fromFile = mm.messages().getString(path, null);
            if (fromFile != null) {
                String prefix = mm.getPrefix();
                return prefix + ChatColor.translateAlternateColorCodes('&', fromFile);
            }
        }

        // 2. locale file (legacy)
        String prefix = ChatColor.translateAlternateColorCodes('&', config.getString("messages.prefix", ""));
        String rawMsg = null;
        if (localeYaml != null && localeYaml.isSet("messages." + path)) {
            rawMsg = localeYaml.getString("messages." + path, def);
        }
        // 3. config.yml messages section (legacy)
        if (rawMsg == null) rawMsg = config.getString("messages." + path, def);
        if (rawMsg == null) rawMsg = def;
        return prefix + ChatColor.translateAlternateColorCodes('&', rawMsg);
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
            plugin.getLogger().log(Level.WARNING, "Could not save staffaccounts.yml", e);
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
            plugin.getLogger().log(Level.WARNING, "Failed to load locale file", t);
            localeYaml = null;
        }
    }
}
