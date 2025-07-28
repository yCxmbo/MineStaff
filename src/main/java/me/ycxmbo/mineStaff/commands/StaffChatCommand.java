package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class StaffChatCommand implements CommandExecutor {

    private final Set<UUID> toggledPlayers = new HashSet<>();
    private final MineStaff plugin;

    public StaffChatCommand(MineStaff plugin) {
        this.plugin = plugin;
    }

    public boolean isToggled(UUID uuid) {
        return toggledPlayers.contains(uuid);
    }

    public void toggle(UUID uuid) {
        if (toggledPlayers.contains(uuid)) {
            toggledPlayers.remove(uuid);
        } else {
            toggledPlayers.add(uuid);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("staffmode.chat")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use staff chat.");
            return true;
        }

        if (args.length == 0) {
            toggle(player.getUniqueId());
            player.sendMessage(ChatColor.YELLOW + "Staff Chat toggled " +
                    (isToggled(player.getUniqueId()) ? ChatColor.GREEN + "on" : ChatColor.RED + "off"));
            return true;
        }

        // Send message
        String msg = String.join(" ", args);
        String format = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("staff-chat.format", "&8[&bStaff&8] &7%player%: %
