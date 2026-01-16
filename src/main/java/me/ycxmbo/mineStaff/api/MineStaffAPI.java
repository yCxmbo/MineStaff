package me.ycxmbo.mineStaff.api;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.api.events.*;
import me.ycxmbo.mineStaff.managers.*;
import me.ycxmbo.mineStaff.reports.ReportedPlayer;
import me.ycxmbo.mineStaff.tickets.StaffTicket;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;

/**
 * Main API interface for MineStaff plugin
 * <p>
 * This API provides access to MineStaff's core functionality for third-party plugins.
 * All methods are safe to call from other plugins and will handle edge cases appropriately.
 * <p>
 * Usage:
 * <pre>{@code
 * Plugin mineStaffPlugin = Bukkit.getPluginManager().getPlugin("MineStaff");
 * if (mineStaffPlugin != null && mineStaffPlugin instanceof MineStaff) {
 *     MineStaffAPI api = ((MineStaff) mineStaffPlugin).getAPI();
 *     // Use API methods
 * }
 * }</pre>
 *
 * @version 1.0
 * @since 1.0
 */
public class MineStaffAPI {
    private final MineStaff plugin;

    public MineStaffAPI(MineStaff plugin) {
        this.plugin = plugin;
    }

    // ========================
    // Staff Mode Management
    // ========================

    /**
     * Checks if a player is in staff mode
     *
     * @param player The player to check
     * @return true if player is in staff mode, false otherwise
     */
    public boolean isInStaffMode(Player player) {
        return plugin.getStaffModeManager().isInStaffMode(player);
    }

    /**
     * Enables staff mode for a player
     *
     * @param player The player to enable staff mode for
     * @return true if successful, false otherwise
     */
    public boolean enableStaffMode(Player player) {
        if (isInStaffMode(player)) return false;
        plugin.getStaffModeManager().enableStaffMode(player);
        return true;
    }

    /**
     * Disables staff mode for a player
     *
     * @param player The player to disable staff mode for
     * @return true if successful, false otherwise
     */
    public boolean disableStaffMode(Player player) {
        if (!isInStaffMode(player)) return false;
        plugin.getStaffModeManager().disableStaffMode(player);
        return true;
    }

    /**
     * Gets all players currently in staff mode
     *
     * @return List of players in staff mode
     */
    public List<Player> getStaffModePlayers() {
        return plugin.getStaffModeManager().getStaffModePlayers();
    }

    // ========================
    // Vanish Management
    // ========================

    /**
     * Checks if a player is vanished
     *
     * @param player The player to check
     * @return true if player is vanished, false otherwise
     */
    public boolean isVanished(Player player) {
        return plugin.getVanishManager().isVanished(player);
    }

    /**
     * Sets a player's vanish state
     *
     * @param player   The player
     * @param vanished true to vanish, false to unvanish
     */
    public void setVanished(Player player, boolean vanished) {
        if (vanished) {
            plugin.getVanishManager().vanish(player);
        } else {
            plugin.getVanishManager().unvanish(player);
        }
    }

    /**
     * Gets all vanished players
     *
     * @return List of vanished players
     */
    public List<Player> getVanishedPlayers() {
        return plugin.getVanishManager().getVanishedPlayers();
    }

    // ========================
    // Freeze Management
    // ========================

    /**
     * Checks if a player is frozen
     *
     * @param player The player to check
     * @return true if player is frozen, false otherwise
     */
    public boolean isFrozen(Player player) {
        return plugin.getFreezeManager().isFrozen(player);
    }

    /**
     * Freezes a player
     *
     * @param player The player to freeze
     * @param staff  The staff member freezing them (can be null for API calls)
     * @return true if successful, false if already frozen
     */
    public boolean freezePlayer(Player player, Player staff) {
        if (isFrozen(player)) return false;
        plugin.getFreezeManager().freezePlayer(player, staff);
        return true;
    }

    /**
     * Unfreezes a player
     *
     * @param player The player to unfreeze
     * @return true if successful, false if not frozen
     */
    public boolean unfreezePlayer(Player player) {
        if (!isFrozen(player)) return false;
        plugin.getFreezeManager().unfreezePlayer(player);
        return true;
    }

    /**
     * Gets all frozen players
     *
     * @return List of frozen player UUIDs
     */
    public List<UUID> getFrozenPlayers() {
        return plugin.getFreezeManager().getFrozenPlayers();
    }

    // ========================
    // Report Management
    // ========================

    /**
     * Creates a new player report
     *
     * @param reporter The player making the report
     * @param reported The player being reported
     * @param reason   The reason for the report
     * @return The created ReportedPlayer object, or null if failed
     */
    public ReportedPlayer createReport(Player reporter, OfflinePlayer reported, String reason) {
        return plugin.getReportManager().reportPlayer(reporter, reported, reason);
    }

    /**
     * Gets all active reports
     *
     * @return List of active reports
     */
    public List<ReportedPlayer> getActiveReports() {
        return plugin.getReportManager().getActiveReports();
    }

    /**
     * Gets reports for a specific player
     *
     * @param player The player to get reports for
     * @return List of reports for the player
     */
    public List<ReportedPlayer> getReportsFor(OfflinePlayer player) {
        return plugin.getReportManager().getReportsFor(player);
    }

