package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.alts.AltDetectionManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Set;
import java.util.UUID;

/**
 * Records connection address hashes on login and notifies staff when a joining
 * player has known alternate accounts (highlighting any that are banned).
 */
public class AltJoinListener implements Listener {
    private final MineStaff plugin;

    public AltJoinListener(MineStaff plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreLogin(AsyncPlayerPreLoginEvent e) {
        AltDetectionManager alts = plugin.getAltDetectionManager();
        if (alts == null || !alts.isEnabled()) return;
        if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        String ip = e.getAddress() != null ? e.getAddress().getHostAddress() : null;
        alts.record(e.getUniqueId(), e.getName(), ip);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        AltDetectionManager alts = plugin.getAltDetectionManager();
        if (alts == null || !alts.isEnabled()) return;
        if (!plugin.getConfig().getBoolean("alts.notify-staff", true)) return;

        Player joined = e.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Set<UUID> found = alts.getAlts(joined.getUniqueId());
            if (found.isEmpty()) return;

            boolean anyBanned = false;
            StringBuilder names = new StringBuilder();
            for (UUID alt : found) {
                String name = alts.getName(alt);
                boolean banned = plugin.getPunishmentManager() != null
                        && !plugin.getPunishmentManager().isLiteBansBackend()
                        && plugin.getPunishmentManager().isBanned(alt);
                if (banned) anyBanned = true;
                if (names.length() > 0) names.append(ChatColor.GRAY).append(", ");
                names.append(banned ? ChatColor.RED + name + " (banned)" : ChatColor.WHITE + name);
            }

            String header = plugin.getConfigManager()
                    .getMessage("altcheck_header", "&8[AltCheck] &e{name} &7has &f{count} &7known alt(s): ")
                    .replace("{name}", joined.getName())
                    .replace("{count}", String.valueOf(found.size()));
            if (anyBanned) header = ChatColor.RED + "⚠ " + ChatColor.stripColor(header);
            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission("staffmode.alts")) {
                    staff.sendMessage(header + names);
                }
            }
        }, 20L);
    }
}
