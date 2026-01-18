package me.ycxmbo.mineStaff.managers;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class StaffDataManager {
    private final MineStaff plugin;
    private final Set<UUID> staffMode = new HashSet<>();
    private final Set<UUID> frozen = new HashSet<>();
    private final Set<UUID> vanished = new HashSet<>();

    private final Map<UUID, GameMode> previousGamemode = new HashMap<>();

    private static class InventorySnapshot {
        final ItemStack[] contents;
        final ItemStack[] armor;
        final ItemStack offhand;
        final int heldSlot;
        InventorySnapshot(ItemStack[] contents, ItemStack[] armor, ItemStack offhand, int heldSlot) {
            this.contents = contents; this.armor = armor; this.offhand = offhand; this.heldSlot = heldSlot;
        }
    }
    private final Map<UUID, InventorySnapshot> savedInventories = new HashMap<>();

    public StaffDataManager(MineStaff plugin) { this.plugin = plugin; }

    public boolean isStaffMode(Player p) { return staffMode.contains(p.getUniqueId()); }
    public void enableStaffMode(Player p) { staffMode.add(p.getUniqueId()); }
    public void disableStaffMode(Player p) { staffMode.remove(p.getUniqueId()); }

    /**
     * Completely cleans up all staff mode state for a player.
     * This should be called when you want to ensure no lingering data remains.
     * Use this for logout cleanup or safety checks on join.
     */
    public void cleanupPlayerState(UUID playerId) {
        staffMode.remove(playerId);
        frozen.remove(playerId);
        vanished.remove(playerId);
        previousGamemode.remove(playerId);
        savedInventories.remove(playerId);
    }

    public boolean isFrozen(Player p) { return frozen.contains(p.getUniqueId()); }
    public void setFrozen(Player p, boolean v) { if (v) frozen.add(p.getUniqueId()); else frozen.remove(p.getUniqueId()); }

    public boolean isVanished(Player p) { return vanished.contains(p.getUniqueId()); }
    public void setVanished(Player p, boolean v) { if (v) vanished.add(p.getUniqueId()); else vanished.remove(p.getUniqueId()); }

    public void rememberGamemode(Player p) { previousGamemode.put(p.getUniqueId(), p.getGameMode()); }
    public GameMode popPreviousGamemode(Player p) { return previousGamemode.remove(p.getUniqueId()); }

    public void saveInventory(Player p) {
        UUID id = p.getUniqueId();
        // clone arrays so we don't keep live references
        ItemStack[] contents = cloneArray(p.getInventory().getContents());
        ItemStack[] armor = cloneArray(p.getInventory().getArmorContents());
        ItemStack offhand = p.getInventory().getItemInOffHand() == null ? null : p.getInventory().getItemInOffHand().clone();
        int held = p.getInventory().getHeldItemSlot();
        savedInventories.put(id, new InventorySnapshot(contents, armor, offhand, held));
    }

    public void restoreInventory(Player p) {
        UUID id = p.getUniqueId();
        InventorySnapshot snap = savedInventories.remove(id);
        if (snap == null) return;
        p.getInventory().clear();
        p.getInventory().setContents(cloneArray(snap.contents));
        p.getInventory().setArmorContents(cloneArray(snap.armor));
        p.getInventory().setItemInOffHand(snap.offhand == null ? null : snap.offhand.clone());
        try { p.getInventory().setHeldItemSlot(Math.max(0, Math.min(8, snap.heldSlot))); } catch (Throwable ignored) {}
        p.updateInventory();
    }

    private ItemStack[] cloneArray(ItemStack[] in) {
        if (in == null) return null;
        ItemStack[] out = new ItemStack[in.length];
        for (int i = 0; i < in.length; i++) out[i] = (in[i] == null ? null : in[i].clone());
        return out;
    }
}
