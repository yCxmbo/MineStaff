package me.ycxmbo.mineStaff.managers;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

public class ReportManager {
    private final MineStaff plugin;
    private final File file;
    private YamlConfiguration yaml;

    public static class Report {
        public UUID id;
        public final UUID reporter;
        public final UUID target;
        public final String reason;
        public final long created;
        public String status; // OPEN, CLAIMED, CLOSED
        public UUID claimedBy;

        public Report(UUID id, UUID reporter, UUID target, String reason, long created, String status, UUID claimedBy) {
            this.id = id; this.reporter = reporter; this.target = target; this.reason = reason;
            this.created = created; this.status = status; this.claimedBy = claimedBy;
        }
    }

    public ReportManager(MineStaff plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "reports.yml");
        reload();
    }

    public void reload() {
        try { if (!file.exists()) { file.getParentFile().mkdirs(); file.createNewFile(); } }
        catch (IOException e) { e.printStackTrace(); }
        yaml = YamlConfiguration.loadConfiguration(file);
    }

    public synchronized void save() {
        try { yaml.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public synchronized UUID add(UUID reporter, UUID target, String reason) {
        UUID id = UUID.randomUUID();
        String base = "reports." + id;
        yaml.set(base + ".reporter", String.valueOf(reporter));
        yaml.set(base + ".target", String.valueOf(target));
        yaml.set(base + ".reason", reason);
        yaml.set(base + ".created", Instant.now().toEpochMilli());
        yaml.set(base + ".status", "OPEN");
        yaml.set(base + ".claimedBy", null);
        save();
        return id;
    }

    public synchronized List<Report> all() {
        List<Report> list = new ArrayList<>();
        if (!yaml.isConfigurationSection("reports")) return list;
        for (String key : Objects.requireNonNull(yaml.getConfigurationSection("reports")).getKeys(false)) {
            String base = "reports." + key;
            try {
                UUID id = UUID.fromString(key);
                UUID reporter = UUID.fromString(Objects.requireNonNull(yaml.getString(base + ".reporter")));
                UUID target = UUID.fromString(Objects.requireNonNull(yaml.getString(base + ".target")));
                String reason = yaml.getString(base + ".reason", "No reason");
                long created = yaml.getLong(base + ".created", 0L);
                String status = yaml.getString(base + ".status", "OPEN");
                String claimed = yaml.getString(base + ".claimedBy", null);
                UUID claimedBy = claimed == null ? null : UUID.fromString(claimed);
                list.add(new Report(id, reporter, target, reason, created, status, claimedBy));
            } catch (Exception ignored) {}
        }
        return list;
    }

    public synchronized Report get(UUID id) {
        String base = "reports." + id;
        if (!yaml.isSet(base)) return null;
        try {
            UUID reporter = UUID.fromString(Objects.requireNonNull(yaml.getString(base + ".reporter")));
            UUID target = UUID.fromString(Objects.requireNonNull(yaml.getString(base + ".target")));
            String reason = yaml.getString(base + ".reason", "No reason");
            long created = yaml.getLong(base + ".created", 0L);
            String status = yaml.getString(base + ".status", "OPEN");
            String claimed = yaml.getString(base + ".claimedBy", null);
            UUID claimedBy = claimed == null ? null : UUID.fromString(claimed);
            return new Report(id, reporter, target, reason, created, status, claimedBy);
        } catch (Exception ignored) { return null; }
    }

    public synchronized void setStatus(UUID id, String status) {
        String base = "reports." + id;
        if (!yaml.isSet(base)) return;
        yaml.set(base + ".status", status.toUpperCase(Locale.ROOT));
        save();
    }

    public synchronized void setClaimed(UUID id, UUID staff) {
        String base = "reports." + id;
        if (!yaml.isSet(base)) return;
        yaml.set(base + ".claimedBy", staff == null ? null : String.valueOf(staff));
        yaml.set(base + ".status", staff == null ? "OPEN" : "CLAIMED");
        save();
    }
}
