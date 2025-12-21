# MineStaff /stafflogin Code Review

## Bug Fix Summary
Fixed the issue where the `/stafflogin` module still enforced login requirements even when disabled in config.

### Root Cause
The `LoginGuardListener` was registered conditionally at plugin startup, but never unregistered/re-registered when the config was reloaded. While the listener did check config dynamically, this fix ensures proper listener lifecycle management.

### Solution Implemented
1. Added `loginGuardListener` field to track the listener instance
2. Created `reloadLoginGuardListener()` method to handle dynamic registration/unregistration
3. Integrated this method into both `onEnable()` and `reloadConfigDrivenServices()`
4. Added logging to indicate when the listener is enabled/disabled

---

## Security Issues

### 🔴 CRITICAL: Passwords Stored in Plain Text
**Location:** `StaffLoginManager.java:48-54`

**Issue:** Passwords are currently stored in plain text in the YAML config file:
```java
public boolean checkPassword(Player p, String password) {
    String stored = plugin.getConfigManager().getStaffAccounts().getString(...);
    return stored != null && stored.equals(password);
}
```

**Recommendation:** Implement password hashing using BCrypt or Argon2:
```java
// Use BCrypt for password hashing
import org.mindrot.jbcrypt.BCrypt;

public void setPassword(Player p, String password) {
    String hashed = BCrypt.hashpw(password, BCrypt.gensalt(12));
    // Store hashed password
}

public boolean checkPassword(Player p, String password) {
    String stored = // retrieve hashed password
    return stored != null && BCrypt.checkpw(password, stored);
}
```

**Impact:** HIGH - Plain text passwords can be compromised if the server files are accessed.

---

### 🟡 MEDIUM: Session Management Improvements

**Location:** `StaffLoginManager.java:13-15, 21-35`

**Issues:**
1. Session data stored in memory only - lost on server restart
2. No session invalidation on player quit/kick
3. Session timeout not enforced actively (only checked on login)

**Recommendations:**
1. **Persist sessions** to survive server restarts
2. **Clear sessions on disconnect** to prevent session hijacking
3. **Add failed login attempt tracking** with temporary lockouts
4. **Log security events** (failed logins, session expirations, etc.)

---

### 🟡 MEDIUM: Rate Limiting

**Issue:** No rate limiting on `/stafflogin` command attempts

**Recommendation:** Add rate limiting to prevent brute force attacks:
```java
// Track failed attempts per player
private final Map<UUID, Integer> failedAttempts = new HashMap<>();
private final Map<UUID, Long> lockoutExpiry = new HashMap<>();

public boolean checkPassword(Player p, String password) {
    // Check if locked out
    if (isLockedOut(p)) {
        return false;
    }

    String stored = ...;
    boolean success = stored != null && BCrypt.checkpw(password, stored);

    if (!success) {
        incrementFailedAttempts(p);
    } else {
        clearFailedAttempts(p);
    }

    return success;
}
```

---

## Code Quality Improvements

### 1. Thread Safety
**Location:** `StaffLoginManager.java:13-15`

**Issue:** Maps are not thread-safe but could be accessed from async events

**Recommendation:**
```java
private final Set<UUID> loggedIn = ConcurrentHashMap.newKeySet();
private final Map<UUID, Long> sessionExpiry = new ConcurrentHashMap<>();
private final Map<UUID, String> sessionIp = new ConcurrentHashMap<>();
```

### 2. Magic Numbers
**Location:** `LoginGuardListener.java:43-46`

**Issue:** Direct comparison of coordinates for movement detection

**Recommendation:** Extract to named constant:
```java
private static final double MOVEMENT_THRESHOLD = 0.001;

private boolean hasMoved(Location from, Location to) {
    return Math.abs(from.getX() - to.getX()) > MOVEMENT_THRESHOLD
        || Math.abs(from.getY() - to.getY()) > MOVEMENT_THRESHOLD
        || Math.abs(from.getZ() - to.getZ()) > MOVEMENT_THRESHOLD;
}
```

### 3. Error Handling
**Location:** `StaffLoginManager.java:82-83`

**Issue:** Silent exception catching returns empty string

**Recommendation:** Log exceptions for debugging:
```java
private String safeIp(Player p) {
    try {
        return p.getAddress() == null ? "" : p.getAddress().getAddress().getHostAddress();
    } catch (Throwable t) {
        plugin.getLogger().warning("Failed to get IP for player " + p.getName() + ": " + t.getMessage());
        return "";
    }
}
```

### 4. Code Duplication
**Location:** `LoginGuardListener.java` - multiple event handlers with similar logic

