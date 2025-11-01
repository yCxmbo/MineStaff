package me.ycxmbo.mineStaff.messaging;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ReportManager;
import me.ycxmbo.mineStaff.util.AlertFormatter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.Map;
import java.util.UUID;

public class RedisBridge {
    private final MineStaff plugin;
    private JedisPool pool;
    private Thread subThread;
    private String chStaff;
    private String chReports;
    private String chAlerts;

    public RedisBridge(MineStaff plugin) { this.plugin = plugin; }

    public void init() {
        var cfg = plugin.getConfigManager().getConfig();
        if (!cfg.getBoolean("redis.enabled", false)) return;
        String host = cfg.getString("redis.host", "localhost");
        int port = cfg.getInt("redis.port", 6379);
        String pass = cfg.getString("redis.password", "");
        boolean ssl = cfg.getBoolean("redis.ssl", false);
        chStaff = cfg.getString("redis.channels.staffchat", "minestaff:sc");
        chReports = cfg.getString("redis.channels.reports", "minestaff:reports");
        chAlerts = cfg.getString("redis.channels.alerts", "minestaff:alerts");
        JedisPoolConfig pc = new JedisPoolConfig();
        pc.setMaxTotal(8);
        if (pass != null && !pass.isEmpty()) pool = new JedisPool(pc, host, port, 2000, pass, ssl);
        else pool = new JedisPool(pc, host, port, 2000, ssl);

        subThread = new Thread(this::subscribeLoop, "MineStaff-Redis-Sub");
        subThread.setDaemon(true);
        subThread.start();
        plugin.getLogger().info("Redis bridge initialized");
    }

