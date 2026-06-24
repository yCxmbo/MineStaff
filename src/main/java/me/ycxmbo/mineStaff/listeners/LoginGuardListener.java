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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class LoginGuardListener implements Listener {
    // How long (ms) after clicking an NPC a dispatched command is treated as
    // originating from that NPC rather than typed by the player.
    private static final long NPC_COMMAND_GRACE_MS = 250L;

    private final StaffLoginManager login;
    private final ConfigManager config;
    // Tracks the last time each logged-out staff member clicked a Citizens NPC, so
    // commands the NPC runs on their behalf are not blocked by the login guard.
    private final java.util.Map<java.util.UUID, Long> recentNpcClicks = new java.util.concurrent.ConcurrentHashMap<>();

    public LoginGuardListener(MineStaff plugin) {
        this.login = plugin.getStaffLoginManager();
        this.config = plugin.getConfigManager();
    }

    private boolean requiresLogin(Player p) {
        // Double-check that staff login is enabled before enforcing any restrictions
        if (!config.isStaffLoginEnabled()) return false;
        if (!config.isLoginRequired()) return false;
        // Only require login for players with staff permissions who haven't logged in
        return p.hasPermission("staffmode.toggle") && !login.isLoggedIn(p);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!requiresLogin(p)) return;
        if (e.getFrom().getX() != e.getTo().getX() || e.getFrom().getY() != e.getTo().getY() || e.getFrom().getZ() != e.getTo().getZ()) {
            e.setTo(e.getFrom());
            p.sendMessage(config.getMessage("login_move_restricted", "Please /stafflogin to move."));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (!requiresLogin(p)) return;
        String msg = e.getMessage().toLowerCase();
        if (msg.startsWith("/stafflogin")) return; // allow both /stafflogin and /stafflogin set
        // Allow commands dispatched by a Citizens NPC the player just clicked, so
        // menu/login NPCs remain usable before a staff member authenticates.
        Long clicked = recentNpcClicks.get(p.getUniqueId());
        if (clicked != null && System.currentTimeMillis() - clicked <= NPC_COMMAND_GRACE_MS) return;
        e.setCancelled(true);
        p.sendMessage(config.getMessage("login_cmd_restricted", "Please /stafflogin before using commands."));
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (!requiresLogin(p)) return;
        e.setCancelled(true);
        p.sendMessage(config.getMessage("login_chat_restricted", "Please /stafflogin before using chat."));
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!requiresLogin(p)) return;
        e.setCancelled(true);
        p.sendMessage(config.getMessage("login_interact_restricted", "Please /stafflogin to interact."));
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        if (!requiresLogin(p)) return;
        // Allow clicking Citizens NPCs (tagged with the "NPC" metadata) so menu/login
        // NPCs remain usable before a staff member authenticates. Record the click so
        // any command the NPC runs on the player's behalf is not blocked either.
        if (e.getRightClicked().hasMetadata("NPC")) {
            recentNpcClicks.put(p.getUniqueId(), System.currentTimeMillis());
            return;
        }
        e.setCancelled(true);
        p.sendMessage(config.getMessage("login_interact_restricted", "Please /stafflogin to interact."));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (!requiresLogin(p)) return;
        e.setCancelled(true);
        p.sendMessage(config.getMessage("login_break_restricted", "Please /stafflogin to break blocks."));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (!requiresLogin(p)) return;
        e.setCancelled(true);
        p.sendMessage(config.getMessage("login_place_restricted", "Please /stafflogin to place blocks."));
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if (!requiresLogin(p)) return;
        e.setCancelled(true);
        p.sendMessage(config.getMessage("login_drop_restricted", "Please /stafflogin to drop items."));
    }

    @EventHandler(ignoreCancelled = true)
    public void onInvClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!requiresLogin(p)) return;
        e.setCancelled(true);
        p.sendMessage(config.getMessage("login_inventory_restricted", "Please /stafflogin to use inventory."));
    }

    @EventHandler(ignoreCancelled = true)
    public void onInvDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!requiresLogin(p)) return;
        e.setCancelled(true);
        p.sendMessage(config.getMessage("login_inventory_restricted", "Please /stafflogin to use inventory."));
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamageOther(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player p)) return;
        if (!requiresLogin(p)) return;
        e.setCancelled(true);
        p.sendMessage(config.getMessage("login_fight_restricted", "Please /stafflogin to fight."));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (!requiresLogin(p)) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        recentNpcClicks.remove(e.getPlayer().getUniqueId());
    }
}
