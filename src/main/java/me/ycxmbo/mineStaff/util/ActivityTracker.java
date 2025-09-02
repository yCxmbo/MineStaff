package me.ycxmbo.mineStaff.util;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ActivityTracker {
    private final Map<UUID, Long> last = new ConcurrentHashMap<>();
    public void mark(Player p) { last.put(p.getUniqueId(), System.currentTimeMillis()); }
    public long lastActive(UUID id) { return last.getOrDefault(id, 0L); }
}

