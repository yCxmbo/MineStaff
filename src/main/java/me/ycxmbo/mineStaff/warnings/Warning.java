package me.ycxmbo.mineStaff.warnings;

import java.util.UUID;

/**
 * Represents a warning issued to a player
 */
public class Warning {
    private final int id;
    private final UUID targetUuid;
    private final String targetName;
    private final UUID issuerUuid;
    private final String issuerName;
    private final String reason;
    private final long timestamp;
    private final long expiresAt; // 0 = never expires
    private boolean active;
    private String severity; // LOW, MEDIUM, HIGH, SEVERE

    public Warning(int id, UUID targetUuid, String targetName, UUID issuerUuid, String issuerName,
                   String reason, long timestamp, long expiresAt, boolean active, String severity) {
        this.id = id;
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.issuerUuid = issuerUuid;
        this.issuerName = issuerName;
        this.reason = reason;
        this.timestamp = timestamp;
        this.expiresAt = expiresAt;
        this.active = active;
        this.severity = severity != null ? severity : "MEDIUM";
    }

    public int getId() {
        return id;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public String getTargetName() {
        return targetName;
    }

    public UUID getIssuerUuid() {
        return issuerUuid;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public String getReason() {
        return reason;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public boolean isActive() {
        // Check if warning has expired
        if (expiresAt > 0 && System.currentTimeMillis() > expiresAt) {
            return false;
        }
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public boolean hasExpired() {
        return expiresAt > 0 && System.currentTimeMillis() > expiresAt;
    }

    public long getTimeRemaining() {
        if (expiresAt <= 0) return -1; // Never expires
        long remaining = expiresAt - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
}
