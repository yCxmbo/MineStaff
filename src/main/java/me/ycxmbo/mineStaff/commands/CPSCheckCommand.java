package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.CPSManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CPSCheckCommand implements CommandExecutor {

    private final MineStaff plugin;
    private final CPSManager cpsManager;

    public CPSCheckCommand(MineStaff plugin) {
        this.plugin = plugin;
        this.cpsManager = plugin.getCPSManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player staff)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (!staff.hasPermission("staffmode.cpscheck")) {
            staff.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (args.length != 1) {
            staff.sendMessage("§cUsage: /cpscheck <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            staff.sendMessage("§cPlayer not found.");
            return true;
        }

        cpsManager.startTest(staff, target);

        return true;
    }
}
