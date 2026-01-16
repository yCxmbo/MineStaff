package me.ycxmbo.mineStaff.crossserver;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ReportManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Syncs reports across network servers
 */
public class NetworkReportSync {
    private final MineStaff plugin;
    private final String serverName;
    
    public NetworkReportSync(MineStaff plugin) {
        this.plugin = plugin;
        this.serverName = plugin.getConfig().getString("server-name", "unknown");
    }
    
    /**
     * Broadcast a new report to network
     */
    public void broadcastNewReport(ReportManager.Report report) {
        var redisBridge = plugin.getRedisBridge();
        if (redisBridge == null) return;
        
        // Format: ADD|id|reporter|target|reason|created|status|claimed|category|priority|dueBy|claimedAt|server
        String message = String.format("ADD|%s|%s|%s|%s|%d|%s|%s|%s|%s|%d|%d|%s",
            report.id,
            report.reporter,
            report.target,
            report.reason,
            report.created,
            report.status,
            report.claimedBy == null ? "null" : report.claimedBy.toString(),
            report.category == null ? "GENERAL" : report.category,
            report.priority == null ? "MEDIUM" : report.priority,
            report.dueBy,
            report.claimedAt,
            serverName
        );
        
        redisBridge.publish("minestaff:reports", message);
    }
    
    /**
     * Broadcast report update to network
     */
    public void broadcastReportUpdate(UUID reportId, String status, UUID claimedBy) {
        var redisBridge = plugin.getRedisBridge();
        if (redisBridge == null) return;
        
        String message = String.format("UPDATE|%s|%s|%s|%s",
            reportId,
            status,
            claimedBy == null ? "null" : claimedBy.toString(),
            serverName
        );
        
        redisBridge.publish("minestaff:reports", message);
    }
    
    /**
     * Handle incoming network report
     */
    public void handleNetworkReport(String message) {
        String[] parts = message.split("\\|");
        if (parts.length == 0) return;
        
        String type = parts[0];
        
        if ("ADD".equalsIgnoreCase(type) && parts.length >= 12) {
            handleReportAdd(parts);
        } else if ("UPDATE".equalsIgnoreCase(type) && parts.length >= 4) {
            handleReportUpdate(parts);
        }
    }
    
    private void handleReportAdd(String[] parts) {
        try {
            UUID id = UUID.fromString(parts[1]);
            UUID reporter = UUID.fromString(parts[2]);
            UUID target = UUID.fromString(parts[3]);
            String reason = parts[4];
            long created = Long.parseLong(parts[5]);
            String status = parts[6];
            UUID claimed = "null".equalsIgnoreCase(parts[7]) ? null : UUID.fromString(parts[7]);
            String category = parts[8];
            String priority = parts[9];
            long dueBy = Long.parseLong(parts[10]);
            long claimedAt = Long.parseLong(parts[11]);
            String originServer = parts.length > 12 ? parts[12] : "unknown";
            
            ReportManager.Report report = new ReportManager.Report(
                id, reporter, target, reason, created, status, claimed,
                category, priority, dueBy, claimedAt
            );
            
            plugin.getReportManager().addNetwork(report);
            
            // Notify staff on this server
            notifyStaff("§6[Network] §eNew report from §c" + originServer + "§e: §f" + Bukkit.getOfflinePlayer(target).getName());
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to handle network report: " + e.getMessage());
        }
    }
    
    private void handleReportUpdate(String[] parts) {
        try {
            UUID reportId = UUID.fromString(parts[1]);
            String status = parts[2];
            UUID claimedBy = "null".equalsIgnoreCase(parts[3]) ? null : UUID.fromString(parts[3]);
            String originServer = parts.length > 4 ? parts[4] : "unknown";
            
            ReportManager.Report report = plugin.getReportManager().getReport(reportId);
            if (report != null) {
                report.status = status;
                report.claimedBy = claimedBy;
                
                if (claimedBy != null) {
                    notifyStaff("§6[Network] §eReport claimed on §c" + originServer);
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to handle network report update: " + e.getMessage());
        }
    }
    
    private void notifyStaff(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("staffmode.reports.notify")) {
                player.sendMessage(message);
            }
        }
    }
    
    /**
     * Request full report sync from network
     */
    public void requestFullSync() {
        var redisBridge = plugin.getRedisBridge();
        if (redisBridge == null) return;
        
        String message = "SYNC_REQUEST|" + serverName;
        redisBridge.publish("minestaff:reports", message);
        
        plugin.getLogger().info("Requested full report sync from network");
    }
    
    /**
     * Send all local reports to requesting server
     */
    public void sendFullSync(String requestingServer) {
        if (requestingServer.equals(serverName)) return;
        
        List<ReportManager.Report> reports = plugin.getReportManager().all();
        
        plugin.getLogger().info("Sending " + reports.size() + " reports to " + requestingServer);
        
        for (ReportManager.Report report : reports) {
            broadcastNewReport(report);
        }
    }
}
