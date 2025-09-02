package me.ycxmbo.mineStaff.managers;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.Map;

public class StaffLoginManager {
    private final MineStaff plugin;
    private final Set<UUID> loggedIn = new HashSet<>();
    private final Map<UUID, Long> sessionExpiry = new java.util.HashMap<>();
    private final Map<UUID, String> sessionIp = new java.util.HashMap<>();

    public StaffLoginManager(MineStaff plugin) {
        this.plugin = plugin;
    }

    public boolean isLoggedIn(Player p) {
        if (loggedIn.contains(p.getUniqueId())) return true;
        // Session-based trust
        long now = System.currentTimeMillis();
        Long exp = sessionExpiry.get(p.getUniqueId());
        if (exp != null && now < exp) {
            if (plugin.getConfigManager().getConfig().getBoolean("security.bind_ip", true)) {
                String ip = safeIp(p);
                String bound = sessionIp.get(p.getUniqueId());
                if (bound != null && bound.equals(ip)) return true; else return false;
            }
            return true;
        }
        return false;
    }

    public void setLoggedIn(Player p, boolean v) {
        if (v) loggedIn.add(p.getUniqueId());
        else loggedIn.remove(p.getUniqueId());
    }

    public void clearLoginStatus(Player p) {
        loggedIn.remove(p.getUniqueId());
        sessionExpiry.remove(p.getUniqueId());
        sessionIp.remove(p.getUniqueId());
    }

    public boolean checkPassword(Player p, String password) {
        String stored = plugin.getConfigManager().getStaffAccounts().getString(p.getUniqueId().toString() + ".password");
        return stored != null && stored.equals(password);
    }

    public void setPassword(Player p, String password) {
        plugin.getConfigManager().getStaffAccounts().set(p.getUniqueId().toString() + ".password", password);
    }

    public void saveAccounts() {
        plugin.getConfigManager().saveStaffAccounts();
    }

    public boolean isTwoFactorEnabled(Player p) {
        return plugin.getConfigManager().getStaffAccounts().getBoolean(p.getUniqueId().toString() + ".2fa.enabled", false);
    }

    public String getTwoFactorSecret(Player p) {
        return plugin.getConfigManager().getStaffAccounts().getString(p.getUniqueId().toString() + ".2fa.secret");
    }

    public void setTwoFactor(Player p, boolean enabled, String secret) {
        String base = p.getUniqueId().toString() + ".2fa";
        plugin.getConfigManager().getStaffAccounts().set(base + ".enabled", enabled);
        if (secret != null) plugin.getConfigManager().getStaffAccounts().set(base + ".secret", secret);
        plugin.getConfigManager().saveStaffAccounts();
    }

    public void startSession(Player p) {
        int mins = plugin.getConfigManager().getConfig().getInt("security.session_timeout_minutes", 60);
        sessionExpiry.put(p.getUniqueId(), System.currentTimeMillis() + mins * 60_000L);
        sessionIp.put(p.getUniqueId(), safeIp(p));
    }

    private String safeIp(Player p) {
        try { return p.getAddress() == null ? "" : p.getAddress().getAddress().getHostAddress(); } catch (Throwable t) { return ""; }
    }
}
