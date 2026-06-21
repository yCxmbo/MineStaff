package me.ycxmbo.mineStaff.messaging;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Two-way Discord integration.
 *
 * <p>Outbound: incoming/outgoing webhooks for plain staff-chat lines and rich
 * embeds for reports, infractions, punishments and tickets.</p>
 *
 * <p>Inbound: an optional bot-token based poller that relays messages from a
 * configured Discord channel back into in-game staff chat.</p>
 */
public class DiscordBridge {
    private final MineStaff plugin;
    private final boolean enabled;
    private final String alertsWebhook;
    private final String staffchatWebhook;

    // Two-way relay (Discord -> in-game staff chat)
    private final boolean relayEnabled;
    private final String botToken;
    private final String relayChannelId;
    private final int pollSeconds;
    private volatile String lastMessageId = null;
    private volatile boolean primed = false;
    private BukkitTask relayTask;

    public DiscordBridge(MineStaff plugin) {
        this.plugin = plugin;
        var cfg = plugin.getConfigManager().getConfig();
        this.enabled = cfg.getBoolean("discord.enabled", false);
        this.alertsWebhook = cfg.getString("discord.alerts_webhook", "");
        this.staffchatWebhook = cfg.getString("discord.staffchat_webhook", "");
        this.relayEnabled = cfg.getBoolean("discord.relay.enabled", false);
        this.botToken = cfg.getString("discord.relay.bot_token", "");
        this.relayChannelId = cfg.getString("discord.relay.channel_id", "");
        this.pollSeconds = Math.max(3, cfg.getInt("discord.relay.poll_seconds", 5));
    }

    public boolean isEnabled() { return enabled; }

    // ---------------------------------------------------------------------
    // Outbound: plain content
    // ---------------------------------------------------------------------

    public void sendStaffChat(String name, String message) {
        if (!ready(staffchatWebhook)) return;
        JsonObject o = new JsonObject();
        o.addProperty("content", "**" + safe(name) + "**: " + safe(message));
        post(staffchatWebhook, o.toString());
    }

    public void sendAlert(String content) {
        if (!ready(alertsWebhook)) return;
        JsonObject o = new JsonObject();
        o.addProperty("content", safe(content));
        post(alertsWebhook, o.toString());
    }

    // ---------------------------------------------------------------------
    // Outbound: rich embeds (sent to the alerts webhook)
    // ---------------------------------------------------------------------

    public void sendReportEmbed(String reporter, String target, String reason) {
        sendEmbed(alertsWebhook, "📋 New Report", 0xE67E22, new String[][]{
                {"Reporter", reporter}, {"Target", target}, {"Reason", reason}});
    }

    public void sendInfractionEmbed(String staff, String target, String type, String reason) {
        sendEmbed(alertsWebhook, "⚖ Infraction: " + type, 0xC0392B, new String[][]{
                {"Staff", staff}, {"Target", target}, {"Type", type}, {"Reason", reason}});
    }

    public void sendPunishmentEmbed(String staff, String target, String type, String duration, String reason) {
        sendEmbed(alertsWebhook, "🔨 Punishment: " + type, 0x992D22, new String[][]{
                {"Staff", staff}, {"Target", target}, {"Type", type}, {"Duration", duration}, {"Reason", reason}});
    }

    public void sendTicketEmbed(String creator, String title, String category, String priority) {
        sendEmbed(alertsWebhook, "🎟 New Ticket", 0x3498DB, new String[][]{
                {"Creator", creator}, {"Title", title}, {"Category", category}, {"Priority", priority}});
    }

    /** Build and POST a single embed to the given webhook. */
    public void sendEmbed(String webhook, String title, int color, String[][] fields) {
        if (!ready(webhook)) return;
        JsonObject embed = new JsonObject();
        embed.addProperty("title", title);
        embed.addProperty("color", color);
        JsonArray arr = new JsonArray();
        for (String[] f : fields) {
            if (f.length < 2 || f[1] == null || f[1].isEmpty()) continue;
            JsonObject fo = new JsonObject();
            fo.addProperty("name", f[0]);
            fo.addProperty("value", f[1]);
            fo.addProperty("inline", true);
            arr.add(fo);
        }
        embed.add("fields", arr);
        JsonArray embeds = new JsonArray();
        embeds.add(embed);
        JsonObject payload = new JsonObject();
        payload.add("embeds", embeds);
        post(webhook, payload.toString());
    }

