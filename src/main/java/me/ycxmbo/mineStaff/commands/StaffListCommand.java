package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import org.bukkit.Bukkit;
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
        String list = Bukkit.getOnlinePlayers().stream()
                .filter(staff::isStaffMode)
                .map(Player::getName)
                .collect(Collectors.joining(", "));
        sender.sendMessage("Staff in StaffMode: " + (list.isEmpty() ? "none" : list));
        return true;
    }
}
