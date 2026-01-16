package me.ycxmbo.mineStaff.channels;

import me.ycxmbo.mineStaff.MineStaff;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages private staff channels
 */
public class ChannelManager {
    private final MineStaff plugin;
    private final Map<String, StaffChannel> channels = new ConcurrentHashMap<>();
    private final Map<UUID, String> activeChannels = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> channelMode = new ConcurrentHashMap<>();

    public ChannelManager(MineStaff plugin) {
        this.plugin = plugin;
        loadChannels();
    }

    /**
     * Load channels from configuration
     */
    private void loadChannels() {
        channels.clear();

        // Check if channels feature is enabled
        if (!plugin.getConfig().getBoolean("channels.enabled", true)) {
            plugin.getLogger().info("Staff channels are disabled.");
            return;
        }

        ConfigurationSection channelsSection = plugin.getConfig().getConfigurationSection("channels.default_channels");
        if (channelsSection == null) {
            // Create default channels
            createDefaultChannels();
            return;
        }

        for (String key : channelsSection.getKeys(false)) {
            String path = "channels.default_channels." + key;
            String name = plugin.getConfig().getString(path + ".name", key);
            String displayName = plugin.getConfig().getString(path + ".display_name", name);
            String format = plugin.getConfig().getString(path + ".format", "&8[&d{channel}&8] &b{name}&7: &f{message}");
            String permission = plugin.getConfig().getString(path + ".permission", "staffmode.channel." + key);
            boolean crossServer = plugin.getConfig().getBoolean(path + ".cross_server", false);

            StaffChannel channel = new StaffChannel(key, name, displayName, format, permission, crossServer);
            channels.put(key, channel);
        }

        plugin.getLogger().info("Loaded " + channels.size() + " staff channel(s).");
    }

    /**
     * Create default channels
     */
    private void createDefaultChannels() {
        // Admin channel
        StaffChannel admin = new StaffChannel(
                "admin",
                "Admin",
                "§cAdmin",
                "&8[&cAdmin&8] &b{name}&7: &f{message}",
                "staffmode.channel.admin",
                true
        );
        channels.put("admin", admin);

        // Mod channel
        StaffChannel mod = new StaffChannel(
                "mod",
                "Mod",
                "§eMod",
                "&8[&eMod&8] &b{name}&7: &f{message}",
                "staffmode.channel.mod",
                true
        );
        channels.put("mod", mod);

        // Staff (general) channel
        StaffChannel staff = new StaffChannel(
                "staff",
                "Staff",
                "§dStaff",
                "&8[&dStaff&8] &b{name}&7: &f{message}",
                "staffmode.channel.staff",
                true
        );
        channels.put("staff", staff);

        plugin.getLogger().info("Created default staff channels.");
    }

    /**
     * Get a channel by ID
     */
    public StaffChannel getChannel(String id) {
        return channels.get(id.toLowerCase());
    }

    /**
     * Get all channels
     */
    public Collection<StaffChannel> getAllChannels() {
        return new ArrayList<>(channels.values());
    }

    /**
     * Get channels a player has access to
     */
    public List<StaffChannel> getAccessibleChannels(Player player) {
        List<StaffChannel> accessible = new ArrayList<>();
        for (StaffChannel channel : channels.values()) {
            if (player.hasPermission(channel.getPermission())) {
                accessible.add(channel);
            }
        }
        return accessible;
    }

    /**
     * Get player's active channel
     */
    public StaffChannel getActiveChannel(Player player) {
        String channelId = activeChannels.get(player.getUniqueId());
        if (channelId == null) {
            // Default to first accessible channel
            List<StaffChannel> accessible = getAccessibleChannels(player);
            if (!accessible.isEmpty()) {
                return accessible.get(0);
            }
            return null;
        }
        return getChannel(channelId);
    }

    /**
     * Set player's active channel
     */
    public boolean setActiveChannel(Player player, String channelId) {
        StaffChannel channel = getChannel(channelId);
        if (channel == null) {
            return false;
        }

        if (!player.hasPermission(channel.getPermission())) {
            return false;
        }

        activeChannels.put(player.getUniqueId(), channelId);
        return true;
    }

    /**
     * Check if player is in channel mode
     */
    public boolean isInChannelMode(Player player) {
        return channelMode.getOrDefault(player.getUniqueId(), false);
    }

    /**
     * Toggle channel mode for a player
     */
    public void toggleChannelMode(Player player) {
        boolean current = isInChannelMode(player);
        channelMode.put(player.getUniqueId(), !current);
    }

    /**
     * Send message to a channel
     */
    public void sendMessage(Player sender, StaffChannel channel, String message) {
        if (channel == null) {
            sender.sendMessage("§cNo active channel set!");
            return;
        }

        if (!sender.hasPermission(channel.getPermission())) {
            sender.sendMessage("§cYou don't have access to this channel!");
            return;
        }

        // Format message
        String formatted = channel.getFormat()
                .replace("{channel}", channel.getDisplayName())
                .replace("{name}", sender.getName())
                .replace("{message}", message);

        // Convert color codes
        formatted = formatted.replace('&', '§');

        // Send to local players
        Component messageComponent = LegacyComponentSerializer.legacySection().deserialize(formatted);
        for (Player recipient : Bukkit.getOnlinePlayers()) {
            if (recipient.hasPermission(channel.getPermission())) {
                recipient.sendMessage(messageComponent);
            }
        }

        // Send to console
        plugin.getLogger().info("[Channel:" + channel.getName() + "] " + sender.getName() + ": " + message);

        // Cross-server if enabled
        if (channel.isCrossServer()) {
            try {
                if (plugin.getRedisBridge() != null) {
                    plugin.getRedisBridge().publishChannelMessage(channel.getId(), sender.getName(), message);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send cross-server channel message: " + e.getMessage());
            }
        }

        // Play sound
        plugin.getSoundManager().playSound(sender, "staffchat.message");
    }

    /**
     * Handle cross-server channel message
     */
    public void handleCrossServerMessage(String channelId, String senderName, String message) {
        StaffChannel channel = getChannel(channelId);
        if (channel == null) return;

        String formatted = channel.getFormat()
                .replace("{channel}", channel.getDisplayName())
                .replace("{name}", senderName + " §7[Remote]")
                .replace("{message}", message);

        formatted = formatted.replace('&', '§');

        Component messageComponent = LegacyComponentSerializer.legacySection().deserialize(formatted);
        for (Player recipient : Bukkit.getOnlinePlayers()) {
            if (recipient.hasPermission(channel.getPermission())) {
                recipient.sendMessage(messageComponent);
            }
        }
    }

    /**
     * Leave channel (cleanup)
     */
    public void leaveChannel(Player player) {
        activeChannels.remove(player.getUniqueId());
        channelMode.remove(player.getUniqueId());
    }

    /**
     * Reload channels from config
     */
    public void reload() {
        loadChannels();
    }
}
