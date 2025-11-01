package me.ycxmbo.mineStaff.messaging;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ReportManager;
import me.ycxmbo.mineStaff.util.AlertFormatter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;
import java.util.UUID;

/** Cross-server messaging via Plugin Messaging Channel (Bungee/Velocity compatible). */
public class ProxyMessenger implements PluginMessageListener {
    private static final String BUNGEE = "BungeeCord";
    private static final String CHANNEL = "minestaff:staff"; // namespaced custom channel

    private final MineStaff plugin;

    public ProxyMessenger(MineStaff plugin) { this.plugin = plugin; }

    public void init() {
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, BUNGEE);
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL);
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, CHANNEL, this);
    }

    public void close() {
        try {
            Bukkit.getMessenger().unregisterIncomingPluginChannel(plugin, CHANNEL, this);
        } catch (Throwable ignored) {}
        try {
            Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin, BUNGEE);
        } catch (Throwable ignored) {}
        try {
            Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin, CHANNEL);
        } catch (Throwable ignored) {}
    }

    private Player anyOnline() {
        Player p = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
        return p;
    }

    // ---- Staff chat ----
    public void sendStaffChat(UUID senderId, String name, String message) {
        if (!plugin.getConfigManager().getConfig().getBoolean("staffchat.cross_server", true)) return;
        Player carrier = anyOnline();
        if (carrier == null) return; // no path when empty server

        try {
            // payload to our custom channel
            ByteArrayOutputStream msg = new ByteArrayOutputStream();
            DataOutputStream data = new DataOutputStream(msg);
            data.writeUTF("SC");
            data.writeUTF(senderId.toString());
            data.writeUTF(name);
            data.writeUTF(message);

            forwardAll(carrier, msg.toByteArray());
        } catch (IOException ignored) {}
    }

    // ---- Staff alerts ----
    public void sendStaffAlert(String content, String tpTarget) {
        if (!plugin.getConfigManager().getConfig().getBoolean("alerts.cross_server", true)) return;
        Player carrier = anyOnline();
        if (carrier == null) return;
        try {
            ByteArrayOutputStream msg = new ByteArrayOutputStream();
            DataOutputStream data = new DataOutputStream(msg);
            data.writeUTF("AL");
            data.writeUTF(content);
            boolean hasTarget = tpTarget != null && !tpTarget.isBlank();
            data.writeBoolean(hasTarget);
            if (hasTarget) {
                data.writeUTF(tpTarget);
            }

            forwardAll(carrier, msg.toByteArray());
        } catch (IOException ignored) {}
    }

    // ---- Reports ----
    public void sendReportAdded(ReportManager.Report r) {
        if (!plugin.getConfigManager().getConfig().getBoolean("reports.cross_server", true)) return;
        Player carrier = anyOnline();
        if (carrier == null) return;
        try {
            ByteArrayOutputStream msg = new ByteArrayOutputStream();
            DataOutputStream data = new DataOutputStream(msg);
            data.writeUTF("RP_ADD2");
            data.writeUTF(r.id.toString());
            data.writeUTF(r.reporter.toString());
            data.writeUTF(r.target.toString());
            data.writeUTF(r.reason == null ? "" : r.reason);
            data.writeLong(r.created);
            data.writeUTF(r.status == null ? "OPEN" : r.status);
            data.writeUTF(r.claimedBy == null ? "null" : r.claimedBy.toString());
            data.writeUTF(r.category == null ? "GENERAL" : r.category);
            data.writeUTF(r.priority == null ? "MEDIUM" : r.priority);
            data.writeLong(r.dueBy);

            forwardAll(carrier, msg.toByteArray());
        } catch (IOException ignored) {}
    }

    public void sendReportUpdate(ReportManager.Report r) {
        if (!plugin.getConfigManager().getConfig().getBoolean("reports.cross_server", true)) return;
        Player carrier = anyOnline();
        if (carrier == null) return;
        try {
            ByteArrayOutputStream msg = new ByteArrayOutputStream();
            DataOutputStream data = new DataOutputStream(msg);
            data.writeUTF("RP_UPDATE");
            data.writeUTF(r.id.toString());
            data.writeUTF(r.status == null ? "OPEN" : r.status);
            data.writeUTF(r.claimedBy == null ? "null" : r.claimedBy.toString());
            data.writeUTF(r.category == null ? "GENERAL" : r.category);
            data.writeUTF(r.priority == null ? "MEDIUM" : r.priority);
            data.writeLong(r.dueBy);

            forwardAll(carrier, msg.toByteArray());
        } catch (IOException ignored) {}
    }

    private void forwardAll(Player carrier, byte[] payload) throws IOException {
        // Wrap for Bungee Forward -> our custom channel
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF(CHANNEL);
        out.writeShort(payload.length);
        out.write(payload);
        carrier.sendPluginMessage(plugin, BUNGEE, b.toByteArray());
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!CHANNEL.equals(channel)) return;
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            String type = in.readUTF();
            switch (type) {
                case "SC": {
                    UUID senderId = UUID.fromString(in.readUTF());
                    String name = in.readUTF();
                    String msg = in.readUTF();
                    // rebroadcast locally without proxy forwarding
                    plugin.getStaffChatManager().broadcastLocal(name, msg);
                    break;
                }
                case "AL": {
                    String content = in.readUTF();
                    boolean hasTarget = in.readBoolean();
                    String target = hasTarget ? in.readUTF() : null;
                    if (!plugin.getConfigManager().getConfig().getBoolean("alerts.cross_server", true)) break;
                    AlertFormatter.broadcast(plugin, content, target, false);
                    break;
                }
                case "RP_ADD": { // backward compat (no category/priority)
                    UUID id = UUID.fromString(in.readUTF());
                    UUID reporter = UUID.fromString(in.readUTF());
                    UUID target = UUID.fromString(in.readUTF());
                    String reason = in.readUTF();
                    long created = in.readLong();
                    String status = in.readUTF();
                    String claimedStr = in.readUTF();
                    UUID claimedBy = "null".equalsIgnoreCase(claimedStr) ? null : UUID.fromString(claimedStr);
                    try {
                        plugin.getReportManager().addNetwork(new ReportManager.Report(id, reporter, target, reason, created, status, claimedBy, "GENERAL", "MEDIUM", 0L));
                    } catch (Throwable ignored) {}
                    break;
                }
                case "RP_ADD2": {
                    UUID id = UUID.fromString(in.readUTF());
                    UUID reporter = UUID.fromString(in.readUTF());
                    UUID target = UUID.fromString(in.readUTF());
                    String reason = in.readUTF();
                    long created = in.readLong();
                    String status = in.readUTF();
                    String claimedStr = in.readUTF();
                    UUID claimedBy = "null".equalsIgnoreCase(claimedStr) ? null : UUID.fromString(claimedStr);
                    String category = in.readUTF();
                    String priority = in.readUTF();
                    long dueBy = in.readLong();
                    try { plugin.getReportManager().addNetwork(new ReportManager.Report(id, reporter, target, reason, created, status, claimedBy, category, priority, dueBy)); } catch (Throwable ignored) {}
                    break;
                }
                case "RP_UPDATE": {
                    if (!plugin.getConfigManager().getConfig().getBoolean("reports.cross_server", true)) break;
                    UUID id = UUID.fromString(in.readUTF());
                    String status = in.readUTF();
                    String claimedStr = in.readUTF();
                    UUID claimedBy = "null".equalsIgnoreCase(claimedStr) ? null : UUID.fromString(claimedStr);
                    String category = in.readUTF();
                    String priority = in.readUTF();
                    long dueBy = in.readLong();
                    try { plugin.getReportManager().applyNetworkUpdate(id, status, claimedBy, category, priority, dueBy); } catch (Throwable ignored) {}
                    break;
                }
                default:
                    break;
            }
        } catch (Throwable ignored) {}
    }
}
