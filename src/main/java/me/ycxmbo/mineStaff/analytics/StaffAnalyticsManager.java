package me.ycxmbo.mineStaff.analytics;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks staff activity metrics: accumulated duty time, number of duty sessions,
 * and counters for warnings/punishments/reports handled. Persisted to
 * {@code analytics.yml}.
 *
 * <p>Duty time is accrued between {@link #startSession} and {@link #endSession}.
 * Active (open) sessions are added on top of the persisted total when queried,
 * so live values are accurate even before a session ends.</p>
 */
public class StaffAnalyticsManager {
    private final MineStaff plugin;
    private final File file;
    private YamlConfiguration yaml;

    private final Map<UUID, Long> activeSessionStart = new ConcurrentHashMap<>();

    public StaffAnalyticsManager(MineStaff plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "analytics.yml");
        load();
    }

    private synchronized void load() {
        try { if (!file.exists()) { file.getParentFile().mkdirs(); file.createNewFile(); } }
        catch (IOException e) { plugin.getLogger().warning("Could not create analytics.yml: " + e.getMessage()); }
        yaml = YamlConfiguration.loadConfiguration(file);
    }

    private synchronized void save() {
        try { yaml.save(file); } catch (IOException e) { plugin.getLogger().warning("Could not save analytics.yml: " + e.getMessage()); }
    }

    // ------------------------------------------------------------------
    // Sessions
    // ------------------------------------------------------------------

    public void startSession(UUID staff, String name) {
        activeSessionStart.put(staff, System.currentTimeMillis());
        synchronized (this) {
            yaml.set("stats." + staff + ".name", name);
            yaml.set("stats." + staff + ".sessions", yaml.getInt("stats." + staff + ".sessions", 0) + 1);
            save();
        }
    }

    public void endSession(UUID staff) {
        Long start = activeSessionStart.remove(staff);
        if (start == null) return;
        long elapsed = Math.max(0L, (System.currentTimeMillis() - start) / 1000L);
        synchronized (this) {
            yaml.set("stats." + staff + ".dutySeconds",
                    yaml.getLong("stats." + staff + ".dutySeconds", 0L) + elapsed);
            save();
        }
    }

    /** End every open session (used on shutdown so duty time isn't lost). */
    public void flushAll() {
        for (UUID id : new ArrayList<>(activeSessionStart.keySet())) endSession(id);
    }

    // ------------------------------------------------------------------
    // Counters
    // ------------------------------------------------------------------

    public synchronized void increment(UUID staff, String name, String stat) {
        if (name != null) yaml.set("stats." + staff + ".name", name);
        yaml.set("stats." + staff + "." + stat, yaml.getInt("stats." + staff + "." + stat, 0) + 1);
        save();
    }

    // ------------------------------------------------------------------
    // Queries
    // ------------------------------------------------------------------

    public synchronized long getDutySeconds(UUID staff) {
        long base = yaml.getLong("stats." + staff + ".dutySeconds", 0L);
        Long start = activeSessionStart.get(staff);
        if (start != null) base += Math.max(0L, (System.currentTimeMillis() - start) / 1000L);
        return base;
    }

    public synchronized int getStat(UUID staff, String stat) {
        return yaml.getInt("stats." + staff + "." + stat, 0);
    }

    public synchronized String getName(UUID staff) {
        return yaml.getString("stats." + staff + ".name", staff.toString().substring(0, 8));
    }

    public synchronized List<UUID> getTracked() {
        List<UUID> out = new ArrayList<>();
        if (yaml.isConfigurationSection("stats")) {
            for (String k : yaml.getConfigurationSection("stats").getKeys(false)) {
                try { out.add(UUID.fromString(k)); } catch (IllegalArgumentException ignored) {}
            }
        }
        return out;
    }

    public boolean isOnDutyNow(UUID staff) {
        return activeSessionStart.containsKey(staff);
    }

    /** Format seconds as a compact "Xd Yh Zm" string. */
    public static String formatDuration(long seconds) {
        long d = seconds / 86400; seconds %= 86400;
        long h = seconds / 3600;  seconds %= 3600;
        long m = seconds / 60;
        StringBuilder sb = new StringBuilder();
        if (d > 0) sb.append(d).append("d ");
        if (h > 0) sb.append(h).append("h ");
        sb.append(m).append("m");
        return sb.toString().trim();
    }
}
