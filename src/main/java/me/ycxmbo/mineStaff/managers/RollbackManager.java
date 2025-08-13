package me.ycxmbo.mineStaff.managers;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

public class RollbackManager {
    private final File file;
    private YamlConfiguration yaml;

    public static class Snapshot {
        public final long ts = Instant.now().toEpochMilli();
        public final ItemStack[] inv;
        public final ItemStack[] ec;

        public Snapshot(ItemStack[] inv, ItemStack[] ec) { this.inv = inv; this.ec = ec; }
    }

    public RollbackManager(MineStaff plugin) {
        this.file = new File(plugin.getDataFolder(), "rollbacks.yml");
        reload();
    }

    public void reload() {
        try { if (!file.exists()) { file.getParentFile().mkdirs(); file.createNewFile(); } }
        catch (IOException e) { e.printStackTrace(); }
        yaml = YamlConfiguration.loadConfiguration(file);
    }

    public synchronized void saveSnapshot(UUID player, Snapshot snap) {
        String base = "players." + player + "." + snap.ts;
        yaml.set(base + ".inv", snap.inv);
        yaml.set(base + ".ec", snap.ec);
        save();
    }

    public synchronized Map<Long, Snapshot> getSnapshots(UUID player) {
        Map<Long, Snapshot> map = new TreeMap<>(Collections.reverseOrder());
        if (!yaml.isConfigurationSection("players." + player)) return map;
        for (String key : Objects.requireNonNull(yaml.getConfigurationSection("players." + player)).getKeys(false)) {
            String b = "players." + player + "." + key;
            ItemStack[] inv = ((List<ItemStack>) yaml.getList(b + ".inv", new ArrayList<>())).toArray(new ItemStack[0]);
            ItemStack[] ec = ((List<ItemStack>) yaml.getList(b + ".ec", new ArrayList<>())).toArray(new ItemStack[0]);
            try { map.put(Long.parseLong(key), new Snapshot(inv, ec)); } catch (NumberFormatException ignored) {}
        }
        return map;
    }

    public synchronized void save() {
        try { yaml.save(file); } catch (IOException e) { e.printStackTrace(); }
    }
}
