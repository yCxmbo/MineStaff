package me.ycxmbo.mineStaff.warnings;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages warnings for players
 */
public class WarningManager {
    private final MineStaff plugin;
    private final File warningsFile;
    private YamlConfiguration warningsYaml;
    private final Map<UUID, List<Warning>> playerWarnings = new ConcurrentHashMap<>();
    private int nextId = 1;

    public WarningManager(MineStaff plugin) {
        this.plugin = plugin;
        this.warningsFile = new File(plugin.getDataFolder(), "warnings.yml");
        load();
    }

    private void load() {
        if (!warningsFile.exists()) {
            try {
                warningsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create warnings.yml: " + e.getMessage());
            }
        }

        warningsYaml = YamlConfiguration.loadConfiguration(warningsFile);
        nextId = warningsYaml.getInt("next_id", 1);

        // Load all warnings
        if (warningsYaml.contains("warnings")) {
            for (String key : warningsYaml.getConfigurationSection("warnings").getKeys(false)) {
                String path = "warnings." + key;
                int id = Integer.parseInt(key);
                UUID targetUuid = UUID.fromString(warningsYaml.getString(path + ".target_uuid"));
                String targetName = warningsYaml.getString(path + ".target_name");
                UUID issuerUuid = UUID.fromString(warningsYaml.getString(path + ".issuer_uuid"));
                String issuerName = warningsYaml.getString(path + ".issuer_name");
                String reason = warningsYaml.getString(path + ".reason");
                long timestamp = warningsYaml.getLong(path + ".timestamp");
                long expiresAt = warningsYaml.getLong(path + ".expires_at", 0);
                boolean active = warningsYaml.getBoolean(path + ".active", true);
                String severity = warningsYaml.getString(path + ".severity", "MEDIUM");

                Warning warning = new Warning(id, targetUuid, targetName, issuerUuid, issuerName,
                        reason, timestamp, expiresAt, active, severity);

                playerWarnings.computeIfAbsent(targetUuid, k -> new ArrayList<>()).add(warning);

                if (id >= nextId) {
                    nextId = id + 1;
                }
            }
        }

        plugin.getLogger().info("Loaded " + getTotalWarnings() + " warnings.");
    }

