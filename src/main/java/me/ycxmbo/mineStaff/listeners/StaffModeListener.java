package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import me.ycxmbo.mineStaff.managers.StaffLoginManager;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import me.ycxmbo.mineStaff.util.VanishUtil;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class StaffModeListener implements Listener {
    private final MineStaff plugin;
    private final StaffDataManager staff;
    private final StaffLoginManager login;

    public StaffModeListener(MineStaff plugin) {
        this.plugin = plugin;
        this.staff = plugin.getStaffDataManager();
        this.login = plugin.getStaffLoginManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        // Clean up any lingering state from previous sessions
        // This prevents players from being stuck in staff mode or having corrupted state
        // (e.g., if server crashed before onQuit could properly clean up)
        staff.cleanupPlayerState(p.getUniqueId());

        // Clear login status to force re-authentication
        login.clearLoginStatus(p);

        // If player was persisted as vanished, restore vanish state
        // (vanish is intentionally persistent across sessions)
        if (MineStaff.getInstance().getVanishStore().isVanished(p.getUniqueId())) {
            staff.setVanished(p, true);
            VanishUtil.applyVanish(p, true);
            MineStaff.getInstance().getToolManager().updateVanishDye(p, true);
        }

        // Hide any currently vanished staff from this joiner
        VanishUtil.reapplyForJoin(p);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        // If player is in staff mode, restore their state before they quit
        // This ensures their player data saves correctly with their original inventory
        if (staff.isStaffMode(p)) {
            // Restore gamemode
            GameMode prev = staff.popPreviousGamemode(p);
            if (prev != null) {
                p.setGameMode(prev);
            }

            // Restore inventory
            staff.restoreInventory(p);

            // Remove from staff mode
            staff.disableStaffMode(p);
        }

        // Persist vanish state if player was vanished (vanish persists across sessions)
        if (staff.isVanished(p)) {
            MineStaff.getInstance().getVanishStore().setVanished(p.getUniqueId(), true);
        }

        // Clean up any other lingering state data to prevent getting stuck
        staff.cleanupPlayerState(p.getUniqueId());

        // Use onPlayerDisconnect to preserve session for reconnection
        login.onPlayerDisconnect(p);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p && staff.isStaffMode(p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent e) {
        if (e.getTarget() instanceof Player p && staff.isStaffMode(p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (staff.isStaffMode(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (staff.isStaffMode(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (staff.isStaffMode(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player p && staff.isStaffMode(p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!staff.isStaffMode(p)) return;
        if (p.getGameMode() != GameMode.CREATIVE) return;

        // Prevent taking items in creative mode while in staff mode
        e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!staff.isStaffMode(p)) return;
        if (p.getGameMode() != GameMode.CREATIVE) return;

        // Prevent dragging items in creative mode while in staff mode
        e.setCancelled(true);
    }

    @EventHandler
    public void onCreativeInventory(InventoryCreativeEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!staff.isStaffMode(p)) return;

        // Prevent creative inventory actions while in staff mode
        e.setCancelled(true);
    }
}
