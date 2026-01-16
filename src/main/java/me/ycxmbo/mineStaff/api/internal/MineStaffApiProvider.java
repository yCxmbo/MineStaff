package me.ycxmbo.mineStaff.api.internal;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.api.MineStaffAPI;
import me.ycxmbo.mineStaff.api.events.*;
import me.ycxmbo.mineStaff.managers.InfractionManager;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import me.ycxmbo.mineStaff.reports.ReportedPlayer;
import me.ycxmbo.mineStaff.tickets.StaffTicket;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

public class MineStaffApiProvider implements MineStaffAPI {

    private final MineStaff plugin;
    private final StaffDataManager data;

    // optional cached reflective methods
    private Method mSetStaffMode;     // setStaffMode(Player, boolean)
    private Method mEnterStaffMode;   // enter/enable/start/activate(Player)
    private Method mExitStaffMode;    // exit/disable/stop/deactivate(Player)
    private boolean lookedUp = false;

    public MineStaffApiProvider(MineStaff plugin) {
        this.plugin = plugin;
        this.data = plugin.getStaffDataManager();
    }

    // ---------- interface reads ----------
    @Override public boolean isStaffMode(Player player) { return data.isStaffMode(player); }
    @Override public boolean isStaffMode(UUID playerId) {
        Player p = playerId == null ? null : Bukkit.getPlayer(playerId);
        return p != null && data.isStaffMode(p);
    }

    @Override public boolean isVanished(Player player) { return data.isVanished(player); }
    @Override public boolean isVanished(UUID playerId) {
        Player p = playerId == null ? null : Bukkit.getPlayer(playerId);
        return p != null && data.isVanished(p);
    }

    @Override public boolean isFrozen(Player player) { return data.isFrozen(player); }
    @Override public boolean isFrozen(UUID playerId) {
        Player p = playerId == null ? null : Bukkit.getPlayer(playerId);
        return p != null && data.isFrozen(p);
    }

    @Override
    public StaffSnapshot snapshot(Player player) {
        return new StaffSnapshot(
                player.getUniqueId(),
                data.isStaffMode(player),
                data.isVanished(player),
                data.isFrozen(player)
        );
    }

    // ---------- interface writes ----------
    @Override
    public boolean setStaffMode(Player player, boolean enabled, ToggleCause cause) {
        boolean before = data.isStaffMode(player);
        boolean after = applyStaffMode(player, enabled);
        if (before != after) {
            Bukkit.getPluginManager().callEvent(new StaffModeToggleEvent(player, after, cause));
        }
        return after;
    }

    @Override
    public boolean setVanish(Player player, boolean enabled, ToggleCause cause) {
        boolean before = data.isVanished(player);
        try {
            data.setVanished(player, enabled); // your manager usually returns void
        } catch (Throwable t) {
            plugin.getLogger().warning("[API] setVanish failed: " + t.getMessage());
        }
        boolean after = data.isVanished(player);
        if (before != after) {
            Bukkit.getPluginManager().callEvent(new VanishToggleEvent(player, after, cause));
        }
        return after;
    }

    @Override
    public boolean setFrozen(Player target, boolean enabled, ToggleCause cause) {
        boolean before = data.isFrozen(target);
        try {
            data.setFrozen(target, enabled);   // your manager usually returns void
        } catch (Throwable t) {
            plugin.getLogger().warning("[API] setFrozen failed: " + t.getMessage());
        }
        boolean after = data.isFrozen(target);
        if (before != after) {
            Bukkit.getPluginManager().callEvent(new FreezeToggleEvent(target, after, cause));
        }
        return after;
    }

    // ---------- helpers ----------
    private void lookupOnce() {
        if (lookedUp) return;
        lookedUp = true;
        Class<?> c = data.getClass();
        try { mSetStaffMode = c.getMethod("setStaffMode", Player.class, boolean.class); } catch (NoSuchMethodException ignored) {}
        // enable/enter
        for (String n : new String[]{"enterStaffMode","enableStaffMode","startStaffMode","activateStaffMode"}) {
            if (mEnterStaffMode != null) break;
            try { mEnterStaffMode = c.getMethod(n, Player.class); } catch (NoSuchMethodException ignored) {}
        }
        // disable/exit
        for (String n : new String[]{"exitStaffMode","disableStaffMode","stopStaffMode","deactivateStaffMode"}) {
            if (mExitStaffMode != null) break;
            try { mExitStaffMode = c.getMethod(n, Player.class); } catch (NoSuchMethodException ignored) {}
        }
    }

    /** Try direct setter; otherwise try enable/exit methods; always return current manager state. */
    private boolean applyStaffMode(Player p, boolean enabled) {
        lookupOnce();
        try {
            if (mSetStaffMode != null) {
                mSetStaffMode.invoke(data, p, enabled);
            } else if (enabled && mEnterStaffMode != null) {
                mEnterStaffMode.invoke(data, p);
            } else if (!enabled && mExitStaffMode != null) {
                mExitStaffMode.invoke(data, p);
            } else {
                plugin.getLogger().warning("[API] No staff-mode method found in StaffDataManager; state unchanged.");
            }
        } catch (Throwable t) {
            plugin.getLogger().warning("[API] Failed to toggle staff mode: " + t.getMessage());
        }
        return data.isStaffMode(p);
    }

