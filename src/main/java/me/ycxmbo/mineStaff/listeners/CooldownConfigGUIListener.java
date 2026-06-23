package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.gui.CooldownConfigGUI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CooldownConfigGUIListener implements Listener {
    private final MineStaff plugin;

    public CooldownConfigGUIListener(MineStaff plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (e.getView() == null || e.getView().getTitle() == null) return;
        if (!ChatColor.stripColor(e.getView().getTitle()).equalsIgnoreCase(CooldownConfigGUI.TITLE)) return;

        e.setCancelled(true);
        if (!p.hasPermission("staffmode.cooldowns")) { p.closeInventory(); return; }

        ItemStack item = e.getCurrentItem();
        if (item == null || item.getItemMeta() == null) return;

        if ("Close".equalsIgnoreCase(ChatColor.stripColor(item.getItemMeta().getDisplayName()))) {
            p.closeInventory();
            return;
        }

        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) return;
        String path = null;
        for (String raw : lore) {
            String s = ChatColor.stripColor(raw);
            if (s.startsWith("PATH:")) { path = s.substring(5); break; }
        }
        if (path == null) return;

        int step = e.isShiftClick() ? 1000 : 250;
        if (e.isRightClick()) step = -step;

        int current = plugin.getConfig().getInt(path, 0);
        int updated = Math.max(0, current + step);
        plugin.getConfig().set(path, updated);
        plugin.saveConfig();

        plugin.getCooldownConfigGUI().open(p);
        p.sendMessage(plugin.getConfigManager().getMessage("cooldown_set", "&a✔ Cooldown updated: &f{path} &8= &e{value}ms")
                .replace("{path}", path)
                .replace("{value}", String.valueOf(updated)));
    }
}
