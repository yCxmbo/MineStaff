package me.ycxmbo.mineStaff.bridge;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class HistoryBridge {
    public static boolean openHistory(MineStaff plugin, Player viewer, String targetName) {
        // Prefer CoreProtect if present
        if (Bukkit.getPluginManager().getPlugin("CoreProtect") != null) {
            // Toggle inspector or run lookup for last N days configurable
            int days = plugin.getConfigManager().getConfig().getInt("history.days", 7);
            // Typical CoreProtect lookup: /co l u:Name t:7d r:#radius
            Bukkit.dispatchCommand(viewer, "co l u:" + targetName + " t:" + days + "d");
            return true;
        }
        // Fallback to Prism
        if (Bukkit.getPluginManager().getPlugin("Prism") != null) {
            int days = plugin.getConfigManager().getConfig().getInt("history.days", 7);
            Bukkit.dispatchCommand(viewer, "pr l p:" + targetName + " t:" + days + "d");
            return true;
        }
        return false;
    }
}

