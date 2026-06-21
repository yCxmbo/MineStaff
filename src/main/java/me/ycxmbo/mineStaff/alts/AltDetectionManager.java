package me.ycxmbo.mineStaff.alts;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Detects alternate accounts by correlating the connection addresses players
 * share. To respect the project's privacy stance, raw IP addresses are never
 * stored or displayed &mdash; only salted SHA-256 hashes are persisted, which is
 * sufficient to link accounts without exposing the address itself.
 *
 * <p>Storage ({@code alts.yml}):</p>
 * <pre>
 * hash-to-players.&lt;hash&gt;: [uuid, ...]
 * player-to-hashes.&lt;uuid&gt;: [hash, ...]
 * names.&lt;uuid&gt;: lastKnownName
 * </pre>
 */
public class AltDetectionManager {
    private final MineStaff plugin;
    private final File file;
    private YamlConfiguration yaml;

    public AltDetectionManager(MineStaff plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "alts.yml");
        load();
    }

    private synchronized void load() {
        try { if (!file.exists()) { file.getParentFile().mkdirs(); file.createNewFile(); } }
        catch (IOException e) { plugin.getLogger().warning("Could not create alts.yml: " + e.getMessage()); }
        yaml = YamlConfiguration.loadConfiguration(file);
    }

    private synchronized void save() {
        try { yaml.save(file); } catch (IOException e) { plugin.getLogger().warning("Could not save alts.yml: " + e.getMessage()); }
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("alts.enabled", true);
    }

    /** Record a login. {@code rawIp} is hashed immediately and never stored. */
    public synchronized void record(UUID uuid, String name, String rawIp) {
        if (!isEnabled() || rawIp == null) return;
        String hash = hash(rawIp);

        List<String> playersForHash = yaml.getStringList("hash-to-players." + hash);
        if (!playersForHash.contains(uuid.toString())) {
            playersForHash.add(uuid.toString());
            yaml.set("hash-to-players." + hash, playersForHash);
        }

        List<String> hashesForPlayer = yaml.getStringList("player-to-hashes." + uuid);
        if (!hashesForPlayer.contains(hash)) {
            hashesForPlayer.add(hash);
            yaml.set("player-to-hashes." + uuid, hashesForPlayer);
        }

        if (name != null) yaml.set("names." + uuid, name);
        save();
    }

    /** All UUIDs that share at least one address hash with {@code uuid} (excluding itself). */
    public synchronized Set<UUID> getAlts(UUID uuid) {
        Set<UUID> alts = new LinkedHashSet<>();
        if (!isEnabled()) return alts;
        for (String hash : yaml.getStringList("player-to-hashes." + uuid)) {
            for (String other : yaml.getStringList("hash-to-players." + hash)) {
                try {
                    UUID o = UUID.fromString(other);
                    if (!o.equals(uuid)) alts.add(o);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return alts;
    }

    public synchronized String getName(UUID uuid) {
        return yaml.getString("names." + uuid, uuid.toString().substring(0, 8));
    }

    public synchronized List<UUID> getKnownPlayers() {
        List<UUID> out = new ArrayList<>();
        if (yaml.isConfigurationSection("player-to-hashes")) {
            for (String k : yaml.getConfigurationSection("player-to-hashes").getKeys(false)) {
                try { out.add(UUID.fromString(k)); } catch (IllegalArgumentException ignored) {}
            }
        }
        return out;
    }

    private String hash(String ip) {
        String salt = plugin.getConfig().getString("alts.salt", "");
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((salt + ip).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
            return sb.toString();
        } catch (Exception e) {
            // Fallback: never store the raw IP; use a stable but coarse hash.
            return Integer.toHexString((salt + ip).hashCode());
        }
    }
}
