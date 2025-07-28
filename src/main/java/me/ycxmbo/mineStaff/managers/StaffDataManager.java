package me.ycxmbo.mineStaff.managers;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StaffDataManager {

    private final MineStaff plugin;

    public StaffDataManager(MineStaff plugin) {
        this.plugin = plugin;
    }

    private static class StaffData {
        private final ItemStack[] inventoryContents;
        private final ItemStack[] armorContents;
        private final GameMode originalGameMode;
        private final Location originalLocation;

        public StaffData(Player player) {
            this.inventoryContents = player.getInventory().getContents();
            this.armorContents = player.getInventory().getArmorContents();
            this.originalGameMode = player.getGameMode();
            this.originalLocation = player.getLocation();
        }

        public void restore(Player player) {
            player.getInventory().setContents(inventoryContents);
            player.getInventory().setArmorContents(armorContents);
            player.setGameMode(originalGameMode);
            player.teleport(originalLocation);
        }
    }

    private final HashMap<UUID, StaffData> staffMap = new HashMap<>();

    public void enableStaffMode(Player player) {
        staffMap.put(player.getUniqueId(), new StaffData(player));
        player.getInventory().clear();
        Bukkit.getOnlinePlayers().forEach(p -> {
            if (!p.hasPermission("staffmode.use")) {
                p.hidePlayer(plugin, player);
            }
        });
    }

    public void disableStaffMode(Player player) {
        StaffData data = staffMap.remove(player.getUniqueId());
        if (data != null) {
            data.restore(player);
        }
        Bukkit.getOnlinePlayers().forEach(p -> p.showPlayer(plugin, player));
    }

    public boolean isInStaffMode(Player player) {
        return staffMap.containsKey(player.getUniqueId());
    }

    // New getter added here
    public Map<UUID, StaffData> getStaffMap() {
        return Collections.unmodifiableMap(staffMap);
    }
}