    // ========== Reports ==========
    @Override
    public ReportedPlayer createReport(Player reporter, OfflinePlayer reported, String reason) {
        try {
            // Fire event first
            PlayerReportEvent event = new PlayerReportEvent(reporter, reported, reason);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return null;
            }
            return plugin.getReportManager().reportPlayer(reporter, reported, reason);
        } catch (Throwable t) {
            plugin.getLogger().warning("[API] createReport failed: " + t.getMessage());
            return null;
        }
    }

    @Override
    public List<ReportedPlayer> getActiveReports() {
        try {
            return plugin.getReportManager().getActiveReports();
        } catch (Throwable t) {
            plugin.getLogger().warning("[API] getActiveReports failed: " + t.getMessage());
            return List.of();
        }
    }

    @Override
    public List<ReportedPlayer> getReportsFor(OfflinePlayer player) {
        try {
            return plugin.getReportManager().getReportsFor(player);
        } catch (Throwable t) {
            plugin.getLogger().warning("[API] getReportsFor failed: " + t.getMessage());
            return List.of();
        }
    }

    @Override
    public boolean closeReport(UUID reportId, Player staff) {
        try {
            ReportedPlayer report = plugin.getReportManager().getReport(reportId);
            if (report == null) return false;
            plugin.getReportManager().closeReport(reportId, staff);
            return true;
        } catch (Throwable t) {
            plugin.getLogger().warning("[API] closeReport failed: " + t.getMessage());
            return false;
        }
    }

    // ========== Infractions ==========
    @Override
    public int addInfraction(OfflinePlayer player, Player staff, String type, String reason) {
        try {
            // Fire event first
            InfractionAddEvent event = new InfractionAddEvent(player, staff, type, reason);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return -1;
            }
            return plugin.getInfractionManager().addInfraction(
                    player.getUniqueId(),
                    staff.getUniqueId(),
                    type,
                    reason
            );
        } catch (Throwable t) {
            plugin.getLogger().warning("[API] addInfraction failed: " + t.getMessage());
            return -1;
        }
    }

    @Override
    public List<InfractionManager.Infraction> getInfractions(OfflinePlayer player) {
        try {
            return plugin.getInfractionManager().getInfractions(player.getUniqueId());
        } catch (Throwable t) {
            plugin.getLogger().warning("[API] getInfractions failed: " + t.getMessage());
            return List.of();
        }
    }

    @Override
    public int getInfractionCount(OfflinePlayer player) {
        try {
            return plugin.getInfractionManager().getInfractionCount(player.getUniqueId());
        } catch (Throwable t) {
            plugin.getLogger().warning("[API] getInfractionCount failed: " + t.getMessage());
            return 0;
        }
    }

    @Override
    public boolean removeInfraction(int id) {
        try {
            return plugin.getInfractionManager().removeInfraction(id);
        } catch (Throwable t) {
            plugin.getLogger().warning("[API] removeInfraction failed: " + t.getMessage());
            return false;
        }
    }

    // ========== Notes ==========
    @Override
    public int addNote(OfflinePlayer player, Player staff, String note) {
        try {
            return plugin.getInfractionManager().addNote(
                    player.getUniqueId(),
                    staff.getUniqueId(),
                    note
            );
        } catch (Throwable t) {
            plugin.getLogger().warning("[API] addNote failed: " + t.getMessage());
            return -1;
        }
    }

    @Override
    public List<InfractionManager.Note> getNotes(OfflinePlayer player) {
        try {
            return plugin.getInfractionManager().getNotes(player.getUniqueId());
        } catch (Throwable t) {
            plugin.getLogger().warning("[API] getNotes failed: " + t.getMessage());
            return List.of();
        }
    }

    @Override
    public boolean removeNote(int id) {
        try {
            return plugin.getInfractionManager().removeNote(id);
        } catch (Throwable t) {
            plugin.getLogger().warning("[API] removeNote failed: " + t.getMessage());
            return false;
        }
    }

    // ========== Tickets ==========
    @Override
    public UUID createTicket(Player creator, String subject, String description, String category, String priority) {
        try {
            UUID ticketId = UUID.randomUUID();
            // Fire event first
            StaffTicketCreateEvent event = new StaffTicketCreateEvent(ticketId, creator, subject, category, priority);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return null;
            }
            return plugin.getStaffTicketManager().createTicket(creator, subject, description, category, priority);
        } catch (Throwable t) {
            plugin.getLogger().warning("[API] createTicket failed: " + t.getMessage());
            return null;
        }
    }

    @Override
    public StaffTicket getTicket(UUID ticketId) {
        try {
            return plugin.getStaffTicketManager().getTicket(ticketId);
        } catch (Throwable t) {
            plugin.getLogger().warning("[API] getTicket failed: " + t.getMessage());
            return null;
        }
    }

    @Override
    public List<StaffTicket> getOpenTickets() {
        try {
            return plugin.getStaffTicketManager().getOpenTickets();
        } catch (Throwable t) {
            plugin.getLogger().warning("[API] getOpenTickets failed: " + t.getMessage());
            return List.of();
        }
    }

    @Override
    public boolean claimTicket(UUID ticketId, Player staff) {
        try {
            return plugin.getStaffTicketManager().claimTicket(ticketId, staff);
        } catch (Throwable t) {
            plugin.getLogger().warning("[API] claimTicket failed: " + t.getMessage());
            return false;
        }
    }

    @Override
    public boolean addTicketComment(UUID ticketId, Player author, String message) {
        try {
            return plugin.getStaffTicketManager().addComment(ticketId, author, message);
        } catch (Throwable t) {
            plugin.getLogger().warning("[API] addTicketComment failed: " + t.getMessage());
            return false;
        }
    }

    @Override
    public boolean resolveTicket(UUID ticketId, Player resolver) {
        try {
            return plugin.getStaffTicketManager().resolveTicket(ticketId, resolver);
        } catch (Throwable t) {
            plugin.getLogger().warning("[API] resolveTicket failed: " + t.getMessage());
            return false;
        }
    }
}
