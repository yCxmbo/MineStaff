package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FollowCommand implements CommandExecutor {
    private final MineStaff plugin;

    public FollowCommand(MineStaff plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("only_players", "Only players can use this."));
            return true;
        }

        if (!player.hasPermission("staffmode.follow")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no_permission", "You don't have permission."));
            return true;
        }

        // Check if staff mode is enabled
        if (!plugin.getStaffDataManager().isStaffMode(player)) {
            player.sendMessage(Component.text("You must be in staff mode to use follow mode!", NamedTextColor.RED));
            return true;
        }

        // Toggle off if no args and already following
        if (args.length == 0) {
            if (plugin.getFollowManager().isFollowing(player)) {
                plugin.getFollowManager().stopFollowing(player);
                player.sendMessage(Component.text("Follow mode disabled.", NamedTextColor.GREEN));
                return true;
            } else {
                player.sendMessage(Component.text("Usage: /follow <player>", NamedTextColor.RED));
                return true;
            }
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            player.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(Component.text("You cannot follow yourself!", NamedTextColor.RED));
            return true;
        }

        // Check if target has bypass permission
        if (target.hasPermission("staffmode.follow.bypass")) {
            player.sendMessage(Component.text("You cannot follow this player!", NamedTextColor.RED));
            return true;
        }

        // Start following
        boolean success = plugin.getFollowManager().startFollowing(player, target);

        if (success) {
            player.sendMessage(Component.text("Now following ", NamedTextColor.GREEN)
                    .append(Component.text(target.getName(), NamedTextColor.YELLOW))
                    .append(Component.text(". Use ", NamedTextColor.GREEN))
                    .append(Component.text("/follow", NamedTextColor.YELLOW))
                    .append(Component.text(" again to stop.", NamedTextColor.GREEN)));

            // Log the action
            plugin.getActionLogger().log(player, "FOLLOW_START", "Target: " + target.getName());
        } else {
            player.sendMessage(Component.text("Failed to start follow mode!", NamedTextColor.RED));
        }

        return true;
    }
}
