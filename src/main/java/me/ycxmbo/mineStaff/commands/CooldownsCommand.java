package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /cooldowns} &mdash; open the per-tool cooldown configuration GUI.
 */
public class CooldownsCommand implements CommandExecutor {
    private final MineStaff plugin;

    public CooldownsCommand(MineStaff plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Only players can use this.");
            return true;
        }
        if (!p.hasPermission("staffmode.cooldowns")) {
            p.sendMessage(plugin.getConfigManager().getMessage("no_permission", "You don't have permission."));
            return true;
        }
        plugin.getCooldownConfigGUI().open(p);
        return true;
    }
}
