package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ConfigManager;
import me.ycxmbo.mineStaff.managers.StaffLoginManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class LoginGuardListener implements Listener {
    private final StaffLoginManager login;
    private final ConfigManager config;

    public LoginGuardListener(MineStaff plugin) {
        this.login = plugin.getStaffLoginManager();
        this.config = plugin.getConfigManager();
    }

    private boolean requiresLogin(Player p) {
        if (!config.isStaffLoginEnabled()) return false;
        if (!config.isLoginRequired()) return false;
        return p.hasPermission("staffmode.toggle") && !login.isLoggedIn(p);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!requiresLogin(p)) return;
        if (e.getFrom().getX() != e.getTo().getX() || e.getFrom().getY() != e.getTo().getY() || e.getFrom().getZ() != e.getTo().getZ()) {
            e.setTo(e.getFrom());
            p.sendMessage(ChatColor.YELLOW + "Please /stafflogin to move.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (!requiresLogin(p)) return;
        String msg = e.getMessage().toLowerCase();
        if (msg.startsWith("/stafflogin")) return; // allow both /stafflogin and /stafflogin set
        e.setCancelled(true);
        p.sendMessage(config.getMessage("login_required", "Please /stafflogin before using commands."));
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (!requiresLogin(p)) return;
        e.setCancelled(true);
        p.sendMessage(config.getMessage("login_required", "Please /stafflogin before using chat."));
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!requiresLogin(p)) return;
        e.setCancelled(true);
        p.sendMessage(config.getMessage("login_required", "Please /stafflogin to interact."));
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        if (!requiresLogin(p)) return;
        e.setCancelled(true);
        p.sendMessage(config.getMessage("login_required", "Please /stafflogin to interact."));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (!requiresLogin(p)) return;
        e.setCancelled(true);
        p.sendMessage(config.getMessage("login_required", "Please /stafflogin to break blocks."));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (!requiresLogin(p)) return;
        e.setCancelled(true);
        p.sendMessage(config.getMessage("login_required", "Please /stafflogin to place blocks."));
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if (!requiresLogin(p)) return;
        e.setCancelled(true);
        p.sendMessage(config.getMessage("login_required", "Please /stafflogin to drop items."));
    }

    @EventHandler(ignoreCancelled = true)
    public void onInvClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!requiresLogin(p)) return;
        e.setCancelled(true);
        p.sendMessage(config.getMessage("login_required", "Please /stafflogin to use inventory."));
    }

    @EventHandler(ignoreCancelled = true)
    public void onInvDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!requiresLogin(p)) return;
        e.setCancelled(true);
        p.sendMessage(config.getMessage("login_required", "Please /stafflogin to use inventory."));
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamageOther(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player p)) return;
        if (!requiresLogin(p)) return;
        e.setCancelled(true);
        p.sendMessage(config.getMessage("login_required", "Please /stafflogin to fight."));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (!requiresLogin(p)) return;
        e.setCancelled(true);
    }
}
