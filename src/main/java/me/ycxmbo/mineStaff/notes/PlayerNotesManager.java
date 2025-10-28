package me.ycxmbo.mineStaff.notes;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

public class PlayerNotesManager {
    private final File file;
    private YamlConfiguration yaml;
    private final boolean useSql;

    public static class Note {
        public final long ts; public final UUID staff; public final String text;
        public Note(long ts, UUID staff, String text) { this.ts = ts; this.staff = staff; this.text = text; }
    }

    public PlayerNotesManager(MineStaff plugin) {
        this.file = new File(plugin.getDataFolder(), "notes.yml");
        this.useSql = plugin.getStorage() != null;
        if (!useSql) reload();
    }

    public void reload() {
        try { if (!file.exists()) { file.getParentFile().mkdirs(); file.createNewFile(); } } catch (IOException ignored) {}
        yaml = YamlConfiguration.loadConfiguration(file);
    }

    public synchronized void add(UUID target, UUID staff, String text) {
        long ts = Instant.now().toEpochMilli();
        if (useSql) {
            me.ycxmbo.mineStaff.MineStaff.getInstance().getStorage().addNote(target, staff, text, ts);
            return;
        }
        reload();
        List<String> list = yaml.getStringList("players." + target);
        list.add(ts + "|" + staff + "|" + text.replace('\n',' ').trim());
        yaml.set("players." + target, list);
        save();
    }

    public synchronized List<Note> get(UUID target) {
        if (useSql) return me.ycxmbo.mineStaff.MineStaff.getInstance().getStorage().listNotes(target);
        reload();
        List<String> raw = yaml.getStringList("players." + target);
        List<Note> out = new ArrayList<>();
        for (String s : raw) {
            String[] parts = s.split("\\|", 3);
            try {
                long ts = Long.parseLong(parts[0]);
                UUID staff = UUID.fromString(parts[1]);
                String text = parts.length >= 3 ? parts[2] : "";
                out.add(new Note(ts, staff, text));
            } catch (Exception ignored) {}
        }
        return out;
    }

    public synchronized boolean remove(UUID target, int index) {
        if (useSql) return me.ycxmbo.mineStaff.MineStaff.getInstance().getStorage().removeNoteByIndex(target, index);
        reload();
        List<String> list = yaml.getStringList("players." + target);
        if (index < 0 || index >= list.size()) return false;
        list.remove(index);
        yaml.set("players." + target, list);
        save();
        return true;
    }

    public synchronized void save() {
        try { yaml.save(file); } catch (IOException ignored) {}
    }
}
