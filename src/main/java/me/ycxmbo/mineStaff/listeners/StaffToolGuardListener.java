package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import me.ycxmbo.mineStaff.tools.ToolManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

public class StaffToolGuardListener implements Listener {
    private final StaffDataManager data;

    public StaffToolGuardListener(MineStaff plugin) {
        this.data = plugin.getStaffDataManager();
    }

    private boolean isStaffTool(ItemStack it) {
        if (it == null) return false;
        Material m = it.getType();
        return m == ToolManager.TELEPORT_TOOL
                || m == ToolManager.FREEZE_TOOL
                || m == ToolManager.INSPECT_TOOL
                || m == ToolManager.VANISH_TOOL;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!data.isStaffMode(p)) return;

        // Cancel when trying to pick up / place / swap any staff tool
        boolean touchingTool =
                isStaffTool(e.getCurrentItem()) ||
                        isStaffTool(e.getCursor());

        // Also handle number-key hotbar swaps (the tool might be in the hotbar slot)
        if (e.getClick() == ClickType.NUMBER_KEY) {
            int hotbar = e.getHotbarButton();
            if (hotbar >= 0 && hotbar < 9) {
                ItemStack hotbarItem = p.getInventory().getItem(hotbar);
                if (isStaffTool(hotbarItem)) touchingTool = true;
            }
        }

        if (touchingTool) {
            e.setCancelled(true);
            p.sendActionBar(ChatColor.RED + "You can't move staff tools in Staff Mode.");
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!data.isStaffMode(p)) return;

        // If the cursor/old cursor is a staff tool, cancel all drag operations
        if (isStaffTool(e.getCursor()) || isStaffTool(e.getOldCursor())) {
            e.setCancelled(true);
            p.sendActionBar(ChatColor.RED + "You can't move staff tools in Staff Mode.");
        }
    }
}
