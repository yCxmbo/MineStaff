package me.ycxmbo.mineStaff.managers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class StaffLoginManager {
    private final MineStaff plugin;

    // Thread-safe collections
    private final Set<UUID> loggedIn = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> sessionExpiry = new ConcurrentHashMap<>();
    private final Map<UUID, String> sessionIp = new ConcurrentHashMap<>();

    // Rate limiting and security
    private final Map<UUID, Integer> failedAttempts = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lockoutExpiry = new ConcurrentHashMap<>();

    // BCrypt cost factor (higher = more secure but slower)
    private static final int BCRYPT_COST = 12;

    // Rate limiting configuration
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 5 * 60 * 1000; // 5 minutes

    public StaffLoginManager(MineStaff plugin) {
        this.plugin = plugin;

        // Schedule periodic cleanup of expired sessions and lockouts
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin,
            this::cleanupExpiredData, 6000L, 6000L); // Every 5 minutes
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
        UUID uuid = p.getUniqueId();

        // Check if player is locked out
        if (isLockedOut(uuid)) {
            long remainingMs = lockoutExpiry.get(uuid) - System.currentTimeMillis();
            long remainingSec = remainingMs / 1000;
            logSecurityEvent(p, "LOGIN_ATTEMPT_LOCKED_OUT", "Remaining: " + remainingSec + "s");
            return false;
        }

        String stored = plugin.getConfigManager().getStaffAccounts().getString(uuid.toString() + ".password");
        if (stored == null) return false;

        // Check if password is hashed or plain text (for migration)
        boolean success;
        if (stored.startsWith("$2") && stored.length() == 60) {
            // BCrypt hashed password
            success = BCrypt.verifyer().verify(password.toCharArray(), stored).verified;
        } else {
            // Legacy plain text password - check and upgrade
            success = stored.equals(password);
            if (success) {
                // Automatically upgrade to hashed password
                setPassword(p, password);
                logSecurityEvent(p, "PASSWORD_UPGRADED", "Plain text password upgraded to BCrypt");
            }
        }

        // Handle success/failure
        if (success) {
            clearFailedAttempts(uuid);
            logSecurityEvent(p, "LOGIN_SUCCESS", "IP: " + safeIp(p));
        } else {
            incrementFailedAttempts(uuid);
            int attempts = failedAttempts.getOrDefault(uuid, 0);
            logSecurityEvent(p, "LOGIN_FAILED", "Failed attempts: " + attempts + ", IP: " + safeIp(p));
        }

        return success;
    }

    public void setPassword(Player p, String password) {
        // Hash password with BCrypt before storing
        String hashed = BCrypt.withDefaults().hashToString(BCRYPT_COST, password.toCharArray());
        plugin.getConfigManager().getStaffAccounts().set(p.getUniqueId().toString() + ".password", hashed);
        logSecurityEvent(p, "PASSWORD_CHANGED", "Password updated");
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
        try {
            return p.getAddress() == null ? "" : p.getAddress().getAddress().getHostAddress();
        } catch (Throwable t) {
            plugin.getLogger().warning("Failed to get IP for player " + p.getName() + ": " + t.getMessage());
            return "";
        }
    }

    // Rate limiting methods
    private boolean isLockedOut(UUID uuid) {
        Long lockout = lockoutExpiry.get(uuid);
        if (lockout == null) return false;
        if (System.currentTimeMillis() >= lockout) {
            lockoutExpiry.remove(uuid);
            return false;
        }
        return true;
    }

    private void incrementFailedAttempts(UUID uuid) {
        int attempts = failedAttempts.getOrDefault(uuid, 0) + 1;
        failedAttempts.put(uuid, attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            long lockoutUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MS;
            lockoutExpiry.put(uuid, lockoutUntil);
            plugin.getLogger().warning("Player " + uuid + " has been locked out due to " + attempts + " failed login attempts");
        }
    }

    private void clearFailedAttempts(UUID uuid) {
        failedAttempts.remove(uuid);
        lockoutExpiry.remove(uuid);
    }

    // Cleanup methods
    private void cleanupExpiredData() {
        long now = System.currentTimeMillis();

        // Clean up expired sessions
        sessionExpiry.entrySet().removeIf(entry -> entry.getValue() < now);

        // Clean up expired lockouts
        lockoutExpiry.entrySet().removeIf(entry -> entry.getValue() < now);

        // Clean up failed attempts for expired lockouts
        lockoutExpiry.keySet().forEach(uuid -> {
            if (!lockoutExpiry.containsKey(uuid)) {
                failedAttempts.remove(uuid);
            }
        });
    }

    /**
     * Clear login status when player disconnects
     * This should be called from a PlayerQuitEvent listener
     */
    public void onPlayerDisconnect(Player p) {
        UUID uuid = p.getUniqueId();
        // Keep session data for reconnection, but remove from active logged in set
        loggedIn.remove(uuid);
        logSecurityEvent(p, "PLAYER_DISCONNECT", "Session preserved until timeout");
    }

    // Security logging
    private void logSecurityEvent(Player p, String event, String details) {
        String message = String.format("[SECURITY] %s - Player: %s (%s) - %s",
            event, p.getName(), p.getUniqueId(), details);
        plugin.getLogger().info(message);

        // Also log to audit logger if available
        if (plugin.getAuditLogger() != null) {
            try {
                Map<String, Object> auditData = new HashMap<>();
                auditData.put("action", "STAFF_LOGIN");
                auditData.put("player", p.getName());
                auditData.put("event", event);
                auditData.put("details", details);
                auditData.put("timestamp", System.currentTimeMillis());
                plugin.getAuditLogger().log(auditData);
            } catch (Throwable ignored) {}
        }
    }

    /**
     * Get remaining lockout time in seconds
     * Returns 0 if not locked out
     */
    public long getRemainingLockoutSeconds(UUID uuid) {
        if (!isLockedOut(uuid)) return 0;
        long remainingMs = lockoutExpiry.get(uuid) - System.currentTimeMillis();
        return Math.max(0, remainingMs / 1000);
    }
}
