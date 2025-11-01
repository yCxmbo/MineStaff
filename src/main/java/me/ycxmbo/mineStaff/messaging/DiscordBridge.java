package me.ycxmbo.mineStaff.messaging;

import me.ycxmbo.mineStaff.MineStaff;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordBridge {
    private final MineStaff plugin;
    private final boolean enabled;
    private final String alertsWebhook;
    private final String staffchatWebhook;

    public DiscordBridge(MineStaff plugin) {
        this.plugin = plugin;
        var cfg = plugin.getConfigManager().getConfig();
        this.enabled = cfg.getBoolean("discord.enabled", false);
        this.alertsWebhook = cfg.getString("discord.alerts_webhook", "");
        this.staffchatWebhook = cfg.getString("discord.staffchat_webhook", "");
    }

    public void sendStaffChat(String name, String message) { if (!enabled || staffchatWebhook == null || staffchatWebhook.isBlank()) return; sendWebhook(staffchatWebhook, "**"+escape(name)+"**: " + escape(message)); }
    public void sendAlert(String content) { if (!enabled || alertsWebhook == null || alertsWebhook.isBlank()) return; sendWebhook(alertsWebhook, escape(content)); }

    public boolean isEnabled() { return enabled; }

    private void sendWebhook(String url, String content) {
        try {
            byte[] body = ('{' + "\"content\":\"" + content + "\"}").getBytes(StandardCharsets.UTF_8);
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.getOutputStream().write(body);
            conn.getInputStream().close();
            conn.disconnect();
        } catch (Throwable ignored) {}
    }

    private String escape(String s) { return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\""); }
}

