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
            sender.sendMessage(plugin.getConfigManager().getMessage("reload_no_permission", "No permission."));
            return true;
        }
        plugin.getMessageManager().reload();
        plugin.getConfigManager().reload();
        plugin.reloadConfigDrivenServices();
        sender.sendMessage(plugin.getConfigManager().getMessage("reload_success", "MineStaff reloaded."));
        return true;
    }
}
