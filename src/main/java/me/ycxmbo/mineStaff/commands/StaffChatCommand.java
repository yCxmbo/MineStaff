package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class StaffChatCommand implements CommandExecutor {
    private final MineStaff plugin;
    private final ConfigManager cfg;
    private final Set<UUID> toggled = new HashSet<>();

    public StaffChatCommand(MineStaff plugin) {
        this.plugin = plugin;
        this.cfg = plugin.getConfigManager();
    }

    public boolean isToggled(Player p) { return toggled.contains(p.getUniqueId()); }
    public void toggle(Player p) {
        if (isToggled(p)) toggled.remove(p.getUniqueId()); else toggled.add(p.getUniqueId());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(ChatColor.RED + "Only players.");
            return true;
        }
        if (!p.hasPermission("staffmode.chat")) {
            p.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (args.length == 0) {
            toggle(p);
            p.sendMessage(ChatColor.YELLOW + "StaffChat " + (isToggled(p) ? "enabled" : "disabled") + ". Prefix: " + cfg.getStaffchatPrefix());
            return true;
        }
        String msg = String.join(" ", args);
        for (Player pl : Bukkit.getOnlinePlayers()) {
            if (pl.hasPermission("staffmode.chat")) {
                pl.sendMessage(ChatColor.DARK_AQUA + "[StaffChat] " + p.getName() + ": " + ChatColor.WHITE + msg);
            }
        }
        return true;
    }
}
