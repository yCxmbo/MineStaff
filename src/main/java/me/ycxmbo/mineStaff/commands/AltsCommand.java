package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.alts.AltDetectionManager;
import me.ycxmbo.mineStaff.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Set;
import java.util.UUID;

public class AltsCommand implements CommandExecutor {
    private final MineStaff plugin;

    public AltsCommand(MineStaff plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        if (!sender.hasPermission("staffmode.alts")) {
            sender.sendMessage(cfg.getMessage("no_permission", "No permission."));
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(cfg.getMessage("alts_usage", "Usage: /alts <player>"));
            return true;
        }

        AltDetectionManager alts = plugin.getAltDetectionManager();
        if (alts == null || !alts.isEnabled()) {
            sender.sendMessage(cfg.getMessage("alts_disabled", "Alt detection is disabled."));
            return true;
        }

        OfflinePlayer off = Bukkit.getOfflinePlayer(args[0]);
        UUID target = off.getUniqueId();
        String name = off.getName() != null ? off.getName() : args[0];

        Set<UUID> found = alts.getAlts(target);
        sender.sendMessage(cfg.getMessage("alts_known_header", "Known alts for {name} ({count})")
                .replace("{name}", name)
                .replace("{count}", String.valueOf(found.size())));
        if (found.isEmpty()) {
            sender.sendMessage(cfg.getMessage("alts_no_linked", "No linked accounts found."));
            return true;
        }
        for (UUID alt : found) {
            String altName = alts.getName(alt);
            boolean online = Bukkit.getPlayer(alt) != null;
            boolean banned = plugin.getPunishmentManager() != null
                    && !plugin.getPunishmentManager().isLiteBansBackend()
                    && plugin.getPunishmentManager().isBanned(alt);
            sender.sendMessage((banned ? ChatColor.RED + " ✖ " : ChatColor.GRAY + " • ")
                    + ChatColor.WHITE + altName
                    + (online ? ChatColor.GREEN + " (online)" : "")
                    + (banned ? ChatColor.RED + " (banned)" : ""));
        }
        return true;
    }
}
