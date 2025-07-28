package me.ycxmbo.mineStaff.managers;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ActionLogger {

    private final MineStaff plugin;

    public ActionLogger(MineStaff plugin) {
        this.plugin = plugin;
    }

    private void sendLog(String message) {
        String prefix = plugin.getConfig().getString("messages.alert_prefix", "&c[MineStaff Alert] &f");
        String fullMessage = ChatColor.translateAlternateColorCodes('&', prefix + message);

        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("staffmode.logs"))
                .forEach(p -> p.sendMessage(fullMessage));
    }

    // --- Core Actions ---
    public void logInspect(Player staff, Player target) {
        sendLog(staff.getName() + " inspected " + target.getName());
    }

    public void logFreeze(Player staff, Player target, boolean frozen) {
        sendLog(staff.getName() + (frozen ? " froze " : " unfroze ") + target.getName());
    }

    public void logTeleport(Player staff, Player target) {
        sendLog(staff.getName() + " teleported to " + target.getName());
    }

    public void logVanish(Player staff, boolean vanished) {
        sendLog(staff.getName() + (vanished ? " vanished" : " is now visible"));
    }

    public void logCommand(Player staff, String command) {
        sendLog(staff.getName() + " executed command: " + command);
    }

    // --- Aliases for Listener Compatibility ---
    public void logFreezeAction(Player staff, Player target, boolean frozen) {
        logFreeze(staff, target, frozen);
    }

    public void logTeleportAction(Player staff, Player target) {
        logTeleport(staff, target);
    }

    public void logVanishAction(Player staff, boolean vanished) {
        logVanish(staff, vanished);
    }

    // --- World Interaction Logs ---
    public void logBlockBreak(Player player, int x, int y, int z, String blockType) {
        sendLog(player.getName() + " broke a block (" + blockType + ") at " + x + ", " + y + ", " + z);
    }

    public void logBlockPlace(Player player, int x, int y, int z, String blockType) {
        sendLog(player.getName() + " placed a block (" + blockType + ") at " + x + ", " + y + ", " + z);
    }

    public void logItemDrop(Player player, String itemType, int amount) {
        sendLog(player.getName() + " dropped " + amount + "x " + itemType);
    }
}
