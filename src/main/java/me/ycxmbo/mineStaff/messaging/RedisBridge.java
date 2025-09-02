package me.ycxmbo.mineStaff.messaging;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ReportManager;
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
        try (Jedis j = pool.getResource()) {
            j.subscribe(new JedisPubSub() {
                @Override public void onMessage(String channel, String message) {
                    try { handle(channel, message); } catch (Throwable ignored) {}
                }
            }, chStaff, chReports);
        } catch (Throwable t) {
            plugin.getLogger().warning("Redis subscribe failed: " + t.getMessage());
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
            // format: ADD|id|reporter|target|reason|created|status|claimed
            String[] p = message.split("\\|", 8);
            if (p.length < 8) return;
            if (!"ADD".equalsIgnoreCase(p[0])) return;
            try {
                UUID id = UUID.fromString(p[1]);
                UUID reporter = UUID.fromString(p[2]);
                UUID target = UUID.fromString(p[3]);
                String reason = p[4];
                long created = Long.parseLong(p[5]);
                String status = p[6];
                UUID claimed = "null".equalsIgnoreCase(p[7]) ? null : UUID.fromString(p[7]);
                plugin.getReportManager().addNetwork(new ReportManager.Report(id, reporter, target, reason, created, status, claimed));
            } catch (Throwable ignored) {}
        }
    }

    public void close() {
        try { if (subThread != null) subThread.interrupt(); } catch (Throwable ignored) {}
        try { if (pool != null) pool.close(); } catch (Throwable ignored) {}
    }

    public void publishStaffChat(String name, String message) {
        var cfg = plugin.getConfigManager().getConfig();
        if (!cfg.getBoolean("redis.enabled", false)) return;
        try (Jedis j = pool.getResource()) {
            j.publish(chStaff, name + '|' + message);
        } catch (Throwable ignored) {}
    }

    public void publishReportAdd(ReportManager.Report r) {
        var cfg = plugin.getConfigManager().getConfig();
        if (!cfg.getBoolean("redis.enabled", false)) return;
        try (Jedis j = pool.getResource()) {
            String payload = String.join("|",
                    "ADD",
                    r.id.toString(),
                    r.reporter.toString(),
                    r.target.toString(),
                    r.reason == null ? "" : r.reason.replace('|', ' '),
                    String.valueOf(r.created),
                    r.status == null ? "OPEN" : r.status,
                    r.claimedBy == null ? "null" : r.claimedBy.toString()
            );
            j.publish(chReports, payload);
        } catch (Throwable ignored) {}
    }
}

