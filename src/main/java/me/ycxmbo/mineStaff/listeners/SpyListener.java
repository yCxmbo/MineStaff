package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.spy.SpyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SpyListener implements Listener {
    private final SpyManager spy;

    private static final Set<String> PM_COMMANDS = new HashSet<>(Arrays.asList(
            "/msg", "/w", "/tell", "/whisper", "/pm", "/m"
    ));

    public SpyListener(MineStaff plugin) { this.spy = plugin.getSpyManager(); }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        String msg = e.getMessage();
        String base = msg.split("\\s+")[0].toLowerCase();

        // Command spy: show all commands except some excluded
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(e.getPlayer()) && p.hasPermission("staffmode.spy") && spy.isCommandSpy(p.getUniqueId())) {
                p.sendMessage(ChatColor.DARK_GRAY + "[CmdSpy] " + ChatColor.GRAY + e.getPlayer().getName() + ": " + ChatColor.WHITE + msg);
            }
        }

        // Social spy: only private message commands
        if (PM_COMMANDS.contains(base)) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.equals(e.getPlayer()) && p.hasPermission("staffmode.spy") && spy.isSocialSpy(p.getUniqueId())) {
                    p.sendMessage(ChatColor.DARK_GRAY + "[SocialSpy] " + ChatColor.GRAY + e.getPlayer().getName() + ": " + ChatColor.WHITE + msg);
                }
            }
        }
    }
}
