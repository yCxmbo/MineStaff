package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.util.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class StaffChatCommand implements CommandExecutor, TabExecutor {

    private final MineStaff plugin;
    private final ConfigManager configManager;
    private final Set<UUID> toggledPlayers = new HashSet<>();

    public StaffChatCommand(MineStaff plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    public boolean isToggled(UUID playerUUID) {
        return toggledPlayers.contains(playerUUID);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + configManager.getMessage("only_players", "Only players can use this command."));
            return true;
        }

        if (!player.hasPermission("staffmode.use")) {
            player.sendMessage(ChatColor.RED + configManager.getMessage("no_permission", "You do not have permission."));
            return true;
        }

        if (args.length == 0) {
            // Toggle staff chat on/off
            if (toggledPlayers.contains(player.getUniqueId())) {
                toggledPlayers.remove(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + configManager.getMessage("staffchat_disabled", "Staff chat disabled."));
            } else {
                toggledPlayers.add(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + configManager.getMessage("staffchat_enabled", "Staff chat enabled."));
            }
            return true;
        }

        // Send message to staff chat
        String message = String.join(" ", args);
        sendStaffChatMessage(player, message);
        return true;
    }

    private void sendStaffChatMessage(Player sender, String message) {
        String format = configManager.getMessage("staff-chat.format", "&8[&bStaff&8] &7%player%: %message%");
        String formattedMessage = ChatColor.translateAlternateColorCodes('&',
                format.replace("%player%", sender.getName())
                        .replace("%message%", message));

        plugin.getServer().getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("staffmode.use"))
                .forEach(p -> p.sendMessage(formattedMessage));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
