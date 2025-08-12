package me.ycxmbo.mineStaff.managers;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class ActionLogger {
    private final MineStaff plugin;
    public ActionLogger(MineStaff plugin) { this.plugin = plugin; }

    public void logCommand(Player p, String action) {
        plugin.getLogger().log(Level.INFO, "[Action] " + p.getName() + ": " + action);
    }
}