    /**
     * Closes a report
     *
     * @param reportId The ID of the report to close
     * @param staff    The staff member closing it
     * @return true if successful, false if report not found
     */
    public boolean closeReport(UUID reportId, Player staff) {
        ReportedPlayer report = plugin.getReportManager().getReport(reportId);
        if (report == null) return false;
        plugin.getReportManager().closeReport(reportId, staff);
        return true;
    }

    // ========================
    // Infraction Management
    // ========================

    /**
     * Adds an infraction to a player
     *
     * @param player The player receiving the infraction
     * @param staff  The staff member issuing it
     * @param type   The type of infraction (WARN, MUTE, KICK, BAN)
     * @param reason The reason for the infraction
     * @return The ID of the created infraction
     */
    public int addInfraction(OfflinePlayer player, Player staff, String type, String reason) {
        return plugin.getInfractionManager().addInfraction(
                player.getUniqueId(),
                staff.getUniqueId(),
                type,
                reason
        );
    }

    /**
     * Gets all infractions for a player
     *
     * @param player The player to get infractions for
     * @return List of infractions
     */
    public List<InfractionManager.Infraction> getInfractions(OfflinePlayer player) {
        return plugin.getInfractionManager().getInfractions(player.getUniqueId());
    }

    /**
     * Gets infraction count for a player
     *
     * @param player The player
     * @return Number of infractions
     */
    public int getInfractionCount(OfflinePlayer player) {
        return plugin.getInfractionManager().getInfractionCount(player.getUniqueId());
    }

    /**
     * Removes an infraction
     *
     * @param id The infraction ID
     * @return true if successful, false if not found
     */
    public boolean removeInfraction(int id) {
        return plugin.getInfractionManager().removeInfraction(id);
    }

    // ========================
    // Note Management
    // ========================

    /**
     * Adds a staff note to a player
     *
     * @param player The player the note is about
     * @param staff  The staff member adding the note
     * @param note   The note content
     * @return The ID of the created note
     */
    public int addNote(OfflinePlayer player, Player staff, String note) {
        return plugin.getInfractionManager().addNote(
                player.getUniqueId(),
                staff.getUniqueId(),
                note
        );
    }

    /**
     * Gets all notes for a player
     *
     * @param player The player to get notes for
     * @return List of notes
     */
    public List<InfractionManager.Note> getNotes(OfflinePlayer player) {
        return plugin.getInfractionManager().getNotes(player.getUniqueId());
    }

    /**
     * Removes a note
     *
     * @param id The note ID
     * @return true if successful, false if not found
     */
    public boolean removeNote(int id) {
        return plugin.getInfractionManager().removeNote(id);
    }

    // ========================
    // Ticket Management
    // ========================

    /**
     * Creates a new staff ticket
     *
     * @param creator     The player creating the ticket
     * @param subject     The ticket subject
     * @param description The ticket description
     * @param category    The category (QUESTION, TECHNICAL, PERMISSION, OTHER)
     * @param priority    The priority (LOW, MEDIUM, HIGH, URGENT)
     * @return The UUID of the created ticket
     */
    public UUID createTicket(Player creator, String subject, String description, String category, String priority) {
        return plugin.getStaffTicketManager().createTicket(creator, subject, description, category, priority);
    }

    /**
     * Gets a ticket by ID
     *
     * @param ticketId The ticket ID
     * @return The ticket, or null if not found
     */
    public StaffTicket getTicket(UUID ticketId) {
        return plugin.getStaffTicketManager().getTicket(ticketId);
    }

    /**
     * Gets all open tickets
     *
     * @return List of open tickets
     */
    public List<StaffTicket> getOpenTickets() {
        return plugin.getStaffTicketManager().getOpenTickets();
    }

    /**
     * Claims a ticket
     *
     * @param ticketId The ticket ID
     * @param staff    The staff member claiming it
     * @return true if successful, false otherwise
     */
    public boolean claimTicket(UUID ticketId, Player staff) {
        return plugin.getStaffTicketManager().claimTicket(ticketId, staff);
    }

    /**
     * Adds a comment to a ticket
     *
     * @param ticketId The ticket ID
     * @param author   The comment author
     * @param message  The comment message
     * @return true if successful, false if ticket not found
     */
    public boolean addTicketComment(UUID ticketId, Player author, String message) {
        return plugin.getStaffTicketManager().addComment(ticketId, author, message);
    }

    /**
     * Resolves a ticket
     *
     * @param ticketId The ticket ID
     * @param resolver The staff member resolving it
     * @return true if successful, false if ticket not found or already closed
     */
    public boolean resolveTicket(UUID ticketId, Player resolver) {
        return plugin.getStaffTicketManager().resolveTicket(ticketId, resolver);
    }

    // ========================
    // Utility Methods
    // ========================

    /**
     * Gets the plugin version
     *
     * @return Plugin version string
     */
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    /**
     * Gets the MineStaff plugin instance
     * <p>
     * Use this sparingly - prefer using API methods when available
     *
     * @return The plugin instance
     */
    public MineStaff getPlugin() {
        return plugin;
    }

    /**
     * Checks if a feature is enabled in the config
     *
     * @param feature The feature path in config (e.g., "staff-mode.enabled")
     * @return true if enabled, false otherwise
     */
    public boolean isFeatureEnabled(String feature) {
        return plugin.getConfig().getBoolean(feature, false);
    }

    /**
     * Plays a configured sound to a player
     *
     * @param player The player
     * @param key    The sound key from sounds.yml (e.g., "alert.normal")
     */
    public void playSound(Player player, String key) {
        plugin.getSoundManager().playSound(player, key);
    }
}
