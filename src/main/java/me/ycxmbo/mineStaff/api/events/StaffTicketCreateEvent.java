package me.ycxmbo.mineStaff.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Called when a staff ticket is created
 */
public class StaffTicketCreateEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final UUID ticketId;
    private final Player creator;
    private final String subject;
    private final String category;
    private final String priority;
    private boolean cancelled = false;

    public StaffTicketCreateEvent(UUID ticketId, Player creator, String subject, String category, String priority) {
        this.ticketId = ticketId;
        this.creator = creator;
        this.subject = subject;
        this.category = category;
        this.priority = priority;
    }

    /**
     * Gets the ticket ID
     *
     * @return The ticket UUID
     */
    public UUID getTicketId() {
        return ticketId;
    }

    /**
     * Gets the player creating the ticket
     *
     * @return The creator
     */
    public Player getCreator() {
        return creator;
    }

    /**
     * Gets the ticket subject
     *
     * @return The subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Gets the ticket category
     *
     * @return The category (QUESTION, TECHNICAL, PERMISSION, OTHER)
     */
    public String getCategory() {
        return category;
    }

    /**
     * Gets the ticket priority
     *
     * @return The priority (LOW, MEDIUM, HIGH, URGENT)
     */
    public String getPriority() {
        return priority;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
