package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.api.events.StaffModeToggleEvent;
import me.ycxmbo.mineStaff.luckperms.LuckPermsBridge;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class LuckPermsContextListener implements Listener {
    private final MineStaff plugin;
    public LuckPermsContextListener(MineStaff plugin) { this.plugin = plugin; }

    @EventHandler
    public void onStaffMode(StaffModeToggleEvent e) {
        Player p = e.getPlayer();
        List<String> nodes = plugin.getConfigManager().getConfig().getStringList("luckperms.staffmode_temp_perms");
        if (nodes == null || nodes.isEmpty()) return;
        if (e.isEnabled()) LuckPermsBridge.addTempPerms(p, nodes); else LuckPermsBridge.removePerms(p, nodes);
    }
}

