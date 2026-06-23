package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ConfigManager;
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
        ConfigManager cfg = plugin.getConfigManager();
        if (!(sender instanceof Player player)) {
            sender.sendMessage(cfg.getMessage("only_players", "Only players can use this."));
            return true;
        }
        if (!player.hasPermission("staffmode.follow")) {
            player.sendMessage(cfg.getMessage("no_permission", "No permission."));
            return true;
        }
        if (!plugin.getStaffDataManager().isStaffMode(player)) {
            player.sendMessage(cfg.getMessage("follow_must_be_staffmode", "You must be in Staff Mode to use follow mode."));
            return true;
        }

        if (args.length == 0) {
            if (plugin.getFollowManager().isFollowing(player)) {
                plugin.getFollowManager().stopFollowing(player);
                player.sendMessage(cfg.getMessage("follow_disabled_self", "Follow mode disabled."));
                return true;
            } else {
                player.sendMessage(cfg.getMessage("follow_usage", "Usage: /follow <player>"));
                return true;
            }
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(cfg.getMessage("player_not_found", "Player not found."));
            return true;
        }
        if (target.equals(player)) {
            player.sendMessage(cfg.getMessage("follow_self", "You cannot follow yourself."));
            return true;
        }
        if (target.hasPermission("staffmode.follow.bypass")) {
            player.sendMessage(cfg.getMessage("follow_target_bypass", "You cannot follow that player."));
            return true;
        }

        boolean success = plugin.getFollowManager().startFollowing(player, target);
        if (success) {
            player.sendMessage(cfg.getMessage("follow_started", "Now following {target}. Use /follow again to stop.")
                    .replace("{target}", target.getName()));
            plugin.getActionLogger().log(player, "FOLLOW_START", "Target: " + target.getName());
        } else {
            player.sendMessage(cfg.getMessage("follow_failed", "Failed to start follow mode."));
        }
        return true;
    }
}
