package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.CPSManager;
import me.ycxmbo.mineStaff.util.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CPSCheckCommand implements CommandExecutor {

    private final MineStaff plugin;
    private final CPSManager cpsManager;
    private final ConfigManager configManager;

    public CPSCheckCommand(MineStaff plugin) {
        this.plugin = plugin;
        this.cpsManager = plugin.getCPSManager();
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player staff)) {
            sender.sendMessage(ChatColor.RED + configManager.getMessage("only_players", "Only players can use this command."));
            return true;
        }

        if (!staff.hasPermission("staffmode.cpscheck")) {
            staff.sendMessage(ChatColor.RED + configManager.getMessage("no_permission", "You do not have permission to use this command."));
            return true;
        }

        if (args.length != 1) {
            staff.sendMessage(ChatColor.RED + configManager.getMessage("staffchat_usage", "Usage: /cpscheck <player>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            staff.sendMessage(ChatColor.RED + configManager.getMessage("player_not_found", "Player not found."));
            return true;
        }

        cpsManager.startTest(staff, target);
        return true;
    }
}
