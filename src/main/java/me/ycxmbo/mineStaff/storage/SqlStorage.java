package me.ycxmbo.mineStaff.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.InfractionManager;
import me.ycxmbo.mineStaff.managers.ReportManager;
import me.ycxmbo.mineStaff.notes.PlayerNotesManager;

import java.sql.*;
import java.util.*;

public class SqlStorage {
    private final MineStaff plugin;
    private final HikariDataSource ds;
    private final String dialect; // sqlite | mysql

    public SqlStorage(MineStaff plugin) {
        this.plugin = plugin;
        var cfg = plugin.getConfigManager().getConfig();
        String mode = cfg.getString("storage.mode", "yaml").toLowerCase(java.util.Locale.ROOT);
        if (!(mode.equals("sqlite") || mode.equals("mysql"))) {
            throw new IllegalStateException("SqlStorage enabled without sqlite/mysql mode");
        }
        this.dialect = mode;
        HikariConfig hc = new HikariConfig();
        if (mode.equals("sqlite")) {
            String file = cfg.getString("storage.sqlite.file", "minestaff.db");
            String url = "jdbc:sqlite:" + new java.io.File(plugin.getDataFolder(), file).getAbsolutePath();
            hc.setJdbcUrl(url);
            hc.setMaximumPoolSize(5);
        } else {
            String host = cfg.getString("storage.mysql.host", "localhost");
            int port = cfg.getInt("storage.mysql.port", 3306);
            String db = cfg.getString("storage.mysql.database", "minestaff");
            String user = cfg.getString("storage.mysql.username", "root");
            String pass = String.valueOf(cfg.get("storage.mysql.password", ""));
            boolean ssl = cfg.getBoolean("storage.mysql.useSSL", false);
            String params = cfg.getString("storage.mysql.params", "useUnicode=true&characterEncoding=utf8");
            String url = String.format(java.util.Locale.ROOT, "jdbc:mysql://%s:%d/%s?%s&useSSL=%s", host, port, db, params, ssl);
            hc.setJdbcUrl(url);
            hc.setUsername(user);
            hc.setPassword(pass);
            hc.setMaximumPoolSize(10);
        }
        hc.setPoolName("MineStaff-Hikari");
        this.ds = new HikariDataSource(hc);
        initSchema();
    }

    public void close() { try { ds.close(); } catch (Throwable ignored) {} }

    private void initSchema() {
        exec("CREATE TABLE IF NOT EXISTS reports (" +
                "id TEXT PRIMARY KEY, reporter TEXT, target TEXT, reason TEXT, created BIGINT, status TEXT, claimed_by TEXT)");
        exec("CREATE TABLE IF NOT EXISTS infractions (" +
                "id TEXT PRIMARY KEY, player TEXT, ts BIGINT, staff TEXT, type TEXT, reason TEXT)");
        exec("CREATE TABLE IF NOT EXISTS notes (" +
                "id TEXT PRIMARY KEY, player TEXT, ts BIGINT, staff TEXT, text TEXT)");
        exec("CREATE TABLE IF NOT EXISTS evidence (" +
                "id TEXT PRIMARY KEY, report_id TEXT, ts BIGINT, staff TEXT, url TEXT)");
    }

    private void exec(String sql) {
        try (Connection c = ds.getConnection(); Statement st = c.createStatement()) { st.execute(sql); }
        catch (SQLException e) { plugin.getLogger().warning("SQL exec failed: " + e.getMessage()); }
    }

