package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.managers.StaffDutyManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffDutyCommand implements CommandExecutor {
    private final StaffDutyManager duty;

    public StaffDutyCommand(StaffDutyManager dutyManager) { this.duty = dutyManager; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Only players."); return true; }
        if (!p.hasPermission("staffmode.duty")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
        boolean now = !duty.isOnDuty(p);
        if (now) {
            duty.enterDuty(p);
            p.sendMessage(ChatColor.AQUA + "You are now ON duty.");
        } else {
            duty.exitDuty(p);
            p.sendMessage(ChatColor.GRAY + "You are now OFF duty.");
        }
        return true;
    }
}

