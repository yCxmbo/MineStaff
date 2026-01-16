package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.crossserver.GlobalStaffList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Command to view network-wide staff list
 */
public class GlobalStaffListCommand implements CommandExecutor {
    private final MineStaff plugin;
    
    public GlobalStaffListCommand(MineStaff plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("staffmode.stafflist.global")) {
            sender.sendMessage("§cYou don't have permission to view the global staff list!");
            return true;
        }
        
        var globalStaffList = plugin.getGlobalStaffList();
        if (globalStaffList == null) {
            sender.sendMessage("§cCross-server features are not enabled!");
            return true;
        }
        
        Map<String, GlobalStaffList.ServerStaffData> networkStaff = globalStaffList.getNetworkStaff();
        
        if (networkStaff.isEmpty()) {
            sender.sendMessage("§cNo network data available yet. Please wait a few seconds...");
            return true;
        }
        
        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage("§6§lGlobal Staff List");
        sender.sendMessage("§7Total: §f" + globalStaffList.getTotalStaffCount() + " §7| In Staff Mode: §a" + globalStaffList.getStaffModeCount());
        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage("");
        
        for (Map.Entry<String, GlobalStaffList.ServerStaffData> entry : networkStaff.entrySet()) {
            String server = entry.getKey();
            GlobalStaffList.ServerStaffData data = entry.getValue();
            
            if (data.staff.isEmpty()) continue;
            
            sender.sendMessage("§e§l" + server + " §7(" + data.staff.size() + ")");
            
            for (GlobalStaffList.StaffMember member : data.staff) {
                StringBuilder status = new StringBuilder();
                
                if (member.inStaffMode) {
                    status.append("§a[SM]");
                }
                if (member.vanished) {
                    status.append("§7[V]");
                }
                
                String statusStr = status.length() > 0 ? status + " " : "";
                sender.sendMessage("  §7• §f" + member.name + " " + statusStr);
            }
            
            sender.sendMessage("");
        }
        
        sender.sendMessage("§7§oLegend: §a[SM] §7= Staff Mode, §7[V] §7= Vanished");
        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        return true;
    }
}
