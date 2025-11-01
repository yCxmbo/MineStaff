package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StaffReloadCommand implements CommandExecutor {
    private final MineStaff plugin;
    public StaffReloadCommand(MineStaff plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("staffmode.reload")) {
            sender.sendMessage("No permission.");
            return true;
        }
        plugin.getConfigManager().reload();
        plugin.reloadConfigDrivenServices();
        sender.sendMessage("MineStaff reloaded.");
        return true;
    }
}
