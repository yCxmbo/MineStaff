package me.ycxmbo.mineStaff.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CPSCheckStartEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player staff, target;

    public CPSCheckStartEvent(Player staff, Player target) {
        this.staff = staff; this.target = target;
    }
    public Player getStaff() { return staff; }
    public Player getTarget() { return target; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
