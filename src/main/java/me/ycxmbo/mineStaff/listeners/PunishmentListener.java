package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.punishments.Punishment;
import me.ycxmbo.mineStaff.punishments.PunishmentManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

/**
 * Enforces the built-in punishment backend: blocks banned players at login and
 * silences muted players in chat. Does nothing when the LiteBans backend is in
 * use (LiteBans performs its own enforcement).
 */
public class PunishmentListener implements Listener {
    private final MineStaff plugin;

    public PunishmentListener(MineStaff plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPreLogin(AsyncPlayerPreLoginEvent e) {
        PunishmentManager pm = plugin.getPunishmentManager();
        if (pm == null || pm.isLiteBansBackend()) return;
        Punishment ban = pm.getActiveBan(e.getUniqueId());
        if (ban != null) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, pm.banScreen(ban));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        PunishmentManager pm = plugin.getPunishmentManager();
        if (pm == null || pm.isLiteBansBackend()) return;
        Punishment mute = pm.getActiveMute(e.getPlayer().getUniqueId());
        if (mute != null) {
            e.setCancelled(true);
            String expires = mute.isPermanent() ? "permanent" : mute.durationString() + " left";
            e.getPlayer().sendMessage(plugin.getConfigManager()
                    .getMessage("punishment_muted_blocked", "&c✖ You are muted: &f{reason} &8(expires {expires})")
                    .replace("{reason}", mute.getReason())
                    .replace("{expires}", expires));
        }
    }
}