    public void save() {
        warningsYaml.set("next_id", nextId);
        warningsYaml.set("warnings", null); // Clear existing

        for (List<Warning> warnings : playerWarnings.values()) {
            for (Warning warning : warnings) {
                String path = "warnings." + warning.getId();
                warningsYaml.set(path + ".target_uuid", warning.getTargetUuid().toString());
                warningsYaml.set(path + ".target_name", warning.getTargetName());
                warningsYaml.set(path + ".issuer_uuid", warning.getIssuerUuid().toString());
                warningsYaml.set(path + ".issuer_name", warning.getIssuerName());
                warningsYaml.set(path + ".reason", warning.getReason());
                warningsYaml.set(path + ".timestamp", warning.getTimestamp());
                warningsYaml.set(path + ".expires_at", warning.getExpiresAt());
                warningsYaml.set(path + ".active", warning.isActive());
                warningsYaml.set(path + ".severity", warning.getSeverity());
            }
        }

        try {
            warningsYaml.save(warningsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save warnings.yml: " + e.getMessage());
        }
    }

    /**
     * Issue a warning to a player
     */
    public Warning issueWarning(UUID targetUuid, String targetName, UUID issuerUuid, String issuerName,
                                String reason, String severity, long durationMs) {
        int id = nextId++;
        long timestamp = System.currentTimeMillis();
        long expiresAt = durationMs > 0 ? timestamp + durationMs : 0;

        Warning warning = new Warning(id, targetUuid, targetName, issuerUuid, issuerName,
                reason, timestamp, expiresAt, true, severity);

        playerWarnings.computeIfAbsent(targetUuid, k -> new ArrayList<>()).add(warning);
        save();

        // Check threshold actions
        checkThresholds(targetUuid);

        // Log audit
        plugin.getAuditLogger().log("WARNING_ISSUED", Map.of(
                "warning_id", String.valueOf(id),
                "target", targetName,
                "target_uuid", targetUuid.toString(),
                "issuer", issuerName,
                "issuer_uuid", issuerUuid.toString(),
                "reason", reason,
                "severity", severity
        ));

        return warning;
    }

    /**
     * Get all warnings for a player
     */
    public List<Warning> getWarnings(UUID playerUuid) {
        return playerWarnings.getOrDefault(playerUuid, new ArrayList<>());
    }

    /**
     * Get active warnings for a player
     */
    public List<Warning> getActiveWarnings(UUID playerUuid) {
        return getWarnings(playerUuid).stream()
                .filter(Warning::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Get warning by ID
     */
    public Warning getWarning(int id) {
        for (List<Warning> warnings : playerWarnings.values()) {
            for (Warning warning : warnings) {
                if (warning.getId() == id) {
                    return warning;
                }
            }
        }
        return null;
    }

    /**
     * Remove a warning
     */
    public boolean removeWarning(int id) {
        for (List<Warning> warnings : playerWarnings.values()) {
            Warning toRemove = null;
            for (Warning warning : warnings) {
                if (warning.getId() == id) {
                    toRemove = warning;
                    break;
                }
            }
            if (toRemove != null) {
                warnings.remove(toRemove);
                save();
                return true;
            }
        }
        return false;
    }

    /**
     * Clear all warnings for a player
     */
    public int clearWarnings(UUID playerUuid) {
        List<Warning> warnings = playerWarnings.remove(playerUuid);
        if (warnings != null) {
            int count = warnings.size();
            save();
            return count;
        }
        return 0;
    }

    /**
     * Deactivate a warning
     */
    public boolean deactivateWarning(int id) {
        Warning warning = getWarning(id);
        if (warning != null) {
            warning.setActive(false);
            save();
            return true;
        }
        return false;
    }

    /**
     * Get total warning count
     */
    public int getTotalWarnings() {
        return playerWarnings.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    /**
     * Check if warning thresholds are met and execute actions
     */
    private void checkThresholds(UUID playerUuid) {
        int activeCount = getActiveWarnings(playerUuid).size();
        Player player = Bukkit.getPlayer(playerUuid);

        // Get threshold configuration
        int warnThreshold = plugin.getConfig().getInt("warnings.thresholds.warn", 3);
        int kickThreshold = plugin.getConfig().getInt("warnings.thresholds.kick", 5);
        int tempbanThreshold = plugin.getConfig().getInt("warnings.thresholds.tempban", 7);
        int banThreshold = plugin.getConfig().getInt("warnings.thresholds.ban", 10);

        if (activeCount >= banThreshold) {
            executeAction(player, "ban", "Exceeded warning threshold (" + activeCount + " warnings)");
        } else if (activeCount >= tempbanThreshold) {
            executeAction(player, "tempban", "Exceeded warning threshold (" + activeCount + " warnings)");
        } else if (activeCount >= kickThreshold) {
            executeAction(player, "kick", "Exceeded warning threshold (" + activeCount + " warnings)");
        } else if (activeCount >= warnThreshold) {
            if (player != null && player.isOnline()) {
                player.sendMessage("§c§lWARNING: You have " + activeCount + " active warnings! Further violations may result in punishment.");
            }
        }
    }

    private void executeAction(Player player, String action, String reason) {
        if (player == null || !player.isOnline()) return;

        String command = plugin.getConfig().getString("warnings.actions." + action, "");
        if (command.isEmpty()) {
            // Default actions
            switch (action) {
                case "kick":
                    player.kickPlayer(reason);
                    break;
                case "tempban":
                    // Try to use external ban plugin
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            "tempban " + player.getName() + " 1d " + reason);
                    break;
                case "ban":
                    // Try to use external ban plugin
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            "ban " + player.getName() + " " + reason);
                    break;
            }
        } else {
            // Execute custom command
            command = command
                    .replace("{player}", player.getName())
                    .replace("{reason}", reason);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    /**
     * Clean up expired warnings
     */
    public int cleanupExpired() {
        int removed = 0;
        for (List<Warning> warnings : playerWarnings.values()) {
            Iterator<Warning> it = warnings.iterator();
            while (it.hasNext()) {
                Warning warning = it.next();
                if (warning.hasExpired()) {
                    it.remove();
                    removed++;
                }
            }
        }
        if (removed > 0) {
            save();
        }
        return removed;
    }
}
