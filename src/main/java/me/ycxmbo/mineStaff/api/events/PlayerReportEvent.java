package me.ycxmbo.mineStaff.api.events;

import me.ycxmbo.mineStaff.reports.ReportedPlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player is reported
 */
public class PlayerReportEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player reporter;
    private final OfflinePlayer reported;
    private final String reason;
    private boolean cancelled = false;

    public PlayerReportEvent(Player reporter, OfflinePlayer reported, String reason) {
        this.reporter = reporter;
        this.reported = reported;
        this.reason = reason;
    }

    /**
     * Gets the player making the report
     *
     * @return The reporter
     */
    public Player getReporter() {
        return reporter;
    }

    /**
     * Gets the player being reported
     *
     * @return The reported player
     */
    public OfflinePlayer getReported() {
        return reported;
    }

    /**
     * Gets the reason for the report
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
