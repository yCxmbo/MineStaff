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
    private final MineStaff plugin;
    private final StaffDataManager staff;

    public StaffListCommand(MineStaff plugin) {
        this.plugin = plugin;
        this.staff = plugin.getStaffDataManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player && !player.hasPermission("staffmode.stafflist.text")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no_permission", "You don't have permission."));
            return true;
        }

        String list = Bukkit.getOnlinePlayers().stream()
                .filter(staff::isStaffMode)
                .map(Player::getName)
                .collect(Collectors.joining(", "));

        String empty = plugin.getConfigManager().getMessage("stafflist_empty", "&7none");
        String msg = plugin.getConfigManager().getMessage("stafflist_header", "&b► Online staff in Staff Mode: &f{list}")
                .replace("{list}", list.isEmpty() ? empty : list);
        sender.sendMessage(msg);
        return true;
    }
}
