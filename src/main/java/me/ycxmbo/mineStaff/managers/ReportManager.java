package me.ycxmbo.mineStaff.managers;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.util.AlertFormatter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

public class ReportManager {
    private final MineStaff plugin;
    private final File file;
    private YamlConfiguration yaml;
    private final boolean useSql;
    private BukkitTask claimMonitorTask;

    public static class Report {
        public UUID id;
        public final UUID reporter;
        public final UUID target;
        public final String reason;
        public final long created;
        public String status; // OPEN, CLAIMED, CLOSED, NEEDS_INFO
        public UUID claimedBy;
        public String category; // e.g., GENERAL, CHEATING, CHAT
        public String priority; // LOW, MEDIUM, HIGH, CRITICAL
        public long dueBy;      // epoch millis when SLA expires (0 if none)
        public long claimedAt;  // epoch millis when report was claimed (0 if unclaimed)

        public Report(UUID id, UUID reporter, UUID target, String reason, long created, String status, UUID claimedBy,
                      String category, String priority, long dueBy, long claimedAt) {
            this.id = id; this.reporter = reporter; this.target = target; this.reason = reason;
            this.created = created; this.status = status; this.claimedBy = claimedBy;
            this.category = category; this.priority = priority; this.dueBy = dueBy;
            this.claimedAt = claimedAt;
        }
    }

