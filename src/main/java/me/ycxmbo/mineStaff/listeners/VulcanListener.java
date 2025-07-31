package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.util.ConfigManager;
import me.frep.vulcan.api.event.VulcanPunishEvent;
import me.frep.vulcan.api.event.VulcanViolationEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class VulcanListener implements Listener {

    private final MineStaff plugin;
    private final ConfigManager config;

    public VulcanListener(MineStaff plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    @EventHandler
    public void onVulcanViolation(VulcanViolationEvent event) {
        if (!configManager.getBoolean("integrations.vulcan.enabled", true)) return;
        if (!config.getConfig().getBoolean("alerts.vulcan_violation")) return;

        Player player = event.getPlayer();
        String check = event.getCheck().getName();
        int vl = event.getViolations();

        String message = config.getMessage("vulcan_violation", "&4Vulcan Alert: &c%player% failed &f%check% &c(VL: %vl%)")
                .replace("%player%", player.getName())
                .replace("%check%", check)
                .replace("%vl%", String.valueOf(vl));

        broadcastAlert(message);
    }

    @EventHandler
    public void onVulcanPunish(VulcanPunishEvent event) {
        if (!configManager.getBoolean("integrations.vulcan.enabled", true)) return;
        if (!config.getConfig().getBoolean("alerts.vulcan_punishment")) return;

        Player player = event.getPlayer();
        String check = event.getCheck().getName();

        String message = config.getMessage("vulcan_punishment", "&c%player% was punished by Vulcan for &f%check%")
                .replace("%player%", player.getName())
                .replace("%check%", check);

        broadcastAlert(message);
    }

    private void broadcastAlert(String message) {
        String prefix = config.getMessage("alert_prefix", "&c[MineStaff Alert] &f");
        String fullMessage = ChatColor.translateAlternateColorCodes('&', prefix + message);

        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("staffmode.alerts"))
                .forEach(p -> p.sendMessage(fullMessage));
    }
}
