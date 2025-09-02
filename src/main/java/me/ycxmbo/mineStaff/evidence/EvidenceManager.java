package me.ycxmbo.mineStaff.evidence;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

public class EvidenceManager {
    public static class Evidence {
        public final UUID id; public final UUID reportId; public final long ts; public final UUID staff; public final String url;
        public Evidence(UUID id, UUID reportId, long ts, UUID staff, String url) { this.id=id; this.reportId=reportId; this.ts=ts; this.staff=staff; this.url=url; }
    }

    private final MineStaff plugin;
    private final boolean useSql;
    private final File file;
    private YamlConfiguration yaml;

    public EvidenceManager(MineStaff plugin) {
        this.plugin = plugin;
        this.useSql = plugin.getStorage() != null;
        this.file = new File(plugin.getDataFolder(), "evidence.yml");
        if (!useSql) reload();
    }

    private void reload() {
        try { if (!file.exists()) { file.getParentFile().mkdirs(); file.createNewFile(); } } catch (IOException ignored) {}
        yaml = YamlConfiguration.loadConfiguration(file);
    }

    public UUID add(UUID reportId, UUID staff, String url) {
        long ts = Instant.now().toEpochMilli();
        if (useSql) {
            return plugin.getStorage().addEvidence(reportId, staff, url, ts);
        }
        UUID id = UUID.randomUUID();
        String base = "reports." + reportId + "." + id;
        yaml.set(base + ".ts", ts);
        yaml.set(base + ".staff", String.valueOf(staff));
        yaml.set(base + ".url", url);
        save();
        return id;
    }

    public List<Evidence> list(UUID reportId) {
        if (useSql) return plugin.getStorage().listEvidence(reportId);
        List<Evidence> list = new ArrayList<>();
        if (!yaml.isConfigurationSection("reports." + reportId)) return list;
        for (String key : Objects.requireNonNull(yaml.getConfigurationSection("reports." + reportId)).getKeys(false)) {
            String base = "reports." + reportId + "." + key;
            try {
                UUID id = UUID.fromString(key);
                long ts = yaml.getLong(base + ".ts", 0L);
                UUID staff = UUID.fromString(Objects.requireNonNull(yaml.getString(base + ".staff")));
                String url = yaml.getString(base + ".url", "");
                list.add(new Evidence(id, reportId, ts, staff, url));
            } catch (Exception ignored) {}
        }
        return list;
    }

    public int count(UUID reportId) { return list(reportId).size(); }

    private void save() { try { yaml.save(file); } catch (IOException ignored) {} }
}

