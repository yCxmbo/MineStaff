package me.ycxmbo.mineStaff.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CPSCheckFinishEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player staff, target;
    private final double cps;

    public CPSCheckFinishEvent(Player staff, Player target, double cps) {
        this.staff = staff; this.target = target; this.cps = cps;
    }
    public Player getStaff() { return staff; }
    public Player getTarget() { return target; }
    public double getCps() { return cps; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
