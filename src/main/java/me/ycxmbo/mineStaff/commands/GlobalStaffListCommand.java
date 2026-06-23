package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.crossserver.GlobalStaffList;
import me.ycxmbo.mineStaff.managers.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class GlobalStaffListCommand implements CommandExecutor {
    private final MineStaff plugin;

    public GlobalStaffListCommand(MineStaff plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        if (!sender.hasPermission("staffmode.stafflist.global")) {
            sender.sendMessage(cfg.getMessage("globalstafflist_no_permission", "No permission."));
            return true;
        }

        var globalStaffList = plugin.getGlobalStaffList();
        if (globalStaffList == null) {
            sender.sendMessage(cfg.getMessage("globalstafflist_disabled", "Cross-server features are not enabled."));
            return true;
        }

        Map<String, GlobalStaffList.ServerStaffData> networkStaff = globalStaffList.getNetworkStaff();
        if (networkStaff.isEmpty()) {
            sender.sendMessage(cfg.getMessage("globalstafflist_no_data", "No network data available yet."));
            return true;
        }

        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage("§6§lGlobal Staff List");
        sender.sendMessage(cfg.getMessage("globalstafflist_summary", "Total: {total} | In Staff Mode: {staffmode}")
                .replace("{total}", String.valueOf(globalStaffList.getTotalStaffCount()))
                .replace("{staffmode}", String.valueOf(globalStaffList.getStaffModeCount())));
        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage("");

        for (Map.Entry<String, GlobalStaffList.ServerStaffData> entry : networkStaff.entrySet()) {
            String server = entry.getKey();
            GlobalStaffList.ServerStaffData data = entry.getValue();
            if (data.staff.isEmpty()) continue;

            sender.sendMessage("§e§l" + server + " §7(" + data.staff.size() + ")");
            for (GlobalStaffList.StaffMember member : data.staff) {
                StringBuilder status = new StringBuilder();
                if (member.inStaffMode) status.append("§a[SM]");
                if (member.vanished) status.append("§7[V]");
                String statusStr = status.length() > 0 ? status + " " : "";
                sender.sendMessage("  §7• §f" + member.name + " " + statusStr);
            }
            sender.sendMessage("");
        }

        sender.sendMessage(cfg.getMessage("globalstafflist_legend", "Legend: [SM] = Staff Mode, [V] = Vanished"));
        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        return true;
    }
}
