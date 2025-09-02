package me.ycxmbo.mineStaff.managers;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

public class InfractionManager {
    private final MineStaff plugin;
    private final File file;
    private YamlConfiguration yaml;
    private final boolean useSql;

    public static class Infraction {
        public final long ts = Instant.now().toEpochMilli();
        public final UUID staff;
        public final String type; // WARN, MUTE, BAN (customizable)
        public final String reason;

        public Infraction(UUID staff, String type, String reason) {
            this.staff = staff; this.type = type; this.reason = reason;
        }
    }

    public InfractionManager(MineStaff plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "infractions.yml");
        this.useSql = plugin.getStorage() != null;
        if (!useSql) reload();
    }

    public void reload() {
        try { if (!file.exists()) { file.getParentFile().mkdirs(); file.createNewFile(); } }
        catch (IOException e) { e.printStackTrace(); }
        yaml = YamlConfiguration.loadConfiguration(file);
    }

    public synchronized void add(UUID player, Infraction inf) {
        if (useSql) { plugin.getStorage().addInfraction(player, inf); return; }
        String base = "players." + player + "." + inf.ts;
        yaml.set(base + ".staff", String.valueOf(inf.staff));
        yaml.set(base + ".type", inf.type);
        yaml.set(base + ".reason", inf.reason);
        save();
    }

    public synchronized List<Infraction> get(UUID player) {
        if (useSql) return plugin.getStorage().listInfractions(player);
        List<Infraction> list = new ArrayList<>();
        if (!yaml.isConfigurationSection("players." + player)) return list;
        for (String key : Objects.requireNonNull(yaml.getConfigurationSection("players." + player)).getKeys(false)) {
            String b = "players." + player + "." + key;
            try {
                UUID staff = UUID.fromString(Objects.requireNonNull(yaml.getString(b + ".staff")));
                String type = yaml.getString(b + ".type", "WARN");
                String reason = yaml.getString(b + ".reason", "No reason");
                list.add(new Infraction(staff, type, reason));
            } catch (Exception ignored) {}
        }
        return list;
    }

    public synchronized void save() {
        try { yaml.save(file); } catch (IOException e) { e.printStackTrace(); }
    }
}
