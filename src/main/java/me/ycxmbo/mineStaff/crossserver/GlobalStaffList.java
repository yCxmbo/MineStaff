package me.ycxmbo.mineStaff.crossserver;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages network-wide staff list
 */
public class GlobalStaffList {
    private final MineStaff plugin;
    private final String serverName;
    private final Map<String, ServerStaffData> networkStaff = new ConcurrentHashMap<>();
    
    public static class StaffMember {
        public final UUID uuid;
        public final String name;
        public final boolean inStaffMode;
        public final boolean vanished;
        public final long lastUpdate;
        
        public StaffMember(UUID uuid, String name, boolean inStaffMode, boolean vanished) {
            this.uuid = uuid;
            this.name = name;
            this.inStaffMode = inStaffMode;
            this.vanished = vanished;
            this.lastUpdate = System.currentTimeMillis();
        }
    }
    
    public static class ServerStaffData {
        public final String serverName;
        public final List<StaffMember> staff = new ArrayList<>();
        public long lastUpdate;
        
        public ServerStaffData(String serverName) {
            this.serverName = serverName;
            this.lastUpdate = System.currentTimeMillis();
        }
    }
    
    public GlobalStaffList(MineStaff plugin) {
        this.plugin = plugin;
        this.serverName = plugin.getConfig().getString("server-name", "unknown");
        
        // Broadcast local staff every 10 seconds
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::broadcastLocalStaff, 20L, 200L);
        
        // Clean stale data every minute
        Bukkit.getScheduler().runTaskTimer(plugin, this::cleanStaleData, 1200L, 1200L);
    }
    
    private void broadcastLocalStaff() {
        List<StaffMember> localStaff = new ArrayList<>();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("staffmode.use")) {
                boolean inStaffMode = plugin.getStaffDataManager().isStaffMode(player);
                boolean vanished = plugin.getStaffDataManager().isVanished(player);
                
                localStaff.add(new StaffMember(player.getUniqueId(), player.getName(), inStaffMode, vanished));
            }
        }
        
        // Format: serverName|count|uuid1,name1,staffMode1,vanished1|uuid2,name2,staffMode2,vanished2|...
        StringBuilder msg = new StringBuilder(serverName).append("|").append(localStaff.size());
        
        for (StaffMember member : localStaff) {
            msg.append("|")
                .append(member.uuid).append(",")
                .append(member.name).append(",")
                .append(member.inStaffMode).append(",")
                .append(member.vanished);
        }
        
        plugin.getRedisBridge().publish("minestaff:stafflist", msg.toString());
    }
    
    public void handleNetworkStaffUpdate(String message) {
        String[] parts = message.split("\\|");
        if (parts.length < 2) return;
        
        String server = parts[0];
        int count;
        
        try {
            count = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return;
        }
        
        ServerStaffData data = new ServerStaffData(server);
        
        for (int i = 2; i < parts.length && i < count + 2; i++) {
            String[] memberParts = parts[i].split(",");
            if (memberParts.length != 4) continue;
            
            try {
                UUID uuid = UUID.fromString(memberParts[0]);
                String name = memberParts[1];
                boolean inStaffMode = Boolean.parseBoolean(memberParts[2]);
                boolean vanished = Boolean.parseBoolean(memberParts[3]);
                
                data.staff.add(new StaffMember(uuid, name, inStaffMode, vanished));
            } catch (Exception ignored) {}
        }
        
        networkStaff.put(server, data);
    }
    
    public Map<String, ServerStaffData> getNetworkStaff() {
        return new HashMap<>(networkStaff);
    }
    
    public List<StaffMember> getAllStaff() {
        List<StaffMember> all = new ArrayList<>();
        for (ServerStaffData data : networkStaff.values()) {
            all.addAll(data.staff);
        }
        return all;
    }
    
    public int getTotalStaffCount() {
        return getAllStaff().size();
    }
    
    public int getStaffModeCount() {
        return (int) getAllStaff().stream().filter(s -> s.inStaffMode).count();
    }
    
    public StaffMember findStaff(String playerName) {
        for (ServerStaffData data : networkStaff.values()) {
            for (StaffMember member : data.staff) {
                if (member.name.equalsIgnoreCase(playerName)) {
                    return member;
                }
            }
        }
        return null;
    }
    
    public String findServerForPlayer(String playerName) {
        for (Map.Entry<String, ServerStaffData> entry : networkStaff.entrySet()) {
            for (StaffMember member : entry.getValue().staff) {
                if (member.name.equalsIgnoreCase(playerName)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }
    
    private void cleanStaleData() {
        long now = System.currentTimeMillis();
        networkStaff.entrySet().removeIf(entry -> 
            now - entry.getValue().lastUpdate > 30000); // 30 second timeout
    }
}
