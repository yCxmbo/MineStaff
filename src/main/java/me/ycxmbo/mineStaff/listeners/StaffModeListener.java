package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import me.ycxmbo.mineStaff.managers.StaffLoginManager;
import me.ycxmbo.mineStaff.managers.ActionLogger;
import me.ycxmbo.mineStaff.managers.CPSManager;
import me.ycxmbo.mineStaff.tools.ToolManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class StaffModeListener implements Listener {

    private final MineStaff plugin;
    private final ToolManager toolManager;
    private final StaffDataManager staffManager;
    private final StaffLoginManager loginManager;
    private final ActionLogger actionLogger;
    private final CPSManager cpsManager;
    private final Map<UUID, Long> toolCooldowns = new HashMap<>();
    private final long cooldownDuration = 20 * 5; // 5 seconds

    public StaffModeListener(MineStaff plugin) {
        this.plugin = plugin;
        this.toolManager = plugin.getToolManager();
        this.staffManager = plugin.getStaffDataManager();
        this.loginManager = plugin.getStaffLoginManager();
        this.actionLogger = plugin.getActionLogger();
        this.cpsManager = plugin.getCPSManager();
    }

    public boolean isOnCooldown(Player player) {
        return toolCooldowns.containsKey(player.getUniqueId()) &&
                System.currentTimeMillis() < toolCooldowns.get(player.getUniqueId());
    }

    public void setCooldown(Player player) {
        toolCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldownDuration);
    }

    private boolean canUseStaffTools(Player player) {
        return staffManager.isInStaffMode(player) && loginManager.isLoggedIn(player);
    }

    @EventHandler
    public void onRightClickEntity(PlayerInteractEntityEvent event) {
        Player staff = event.getPlayer();
        if (!canUseStaffTools(staff)) return;

        if (!(event.getRightClicked() instanceof Player target)) return;

        ItemStack item = staff.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return;

        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        switch (displayName.toLowerCase()) {
            case "inspect player" -> staff.openInventory(target.getInventory());
            case "freeze player" -> {
                if (staffManager.isFrozen(target)) {
                    staffManager.unfreezePlayer(target);
                    target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfig().getString("messages.unfreeze_notify", "&aYou have been unfrozen.")));
                    staff.sendMessage(ChatColor.GREEN + "Unfroze " + target.getName());
                    actionLogger.logFreezeAction(staff, target, false);
                } else {
                    staffManager.freezePlayer(target);
                    target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfig().getString("messages.freeze_notify", "&eYou have been frozen by a staff member.")));
                    staff.sendMessage(ChatColor.RED + "Froze " + target.getName());
                    actionLogger.logFreezeAction(staff, target, true);
                }
            }
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player staff = event.getPlayer();
        if (!canUseStaffTools(staff)) return;

        ItemStack item = staff.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return;

        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        switch (displayName.toLowerCase()) {
            case "random teleport" -> {
                if (isOnCooldown(staff)) {
                    staff.sendMessage(ChatColor.RED + "Please wait before using the teleport tool again.");
                    return;
                }

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

                actionLogger.logTeleportAction(staff, target);
                setCooldown(staff);
            }

            case "toggle vanish" -> {
                if (isOnCooldown(staff)) {
                    staff.sendMessage(ChatColor.RED + "Please wait before using the vanish tool again.");
                    return;
                }

                boolean vanished = staff.hasPotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY);
                if (vanished) {
                    staff.removePotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY);
                    Bukkit.getOnlinePlayers().forEach(p -> p.showPlayer(plugin, staff));
                    staff.sendMessage(ChatColor.GREEN + "You are now visible.");
                    actionLogger.logVanishAction(staff, false);
                } else {
                    staff.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
                    Bukkit.getOnlinePlayers().forEach(p -> {
                        if (!p.hasPermission("staffmode.use")) {
                            p.hidePlayer(plugin, staff);
                        }
                    });
                    staff.sendMessage(ChatColor.YELLOW + "You are now invisible.");
                    actionLogger.logVanishAction(staff, true);
                }

                // Update vanish tool to reflect current vanish state
                toolManager.giveStaffTools(staff);
                setCooldown(staff);
            }
        }
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (cpsManager.isTesting(player)) {
            switch (event.getAction()) {
                case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> cpsManager.recordClick(player);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (staffManager.isFrozen(event.getPlayer())) {
            event.setTo(event.getFrom()); // prevent movement if frozen
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!canUseStaffTools(player)) return;

        event.setCancelled(true);
        actionLogger.logBlockBreak(player, event.getBlock().getLocation());
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!canUseStaffTools(player)) return;

        event.setCancelled(true);
        actionLogger.logBlockPlace(player, event.getBlock().getLocation());
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!canUseStaffTools(player)) return;

        event.setCancelled(true);
        actionLogger.logItemDrop(player, event.getItemDrop().getLocation());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player p && canUseStaffTools(p)) {
            if (event.getClickedInventory() instanceof PlayerInventory) {
                event.setCancelled(true); // Prevent modifying own inventory in staff mode
            }
        }
    }
}
