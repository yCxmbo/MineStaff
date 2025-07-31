package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ActionLogger;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import me.ycxmbo.mineStaff.util.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffInspectCommand implements CommandExecutor {

    private final MineStaff plugin;
    private final StaffDataManager dataManager;
    private final ActionLogger logger;
    private final ConfigManager configManager;

    public StaffInspectCommand(MineStaff plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getStaffDataManager();
        this.logger = plugin.getActionLogger();
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player staff)) {
            sender.sendMessage(ChatColor.RED + configManager.getMessage("only_players", "Only players can use this command."));
            return true;
        }

        if (!staff.hasPermission("staffmode.use")) {
            staff.sendMessage(ChatColor.RED + configManager.getMessage("no_permission", "You don't have permission to use this command."));
            return true;
        }

        if (!dataManager.isInStaffMode(staff.getUniqueId())) {
            staff.sendMessage(ChatColor.translateAlternateColorCodes('&', configManager.getMessage("staffmode_required", "&cYou must be in Staff Mode to inspect players.")));
            return true;
        }

        if (args.length != 1) {
            staff.sendMessage(ChatColor.translateAlternateColorCodes('&', configManager.getMessage("staffinspect_usage", "&eUsage: /" + label + " <player>")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            staff.sendMessage(ChatColor.RED + configManager.getMessage("player_not_found", "Player not found."));
            return true;
        }

        plugin.getInspectorGUI().openInspectorGUI(staff, target);
        logger.logInspect(staff, target);
        return true;
    }
}
