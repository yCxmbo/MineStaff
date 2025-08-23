package me.ycxmbo.mineStaff.api.events;

import me.ycxmbo.mineStaff.api.MineStaffAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FreezeToggleEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player target;
    private final boolean enabled;
    private final MineStaffAPI.ToggleCause cause;

    public FreezeToggleEvent(Player target, boolean enabled, MineStaffAPI.ToggleCause cause) {
        this.target = target; this.enabled = enabled; this.cause = cause;
    }
    public Player getTarget() { return target; }
    public boolean isEnabled() { return enabled; }
    public MineStaffAPI.ToggleCause getCause() { return cause; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
