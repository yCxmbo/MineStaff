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

public class LoginGuardListener implements Listener {
    private final StaffLoginManager login;
    private final ConfigManager config;

    public LoginGuardListener(MineStaff plugin) {
        this.login = plugin.getStaffLoginManager();
        this.config = plugin.getConfigManager();
    }

    private boolean requiresLogin(Player p) {
        if (!config.isLoginRequired()) return false;
        // staff must login if they have the staff toggle perm
        return p.hasPermission("staffmode.toggle") && !login.isLoggedIn(p);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!requiresLogin(p)) return;

        // Only block real movement (not head rotation)
        if (e.getFrom().getX() != e.getTo().getX()
                || e.getFrom().getY() != e.getTo().getY()
                || e.getFrom().getZ() != e.getTo().getZ()) {
            e.setTo(e.getFrom());
            p.sendActionBar(ChatColor.YELLOW + "Please /stafflogin to move.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = (Player) e.getPlayer();
        if (!requiresLogin(p)) return;
        String msg = e.getMessage().toLowerCase();
        if (msg.startsWith("/stafflogin")) return; // allow login
        e.setCancelled(true);
        p.sendMessage(config.getMessage("login_required", "Please /stafflogin before using commands."));
    }
}
