package me.ycxmbo.mineStaff.punishments;

import java.util.UUID;

/**
 * A single punishment record for the built-in punishment backend.
 *
 * <p>{@code expires == -1} denotes a permanent punishment.</p>
 */
public class Punishment {
    public enum Type { BAN, MUTE, KICK, WARN }

    private final String id;
    private final UUID target;
    private final Type type;
    private final String staff;     // display name of the issuer
    private final String reason;
    private final long start;
    private final long expires;     // epoch millis, or -1 for permanent
    private boolean active;

    public Punishment(String id, UUID target, Type type, String staff, String reason,
                      long start, long expires, boolean active) {
        this.id = id;
        this.target = target;
        this.type = type;
        this.staff = staff;
        this.reason = reason;
        this.start = start;
        this.expires = expires;
        this.active = active;
    }

    public String getId() { return id; }
    public UUID getTarget() { return target; }
    public Type getType() { return type; }
    public String getStaff() { return staff; }
    public String getReason() { return reason; }
    public long getStart() { return start; }
    public long getExpires() { return expires; }
    public boolean isPermanent() { return expires < 0; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    /** True if this punishment has lapsed (temporary and past its expiry). */
    public boolean isExpired() {
        return !isPermanent() && System.currentTimeMillis() >= expires;
    }

    /** Remaining time in millis, or -1 if permanent, 0 if expired. */
    public long remaining() {
        if (isPermanent()) return -1L;
        return Math.max(0L, expires - System.currentTimeMillis());
    }

    /** Human-readable remaining/duration string. */
    public String durationString() {
        if (isPermanent()) return "Permanent";
        long ms = remaining();
        if (ms <= 0) return "Expired";
        long s = ms / 1000;
        long d = s / 86400; s %= 86400;
        long h = s / 3600;  s %= 3600;
        long m = s / 60;    s %= 60;
        StringBuilder sb = new StringBuilder();
        if (d > 0) sb.append(d).append("d ");
        if (h > 0) sb.append(h).append("h ");
        if (m > 0) sb.append(m).append("m ");
        if (sb.length() == 0) sb.append(s).append("s");
        return sb.toString().trim();
    }
}
