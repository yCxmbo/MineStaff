package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SilentChestListener implements Listener {
    private static final String VIEW_PREFIX = ChatColor.DARK_GRAY + "SilentView: " + ChatColor.GRAY;

    private final StaffDataManager data;
    private final MineStaff plugin;

    public SilentChestListener(MineStaff plugin) {
        this.plugin = plugin;
        this.data = plugin.getStaffDataManager();
    }

    @EventHandler(ignoreCancelled = true)
    public void onOpen(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = e.getClickedBlock();
        if (block == null) return;
        if (!(block.getState() instanceof Container container)) return;

        Player p = e.getPlayer();
        if (!data.isStaffMode(p)) return;

        // config: require sneak to open silently (defaults to true)
        boolean requireSneak = plugin.getConfigManager().getConfig()
                .getBoolean("silent-chest.require-sneak", true);
        if (requireSneak && !p.isSneaking()) return;

        // Intercept normal open and show a detached copy instead
        e.setCancelled(true);

        Inventory source = container.getInventory();
        Inventory copy = Bukkit.createInventory(
                p,
                source.getSize(),
                VIEW_PREFIX + prettify(block.getType().name())
        );
        copy.setContents(source.getContents());

        p.openInventory(copy);
        try { p.playSound(p.getLocation(), Sound.UI_TOAST_IN, 0.4f, 1.4f); } catch (Throwable ignored) {}
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!data.isStaffMode(p)) return;
        String title = e.getView().getTitle();
        if (title == null || !ChatColor.stripColor(title).startsWith("SilentView:")) return;

        // Absolutely no taking/placing/swap in silent view
        e.setCancelled(true);

        // Also nuke number-key swaps that reference hotbar items
        if (e.getClick() == ClickType.NUMBER_KEY) {
            int hotbar = e.getHotbarButton();
            if (hotbar >= 0 && hotbar < 9) {
                ItemStack hb = p.getInventory().getItem(hotbar);
                if (hb != null) e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!data.isStaffMode(p)) return;
        String title = e.getView().getTitle();
        if (title == null || !ChatColor.stripColor(title).startsWith("SilentView:")) return;

        // No drag moves in the silent view
        e.setCancelled(true);
    }

    private String prettify(String name) {
        String lower = name.toLowerCase().replace('_', ' ');
        String[] parts = lower.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            sb.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.substring(1))
                    .append(' ');
        }
        return sb.toString().trim();
    }
}
