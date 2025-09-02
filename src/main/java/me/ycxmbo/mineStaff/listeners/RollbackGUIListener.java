package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.RollbackManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

public class RollbackGUIListener implements Listener {
    private final RollbackManager rb;

    public RollbackGUIListener(MineStaff plugin) { this.rb = plugin.getRollbackManager(); }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (e.getView() == null || e.getView().getTitle() == null) return;
        if (!ChatColor.stripColor(e.getView().getTitle()).equalsIgnoreCase("Rollbacks")) return;
        e.setCancelled(true);
        if (e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null || e.getCurrentItem().getItemMeta().getLore() == null) return;

        String tsLine = null, targetLine = null;
        for (String l : e.getCurrentItem().getItemMeta().getLore()) {
            String s = ChatColor.stripColor(l);
            if (s.startsWith("TS:")) tsLine = s.substring(3);
            if (s.startsWith("TARGET:")) targetLine = s.substring(7);
        }
        if (tsLine == null || targetLine == null) return;

        long ts;
        UUID targetId;
        try { ts = Long.parseLong(tsLine.trim()); targetId = UUID.fromString(targetLine.trim()); }
        catch (Exception ex) { return; }

        OfflinePlayer off = Bukkit.getOfflinePlayer(targetId);
        if (!off.isOnline()) {
            p.sendMessage(ChatColor.RED + "Target is not online for restore.");
            return;
        }
        Player target = off.getPlayer();
        var snaps = rb.getSnapshots(targetId);
        var snap = snaps.get(ts);
        if (snap == null) {
            p.sendMessage(ChatColor.RED + "Snapshot not found.");
            return;
        }
        target.getInventory().setContents(snap.inv);
        target.getEnderChest().setContents(snap.ec);
        target.updateInventory();
        p.sendMessage(ChatColor.GREEN + "Restored " + target.getName() + "'s inventory from snapshot.");
        target.sendMessage(ChatColor.YELLOW + "Your inventory was restored by staff.");
        me.ycxmbo.mineStaff.MineStaff.getInstance().getAuditLogger().log(java.util.Map.of(
                "type","rollback","actor",p.getUniqueId().toString(),
                "target",target.getUniqueId().toString(),
                "timestamp",String.valueOf(ts)
        ));
        p.closeInventory();
    }
}
