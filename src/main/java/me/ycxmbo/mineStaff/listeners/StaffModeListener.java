package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import me.ycxmbo.mineStaff.tools.ToolManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class StaffModeListener implements Listener {

    private final MineStaff plugin;
    private final ToolManager toolManager;
    private final StaffDataManager staffManager;

    // Keep track of frozen players
    private final Set<UUID> frozenPlayers = new HashSet<>();

    public StaffModeListener(MineStaff plugin) {
        this.plugin = plugin;
        this.toolManager = plugin.getToolManager();
        this.staffManager = plugin.getStaffDataManager();
    }

    @EventHandler
    public void onRightClickEntity(PlayerInteractEntityEvent event) {
        Player staff = event.getPlayer();
        if (!(event.getRightClicked() instanceof Player target)) return;

        if (!staffManager.isInStaffMode(staff)) return;
        ItemStack item = staff.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return;

        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        switch (displayName.toLowerCase()) {
            case "inspect player" -> staff.openInventory(target.getInventory());
            case "freeze player" -> {
                UUID uuid = target.getUniqueId();
                boolean isFrozen = frozenPlayers.contains(uuid);
                if (isFrozen) {
                    frozenPlayers.remove(uuid);
                    target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfig().getString("messages.unfreeze_notify", "&aYou have been unfrozen.")));
                    staff.sendMessage(ChatColor.GREEN + "Unfroze " + target.getName());
                } else {
                    frozenPlayers.add(uuid);
                    target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfig().getString("messages.freeze_notify", "&eYou have been frozen by a staff member.")));
                    staff.sendMessage(ChatColor.RED + "Froze " + target.getName());
                }
            }
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player staff = event.getPlayer();
        if (!staffManager.isInStaffMode(staff)) return;
        ItemStack item = staff.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return;

        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        switch (displayName.toLowerCase()) {
            case "random teleport" -> {
                List<Player> onlinePlayers = Bukkit.getOnlinePlayers().stream()
                        .filter(p -> !p.equals(staff))
                        .filter(p -> !staffManager.isInStaffMode(p))
                        .collect(Collectors.toCollection(ArrayList::new));

                if (onlinePlayers.isEmpty()) {
                    staff.sendMessage(ChatColor.RED + plugin.getConfig().getString("messages.teleport_failed", "No players to teleport to."));
                    return;
                }

                Player target = onlinePlayers.get(new Random().nextInt(onlinePlayers.size()));
                staff.teleport(target.getLocation());
                staff.sendMessage(ChatColor.YELLOW + "Teleported to " + target.getName());
            }

            case "toggle vanish" -> {
                boolean vanished = staff.hasPotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY);
                if (vanished) {
                    staff.removePotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY);
                    Bukkit.getOnlinePlayers().forEach(p -> p.showPlayer(plugin, staff));
                    staff.sendMessage(ChatColor.GREEN + "You are now visible.");
                } else {
                    staff.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
                    Bukkit.getOnlinePlayers().forEach(p -> {
                        if (!p.hasPermission("staffmode.use")) {
                            p.hidePlayer(plugin, staff);
                        }
                    });
                    staff.sendMessage(ChatColor.YELLOW + "You are now invisible.");
                }

                // Update vanish tool to reflect current vanish state
                toolManager.giveStaffTools(staff);
            }
        }
    }

    @EventHandler
    public void onMove(org.bukkit.event.player.PlayerMoveEvent event) {
        if (frozenPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setTo(event.getFrom()); // prevent movement
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (staffManager.isInStaffMode(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (staffManager.isInStaffMode(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (staffManager.isInStaffMode(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player p && staffManager.isInStaffMode(p)) {
            if (event.getClickedInventory() instanceof PlayerInventory) {
                event.setCancelled(true); // Prevent modifying own inventory in staff mode
            }
        }
    }

    public boolean isFrozen(Player player) {
        return frozenPlayers.contains(player.getUniqueId());
    }

    public Set<UUID> getFrozenPlayers() {
        return Collections.unmodifiableSet(frozenPlayers);
    }
}
