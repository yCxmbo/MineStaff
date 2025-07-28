package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
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

    public StaffListCommand(MineStaff plugin) {
        this.plugin = plugin;
        this.staffDataManager = plugin.getStaffDataManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("staffmode.list")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        var staffUuids = staffDataManager.getStaffMap().keySet();

        if (staffUuids.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No players are currently in Staff Mode.");
            return true;
        }

        String names = staffUuids.stream()
                .map(uuid -> plugin.getServer().getPlayer(uuid))
                .filter(player -> player != null)
                .map(Player::getName)
                .collect(Collectors.joining(", "));

        sender.sendMessage(ChatColor.GREEN + "Players in Staff Mode (" + staffUuids.size() + "): " + ChatColor.WHITE + names);
        return true;
    }
}
