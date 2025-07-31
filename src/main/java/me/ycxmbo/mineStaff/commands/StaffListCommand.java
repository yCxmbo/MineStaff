package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import me.ycxmbo.mineStaff.util.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.stream.Collectors;

public class StaffListCommand implements CommandExecutor {

    private final MineStaff plugin;
    private final StaffDataManager staffDataManager;
    private final ConfigManager configManager;

    public StaffListCommand(MineStaff plugin) {
        this.plugin = plugin;
        this.staffDataManager = plugin.getStaffDataManager();
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("staffmode.list")) {
            sender.sendMessage(ChatColor.RED + configManager.getMessage("no_permission", "You do not have permission to use this command."));
            return true;
        }

        var staffUuids = staffDataManager.getStaffMap().keySet();

        if (staffUuids.isEmpty()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', configManager.getMessage("messages.no_staff_online", "&eNo players are currently in Staff Mode.")));
            return true;
        }

        String names = staffUuids.stream()
                .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                configManager.getMessage("messages.staff_list", "&6Players in Staff Mode (%count%): &f%names%")
                        .replace("%count%", String.valueOf(staffUuids.size()))
                        .replace("%names%", names)));
        return true;
    }
}
