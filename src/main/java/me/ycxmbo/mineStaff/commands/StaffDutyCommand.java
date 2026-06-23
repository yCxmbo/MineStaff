package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ConfigManager;
import me.ycxmbo.mineStaff.managers.StaffDutyManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffDutyCommand implements CommandExecutor {
    private final StaffDutyManager duty;
    private final ConfigManager cfg;

    public StaffDutyCommand(MineStaff plugin) {
        this.duty = plugin.getStaffDutyManager();
        this.cfg = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage(cfg.getMessage("only_players", "Only players can use this.")); return true; }
        if (!p.hasPermission("staffmode.duty")) { p.sendMessage(cfg.getMessage("no_permission", "You don't have permission.")); return true; }
        boolean now = !duty.isOnDuty(p);
        if (now) {
            duty.enterDuty(p);
            p.sendMessage(cfg.getMessage("duty_on", "You are now ON duty."));
        } else {
            duty.exitDuty(p);
            p.sendMessage(cfg.getMessage("duty_off", "You are now off duty."));
        }
        return true;
    }
}

