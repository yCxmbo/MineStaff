package me.ycxmbo.mineStaff.managers;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.util.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class ActionLogger {

    private final MineStaff plugin;
    private final ConfigManager configManager;

    public ActionLogger(MineStaff plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    private void sendLog(String message) {
        String prefix = configManager.getMessage("alert_prefix", "[MineStaff Alert] ");
        String fullMessage = ChatColor.translateAlternateColorCodes('&', prefix + message);

        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("staffmode.alerts"))
                .forEach(p -> p.sendMessage(fullMessage));
    }

    // --- Core Actions ---
    public void logInspect(Player staff, Player target) {
        String message = configManager.getMessage("messages.inspect", "%staff% inspected %target%")
                .replace("%staff%", staff.getName())
                .replace("%target%", target.getName());
        sendLog(message);
    }

    public void logFreeze(Player staff, Player target, boolean frozen) {
        String action = frozen ? "froze" : "unfroze";
        String message = configManager.getMessage("messages.freeze", "%staff% %action% %target%")
                .replace("%staff%", staff.getName())
                .replace("%target%", target.getName())
                .replace("%action%", action);
        sendLog(message);
    }

    public void logTeleport(Player staff, Player target) {
        String message = configManager.getMessage("messages.teleport", "%staff% teleported to %target%")
                .replace("%staff%", staff.getName())
                .replace("%target%", target.getName());
        sendLog(message);
    }

    public void logVanish(Player staff, boolean vanished) {
        String action = vanished ? "vanished" : "is now visible";
        String message = configManager.getMessage("messages.vanish", "%staff% %action%")
                .replace("%staff%", staff.getName())
                .replace("%action%", action);
        sendLog(message);
    }

    public void logCommand(Player staff, String command) {
        String message = configManager.getMessage("messages.command_log", "%staff% executed command: %command%")
                .replace("%staff%", staff.getName())
                .replace("%command%", command);
        sendLog(message);
    }

    // --- World Interaction Logs ---
    public void logBlockBreak(Player player, Location loc, String blockType) {
        String message = configManager.getMessage("messages.block_break", "%player% broke a %block% at %x%, %y%, %z%")
                .replace("%player%", player.getName())
                .replace("%block%", blockType)
                .replace("%x%", String.valueOf(loc.getBlockX()))
                .replace("%y%", String.valueOf(loc.getBlockY()))
                .replace("%z%", String.valueOf(loc.getBlockZ()));
        sendLog(message);
    }

    public void logBlockPlace(Player player, Location loc, String blockType) {
        String message = configManager.getMessage("messages.block_place", "%player% placed a %block% at %x%, %y%, %z%")
                .replace("%player%", player.getName())
                .replace("%block%", blockType)
                .replace("%x%", String.valueOf(loc.getBlockX()))
                .replace("%y%", String.valueOf(loc.getBlockY()))
                .replace("%z%", String.valueOf(loc.getBlockZ()));
        sendLog(message);
    }

    public void logItemDrop(Player player, Location loc, String itemType, int amount) {
        String message = configManager.getMessage("messages.item_drop", "%player% dropped %amount%x %item% at %x%, %y%, %z%")
                .replace("%player%", player.getName())
                .replace("%item%", itemType)
                .replace("%amount%", String.valueOf(amount))
                .replace("%x%", String.valueOf(loc.getBlockX()))
                .replace("%y%", String.valueOf(loc.getBlockY()))
                .replace("%z%", String.valueOf(loc.getBlockZ()));
        sendLog(message);
    }
}