    private boolean ready(String webhook) {
        return enabled && webhook != null && !webhook.isBlank();
    }

    /** Trim overly long values so Discord never rejects the payload. */
    private String safe(String s) {
        if (s == null) return "";
        return s.length() > 1800 ? s.substring(0, 1800) + "…" : s;
    }

    private void post(String url, String json) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                byte[] body = json.getBytes(StandardCharsets.UTF_8);
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setConnectTimeout(4000);
                conn.setReadTimeout(4000);
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("User-Agent", "MineStaff");
                try (OutputStream os = conn.getOutputStream()) { os.write(body); }
                conn.getResponseCode();
                conn.disconnect();
            } catch (Throwable ignored) {}
        });
    }

    // ---------------------------------------------------------------------
    // Inbound: Discord -> in-game staff chat relay
    // ---------------------------------------------------------------------

    public void startRelay() {
        stopRelay();
        if (!enabled || !relayEnabled
                || botToken == null || botToken.isBlank()
                || relayChannelId == null || relayChannelId.isBlank()) {
            return;
        }
        long period = pollSeconds * 20L;
        relayTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::pollOnce, 100L, period);
        plugin.getLogger().info("Discord relay enabled (polling channel every " + pollSeconds + "s).");
    }

    public void stopRelay() {
        if (relayTask != null) {
            try { relayTask.cancel(); } catch (Throwable ignored) {}
            relayTask = null;
        }
    }

    public void shutdown() { stopRelay(); }

    private void pollOnce() {
        try {
            StringBuilder ep = new StringBuilder("https://discord.com/api/v10/channels/")
                    .append(relayChannelId).append("/messages?limit=10");
            if (lastMessageId != null) ep.append("&after=").append(lastMessageId);

            HttpURLConnection conn = (HttpURLConnection) new URL(ep.toString()).openConnection();
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bot " + botToken);
            conn.setRequestProperty("User-Agent", "MineStaff (https://github.com/ycxmbo/MineStaff, 1.0)");
            int code = conn.getResponseCode();
            if (code != 200) { conn.disconnect(); return; }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }
            conn.disconnect();

            JsonArray msgs = JsonParser.parseString(sb.toString()).getAsJsonArray();
            if (msgs.isEmpty()) return;

            // Discord returns newest-first; relay oldest-first.
            String newest = lastMessageId;
            List<String[]> toRelay = new ArrayList<>();
            for (int i = msgs.size() - 1; i >= 0; i--) {
                JsonObject m = msgs.get(i).getAsJsonObject();
                String id = m.get("id").getAsString();
                if (newest == null || compareSnowflake(id, newest) > 0) newest = id;

                JsonObject author = m.has("author") ? m.getAsJsonObject("author") : null;
                boolean isBot = author != null && author.has("bot") && author.get("bot").getAsBoolean();
                String username = author != null && author.has("username")
                        ? author.get("username").getAsString() : "Discord";
                String content = m.has("content") && !m.get("content").isJsonNull()
                        ? m.get("content").getAsString() : "";
                if (isBot || content.isBlank()) continue;
                toRelay.add(new String[]{username, content});
            }
            lastMessageId = newest;

            // First successful poll just establishes a cursor; don't replay history.
            if (!primed) { primed = true; return; }
            if (toRelay.isEmpty()) return;

            Bukkit.getScheduler().runTask(plugin, () -> {
                for (String[] r : toRelay) {
                    plugin.getStaffChatManager().broadcastLocal("[Discord] " + r[0], r[1]);
                }
            });
        } catch (Throwable ignored) {}
    }

    private static int compareSnowflake(String a, String b) {
        try {
            return Long.compareUnsigned(Long.parseUnsignedLong(a), Long.parseUnsignedLong(b));
        } catch (NumberFormatException e) {
            return a.compareTo(b);
        }
    }
}
