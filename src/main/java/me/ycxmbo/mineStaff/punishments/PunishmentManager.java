package me.ycxmbo.mineStaff.punishments;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.util.AlertFormatter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Issues and tracks punishments through a selectable backend.
 *
 * <ul>
 *     <li>{@code builtin} &mdash; MineStaff stores and enforces punishments
 *     itself (login blocking for bans, chat blocking for mutes).</li>
 *     <li>{@code litebans} &mdash; punishments are delegated to LiteBans via
 *     console commands; MineStaff keeps no local enforcement state.</li>
 * </ul>
 *
 * The backend is chosen with {@code punishments.backend} in config.yml.
 */
public class PunishmentManager {
    private final MineStaff plugin;
    private final File file;
    private YamlConfiguration yaml;
    private final List<Punishment> records = new ArrayList<>();

    public PunishmentManager(MineStaff plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "punishments.yml");
        load();
    }

    /** @return the configured backend, lowercased ("builtin" or "litebans"). */
    public String backend() {
        return plugin.getConfig().getString("punishments.backend", "builtin").toLowerCase();
    }

    public boolean isLiteBansBackend() {
        return backend().equals("litebans")
                && Bukkit.getPluginManager().getPlugin("LiteBans") != null;
    }

    // ------------------------------------------------------------------
    // Persistence (built-in backend only)
    // ------------------------------------------------------------------

    private synchronized void load() {
        try { if (!file.exists()) { file.getParentFile().mkdirs(); file.createNewFile(); } }
        catch (IOException e) { plugin.getLogger().warning("Could not create punishments.yml: " + e.getMessage()); }
        yaml = YamlConfiguration.loadConfiguration(file);
        records.clear();
        if (yaml.isConfigurationSection("records")) {
            for (String id : yaml.getConfigurationSection("records").getKeys(false)) {
                String b = "records." + id + ".";
                try {
                    UUID target = UUID.fromString(yaml.getString(b + "target"));
                    Punishment.Type type = Punishment.Type.valueOf(yaml.getString(b + "type", "BAN"));
                    String staff = yaml.getString(b + "staff", "Console");
                    String reason = yaml.getString(b + "reason", "No reason");
                    long start = yaml.getLong(b + "start", System.currentTimeMillis());
                    long expires = yaml.getLong(b + "expires", -1L);
                    boolean active = yaml.getBoolean(b + "active", true);
                    records.add(new Punishment(id, target, type, staff, reason, start, expires, active));
                } catch (Exception ignored) {}
            }
        }
    }

    private synchronized void persist(Punishment p) {
        String b = "records." + p.getId() + ".";
        yaml.set(b + "target", p.getTarget().toString());
        yaml.set(b + "type", p.getType().name());
        yaml.set(b + "staff", p.getStaff());
        yaml.set(b + "reason", p.getReason());
        yaml.set(b + "start", p.getStart());
        yaml.set(b + "expires", p.getExpires());
        yaml.set(b + "active", p.isActive());
        save();
    }

    private synchronized void save() {
        try { yaml.save(file); } catch (IOException e) { plugin.getLogger().warning("Could not save punishments.yml: " + e.getMessage()); }
    }

    // ------------------------------------------------------------------
    // Lookups (built-in backend)
    // ------------------------------------------------------------------

    public synchronized Punishment getActiveBan(UUID target) {
        return getActive(target, Punishment.Type.BAN);
    }

    public synchronized Punishment getActiveMute(UUID target) {
        return getActive(target, Punishment.Type.MUTE);
    }

    private Punishment getActive(UUID target, Punishment.Type type) {
        for (Punishment p : records) {
            if (!p.isActive() || p.getType() != type) continue;
            if (!p.getTarget().equals(target)) continue;
            if (p.isExpired()) { p.setActive(false); persist(p); continue; }
            return p;
        }
        return null;
    }

    public boolean isBanned(UUID target) { return getActiveBan(target) != null; }
    public boolean isMuted(UUID target) { return getActiveMute(target) != null; }

    public synchronized List<Punishment> getHistory(UUID target) {
        List<Punishment> out = new ArrayList<>();
        for (Punishment p : records) if (p.getTarget().equals(target)) out.add(p);
        out.sort(Comparator.comparingLong(Punishment::getStart).reversed());
        return out;
    }

    // ------------------------------------------------------------------
    // Issuing
    // ------------------------------------------------------------------

    /**
     * Ban a player.
     *
     * @param durationMs duration in millis, or {@code <= 0} for permanent.
     */
    public void ban(UUID target, String targetName, String staffName, String reason, long durationMs) {
        long expires = durationMs > 0 ? System.currentTimeMillis() + durationMs : -1L;
        if (isLiteBansBackend()) {
            if (expires < 0) dispatch("ban " + targetName + " " + reason);
            else dispatch("tempban " + targetName + " " + formatDuration(durationMs) + " " + reason);
        } else {
            Punishment p = newRecord(target, Punishment.Type.BAN, staffName, reason, expires);
            persist(p);
            Player online = Bukkit.getPlayer(target);
            if (online != null) online.kickPlayer(banScreen(p));
        }
        announce(staffName, targetName, "BAN", expires < 0 ? "Permanent" : formatDuration(durationMs), reason);
    }

    public void mute(UUID target, String targetName, String staffName, String reason, long durationMs) {
        long expires = durationMs > 0 ? System.currentTimeMillis() + durationMs : -1L;
        if (isLiteBansBackend()) {
            if (expires < 0) dispatch("mute " + targetName + " " + reason);
            else dispatch("tempmute " + targetName + " " + formatDuration(durationMs) + " " + reason);
        } else {
            Punishment p = newRecord(target, Punishment.Type.MUTE, staffName, reason, expires);
            persist(p);
            Player online = Bukkit.getPlayer(target);
            if (online != null) {
                online.sendMessage(org.bukkit.ChatColor.RED + "You have been muted: " + reason
                        + (p.isPermanent() ? " (permanent)" : " (" + p.durationString() + ")"));
            }
        }
        announce(staffName, targetName, "MUTE", expires < 0 ? "Permanent" : formatDuration(durationMs), reason);
    }

    public void kick(UUID target, String targetName, String staffName, String reason) {
        if (isLiteBansBackend()) {
            dispatch("kick " + targetName + " " + reason);
        } else {
            Player online = Bukkit.getPlayer(target);
            if (online != null) {
                online.kickPlayer(org.bukkit.ChatColor.RED + "You were kicked.\n"
                        + org.bukkit.ChatColor.GRAY + "Reason: " + org.bukkit.ChatColor.WHITE + reason);
            }
            // Record kicks for history without persistent enforcement.
            Punishment p = newRecord(target, Punishment.Type.KICK, staffName, reason, System.currentTimeMillis());
            p.setActive(false);
            persist(p);
        }
        announce(staffName, targetName, "KICK", "-", reason);
    }

    public void unban(UUID target, String targetName, String staffName) {
        if (isLiteBansBackend()) {
            dispatch("unban " + targetName);
        } else {
            deactivate(target, Punishment.Type.BAN);
        }
        announce(staffName, targetName, "UNBAN", "-", "");
    }

    public void unmute(UUID target, String targetName, String staffName) {
        if (isLiteBansBackend()) {
            dispatch("unmute " + targetName);
        } else {
            deactivate(target, Punishment.Type.MUTE);
        }
        announce(staffName, targetName, "UNMUTE", "-", "");
    }

    private synchronized void deactivate(UUID target, Punishment.Type type) {
        for (Punishment p : records) {
            if (p.isActive() && p.getType() == type && p.getTarget().equals(target)) {
                p.setActive(false);
                persist(p);
            }
        }
    }

    private Punishment newRecord(UUID target, Punishment.Type type, String staff, String reason, long expires) {
        Punishment p = new Punishment(UUID.randomUUID().toString().substring(0, 8),
                target, type, staff, reason, System.currentTimeMillis(), expires, true);
        records.add(p);
        return p;
    }

    // ------------------------------------------------------------------
    // Enforcement helpers
    // ------------------------------------------------------------------

    /** The kick/login screen shown for an active ban. */
    public String banScreen(Punishment ban) {
        String header = plugin.getConfig().getString("punishments.ban-screen.header", "&cYou are banned from this server.");
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', header)
                + "\n\n" + org.bukkit.ChatColor.GRAY + "Reason: " + org.bukkit.ChatColor.WHITE + ban.getReason()
                + "\n" + org.bukkit.ChatColor.GRAY + "Expires: " + org.bukkit.ChatColor.WHITE
                + (ban.isPermanent() ? "Never" : ban.durationString());
    }

    // ------------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------------

    private void dispatch(String command) {
        Runnable run = () -> {
            try { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command); }
            catch (Throwable t) { plugin.getLogger().warning("LiteBans dispatch failed: " + t.getMessage()); }
        };
        if (Bukkit.isPrimaryThread()) run.run();
        else Bukkit.getScheduler().runTask(plugin, run);
    }

    private void announce(String staff, String target, String type, String duration, String reason) {
        String msg = staff + " issued " + type + " on " + target
                + (duration != null && !duration.equals("-") ? " (" + duration + ")" : "")
                + (reason != null && !reason.isEmpty() ? ": " + reason : "");
        try { AlertFormatter.broadcast(plugin, msg, null); } catch (Throwable ignored) {}
        try { plugin.getDiscordBridge().sendPunishmentEmbed(staff, target, type, duration, reason); } catch (Throwable ignored) {}
        try {
            if (plugin.getStaffAnalyticsManager() != null) {
                // Resolve issuer UUID best-effort for analytics.
                Player issuer = Bukkit.getPlayerExact(staff);
                if (issuer != null) plugin.getStaffAnalyticsManager().increment(issuer.getUniqueId(), staff, "punishments");
            }
        } catch (Throwable ignored) {}
    }

    /** Parse durations like {@code 1h}, {@code 3d}, {@code perm}. Returns millis, 0 for permanent, -1 if invalid. */
    public static long parseDuration(String s) {
        if (s == null) return -1;
        s = s.toLowerCase();
        if (s.equals("perm") || s.equals("permanent")) return 0;
        try {
            char unit = s.charAt(s.length() - 1);
            long amount = Long.parseLong(s.substring(0, s.length() - 1));
            return switch (unit) {
                case 's' -> TimeUnit.SECONDS.toMillis(amount);
                case 'm' -> TimeUnit.MINUTES.toMillis(amount);
                case 'h' -> TimeUnit.HOURS.toMillis(amount);
                case 'd' -> TimeUnit.DAYS.toMillis(amount);
                case 'w' -> TimeUnit.DAYS.toMillis(amount * 7L);
                default -> -1;
            };
        } catch (Exception e) {
            return -1;
        }
    }

    /** Format millis as a compact LiteBans-compatible duration (e.g. "3d", "12h"). */
    public static String formatDuration(long ms) {
        if (ms <= 0) return "perm";
        long minutes = ms / 60000L;
        if (minutes % (60 * 24) == 0) return (minutes / (60 * 24)) + "d";
        if (minutes % 60 == 0) return (minutes / 60) + "h";
        return minutes + "m";
    }
}
