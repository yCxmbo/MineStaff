package me.ycxmbo.mineStaff.util;

import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * Validates configuration values and warns about invalid entries
 */
public class ConfigValidator {
    private final JavaPlugin plugin;
    private final FileConfiguration config;
    private final List<String> warnings = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    public ConfigValidator(JavaPlugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    /**
     * Validate the entire configuration
     */
    public void validate() {
        plugin.getLogger().info("Validating configuration...");

        validateGameMode();
        validateToolSlots();
        validateTimeouts();
        validateRanges();
        validateSounds();
        validateParticles();
        validateStorageMode();
        validateRedisConfig();
        validateMySQLConfig();
        validate2FAConfig();

        // Report findings
        if (!errors.isEmpty()) {
            plugin.getLogger().severe("=========================================");
            plugin.getLogger().severe("CONFIGURATION ERRORS DETECTED:");
            for (String error : errors) {
                plugin.getLogger().severe("  [ERROR] " + error);
            }
            plugin.getLogger().severe("=========================================");
            plugin.getLogger().severe("Please fix these errors or the plugin may not work correctly!");
        }

        if (!warnings.isEmpty()) {
            plugin.getLogger().warning("=========================================");
            plugin.getLogger().warning("CONFIGURATION WARNINGS:");
            for (String warning : warnings) {
                plugin.getLogger().warning("  [WARN] " + warning);
            }
            plugin.getLogger().warning("=========================================");
        }

        if (errors.isEmpty() && warnings.isEmpty()) {
            plugin.getLogger().info("Configuration validation passed! No issues found.");
        } else {
            plugin.getLogger().info("Configuration validation complete. Found " + errors.size() + " error(s) and " + warnings.size() + " warning(s).");
        }
    }

    private void validateGameMode() {
        String gameMode = config.getString("options.staffmode_gamemode", "CREATIVE");
        try {
            GameMode.valueOf(gameMode.toUpperCase());
        } catch (IllegalArgumentException e) {
            errors.add("Invalid gamemode '" + gameMode + "' in options.staffmode_gamemode. Valid values: SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR");
        }
    }

    private void validateToolSlots() {
        String[] tools = {"teleport", "freeze", "inspect", "vanish", "cps", "randomtp"};
        List<Integer> usedSlots = new ArrayList<>();

        for (String tool : tools) {
            int slot = config.getInt("tools.slots." + tool, -1);
            if (slot < 0 || slot > 8) {
                warnings.add("Tool slot for '" + tool + "' is " + slot + " but should be between 0-8. Using slot 0.");
            } else if (usedSlots.contains(slot)) {
                warnings.add("Tool slot " + slot + " is used by multiple tools (including '" + tool + "'). Tools may overlap!");
            } else {
                usedSlots.add(slot);
            }
        }
    }

    private void validateTimeouts() {
        validatePositiveInt("options.teleport_cooldown_ms", "Teleport cooldown");
        validatePositiveInt("cps.cooldown_ms", "CPS cooldown");
        validatePositiveInt("cps.duration_seconds", "CPS duration");
        validatePositiveInt("randomtp.cooldown_ms", "Random TP cooldown");
        validatePositiveInt("randomtp.active_seconds", "Random TP active duration");
        validatePositiveInt("freeze.cooldown_ms", "Freeze cooldown");
        validatePositiveInt("security.session_timeout_minutes", "Session timeout");
        validatePositiveInt("history.days", "History retention days");
    }

    private void validateRanges() {
        int normalRange = config.getInt("options.teleport_max_range", 60);
        int sneakRange = config.getInt("options.teleport_max_range_sneak", 120);

        if (normalRange <= 0) {
            errors.add("teleport_max_range must be positive, got: " + normalRange);
        }
        if (sneakRange <= 0) {
            errors.add("teleport_max_range_sneak must be positive, got: " + sneakRange);
        }
        if (sneakRange < normalRange) {
            warnings.add("teleport_max_range_sneak (" + sneakRange + ") is less than teleport_max_range (" + normalRange + "). Usually sneak range should be higher.");
        }

        double cageRadius = config.getDouble("freeze.visual_cage.radius", 0.7);
        if (cageRadius <= 0 || cageRadius > 5.0) {
            warnings.add("freeze.visual_cage.radius is " + cageRadius + " but should typically be between 0.5 and 2.0");
        }

        int maxSnapshots = config.getInt("rollback.max-snapshots-per-player", 25);
        if (maxSnapshots <= 0) {
            errors.add("rollback.max-snapshots-per-player must be positive, got: " + maxSnapshots);
        } else if (maxSnapshots > 100) {
            warnings.add("rollback.max-snapshots-per-player is " + maxSnapshots + " which may use excessive disk space. Consider reducing it.");
        }
    }

    private void validateSounds() {
        validateSound("alerts.sound", "Alert sound");
        validateSound("staffchat.mention_sound", "Staff chat mention sound");
    }

    private void validateSound(String path, String description) {
        String soundName = config.getString(path, "");
        if (soundName.isEmpty()) return;

        try {
            Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            warnings.add(description + " '" + soundName + "' at " + path + " is not a valid sound name. It may not play correctly.");
        }
    }

    private void validateParticles() {
        String particleName = config.getString("freeze.visual_cage.particle", "SNOWFLAKE");
        try {
            // Try to get the particle enum
            Particle.valueOf(particleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            warnings.add("Freeze particle '" + particleName + "' is not a valid particle name. Cage may not display correctly.");
        }
    }

    private void validateStorageMode() {
        String mode = config.getString("storage.mode", "yaml");
        List<String> validModes = Arrays.asList("yaml", "sqlite", "mysql");

        if (!validModes.contains(mode.toLowerCase())) {
            errors.add("Invalid storage mode '" + mode + "'. Valid values: yaml, sqlite, mysql");
        }

        if ("mysql".equalsIgnoreCase(mode)) {
            String host = config.getString("storage.mysql.host", "");
            String database = config.getString("storage.mysql.database", "");
            String username = config.getString("storage.mysql.username", "");

            if (host.isEmpty()) {
                errors.add("MySQL storage mode is enabled but storage.mysql.host is not set!");
            }
            if (database.isEmpty()) {
                errors.add("MySQL storage mode is enabled but storage.mysql.database is not set!");
            }
            if (username.isEmpty()) {
                warnings.add("MySQL storage mode is enabled but storage.mysql.username is empty!");
            }
            if (host.equals("localhost") || host.equals("127.0.0.1")) {
                warnings.add("MySQL host is set to localhost. For production networks, consider using a dedicated database server.");
            }
        }
    }

    private void validateRedisConfig() {
        boolean redisEnabled = config.getBoolean("redis.enabled", false);
        if (!redisEnabled) return;

        String host = config.getString("redis.host", "localhost");
        int port = config.getInt("redis.port", 6379);

        if (host.isEmpty()) {
            errors.add("Redis is enabled but redis.host is not set!");
        }
        if (port <= 0 || port > 65535) {
            errors.add("Redis port " + port + " is invalid. Must be between 1-65535.");
        }

        // Check if cross-server features are enabled but Redis is not
        boolean staffchatCross = config.getBoolean("staffchat.cross_server", false);
        boolean reportsCross = config.getBoolean("reports.cross_server", false);
        boolean alertsCross = config.getBoolean("alerts.cross_server", false);

        if ((staffchatCross || reportsCross || alertsCross) && !redisEnabled) {
            warnings.add("Cross-server features are enabled but Redis is disabled. Cross-server communication will not work!");
        }
    }

    private void validateMySQLConfig() {
        String mode = config.getString("storage.mode", "yaml");
        if (!"mysql".equalsIgnoreCase(mode)) return;

        int port = config.getInt("storage.mysql.port", 3306);
        if (port <= 0 || port > 65535) {
            errors.add("MySQL port " + port + " is invalid. Must be between 1-65535.");
        }

        String password = config.getString("storage.mysql.password", "");
        if (password.isEmpty()) {
            warnings.add("MySQL password is empty! This is a security risk for production environments.");
        }
    }

    private void validate2FAConfig() {
        boolean tfaEnabled = config.getBoolean("security.2fa.enabled", false);
        boolean staffLoginEnabled = config.getBoolean("options.staff_login_enabled", true);

        if (tfaEnabled && !staffLoginEnabled) {
            warnings.add("2FA is enabled but staff login is disabled! 2FA will not work without staff login.");
        }
    }

    private void validatePositiveInt(String path, String description) {
        int value = config.getInt(path, -1);
        if (value <= 0) {
            if (config.isSet(path)) {
                errors.add(description + " at " + path + " must be positive, got: " + value);
            }
        }
    }

    /**
     * Check if there were any errors during validation
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Check if there were any warnings during validation
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Get all errors
     */
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    /**
     * Get all warnings
     */
    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }
}
