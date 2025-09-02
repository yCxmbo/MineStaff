package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ReportManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

public class ReportsGUIListener implements Listener {
    private final ReportManager reports;

    public ReportsGUIListener(MineStaff plugin) { this.reports = plugin.getReportManager(); }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (e.getView() == null || e.getView().getTitle() == null) return;
        if (!ChatColor.stripColor(e.getView().getTitle()).equalsIgnoreCase("Reports")) return;
        e.setCancelled(true);
        if (e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;

        // Top row filter controls
        int slot = e.getSlot();
        if (slot >= 0 && slot <= 8) {
            switch (slot) {
                case 0: me.ycxmbo.mineStaff.gui.ReportsGUI.cycleStatus(p); break;
                case 1: me.ycxmbo.mineStaff.gui.ReportsGUI.cycleCategory(p); break;
                case 2: me.ycxmbo.mineStaff.gui.ReportsGUI.cyclePriority(p); break;
                case 8: default: break;
            }
            new me.ycxmbo.mineStaff.gui.ReportsGUI(reports).open(p);
            return;
        }

        if (e.getCurrentItem().getItemMeta().getLore() == null) return;
        String idLine = e.getCurrentItem().getItemMeta().getLore().stream()
                .map(ChatColor::stripColor)
                .filter(s -> s.startsWith("ID: "))
                .findFirst().orElse(null);
        if (idLine == null) return;

        UUID id;
        try { id = UUID.fromString(idLine.substring(4).trim()); } catch (Exception ex) { return; }

        switch (e.getClick()) {
            case LEFT:
                reports.setClaimed(id, p.getUniqueId());
                p.sendMessage(ChatColor.AQUA + "Report claimed.");
                me.ycxmbo.mineStaff.MineStaff.getInstance().getAuditLogger().log(java.util.Map.of(
                        "type","report","action","claim","id",id.toString(),"actor",p.getUniqueId().toString()
                ));
                p.closeInventory();
                break;
            case RIGHT:
                reports.setStatus(id, "CLOSED");
                p.sendMessage(ChatColor.GREEN + "Report closed.");
                me.ycxmbo.mineStaff.MineStaff.getInstance().getAuditLogger().log(java.util.Map.of(
                        "type","report","action","close","id",id.toString(),"actor",p.getUniqueId().toString()
                ));
                p.closeInventory();
                break;
            default:
                break;
        }
    }
}
