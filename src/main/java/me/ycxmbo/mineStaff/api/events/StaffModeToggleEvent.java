package me.ycxmbo.mineStaff.api.events;

import me.ycxmbo.mineStaff.api.MineStaffAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class StaffModeToggleEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final boolean enabled;
    private final MineStaffAPI.ToggleCause cause;

    public StaffModeToggleEvent(Player player, boolean enabled, MineStaffAPI.ToggleCause cause) {
        this.player = player; this.enabled = enabled; this.cause = cause;
    }
    public Player getPlayer() { return player; }
    public boolean isEnabled() { return enabled; }
    public MineStaffAPI.ToggleCause getCause() { return cause; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
