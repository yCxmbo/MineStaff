package me.ycxmbo.mineStaff.storage;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishStore {
    private final MineStaff plugin;
    private final File file;
    private YamlConfiguration yaml;
    private final Set<UUID> vanished = new HashSet<>();

    public VanishStore(MineStaff plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "vanish.yml");
        reload();
    }

    public void reload() {
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.yaml = YamlConfiguration.loadConfiguration(file);
        vanished.clear();
        for (String key : yaml.getStringList("vanished")) {
            try { vanished.add(UUID.fromString(key)); } catch (Exception ignored) {}
        }
    }

    public void save() {
        try {
            yaml.set("vanished", vanished.stream().map(UUID::toString).toList());
            yaml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isVanished(UUID id) { return vanished.contains(id); }
    public void setVanished(UUID id, boolean v) {
        if (v) vanished.add(id); else vanished.remove(id);
    }
    public Set<UUID> all() { return new HashSet<>(vanished); }
}