**Recommendation:** Extract common logic:
```java
private void cancelIfLoginRequired(Player p, Cancellable event, String action) {
    if (!requiresLogin(p)) return;
    event.setCancelled(true);
    p.sendMessage(config.getMessage("login_required", "Please /stafflogin to " + action + "."));
}

@EventHandler(ignoreCancelled = true)
public void onBreak(BlockBreakEvent e) {
    cancelIfLoginRequired(e.getPlayer(), e, "break blocks");
}
```

---

## Feature Suggestions

### 1. Password Strength Requirements
Add configurable password requirements:
```yaml
security:
  password:
    min_length: 8
    require_uppercase: true
    require_lowercase: true
    require_numbers: true
    require_special: true
```

### 2. Password Change on First Login
Force password change on first login or after admin reset:
```java
public boolean requiresPasswordChange(Player p) {
    return staffAccounts.getBoolean(p.getUniqueId() + ".force_password_change", false);
}
```

### 3. Session Activity Timeout
Automatically logout staff after period of inactivity:
```yaml
security:
  idle_timeout_minutes: 30  # Auto-logout after 30 mins of inactivity
```

### 4. Login Notifications
Notify staff members when someone logs into their account:
```java
// Send notification with IP, timestamp, and location
p.sendMessage("Last login: " + lastLoginTime + " from " + lastLoginIp);
```

### 5. Hardware Device Binding
Beyond IP binding, support device fingerprinting:
```yaml
security:
  bind_device: true  # Require same client/device
  trusted_devices_per_account: 3
```

### 6. Emergency Lockdown Mode
Add command to temporarily disable all staff access:
```java
/stafflockdown [on|off] - Immediately revoke all staff sessions
```

### 7. Audit Logging
Enhanced logging for security events:
```java
// Log to separate security.log file:
- Failed login attempts with IP
- Successful logins
- Session expirations
- Password changes
- Permission elevation events
```

### 8. Multi-Factor Recovery Codes
Generate backup codes for 2FA in case of device loss:
```java
/staff2fa recovery - Generate 10 single-use backup codes
```

### 9. Permission-Based Login Requirements
Make login optional for lower-tier staff:
```yaml
security:
  require_login_permissions:
    - "staffmode.admin"    # Only admins must login
    - "staffmode.senior"   # Senior staff must login
    # Helpers/Mods don't need to login
```

### 10. Grace Period on Login
Allow brief grace period before freezing player:
```yaml
security:
  login_grace_period_seconds: 30  # 30 seconds to login before freeze
```

---

## Testing Recommendations

1. **Test config reload behavior:**
   - Start with `staff_login_enabled: true`
   - Execute `/staffreload` with `staff_login_enabled: false`
   - Verify staff can move/interact without login

2. **Test session persistence:**
   - Login as staff
   - Disconnect and reconnect
   - Verify session is maintained (within timeout)

3. **Test 2FA flow:**
   - Enable 2FA for account
   - Attempt login with wrong OTP
   - Attempt login with correct OTP

4. **Test IP binding:**
   - Login from one IP
   - Attempt to use session from different IP (if bind_ip: true)

---

## Performance Considerations

1. **Movement Event Optimization:**
   The `onMove` handler fires very frequently. Consider:
   ```java
   // Only check if player actually moved blocks
   if (e.getFrom().getBlockX() == e.getTo().getBlockX()
       && e.getFrom().getBlockY() == e.getTo().getBlockY()
       && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) {
       return; // No block movement, skip check
   }
   ```

2. **Cache Config Lookups:**
   Cache frequently accessed config values instead of reading each time

3. **Batch Session Cleanup:**
   Periodically clean expired sessions instead of checking on each login:
   ```java
   // Run every 5 minutes
   Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,
       this::cleanExpiredSessions, 6000L, 6000L);
   ```

---

## Priority Recommendations

**Must Fix (High Priority):**
1. ✅ Login guard not respecting disabled config (FIXED)
2. 🔴 Implement password hashing
3. 🟡 Add session cleanup on player disconnect
4. 🟡 Add rate limiting for login attempts

**Should Implement (Medium Priority):**
5. Thread-safe collections
6. Failed login tracking and lockouts
7. Security event logging
8. Password strength requirements

**Nice to Have (Low Priority):**
9. Grace period before freeze
10. Login notifications
11. Device binding
12. Emergency lockdown mode

---

## Conclusion

The `/stafflogin` system provides a solid foundation for staff authentication, but has critical security gaps (plain text passwords) that should be addressed immediately. The bug fix ensures the module properly respects the disabled state, and the recommended improvements would significantly enhance security and user experience.
