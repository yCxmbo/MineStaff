package me.ycxmbo.mineStaff.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player's vanish state changes
 */
public class PlayerVanishEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final boolean vanishing;
    private boolean cancelled = false;

    public PlayerVanishEvent(Player player, boolean vanishing) {
        this.player = player;
        this.vanishing = vanishing;
    }

    /**
     * Gets the player whose vanish state is changing
     *
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Checks if the player is vanishing
     *
     * @return true if vanishing, false if unvanishing
     */
    public boolean isVanishing() {
        return vanishing;
    }

    /**
     * Checks if the player is unvanishing
     *
     * @return true if unvanishing, false if vanishing
     */
    public boolean isUnvanishing() {
        return !vanishing;
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
