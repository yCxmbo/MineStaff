package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ConfigManager;
import me.ycxmbo.mineStaff.spy.SpyManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SpyListener implements Listener {
    private final SpyManager spy;
    private final MineStaff plugin;

    private static final Set<String> PM_COMMANDS = new HashSet<>(Arrays.asList(
            "/msg", "/w", "/tell", "/whisper", "/pm", "/m"
    ));

    public SpyListener(MineStaff plugin) {
        this.plugin = plugin;
        this.spy = plugin.getSpyManager();
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        String msg = e.getMessage();
        String base = msg.split("\\s+")[0].toLowerCase();
        ConfigManager cfg = plugin.getConfigManager();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(e.getPlayer()) && p.hasPermission("staffmode.spy") && spy.isCommandSpy(p.getUniqueId())) {
                p.sendMessage(cfg.getMessage("spy_cmdspy_format", "&8[CmdSpy] &7{player}: &f{command}")
                        .replace("{player}", e.getPlayer().getName())
                        .replace("{command}", msg));
            }
        }

        if (PM_COMMANDS.contains(base)) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.equals(e.getPlayer()) && p.hasPermission("staffmode.spy") && spy.isSocialSpy(p.getUniqueId())) {
                    p.sendMessage(cfg.getMessage("spy_socialspy_format", "&8[SocialSpy] &7{player}: &f{message}")
                            .replace("{player}", e.getPlayer().getName())
                            .replace("{message}", msg));
                }
            }
        }
    }
}
