package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.InfractionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LiteBansBridgeListener implements Listener {
    private static final Set<String> LB_CMDS = new HashSet<>(Arrays.asList("ban","tempban","kick","warn","mute","tempmute"));
    private final InfractionManager mgr;

    public LiteBansBridgeListener(MineStaff plugin) { this.mgr = plugin.getInfractionManager(); }

    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent e) {
        String msg = e.getMessage().trim();
        if (!msg.startsWith("/")) return;
        String[] parts = msg.substring(1).split("\\s+");
        if (parts.length < 2) return;

        String base = parts[0].toLowerCase();
        if (!LB_CMDS.contains(base)) return;

        Player staff = e.getPlayer();
        String targetName = parts[1];
        String reason = (parts.length >= 3) ? String.join(" ", Arrays.copyOfRange(parts, 2, parts.length)) : base;

        UUID targetUUID = null;
        var t = Bukkit.getPlayerExact(targetName);
        if (t != null) targetUUID = t.getUniqueId();
        else reason = "[offline:" + targetName + "] " + reason;

        if (targetUUID == null) targetUUID = staff.getUniqueId(); // fallback so it shows in GUI
        mgr.add(targetUUID, new me.ycxmbo.mineStaff.managers.InfractionManager.Infraction(staff.getUniqueId(), base.toUpperCase(), reason));
    }
}
