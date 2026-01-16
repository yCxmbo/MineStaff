package me.ycxmbo.mineStaff.api;

import me.ycxmbo.mineStaff.managers.InfractionManager;
import me.ycxmbo.mineStaff.reports.ReportedPlayer;
import me.ycxmbo.mineStaff.tickets.StaffTicket;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MineStaffAPI {
    // ========== Staff Mode ==========
    boolean isStaffMode(Player player);
    boolean isStaffMode(UUID playerId);

    boolean isVanished(Player player);
    boolean isVanished(UUID playerId);

    boolean isFrozen(Player player);
    boolean isFrozen(UUID playerId);

    /** Toggle/set; returns final state. */
    boolean setStaffMode(Player player, boolean enabled, ToggleCause cause);
    boolean setVanish(Player player, boolean enabled, ToggleCause cause);
    boolean setFrozen(Player target, boolean enabled, ToggleCause cause);

    StaffSnapshot snapshot(Player player);

    // ========== Reports ==========
    /**
     * Creates a new player report
     * @param reporter The player making the report
     * @param reported The player being reported
     * @param reason The reason for the report
     * @return The created ReportedPlayer object, or null if failed
     */
    ReportedPlayer createReport(Player reporter, OfflinePlayer reported, String reason);

    /**
     * Gets all active reports
     * @return List of active reports
     */
    List<ReportedPlayer> getActiveReports();

    /**
     * Gets reports for a specific player
     * @param player The player to get reports for
     * @return List of reports for the player
     */
    List<ReportedPlayer> getReportsFor(OfflinePlayer player);

    /**
     * Closes a report
     * @param reportId The ID of the report to close
     * @param staff The staff member closing it
     * @return true if successful, false if report not found
     */
    boolean closeReport(UUID reportId, Player staff);

    // ========== Infractions ==========
    /**
     * Adds an infraction to a player
     * @param player The player receiving the infraction
     * @param staff The staff member issuing it
     * @param type The type of infraction (WARN, MUTE, KICK, BAN)
     * @param reason The reason for the infraction
     * @return The ID of the created infraction
     */
    int addInfraction(OfflinePlayer player, Player staff, String type, String reason);

    /**
     * Gets all infractions for a player
     * @param player The player to get infractions for
     * @return List of infractions
     */
    List<InfractionManager.Infraction> getInfractions(OfflinePlayer player);

    /**
     * Gets infraction count for a player
     * @param player The player
     * @return Number of infractions
     */
    int getInfractionCount(OfflinePlayer player);

    /**
     * Removes an infraction
     * @param id The infraction ID
     * @return true if successful, false if not found
     */
    boolean removeInfraction(int id);

    // ========== Notes ==========
    /**
     * Adds a staff note to a player
     * @param player The player the note is about
     * @param staff The staff member adding the note
     * @param note The note content
     * @return The ID of the created note
     */
    int addNote(OfflinePlayer player, Player staff, String note);

    /**
     * Gets all notes for a player
     * @param player The player to get notes for
     * @return List of notes
     */
    List<InfractionManager.Note> getNotes(OfflinePlayer player);

    /**
     * Removes a note
     * @param id The note ID
     * @return true if successful, false if not found
     */
    boolean removeNote(int id);

    // ========== Tickets ==========
    /**
     * Creates a new staff ticket
     * @param creator The player creating the ticket
     * @param subject The ticket subject
     * @param description The ticket description
     * @param category The category (QUESTION, TECHNICAL, PERMISSION, OTHER)
     * @param priority The priority (LOW, MEDIUM, HIGH, URGENT)
     * @return The UUID of the created ticket
     */
    UUID createTicket(Player creator, String subject, String description, String category, String priority);

    /**
     * Gets a ticket by ID
     * @param ticketId The ticket ID
     * @return The ticket, or null if not found
     */
    StaffTicket getTicket(UUID ticketId);

    /**
     * Gets all open tickets
     * @return List of open tickets
     */
    List<StaffTicket> getOpenTickets();

    /**
     * Claims a ticket
     * @param ticketId The ticket ID
     * @param staff The staff member claiming it
     * @return true if successful, false otherwise
     */
    boolean claimTicket(UUID ticketId, Player staff);

    /**
     * Adds a comment to a ticket
     * @param ticketId The ticket ID
     * @param author The comment author
     * @param message The comment message
     * @return true if successful, false if ticket not found
     */
    boolean addTicketComment(UUID ticketId, Player author, String message);

    /**
     * Resolves a ticket
     * @param ticketId The ticket ID
     * @param resolver The staff member resolving it
     * @return true if successful, false if ticket not found or already closed
     */
    boolean resolveTicket(UUID ticketId, Player resolver);

    // ========== Utility ==========
    enum ToggleCause { COMMAND, TOOL, API, OTHER }

    record StaffSnapshot(UUID uuid, boolean staffMode, boolean vanished, boolean frozen) {}

    static Optional<MineStaffAPI> get() {
        var plugin = org.bukkit.Bukkit.getPluginManager().getPlugin("MineStaff");
        if (plugin == null || !plugin.isEnabled()) return Optional.empty();
        var reg = org.bukkit.Bukkit.getServicesManager().getRegistration(MineStaffAPI.class);
        return reg == null ? Optional.empty() : Optional.of(reg.getProvider());
    }
}