    private void subscribeLoop() {
        int backoff = 1000;
        while (!Thread.currentThread().isInterrupted()) {
            try (Jedis j = pool.getResource()) {
                j.subscribe(new JedisPubSub() {
                    @Override public void onMessage(String channel, String message) {
                        try { handle(channel, message); } catch (Throwable ignored) {}
                    }
                }, chStaff, chReports, chAlerts);
                backoff = 1000; // reset when returns normally
            } catch (Throwable t) {
                plugin.getLogger().warning("Redis subscribe failed: " + t.getMessage());
                try { Thread.sleep(backoff); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                backoff = Math.min(16000, backoff * 2);
            }
        }
    }

    private void handle(String channel, String message) {
        if (channel.equals(chStaff)) {
            int i = message.indexOf('|');
            if (i <= 0) return;
            String name = message.substring(0, i);
            String msg = message.substring(i + 1);
            plugin.getStaffChatManager().broadcastLocal(name, msg);
            return;
        }
        if (channel.equals(chReports)) {
            // format v2: ADD|id|reporter|target|reason|created|status|claimed|category|priority|dueBy
            // updates: UPDATE|id|status|claimed|category|priority|dueBy
            String[] p = message.split("\\|", 12);
            if (p.length == 0) return;
            String type = p[0];
            if ("ADD".equalsIgnoreCase(type)) {
                if (p.length < 8) return;
                try {
                    UUID id = UUID.fromString(p[1]);
                    UUID reporter = UUID.fromString(p[2]);
                    UUID target = UUID.fromString(p[3]);
                    String reason = p[4];
                    long created = Long.parseLong(p[5]);
                    String status = p[6];
                    UUID claimed = "null".equalsIgnoreCase(p[7]) ? null : UUID.fromString(p[7]);
                    String category = p.length >= 10 ? (p[8].isEmpty() ? "GENERAL" : p[8]) : "GENERAL";
                    String priority = p.length >= 11 ? (p[9].isEmpty() ? "MEDIUM" : p[9]) : "MEDIUM";
                    long dueBy = p.length >= 12 ? Long.parseLong(p[10]) : 0L;
                    plugin.getReportManager().addNetwork(new ReportManager.Report(id, reporter, target, reason, created, status, claimed, category, priority, dueBy));
                } catch (Throwable ignored) {}
            } else if ("UPDATE".equalsIgnoreCase(type)) {
                if (p.length < 3) return;
                try {
                    UUID id = UUID.fromString(p[1]);
                    String status = p.length >= 3 ? p[2] : null;
                    String claimedRaw = p.length >= 4 ? p[3] : "null";
                    UUID claimed = "null".equalsIgnoreCase(claimedRaw) || claimedRaw.isEmpty() ? null : UUID.fromString(claimedRaw);
                    String category = p.length >= 5 ? p[4] : null;
                    String priority = p.length >= 6 ? p[5] : null;
                    long dueBy = p.length >= 7 && !p[6].isEmpty() ? Long.parseLong(p[6]) : 0L;
                    plugin.getReportManager().applyNetworkUpdate(id, status, claimed, category, priority, dueBy);
                } catch (Throwable ignored) {}
            }
            return;
        }
        if (channel.equals(chAlerts)) {
            int i = message.indexOf('|');
            String content = i >= 0 ? message.substring(0, i) : message;
            String target = i >= 0 ? message.substring(i + 1) : null;
            if (target != null && target.isBlank()) target = null;
            if (!plugin.getConfigManager().getConfig().getBoolean("alerts.cross_server", true)) return;
            AlertFormatter.broadcast(plugin, content, target, false);
        }
    }

    public void close() {
        try { if (subThread != null) subThread.interrupt(); } catch (Throwable ignored) {}
        try { if (pool != null) pool.close(); } catch (Throwable ignored) {}
        subThread = null;
        pool = null;
    }

    public boolean isInitialized() {
        return pool != null && !pool.isClosed();
    }

    public void publishStaffChat(String name, String message) {
        var cfg = plugin.getConfigManager().getConfig();
        if (!cfg.getBoolean("redis.enabled", false)) return;
        if (pool == null) return;
        try (Jedis j = pool.getResource()) {
            j.publish(chStaff, name + '|' + message);
        } catch (Throwable ignored) {}
    }

    public void publishStaffAlert(String content, String tpTarget) {
        var cfg = plugin.getConfigManager().getConfig();
        if (!cfg.getBoolean("redis.enabled", false)) return;
        if (!cfg.getBoolean("alerts.cross_server", true)) return;
        if (pool == null) return;
        try (Jedis j = pool.getResource()) {
            String payload = content + '|' + (tpTarget == null ? "" : tpTarget);
            j.publish(chAlerts, payload);
        } catch (Throwable ignored) {}
    }

    public void publishReportAdd(ReportManager.Report r) {
        var cfg = plugin.getConfigManager().getConfig();
        if (!cfg.getBoolean("redis.enabled", false)) return;
        if (pool == null) return;
        try (Jedis j = pool.getResource()) {
            String payload = String.join("|",
                    "ADD",
                    r.id.toString(),
                    r.reporter.toString(),
                    r.target.toString(),
                    r.reason == null ? "" : r.reason.replace('|', ' '),
                    String.valueOf(r.created),
                    r.status == null ? "OPEN" : r.status,
                    r.claimedBy == null ? "null" : r.claimedBy.toString(),
                    r.category == null ? "GENERAL" : r.category,
                    r.priority == null ? "MEDIUM" : r.priority,
                    String.valueOf(r.dueBy)
            );
            j.publish(chReports, payload);
        } catch (Throwable ignored) {}
    }

    public void publishReportUpdate(ReportManager.Report r) {
        var cfg = plugin.getConfigManager().getConfig();
        if (!cfg.getBoolean("redis.enabled", false)) return;
        if (pool == null) return;
        try (Jedis j = pool.getResource()) {
            String payload = String.join("|",
                    "UPDATE",
                    r.id.toString(),
                    r.status == null ? "OPEN" : r.status,
                    r.claimedBy == null ? "null" : r.claimedBy.toString(),
                    r.category == null ? "GENERAL" : r.category,
                    r.priority == null ? "MEDIUM" : r.priority,
                    String.valueOf(r.dueBy)
            );
            j.publish(chReports, payload);
        } catch (Throwable ignored) {}
    }
}
