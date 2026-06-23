package me.ycxmbo.mineStaff.automod;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.util.AlertFormatter;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Configurable automatic chat moderation: banned words, excessive caps,
 * spam/flood detection and advertising. Each filter has its own action
 * ({@code cancel}, {@code warn} or {@code mute}).
 *
 * <p>{@link #inspect} is safe to call from the async chat thread (uses only
 * concurrent state); {@link #punish} applies side-effects and must run on the
 * main thread.</p>
 */
public class AutoModManager {
    public enum ViolationType { BANNED_WORD, CAPS, SPAM, FLOOD, ADVERTISING }

    public static class Violation {
        public final ViolationType type;
        public final String action; // cancel | warn | mute
        public Violation(ViolationType type, String action) {
            this.type = type;
            this.action = action;
        }
    }

    private final MineStaff plugin;
    private Pattern adPattern;

    // Spam state
    private final Map<UUID, String> lastMessage = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> repeatCount = new ConcurrentHashMap<>();
    private final Map<UUID, Deque<Long>> floodTimes = new ConcurrentHashMap<>();

    public AutoModManager(MineStaff plugin) {
        this.plugin = plugin;
        compile();
    }

    public void reload() { compile(); }

    private void compile() {
        String pat = plugin.getConfig().getString("automod.filters.advertising.pattern",
                "(?i)(\\b\\d{1,3}(\\.\\d{1,3}){3}\\b|[a-z0-9.-]+\\.(com|net|org|gg|io|me|tv|fun|xyz)\\b)");
        try { adPattern = Pattern.compile(pat); }
        catch (Exception e) { adPattern = null; plugin.getLogger().warning("Invalid automod advertising pattern: " + e.getMessage()); }
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("automod.enabled", false);
    }

    /** Analyse a message. Returns the first violation found, or null. */
    public Violation inspect(Player player, String message) {
        if (!isEnabled()) return null;
        String bypass = plugin.getConfig().getString("automod.exempt-permission", "staffmode.automod.bypass");
        if (bypass != null && !bypass.isBlank() && player.hasPermission(bypass)) return null;

        String lower = message.toLowerCase(Locale.ROOT);

        // Banned words
        if (cfgBool("banned-words", true)) {
            List<String> words = plugin.getConfig().getStringList("automod.filters.banned-words.words");
            for (String w : words) {
                if (w == null || w.isBlank()) continue;
                if (lower.contains(w.toLowerCase(Locale.ROOT))) {
                    return new Violation(ViolationType.BANNED_WORD, action("banned-words", "cancel"));
                }
            }
        }

        // Advertising
        if (cfgBool("advertising", true) && adPattern != null && adPattern.matcher(message).find()) {
            return new Violation(ViolationType.ADVERTISING, action("advertising", "warn"));
        }

        // Caps
        if (cfgBool("caps", true)) {
            int minLen = plugin.getConfig().getInt("automod.filters.caps.min-length", 8);
            int maxPct = plugin.getConfig().getInt("automod.filters.caps.max-percent", 70);
            if (message.length() >= minLen) {
                int letters = 0, upper = 0;
                for (char c : message.toCharArray()) {
                    if (Character.isLetter(c)) { letters++; if (Character.isUpperCase(c)) upper++; }
                }
                if (letters > 0 && (upper * 100 / letters) >= maxPct) {
                    return new Violation(ViolationType.CAPS, action("caps", "cancel"));
                }
            }
        }

        // Spam (identical repeats) + flood (rate)
        if (cfgBool("spam", true)) {
            UUID id = player.getUniqueId();
            int repeatThreshold = plugin.getConfig().getInt("automod.filters.spam.repeat-threshold", 3);
            int floodThreshold = plugin.getConfig().getInt("automod.filters.spam.flood-threshold", 5);
            int floodSeconds = plugin.getConfig().getInt("automod.filters.spam.flood-seconds", 3);

            String prev = lastMessage.put(id, lower);
            int repeats = lower.equals(prev) ? repeatCount.merge(id, 1, Integer::sum) : reset(id);
            if (repeatThreshold > 0 && repeats >= repeatThreshold) {
                return new Violation(ViolationType.SPAM, action("spam", "cancel"));
            }

            long now = System.currentTimeMillis();
            Deque<Long> times = floodTimes.computeIfAbsent(id, k -> new ArrayDeque<>());
            synchronized (times) {
                times.addLast(now);
                long cutoff = now - floodSeconds * 1000L;
                while (!times.isEmpty() && times.peekFirst() < cutoff) times.pollFirst();
                if (floodThreshold > 0 && times.size() > floodThreshold) {
                    return new Violation(ViolationType.FLOOD, action("spam", "cancel"));
                }
            }
        }
        return null;
    }

    private int reset(UUID id) { repeatCount.put(id, 1); return 1; }

    /** Apply the consequences of a violation. Must run on the main thread. */
    public void punish(Player player, Violation v) {
        String label = label(v.type);
        player.sendMessage(plugin.getConfigManager()
                .getMessage("automod_blocked", "&c✖ Message blocked &8(reason: &7{reason}&8)")
                .replace("{reason}", label));

        if (plugin.getConfig().getBoolean("automod.notify-staff", true)) {
            try {
                AlertFormatter.broadcast(plugin, "AutoMod: " + player.getName() + " - " + label
                        + " (" + v.action + ")", player.getName());
            } catch (Throwable ignored) {}
        }

        switch (v.action.toLowerCase(Locale.ROOT)) {
            case "warn" -> {
                try {
                    plugin.getWarningManager().issueWarning(player.getUniqueId(), player.getName(),
                            new UUID(0, 0), "AutoMod", label, "MEDIUM", 0L);
                } catch (Throwable ignored) {}
            }
            case "mute" -> {
                long dur = muteDurationMs();
                try {
                    plugin.getPunishmentManager().mute(player.getUniqueId(), player.getName(),
                            "AutoMod", label, dur);
                } catch (Throwable ignored) {}
            }
            default -> { /* cancel only */ }
        }
    }

    private long muteDurationMs() {
        String d = plugin.getConfig().getString("automod.mute-duration", "10m");
        long ms = me.ycxmbo.mineStaff.punishments.PunishmentManager.parseDuration(d);
        return ms <= 0 ? 600000L : ms;
    }

    private boolean cfgBool(String filter, boolean def) {
        return plugin.getConfig().getBoolean("automod.filters." + filter + ".enabled", def);
    }

    private String action(String filter, String def) {
        return plugin.getConfig().getString("automod.filters." + filter + ".action", def);
    }

    private String label(ViolationType t) {
        return switch (t) {
            case BANNED_WORD -> "Prohibited language";
            case CAPS -> "Excessive caps";
            case SPAM -> "Spam (repeated messages)";
            case FLOOD -> "Chat flooding";
            case ADVERTISING -> "Advertising";
        };
    }

    /** Clear per-player spam state on quit. */
    public void clear(UUID id) {
        lastMessage.remove(id);
        repeatCount.remove(id);
        floodTimes.remove(id);
    }
}
