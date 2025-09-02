package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FreezeCommand implements CommandExecutor {
    private final StaffDataManager staff;
    private final MineStaff plugin;

    public FreezeCommand(MineStaff plugin) {
        this.plugin = plugin;
        this.staff = plugin.getStaffDataManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(ChatColor.RED + "Only players.");
            return true;
        }
        if (!p.hasPermission("staffmode.freeze")) {
            p.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (args.length < 1) {
            p.sendMessage(ChatColor.YELLOW + "Usage: /freeze <player>");
            return true;
        }
        Player t = Bukkit.getPlayerExact(args[0]);
        if (t == null) {
            p.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }
        boolean newState = !staff.isFrozen(t);
        staff.setFrozen(t, newState);
        p.sendMessage(ChatColor.YELLOW + "Player " + t.getName() + " " + (newState ? "frozen." : "unfrozen."));
        if (newState) {
            t.sendMessage(ChatColor.RED + "You have been frozen by staff. Do not log out.");
        }
        MineStaff.getInstance().getAuditLogger().log(java.util.Map.of(
                "type","freeze","actor",p.getUniqueId().toString(),
                "target",t.getUniqueId().toString(),
                "state",String.valueOf(newState)
        ));
        plugin.getActionLogger().logCommand(p, "Freeze " + t.getName() + " -> " + newState);
        return true;
    }
}
