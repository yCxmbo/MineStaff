package me.ycxmbo.mineStaff.integrations;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Integration with CoreProtect for block logging and rollback
 * Uses reflection to avoid hard dependency on CoreProtect
 */
public class CoreProtectIntegration {
    private final MineStaff plugin;
    private Object coreProtectAPI;
    private boolean enabled = false;

    // Reflected methods
    private Method performLookupMethod;
    private Method performRollbackMethod;
    private Method performRestoreMethod;
    private Method isEnabledMethod;
    private Method apiVersionMethod;

    public CoreProtectIntegration(MineStaff plugin) {
        this.plugin = plugin;
        initialize();
    }

    private void initialize() {
        Plugin coreProtectPlugin = plugin.getServer().getPluginManager().getPlugin("CoreProtect");

        if (coreProtectPlugin == null || !coreProtectPlugin.isEnabled()) {
            plugin.getLogger().info("CoreProtect not found. Block logging integration disabled.");
            return;
        }

        try {
            // Use reflection to get CoreProtect API
            Method getAPIMethod = coreProtectPlugin.getClass().getMethod("getAPI");
            Object api = getAPIMethod.invoke(coreProtectPlugin);

            // Get methods using reflection
            Class<?> apiClass = api.getClass();
            isEnabledMethod = apiClass.getMethod("isEnabled");
            apiVersionMethod = apiClass.getMethod("APIVersion");

            boolean apiEnabled = (boolean) isEnabledMethod.invoke(api);
            int apiVersion = (int) apiVersionMethod.invoke(api);

            if (apiEnabled) {
                if (apiVersion < 9) {
                    plugin.getLogger().warning("CoreProtect API version is too old. Block logging integration disabled.");
                    return;
                }

                // Cache reflection methods
                performLookupMethod = apiClass.getMethod("performLookup",
                        int.class, List.class, List.class, List.class, List.class, Location.class, int.class);
                performRollbackMethod = apiClass.getMethod("performRollback",
                        int.class, List.class, List.class, List.class, Location.class, int.class);
                performRestoreMethod = apiClass.getMethod("performRestore",
                        int.class, List.class, List.class, List.class, Location.class, int.class);

                this.coreProtectAPI = api;
                this.enabled = true;
                plugin.getLogger().info("CoreProtect integration enabled (API v" + apiVersion + ")");
            } else {
                plugin.getLogger().warning("CoreProtect API is not enabled. Block logging integration disabled.");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize CoreProtect integration: " + e.getMessage());
            this.enabled = false;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Look up block changes at a specific location
     */
    @SuppressWarnings("unchecked")
    public List<BlockChange> lookupBlock(Location location, int timeInSeconds, int radius) {
        if (!enabled) return new ArrayList<>();

        try {
            List<String[]> results = (List<String[]>) performLookupMethod.invoke(coreProtectAPI,
                    timeInSeconds,
                    null, // All users
                    null, // All blocks
                    null, // All actions
                    null, // All containers
                    location,
                    radius
            );

            List<BlockChange> changes = new ArrayList<>();
            if (results != null) {
                for (String[] result : results) {
                    BlockChange change = parseResult(result);
                    if (change != null) {
                        changes.add(change);
                    }
                }
            }

            return changes;
        } catch (Exception e) {
            plugin.getLogger().warning("CoreProtect lookup failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Look up actions by a specific player
     */
    @SuppressWarnings("unchecked")
    public List<BlockChange> lookupPlayer(String playerName, int timeInSeconds, Location center, int radius) {
        if (!enabled) return new ArrayList<>();

        try {
            List<String[]> results = (List<String[]>) performLookupMethod.invoke(coreProtectAPI,
                    timeInSeconds,
                    List.of(playerName),
                    null, // All blocks
                    null, // All actions
                    null, // All containers
                    center,
                    radius
            );

            List<BlockChange> changes = new ArrayList<>();
            if (results != null) {
                for (String[] result : results) {
                    BlockChange change = parseResult(result);
                    if (change != null) {
                        changes.add(change);
                    }
                }
            }

            return changes;
        } catch (Exception e) {
            plugin.getLogger().warning("CoreProtect player lookup failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Look up interactions with a specific block type
     */
    @SuppressWarnings("unchecked")
    public List<BlockChange> lookupBlockType(Material material, int timeInSeconds, Location center, int radius) {
        if (!enabled) return new ArrayList<>();

        try {
            List<String[]> results = (List<String[]>) performLookupMethod.invoke(coreProtectAPI,
                    timeInSeconds,
                    null, // All users
                    List.of(material),
                    null, // All actions
                    null, // All containers
                    center,
                    radius
            );

            List<BlockChange> changes = new ArrayList<>();
            if (results != null) {
                for (String[] result : results) {
                    BlockChange change = parseResult(result);
                    if (change != null) {
                        changes.add(change);
                    }
                }
            }

            return changes;
        } catch (Exception e) {
            plugin.getLogger().warning("CoreProtect block type lookup failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Perform a rollback
     */
    @SuppressWarnings("unchecked")
    public RollbackResult rollback(String playerName, int timeInSeconds, Location center, int radius) {
        if (!enabled) {
            return new RollbackResult(false, 0, "CoreProtect is not enabled");
        }

        try {
            List<String[]> rollbackResult = (List<String[]>) performRollbackMethod.invoke(coreProtectAPI,
                    timeInSeconds,
                    List.of(playerName),
                    null, // All blocks
                    null, // All actions (break, place, interact, etc)
                    center,
                    radius
            );

            if (rollbackResult != null) {
                return new RollbackResult(true, rollbackResult.size(), "Successfully rolled back " + rollbackResult.size() + " changes");
            } else {
                return new RollbackResult(false, 0, "Rollback failed");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("CoreProtect rollback failed: " + e.getMessage());
            return new RollbackResult(false, 0, "Rollback failed: " + e.getMessage());
        }
    }

    /**
     * Perform a restore (opposite of rollback)
     */
    @SuppressWarnings("unchecked")
    public RollbackResult restore(String playerName, int timeInSeconds, Location center, int radius) {
        if (!enabled) {
            return new RollbackResult(false, 0, "CoreProtect is not enabled");
        }

        try {
            List<String[]> restoreResult = (List<String[]>) performRestoreMethod.invoke(coreProtectAPI,
                    timeInSeconds,
                    List.of(playerName),
                    null, // All blocks
                    null, // All actions
                    center,
                    radius
            );

            if (restoreResult != null) {
                return new RollbackResult(true, restoreResult.size(), "Successfully restored " + restoreResult.size() + " changes");
            } else {
                return new RollbackResult(false, 0, "Restore failed");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("CoreProtect restore failed: " + e.getMessage());
            return new RollbackResult(false, 0, "Restore failed: " + e.getMessage());
        }
    }

    /**
     * Check if a block has been modified recently
     */
    @SuppressWarnings("unchecked")
    public boolean hasRecentActivity(Block block, int timeInSeconds) {
        if (!enabled) return false;

        try {
            List<String[]> results = (List<String[]>) performLookupMethod.invoke(coreProtectAPI,
                    timeInSeconds,
                    null,
                    null,
                    null,
                    null,
                    block.getLocation(),
                    0 // Exact block
            );

            return results != null && !results.isEmpty();
        } catch (Exception e) {
            plugin.getLogger().warning("CoreProtect recent activity check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get the player who last modified a block
     */
    @SuppressWarnings("unchecked")
    public String getLastModifier(Block block) {
        if (!enabled) return null;

        try {
            List<String[]> results = (List<String[]>) performLookupMethod.invoke(coreProtectAPI,
                    Integer.MAX_VALUE, // All time
                    null,
                    null,
                    null,
                    null,
                    block.getLocation(),
                    0 // Exact block
            );

            if (results != null && !results.isEmpty()) {
                // Results are ordered newest first
                String[] mostRecent = results.get(0);
                return mostRecent[1]; // Player name is index 1
            }

            return null;
        } catch (Exception e) {
            plugin.getLogger().warning("CoreProtect last modifier lookup failed: " + e.getMessage());
            return null;
        }
    }

    private BlockChange parseResult(String[] result) {
        // CoreProtect result format:
        // 0: timestamp
        // 1: player
        // 2: x
        // 3: y
        // 4: z
        // 5: block type
        // 6: block data
        // 7: action (0=break, 1=place, 2=interaction)
        // 8: rolled back (0=no, 1=yes)
        // 9: world name

        try {
            long timestamp = Long.parseLong(result[0]);
            String player = result[1];
            int x = Integer.parseInt(result[2]);
            int y = Integer.parseInt(result[3]);
            int z = Integer.parseInt(result[4]);
            String blockType = result[5];
            String blockData = result.length > 6 ? result[6] : "";
            int actionCode = result.length > 7 ? Integer.parseInt(result[7]) : 0;
            boolean rolledBack = result.length > 8 && Integer.parseInt(result[8]) == 1;
            String world = result.length > 9 ? result[9] : "world";

            String action = switch (actionCode) {
                case 0 -> "broke";
                case 1 -> "placed";
                case 2 -> "interacted";
                default -> "modified";
            };

            return new BlockChange(timestamp, player, x, y, z, world, blockType, blockData, action, rolledBack);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to parse CoreProtect result: " + e.getMessage());
            return null;
        }
    }

    /**
     * Represents a block change from CoreProtect
     */
    public static class BlockChange {
        public final long timestamp;
        public final String player;
        public final int x, y, z;
        public final String world;
        public final String blockType;
        public final String blockData;
        public final String action;
        public final boolean rolledBack;

        public BlockChange(long timestamp, String player, int x, int y, int z, String world,
                          String blockType, String blockData, String action, boolean rolledBack) {
            this.timestamp = timestamp;
            this.player = player;
            this.x = x;
            this.y = y;
            this.z = z;
            this.world = world;
            this.blockType = blockType;
            this.blockData = blockData;
            this.action = action;
            this.rolledBack = rolledBack;
        }

        public String getLocationString() {
            return String.format("(%d, %d, %d) in %s", x, y, z, world);
        }

        public String getFormattedAge() {
            long age = System.currentTimeMillis() - timestamp;
            long seconds = age / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (days > 0) return days + "d ago";
            if (hours > 0) return hours + "h ago";
            if (minutes > 0) return minutes + "m ago";
            return seconds + "s ago";
        }
    }

    /**
     * Represents the result of a rollback/restore operation
     */
    public static class RollbackResult {
        public final boolean success;
        public final int affectedBlocks;
        public final String message;

        public RollbackResult(boolean success, int affectedBlocks, String message) {
            this.success = success;
            this.affectedBlocks = affectedBlocks;
            this.message = message;
        }
    }
}
