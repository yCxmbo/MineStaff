package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.channels.StaffChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ChannelCommand implements CommandExecutor {
    private final MineStaff plugin;

    public ChannelCommand(MineStaff plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("only_players", "Only players can use this."));
            return true;
        }

        if (!player.hasPermission("staffmode.channel")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no_permission", "You don't have permission."));
            return true;
        }

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "list":
                listChannels(player);
                break;
            case "join":
            case "switch":
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage: /channel join <channel>", NamedTextColor.RED));
                } else {
                    switchChannel(player, args[1]);
                }
                break;
            case "toggle":
                toggleChannelMode(player);
                break;
            case "info":
                showChannelInfo(player);
                break;
            default:
                // Assume it's a message if channel mode is on, otherwise show help
                if (plugin.getChannelManager().isInChannelMode(player)) {
                    sendMessage(player, String.join(" ", args));
                } else {
                    showHelp(player);
                }
                break;
        }

        return true;
    }

    private void showHelp(Player player) {
        player.sendMessage(Component.text("═══════════════════════════════", NamedTextColor.GOLD));
        player.sendMessage(Component.text(" Staff Channels", NamedTextColor.YELLOW, TextDecoration.BOLD));
        player.sendMessage(Component.text("═══════════════════════════════", NamedTextColor.GOLD));
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("/channel list", NamedTextColor.YELLOW)
                .append(Component.text(" - List all channels", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/channel join <name>", NamedTextColor.YELLOW)
                .append(Component.text(" - Switch to a channel", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/channel toggle", NamedTextColor.YELLOW)
                .append(Component.text(" - Toggle channel mode", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/channel info", NamedTextColor.YELLOW)
                .append(Component.text(" - Show current channel", NamedTextColor.GRAY)));
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("When channel mode is ON, all messages go to the active channel.", NamedTextColor.DARK_GRAY, TextDecoration.ITALIC));
    }

    private void listChannels(Player player) {
        List<StaffChannel> accessible = plugin.getChannelManager().getAccessibleChannels(player);
        StaffChannel active = plugin.getChannelManager().getActiveChannel(player);

        player.sendMessage(Component.text("═══════════════════════════════", NamedTextColor.GOLD));
        player.sendMessage(Component.text(" Available Channels (" + accessible.size() + ")", NamedTextColor.YELLOW, TextDecoration.BOLD));
        player.sendMessage(Component.text("═══════════════════════════════", NamedTextColor.GOLD));

        if (accessible.isEmpty()) {
            player.sendMessage(Component.text("No channels available.", NamedTextColor.GRAY));
            return;
        }

        for (StaffChannel channel : accessible) {
            boolean isActive = active != null && active.getId().equals(channel.getId());

            Component line = Component.text()
                    .append(Component.text(isActive ? " » " : "   ", isActive ? NamedTextColor.GREEN : NamedTextColor.DARK_GRAY))
                    .append(Component.text(channel.getDisplayName()))
                    .append(Component.text(" (" + channel.getName() + ")", NamedTextColor.GRAY))
                    .clickEvent(ClickEvent.runCommand("/channel join " + channel.getId()))
                    .hoverEvent(HoverEvent.showText(
                            Component.text("Click to join ", NamedTextColor.GREEN)
                                    .append(Component.text(channel.getDisplayName()))))
                    .build();

            player.sendMessage(line);

            if (isActive) {
                player.sendMessage(Component.text("     Currently active", NamedTextColor.DARK_GRAY, TextDecoration.ITALIC));
            }
        }

        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("Click a channel to join it", NamedTextColor.DARK_GRAY, TextDecoration.ITALIC));
    }

    private void switchChannel(Player player, String channelId) {
        boolean success = plugin.getChannelManager().setActiveChannel(player, channelId);

        if (success) {
            StaffChannel channel = plugin.getChannelManager().getChannel(channelId);
            player.sendMessage(Component.text("✓ Switched to channel: ", NamedTextColor.GREEN)
                    .append(Component.text(channel.getDisplayName())));

            // Auto-enable channel mode if not already on
            if (!plugin.getChannelManager().isInChannelMode(player)) {
                player.sendMessage(Component.text("Tip: Use ", NamedTextColor.GRAY)
                        .append(Component.text("/channel toggle", NamedTextColor.YELLOW))
                        .append(Component.text(" to enable channel mode", NamedTextColor.GRAY)));
            }
        } else {
            player.sendMessage(Component.text("✗ Channel not found or no access!", NamedTextColor.RED));
        }
    }

    private void toggleChannelMode(Player player) {
        plugin.getChannelManager().toggleChannelMode(player);
        boolean isOn = plugin.getChannelManager().isInChannelMode(player);

        if (isOn) {
            StaffChannel active = plugin.getChannelManager().getActiveChannel(player);
            if (active != null) {
                player.sendMessage(Component.text("✓ Channel mode enabled for: ", NamedTextColor.GREEN)
                        .append(Component.text(active.getDisplayName())));
                player.sendMessage(Component.text("All messages will now go to this channel.", NamedTextColor.GRAY));
            } else {
                player.sendMessage(Component.text("✗ No active channel set!", NamedTextColor.RED));
                player.sendMessage(Component.text("Use /channel join <name> first.", NamedTextColor.GRAY));
                plugin.getChannelManager().toggleChannelMode(player); // Toggle back off
            }
        } else {
            player.sendMessage(Component.text("✗ Channel mode disabled.", NamedTextColor.YELLOW));
        }
    }

    private void showChannelInfo(Player player) {
        StaffChannel active = plugin.getChannelManager().getActiveChannel(player);
        boolean channelMode = plugin.getChannelManager().isInChannelMode(player);

        player.sendMessage(Component.text("═══════════════════════════════", NamedTextColor.GOLD));
        player.sendMessage(Component.text(" Channel Info", NamedTextColor.YELLOW, TextDecoration.BOLD));
        player.sendMessage(Component.text("═══════════════════════════════", NamedTextColor.GOLD));

        if (active != null) {
            player.sendMessage(Component.text("Active Channel: ", NamedTextColor.GRAY)
                    .append(Component.text(active.getDisplayName())));
            player.sendMessage(Component.text("Channel ID: ", NamedTextColor.GRAY)
                    .append(Component.text(active.getId(), NamedTextColor.YELLOW)));
            player.sendMessage(Component.text("Cross-Server: ", NamedTextColor.GRAY)
                    .append(Component.text(active.isCrossServer() ? "Yes" : "No",
                            active.isCrossServer() ? NamedTextColor.GREEN : NamedTextColor.RED)));
        } else {
            player.sendMessage(Component.text("No active channel", NamedTextColor.GRAY));
        }

        player.sendMessage(Component.text("Channel Mode: ", NamedTextColor.GRAY)
                .append(Component.text(channelMode ? "ON" : "OFF",
                        channelMode ? NamedTextColor.GREEN : NamedTextColor.RED)));
    }

    private void sendMessage(Player player, String message) {
        StaffChannel active = plugin.getChannelManager().getActiveChannel(player);
        if (active == null) {
            player.sendMessage(Component.text("No active channel! Use /channel join <name>", NamedTextColor.RED));
            return;
        }

        plugin.getChannelManager().sendMessage(player, active, message);
    }
}
