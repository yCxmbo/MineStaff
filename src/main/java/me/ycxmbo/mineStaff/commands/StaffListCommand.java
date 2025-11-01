package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

public class StaffListCommand implements CommandExecutor {
    private final StaffDataManager staff;

    public StaffListCommand(MineStaff plugin) {
        this.staff = plugin.getStaffDataManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player && !player.hasPermission("staffmode.stafflist.text")) {
            player.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        String list = Bukkit.getOnlinePlayers().stream()
                .filter(staff::isStaffMode)
                .map(Player::getName)
                .collect(Collectors.joining(", "));
        sender.sendMessage(ChatColor.AQUA + "Staff in Staff Mode: " + (list.isEmpty() ? ChatColor.GRAY + "none" : ChatColor.YELLOW + list));
        return true;
    }
}