    public ReportManager(MineStaff plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "reports.yml");
        this.useSql = plugin.getStorage() != null;
        if (!useSql) reload();
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
        UUID id;
        long created = Instant.now().toEpochMilli();
        String status = "OPEN";
        String category = defaultCategory();
        String priority = defaultPriority();
        long dueBy = computeSlaDue(created, priority);
        Report report;
        if (useSql) {
            id = plugin.getStorage().addReport(reporter, target, reason, created, status, null, category, priority, dueBy, 0L);
            report = new Report(id, reporter, target, reason, created, status, null, category, priority, dueBy, 0L);
        } else {
            id = UUID.randomUUID();
            report = new Report(id, reporter, target, reason, created, status, null, category, priority, dueBy, 0L);
            setInternal(id, report);
        }
        try { plugin.getProxyMessenger().sendReportAdded(report); } catch (Throwable ignored) {}
        try { plugin.getRedisBridge().publishReportAdd(report); } catch (Throwable ignored) {}
        try { plugin.getAuditLogger().log(java.util.Map.of(
                "type","report","id",id.toString(),
                "reporter", String.valueOf(reporter),
                "target", String.valueOf(target),
                "reason", reason
        )); } catch (Throwable ignored) {}
        notifyStaffOfNewReport(report, true);
        return id;
    }

    private void setInternal(UUID id, Report r) {
        if (useSql) return;
        String base = "reports." + id;
        yaml.set(base + ".reporter", String.valueOf(r.reporter));
        yaml.set(base + ".target", String.valueOf(r.target));
        yaml.set(base + ".reason", r.reason);
        yaml.set(base + ".created", r.created);
        yaml.set(base + ".status", r.status);
        yaml.set(base + ".claimedBy", r.claimedBy == null ? null : String.valueOf(r.claimedBy));
        yaml.set(base + ".category", r.category);
        yaml.set(base + ".priority", r.priority);
        yaml.set(base + ".dueBy", r.dueBy);
        yaml.set(base + ".claimedAt", r.claimedAt);
        save();
    }

    /** Add report received from network. */
    public synchronized void addNetwork(Report r) {
        if (useSql) {
            if (plugin.getStorage().getReport(r.id) != null) return;
            if (r.dueBy == 0L) r.dueBy = computeSlaDue(r.created, r.priority == null ? defaultPriority() : r.priority);
            if (r.category == null) r.category = defaultCategory();
            if (r.priority == null) r.priority = defaultPriority();
            if (r.claimedBy != null && r.claimedAt <= 0L) r.claimedAt = System.currentTimeMillis();
            plugin.getStorage().upsertReport(r);
            notifyStaffOfNewReport(r, false);
            return;
        }

        String base = "reports." + r.id;
        if (yaml.isSet(base)) return; // already have it
        if (r.dueBy == 0L) r.dueBy = computeSlaDue(r.created, r.priority == null ? defaultPriority() : r.priority);
        if (r.category == null) r.category = defaultCategory();
        if (r.priority == null) r.priority = defaultPriority();
        if (r.claimedBy != null && r.claimedAt <= 0L) r.claimedAt = System.currentTimeMillis();
        setInternal(r.id, r);
        notifyStaffOfNewReport(r, false);
    }

    /** Apply updates received from the network. */
    public synchronized void applyNetworkUpdate(UUID id, String status, UUID claimedBy, String category, String priority, long dueBy, long claimedAt) {
        Report existing = get(id);
        if (existing == null) return;

        if (status != null && !status.isBlank()) existing.status = status.toUpperCase(Locale.ROOT);
        existing.claimedBy = claimedBy;
        if (claimedBy != null) {
            existing.claimedAt = claimedAt > 0L ? claimedAt : System.currentTimeMillis();
        } else {
            existing.claimedAt = 0L;
        }
        if (category != null && !category.isBlank()) existing.category = category.toUpperCase(Locale.ROOT);
        if (priority != null && !priority.isBlank()) existing.priority = priority.toUpperCase(Locale.ROOT);
        existing.dueBy = dueBy > 0L ? dueBy : computeSlaDue(existing.created, existing.priority == null ? defaultPriority() : existing.priority);

        if (useSql) {
            plugin.getStorage().upsertReport(existing);
        } else {
            setInternal(id, existing);
        }
    }

    public synchronized List<Report> all() {
        if (useSql) return plugin.getStorage().listReports();
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
                String category = yaml.getString(base + ".category", defaultCategory());
                String priority = yaml.getString(base + ".priority", defaultPriority());
                long dueBy = yaml.getLong(base + ".dueBy", 0L);
                if (dueBy == 0L) dueBy = computeSlaDue(created, priority);
                long claimedAt = yaml.getLong(base + ".claimedAt", 0L);
                if (claimedBy != null && claimedAt <= 0L) claimedAt = System.currentTimeMillis();
                list.add(new Report(id, reporter, target, reason, created, status, claimedBy, category, priority, dueBy, claimedAt));
            } catch (Exception ignored) {}
        }
        return list;
    }

    public synchronized Report get(UUID id) {
        if (useSql) return plugin.getStorage().getReport(id);
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
            String category = yaml.getString(base + ".category", defaultCategory());
            String priority = yaml.getString(base + ".priority", defaultPriority());
            long dueBy = yaml.getLong(base + ".dueBy", 0L);
            if (dueBy == 0L) dueBy = computeSlaDue(created, priority);
            long claimedAt = yaml.getLong(base + ".claimedAt", 0L);
            if (claimedBy != null && claimedAt <= 0L) claimedAt = System.currentTimeMillis();
            return new Report(id, reporter, target, reason, created, status, claimedBy, category, priority, dueBy, claimedAt);
        } catch (Exception ignored) { return null; }
    }

    private String defaultCategory() {
        String def = plugin.getConfigManager().getConfig().getString("reports.default_category", "GENERAL");
        return (def == null || def.isBlank()) ? "GENERAL" : def.toUpperCase(java.util.Locale.ROOT);
    }
    private String defaultPriority() {
        String def = plugin.getConfigManager().getConfig().getString("reports.default_priority", "MEDIUM");
        return (def == null || def.isBlank()) ? "MEDIUM" : def.toUpperCase(java.util.Locale.ROOT);
    }
    private long computeSlaDue(long created, String priority) {
        // SLA seconds mapping from config: reports.sla_seconds.<PRIORITY>
        int seconds = 0;
        try { seconds = plugin.getConfigManager().getConfig().getInt("reports.sla_seconds." + priority.toUpperCase(java.util.Locale.ROOT), 0); } catch (Throwable ignored) {}
        if (seconds <= 0) {
            // fallback defaults
            switch (priority.toUpperCase(java.util.Locale.ROOT)) {
                case "CRITICAL": seconds = 3600; break; // 1h
                case "HIGH": seconds = 2 * 3600; break;   // 2h
                case "MEDIUM": seconds = 6 * 3600; break; // 6h
                case "LOW": seconds = 24 * 3600; break;   // 24h
                default: seconds = 0; break;
            }
        }
        return seconds <= 0 ? 0L : created + seconds * 1000L;
    }

    public synchronized void setStatus(UUID id, String status) {
        String upper = status.toUpperCase(Locale.ROOT);
        if (useSql) {
            plugin.getStorage().setReportStatus(id, upper);
        } else {
            String base = "reports." + id;
            if (!yaml.isSet(base)) return;
            yaml.set(base + ".status", upper);
            save();
        }
        Report r = null;
        try {
            r = get(id);
            if (r != null) {
                r.status = upper;
                // Notify reporter on close or needs info
                boolean notifyClose = plugin.getConfigManager().getConfig().getBoolean("reports.notify_reporter_on_close", true);
                boolean notifyNeeds = plugin.getConfigManager().getConfig().getBoolean("reports.notify_reporter_on_needs_info", true);
                if (("CLOSED".equals(upper) && notifyClose) || ("NEEDS_INFO".equals(upper) && notifyNeeds)) {
                    org.bukkit.entity.Player reporter = org.bukkit.Bukkit.getPlayer(r.reporter);
                    if (reporter != null) {
                        reporter.sendMessage(org.bukkit.ChatColor.AQUA + "Your report (" + id + ") is now " + upper + ".");
                    }
                    try { plugin.getDiscordBridge().sendAlert("Report " + id + " status -> " + upper); } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable ignored) {}

        if (r != null) {
            try { plugin.getProxyMessenger().sendReportUpdate(r); } catch (Throwable ignored) {}
            try { plugin.getRedisBridge().publishReportUpdate(r); } catch (Throwable ignored) {}
        }
    }

    private void notifyStaffOfNewReport(Report report, boolean includeExternalChannels) {
        if (report == null) return;
        var cfg = plugin.getConfigManager().getConfig();
        String permission = cfg.getString("reports.notify-staff-permission", "staffmode.alerts");
        if (permission == null || permission.isBlank()) return;

        String template = cfg.getString("reports.notify_staff_message",
                "New report filed by {reporter} against {target}: {reason}");
        String reporterName = resolveName(report.reporter);
        String targetName = resolveName(report.target);
        String reason = (report.reason == null || report.reason.isBlank()) ? "No reason" : report.reason;
        String idText = report.id == null ? "" : report.id.toString();
        String content = template
                .replace("{reporter}", reporterName)
                .replace("{target}", targetName)
                .replace("{reason}", reason)
                .replace("{id}", idText);

        Component message = AlertFormatter.format(plugin, content, null);
        String soundName = cfg.getString("alerts.sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
        Sound notifySound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        try { notifySound = Sound.valueOf(soundName); } catch (IllegalArgumentException ignored) {}

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission(permission)) {
                online.sendMessage(message);
                try { online.playSound(online.getLocation(), notifySound, 0.6f, 1.2f); } catch (Throwable ignored) {}
            }
        }

        if (includeExternalChannels) {
            try { plugin.getDiscordBridge().sendAlert(content); } catch (Throwable ignored) {}
        }
    }

    private String resolveName(UUID uuid) {
        if (uuid == null) return "Unknown";
        try {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) return player.getName();
            OfflinePlayer offline = plugin.getServer().getOfflinePlayer(uuid);
            if (offline != null && offline.getName() != null && !offline.getName().isBlank()) {
                return offline.getName();
            }
        } catch (Throwable ignored) {}
        return uuid.toString();
    }

    public synchronized void setClaimed(UUID id, UUID staff) {
        long claimTs = staff == null ? 0L : System.currentTimeMillis();
        if (useSql) {
            plugin.getStorage().setReportClaim(id, staff, claimTs);
        } else {
            String base = "reports." + id;
            if (!yaml.isSet(base)) return;
            yaml.set(base + ".claimedBy", staff == null ? null : String.valueOf(staff));
            yaml.set(base + ".status", staff == null ? "OPEN" : "CLAIMED");
            yaml.set(base + ".claimedAt", claimTs);
            save();
        }
        // Notify
        Report r = null;
        try {
            r = get(id);
            if (r != null) {
                r.claimedBy = staff;
                r.status = staff == null ? "OPEN" : "CLAIMED";
                r.claimedAt = claimTs;
                boolean notify = plugin.getConfigManager().getConfig().getBoolean("reports.notify_reporter_on_claim", true);
                if (notify && staff != null) {
                    org.bukkit.entity.Player reporter = org.bukkit.Bukkit.getPlayer(r.reporter);
                    org.bukkit.entity.Player claimer = org.bukkit.Bukkit.getPlayer(staff);
                    if (reporter != null && claimer != null) {
                        reporter.sendMessage(org.bukkit.ChatColor.AQUA + "Your report (" + id + ") was claimed by " + claimer.getName() + ".");
                    }
                    try { plugin.getDiscordBridge().sendAlert("Report " + id + " claimed by " + (claimer == null ? staff : claimer.getName())); } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable ignored) {}

        if (r != null) {
            try { plugin.getProxyMessenger().sendReportUpdate(r); } catch (Throwable ignored) {}
            try { plugin.getRedisBridge().publishReportUpdate(r); } catch (Throwable ignored) {}
        }

        ensureClaimMonitor();
    }

    public synchronized void ensureClaimMonitor() {
        long timeoutSeconds = 0L;
        try {
            timeoutSeconds = plugin.getConfigManager().getConfig().getLong("reports.claim_timeout_seconds", 0L);
        } catch (Throwable ignored) {}
        if (timeoutSeconds <= 0L) {
            if (claimMonitorTask != null) {
                claimMonitorTask.cancel();
                claimMonitorTask = null;
            }
            return;
        }

        if (claimMonitorTask != null && !claimMonitorTask.isCancelled()) {
            return;
        }

        long intervalSeconds = Math.max(10L, Math.min(timeoutSeconds, 60L));
        long period = intervalSeconds * 20L;
        claimMonitorTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::runClaimTimeoutCheck, period, period);
    }

    private void runClaimTimeoutCheck() {
        long timeoutSeconds = plugin.getConfigManager().getConfig().getLong("reports.claim_timeout_seconds", 0L);
        if (timeoutSeconds <= 0L) return;
        long timeoutMillis = timeoutSeconds * 1000L;
        long now = System.currentTimeMillis();
        List<Report> snapshot = all();
        for (Report report : snapshot) {
            if (report == null || report.claimedBy == null) continue;
            long claimedAt = report.claimedAt;
            if (claimedAt <= 0L) continue;
            if (now - claimedAt >= timeoutMillis) {
                setClaimed(report.id, null);
            }
        }
    }

    public synchronized void shutdown() {
        if (claimMonitorTask != null) {
            claimMonitorTask.cancel();
            claimMonitorTask = null;
        }
    }

    // API methods for backward compatibility
    public synchronized UUID reportPlayer(Player reporter, OfflinePlayer target, String reason) {
        return add(reporter.getUniqueId(), target.getUniqueId(), reason);
    }

    public synchronized List<Report> getActiveReports() {
        List<Report> active = new ArrayList<>();
        for (Report r : all()) {
            if ("OPEN".equals(r.status) || "CLAIMED".equals(r.status) || "NEEDS_INFO".equals(r.status)) {
                active.add(r);
            }
        }
        return active;
    }

    public synchronized List<Report> getReportsFor(OfflinePlayer player) {
        List<Report> forPlayer = new ArrayList<>();
        for (Report r : all()) {
            if (r.target.equals(player.getUniqueId())) {
                forPlayer.add(r);
            }
        }
        return forPlayer;
    }

    public synchronized Report getReport(UUID id) {
        return get(id);
    }

    public synchronized boolean closeReport(UUID id, Player staff) {
        Report r = get(id);
        if (r == null) return false;
        setStatus(id, "CLOSED");
        return true;
    }

    public synchronized List<Report> getAll() {
        return all();
    }
}
