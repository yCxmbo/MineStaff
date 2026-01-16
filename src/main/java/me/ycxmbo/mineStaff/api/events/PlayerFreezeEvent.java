package me.ycxmbo.mineStaff.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player is frozen or unfrozen
 */
public class PlayerFreezeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Player staff;
    private final boolean freezing;
    private boolean cancelled = false;

    public PlayerFreezeEvent(Player player, Player staff, boolean freezing) {
        this.player = player;
        this.staff = staff;
        this.freezing = freezing;
    }

    /**
     * Gets the player being frozen/unfrozen
     *
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the staff member who initiated the freeze/unfreeze
     *
     * @return The staff member, or null if done via API
     */
    public Player getStaff() {
        return staff;
    }

    /**
     * Checks if the player is being frozen
     *
     * @return true if freezing, false if unfreezing
     */
    public boolean isFreezing() {
        return freezing;
    }

    /**
     * Checks if the player is being unfrozen
     *
     * @return true if unfreezing, false if freezing
     */
    public boolean isUnfreezing() {
        return !freezing;
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