    // -------- Reports --------
    public UUID addReport(UUID reporter, UUID target, String reason, long created, String status, UUID claimedBy) {
        UUID id = UUID.randomUUID();
        String sql = "INSERT INTO reports (id, reporter, target, reason, created, status, claimed_by) VALUES (?,?,?,?,?,?,?)";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ps.setString(2, String.valueOf(reporter));
            ps.setString(3, String.valueOf(target));
            ps.setString(4, reason);
            ps.setLong(5, created);
            ps.setString(6, status);
            ps.setString(7, claimedBy == null ? null : String.valueOf(claimedBy));
            ps.executeUpdate();
        } catch (SQLException e) { plugin.getLogger().warning("SQL addReport: " + e.getMessage()); }
        return id;
    }

    public java.util.List<ReportManager.Report> listReports() {
        java.util.List<ReportManager.Report> out = new java.util.ArrayList<>();
        String sql = "SELECT id, reporter, target, reason, created, status, claimed_by FROM reports";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                UUID id = UUID.fromString(rs.getString(1));
                UUID reporter = UUID.fromString(rs.getString(2));
                UUID target = UUID.fromString(rs.getString(3));
                String reason = rs.getString(4);
                long created = rs.getLong(5);
                String status = rs.getString(6);
                String claimed = rs.getString(7);
                UUID claimedBy = (claimed == null) ? null : UUID.fromString(claimed);
                out.add(new ReportManager.Report(id, reporter, target, reason, created, status, claimedBy));
            }
        } catch (SQLException e) { plugin.getLogger().warning("SQL listReports: " + e.getMessage()); }
        return out;
    }

    public ReportManager.Report getReport(UUID id) {
        String sql = "SELECT reporter, target, reason, created, status, claimed_by FROM reports WHERE id=?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UUID reporter = UUID.fromString(rs.getString(1));
                    UUID target = UUID.fromString(rs.getString(2));
                    String reason = rs.getString(3);
                    long created = rs.getLong(4);
                    String status = rs.getString(5);
                    String claimed = rs.getString(6);
                    UUID claimedBy = (claimed == null) ? null : UUID.fromString(claimed);
                    return new ReportManager.Report(id, reporter, target, reason, created, status, claimedBy);
                }
            }
        } catch (SQLException e) { plugin.getLogger().warning("SQL getReport: " + e.getMessage()); }
        return null;
    }

    public void setReportStatus(UUID id, String status) {
        String sql = "UPDATE reports SET status=? WHERE id=?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, id.toString());
            ps.executeUpdate();
        } catch (SQLException e) { plugin.getLogger().warning("SQL setReportStatus: " + e.getMessage()); }
    }

    public void setReportClaim(UUID id, UUID staff) {
        String sql = "UPDATE reports SET claimed_by=?, status=? WHERE id=?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, staff == null ? null : staff.toString());
            ps.setString(2, staff == null ? "OPEN" : "CLAIMED");
            ps.setString(3, id.toString());
            ps.executeUpdate();
        } catch (SQLException e) { plugin.getLogger().warning("SQL setReportClaim: " + e.getMessage()); }
    }

    // -------- Infractions --------
    public void addInfraction(UUID player, InfractionManager.Infraction inf) {
        String sql = "INSERT INTO infractions (id, player, ts, staff, type, reason) VALUES (?,?,?,?,?,?)";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, player.toString());
            ps.setLong(3, inf.ts);
            ps.setString(4, inf.staff.toString());
            ps.setString(5, inf.type);
            ps.setString(6, inf.reason);
            ps.executeUpdate();
        } catch (SQLException e) { plugin.getLogger().warning("SQL addInfraction: " + e.getMessage()); }
    }

    public java.util.List<InfractionManager.Infraction> listInfractions(UUID player) {
        java.util.List<InfractionManager.Infraction> out = new java.util.ArrayList<>();
        String sql = "SELECT ts, staff, type, reason FROM infractions WHERE player=? ORDER BY ts ASC";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, player.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long ts = rs.getLong(1);
                    UUID staff = UUID.fromString(rs.getString(2));
                    String type = rs.getString(3);
                    String reason = rs.getString(4);
                    out.add(new InfractionManager.Infraction(staff, type, reason));
                }
            }
        } catch (SQLException e) { plugin.getLogger().warning("SQL listInfractions: " + e.getMessage()); }
        return out;
    }

    // -------- Notes --------
    public void addNote(UUID player, UUID staff, String text, long ts) {
        String sql = "INSERT INTO notes (id, player, ts, staff, text) VALUES (?,?,?,?,?)";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, player.toString());
            ps.setLong(3, ts);
            ps.setString(4, staff.toString());
            ps.setString(5, text);
            ps.executeUpdate();
        } catch (SQLException e) { plugin.getLogger().warning("SQL addNote: " + e.getMessage()); }
    }

    public java.util.List<PlayerNotesManager.Note> listNotes(UUID player) {
        java.util.List<PlayerNotesManager.Note> out = new java.util.ArrayList<>();
        String sql = "SELECT ts, staff, text FROM notes WHERE player=? ORDER BY ts ASC";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, player.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long ts = rs.getLong(1);
                    UUID staff = UUID.fromString(rs.getString(2));
                    String text = rs.getString(3);
                    out.add(new PlayerNotesManager.Note(ts, staff, text));
                }
            }
        } catch (SQLException e) { plugin.getLogger().warning("SQL listNotes: " + e.getMessage()); }
        return out;
    }

    public boolean removeNoteByIndex(UUID player, int index) {
        String sql = "SELECT rowid FROM notes WHERE player=? ORDER BY ts ASC"; // sqlite rowid works, mysql won't
        // fallback: fetch all ids then delete by position
        java.util.List<Long> rows = new java.util.ArrayList<>();
        java.util.List<Long> idsTs = new java.util.ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT ts FROM notes WHERE player=? ORDER BY ts ASC")) {
            ps.setString(1, player.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) idsTs.add(rs.getLong(1));
            }
        } catch (SQLException e) { return false; }
        if (index < 0 || index >= idsTs.size()) return false;
        long ts = idsTs.get(index);
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM notes WHERE player=? AND ts=?")) {
            ps.setString(1, player.toString());
            ps.setLong(2, ts);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // -------- Evidence --------
    public UUID addEvidence(UUID reportId, UUID staff, String url, long ts) {
        UUID id = UUID.randomUUID();
        String sql = "INSERT INTO evidence (id, report_id, ts, staff, url) VALUES (?,?,?,?,?)";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ps.setString(2, reportId.toString());
            ps.setLong(3, ts);
            ps.setString(4, staff.toString());
            ps.setString(5, url);
            ps.executeUpdate();
        } catch (SQLException e) { plugin.getLogger().warning("SQL addEvidence: " + e.getMessage()); }
        return id;
    }

    public java.util.List<me.ycxmbo.mineStaff.evidence.EvidenceManager.Evidence> listEvidence(UUID reportId) {
        java.util.List<me.ycxmbo.mineStaff.evidence.EvidenceManager.Evidence> out = new java.util.ArrayList<>();
        String sql = "SELECT id, ts, staff, url FROM evidence WHERE report_id=? ORDER BY ts ASC";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, reportId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID id = java.util.UUID.fromString(rs.getString(1));
                    long ts = rs.getLong(2);
                    UUID staff = java.util.UUID.fromString(rs.getString(3));
                    String url = rs.getString(4);
                    out.add(new me.ycxmbo.mineStaff.evidence.EvidenceManager.Evidence(id, reportId, ts, staff, url));
                }
            }
        } catch (SQLException e) { plugin.getLogger().warning("SQL listEvidence: " + e.getMessage()); }
        return out;
    }
}
