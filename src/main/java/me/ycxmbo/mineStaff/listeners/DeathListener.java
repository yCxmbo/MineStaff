package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.RollbackManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {
    private final RollbackManager rb;

    public DeathListener(MineStaff plugin) { this.rb = new RollbackManager(plugin); }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        var p = e.getEntity();
        rb.saveSnapshot(p.getUniqueId(), new RollbackManager.Snapshot(p.getInventory().getContents(), p.getEnderChest().getContents()));
    }
}
