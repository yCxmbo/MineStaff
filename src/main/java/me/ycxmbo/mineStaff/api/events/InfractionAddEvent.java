package me.ycxmbo.mineStaff.api.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when an infraction is added to a player
 */
public class InfractionAddEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final OfflinePlayer player;
    private final Player staff;
    private final String type;
    private final String reason;
    private boolean cancelled = false;

    public InfractionAddEvent(OfflinePlayer player, Player staff, String type, String reason) {
        this.player = player;
        this.staff = staff;
        this.type = type;
        this.reason = reason;
    }

    /**
     * Gets the player receiving the infraction
     *
     * @return The player
     */
    public OfflinePlayer getPlayer() {
        return player;
    }

    /**
     * Gets the staff member issuing the infraction
     *
     * @return The staff member
     */
    public Player getStaff() {
        return staff;
    }

    /**
     * Gets the infraction type
     *
     * @return The type (WARN, MUTE, KICK, BAN)
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the reason for the infraction
     *
     * @return The reason
     */
    public String getReason() {
        return reason;
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
