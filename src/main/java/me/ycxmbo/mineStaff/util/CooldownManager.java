package me.ycxmbo.mineStaff.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {
    private final Map<String, Long> cd = new HashMap<>();

    private String key(UUID id, String action) {
        return id.toString() + ":" + action;
    }

    public boolean ready(UUID id, String action) {
        long now = System.currentTimeMillis();
        Long until = cd.get(key(id, action));
        return until == null || now >= until;
    }

    public void set(UUID id, String action, long millis) {
        cd.put(key(id, action), System.currentTimeMillis() + millis);
    }

    public long remaining(UUID id, String action) {
        Long until = cd.get(key(id, action));
        if (until == null) return 0L;
        long rem = until - System.currentTimeMillis();
        return Math.max(rem, 0L);
    }
}
