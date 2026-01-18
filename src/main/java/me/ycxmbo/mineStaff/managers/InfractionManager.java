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
        public final String id;
        public final String player;
        public final long ts;
        public final String staff;
        public final String type; // WARN, MUTE, BAN (customizable)
        public final String reason;

        // New constructor for SQL storage compatibility
        public Infraction(String id, String player, long ts, String staff, String type, String reason) {
            this.id = id;
            this.player = player;
            this.ts = ts;
            this.staff = staff;
            this.type = type;
            this.reason = reason;
        }

        // Legacy constructor for backward compatibility
        public Infraction(UUID staff, String type, String reason) {
            this.id = UUID.randomUUID().toString();
            this.player = null;
            this.ts = Instant.now().toEpochMilli();
            this.staff = staff.toString();
            this.type = type;
            this.reason = reason;
        }
    }

    public static class Note {
        public final long ts;
        public final String staff;
        public final String text;

        public Note(long ts, String staff, String text) {
            this.ts = ts;
            this.staff = staff;
            this.text = text;
        }

        public Note(long ts, UUID staff, String text) {
            this.ts = ts;
            this.staff = staff.toString();
            this.text = text;
        }
    }

    public InfractionManager(MineStaff plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "infractions.yml");
        this.useSql = plugin.getStorage() != null;
        if (!useSql) reload();
    }

    public boolean isSqlBacked() { return useSql; }

    public void reload() {
        if (useSql) return;
        try { if (!file.exists()) { file.getParentFile().mkdirs(); file.createNewFile(); } }
        catch (IOException e) { e.printStackTrace(); }
        yaml = YamlConfiguration.loadConfiguration(file);
    }

    public synchronized void add(UUID player, Infraction inf) {
        if (useSql) { plugin.getStorage().addInfraction(player, inf); return; }
        String base = "players." + player + "." + inf.ts;
        yaml.set(base + ".id", inf.id);
        yaml.set(base + ".staff", inf.staff);
        yaml.set(base + ".type", inf.type);
        yaml.set(base + ".reason", inf.reason);
        save();
    }

    public synchronized void addInfraction(UUID player, UUID staff, String type, String reason) {
        Infraction inf = new Infraction(UUID.randomUUID().toString(), player.toString(),
                Instant.now().toEpochMilli(), staff.toString(), type, reason);
        add(player, inf);
    }

    public synchronized List<Infraction> get(UUID player) {
        if (useSql) return plugin.getStorage().listInfractions(player);
        List<Infraction> list = new ArrayList<>();
        if (!yaml.isConfigurationSection("players." + player)) return list;
        for (String key : Objects.requireNonNull(yaml.getConfigurationSection("players." + player)).getKeys(false)) {
            String b = "players." + player + "." + key;
            try {
                String id = yaml.getString(b + ".id", UUID.randomUUID().toString());
                long ts = Long.parseLong(key);
                String staff = yaml.getString(b + ".staff");
                String type = yaml.getString(b + ".type", "WARN");
                String reason = yaml.getString(b + ".reason", "No reason");
                list.add(new Infraction(id, player.toString(), ts, staff, type, reason));
            } catch (Exception ignored) {}
        }
        return list;
    }

    public synchronized List<Infraction> getInfractions(UUID player) {
        return get(player);
    }

    public synchronized int getInfractionCount(UUID player) {
        return get(player).size();
    }

    public synchronized boolean removeInfraction(int id) {
        // Note: int id is legacy - modern approach would use String id
        // This is a stub for backward compatibility
        return false;
    }

    public synchronized void addNote(UUID player, UUID staff, String text) {
        long ts = Instant.now().toEpochMilli();
        if (useSql) {
            plugin.getStorage().addNote(player, staff, text);
            return;
        }
        String base = "notes." + player + "." + ts;
        yaml.set(base + ".staff", staff.toString());
        yaml.set(base + ".text", text);
        save();
    }

    public synchronized List<Note> getNotes(UUID player) {
        if (useSql) {
            List<me.ycxmbo.mineStaff.notes.PlayerNotesManager.Note> sqlNotes = plugin.getStorage().listNotes(player);
            List<Note> list = new ArrayList<>();
            for (me.ycxmbo.mineStaff.notes.PlayerNotesManager.Note n : sqlNotes) {
                list.add(new Note(n.ts, n.staff, n.text));
            }
            return list;
        }
        List<Note> list = new ArrayList<>();
        if (!yaml.isConfigurationSection("notes." + player)) return list;
        for (String key : Objects.requireNonNull(yaml.getConfigurationSection("notes." + player)).getKeys(false)) {
            String b = "notes." + player + "." + key;
            try {
                long ts = Long.parseLong(key);
                String staff = yaml.getString(b + ".staff");
                String text = yaml.getString(b + ".text", "");
                list.add(new Note(ts, staff, text));
            } catch (Exception ignored) {}
        }
        return list;
    }

    public synchronized boolean removeNote(int id) {
        // Note: int id is legacy - modern approach would use different id
        // This is a stub for backward compatibility
        return false;
    }

    public synchronized void save() {
        try { yaml.save(file); } catch (IOException e) { e.printStackTrace(); }
    }
}
