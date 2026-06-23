package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.gui.PunishmentGUI;
import me.ycxmbo.mineStaff.punishments.PunishmentManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class PunishmentGUIListener implements Listener {
    private final MineStaff plugin;

    public PunishmentGUIListener(MineStaff plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (e.getView() == null || e.getView().getTitle() == null) return;
        String title = ChatColor.stripColor(e.getView().getTitle());
        if (title == null || !title.startsWith(PunishmentGUI.TITLE_PREFIX)) return;

        e.setCancelled(true);
        ItemStack item = e.getCurrentItem();
        if (item == null || item.getItemMeta() == null) return;

        if ("Close".equalsIgnoreCase(ChatColor.stripColor(item.getItemMeta().getDisplayName()))) {
            p.closeInventory();
            return;
        }

        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) return;

        String action = null, dur = null, reason = null, targetStr = null, targetName = null;
        for (String raw : lore) {
            String s = ChatColor.stripColor(raw);
            if (s.startsWith("ACTION:")) action = s.substring(7);
            else if (s.startsWith("DUR:")) dur = s.substring(4);
            else if (s.startsWith("REASON:")) reason = s.substring(7);
            else if (s.startsWith("TARGET:")) targetStr = s.substring(7);
            else if (s.startsWith("TNAME:")) targetName = s.substring(6);
        }
        if (action == null || targetStr == null || targetName == null) return;

        UUID target;
        try { target = UUID.fromString(targetStr.trim()); }
        catch (Exception ex) { return; }

        if (!p.hasPermission("staffmode.punish")) {
            p.sendMessage(plugin.getConfigManager().getMessage("no_permission", "You don't have permission."));
            p.closeInventory();
            return;
        }

        PunishmentManager pm = plugin.getPunishmentManager();
        long durationMs = PunishmentManager.parseDuration(dur);
        if (reason == null || reason.isBlank()) reason = "Staff action";

        switch (action) {
            case "ban" -> pm.ban(target, targetName, p.getName(), reason, durationMs <= 0 ? 0 : durationMs);
            case "mute" -> pm.mute(target, targetName, p.getName(), reason, durationMs <= 0 ? 0 : durationMs);
            case "kick" -> pm.kick(target, targetName, p.getName(), reason);
            case "unban" -> pm.unban(target, targetName, p.getName());
            case "unmute" -> pm.unmute(target, targetName, p.getName());
            default -> { return; }
        }
        p.sendMessage(plugin.getConfigManager().getMessage("punishment_applied", "&a✔ &e{type} &aapplied to &f{target}&a.")
                .replace("{type}", action.toUpperCase())
                .replace("{target}", targetName));
        p.closeInventory();
    }
}
