package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.punishments.Punishment;
import me.ycxmbo.mineStaff.punishments.PunishmentManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Entry point for the built-in punishment system. With only a player argument it
 * opens the punishment GUI; subcommands allow direct ban/mute/kick/unban/unmute
 * and a punishment history view.
 */
public class PunishCommand implements CommandExecutor {
    private final MineStaff plugin;
    private final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public PunishCommand(MineStaff plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("staffmode.punish")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no_permission", "You don't have permission."));
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(ChatColor.YELLOW + "Usage:");
            sender.sendMessage(ChatColor.GRAY + "  /punish <player>            " + ChatColor.DARK_GRAY + "open GUI");
            sender.sendMessage(ChatColor.GRAY + "  /punish <player> ban <dur|perm> <reason>");
            sender.sendMessage(ChatColor.GRAY + "  /punish <player> mute <dur|perm> <reason>");
            sender.sendMessage(ChatColor.GRAY + "  /punish <player> kick <reason>");
            sender.sendMessage(ChatColor.GRAY + "  /punish <player> unban|unmute|check");
            return true;
        }

        String targetName = args[0];
        OfflinePlayer off = Bukkit.getOfflinePlayer(targetName);
        UUID target = off.getUniqueId();
        if (off.getName() != null) targetName = off.getName();

        PunishmentManager pm = plugin.getPunishmentManager();

        if (args.length == 1) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(ChatColor.RED + "Only players can open the GUI. Use a subcommand from console.");
                return true;
            }
            plugin.getPunishmentGUI().open(p, targetName, target);
            return true;
        }

        String sub = args[1].toLowerCase();
        String staffName = sender.getName();

        switch (sub) {
            case "ban" -> {
                if (args.length < 4) { sender.sendMessage(ChatColor.RED + "Usage: /punish <player> ban <dur|perm> <reason>"); return true; }
                long dur = PunishmentManager.parseDuration(args[2]);
                if (dur < 0) { sender.sendMessage(ChatColor.RED + "Invalid duration. Use e.g. 1h, 3d, perm."); return true; }
                String reason = join(args, 3);
                pm.ban(target, targetName, staffName, reason, dur);
                sender.sendMessage(ChatColor.GREEN + "Banned " + targetName + ".");
            }
            case "mute" -> {
                if (args.length < 4) { sender.sendMessage(ChatColor.RED + "Usage: /punish <player> mute <dur|perm> <reason>"); return true; }
                long dur = PunishmentManager.parseDuration(args[2]);
                if (dur < 0) { sender.sendMessage(ChatColor.RED + "Invalid duration. Use e.g. 1h, 3d, perm."); return true; }
                String reason = join(args, 3);
                pm.mute(target, targetName, staffName, reason, dur);
                sender.sendMessage(ChatColor.GREEN + "Muted " + targetName + ".");
            }
            case "kick" -> {
                if (args.length < 3) { sender.sendMessage(ChatColor.RED + "Usage: /punish <player> kick <reason>"); return true; }
                pm.kick(target, targetName, staffName, join(args, 2));
                sender.sendMessage(ChatColor.GREEN + "Kicked " + targetName + ".");
            }
            case "unban" -> {
                if (!sender.hasPermission("staffmode.punish.manage")) { noManage(sender); return true; }
                pm.unban(target, targetName, staffName);
                sender.sendMessage(ChatColor.GREEN + "Unbanned " + targetName + ".");
            }
            case "unmute" -> {
                if (!sender.hasPermission("staffmode.punish.manage")) { noManage(sender); return true; }
                pm.unmute(target, targetName, staffName);
                sender.sendMessage(ChatColor.GREEN + "Unmuted " + targetName + ".");
            }
            case "check" -> showHistory(sender, pm, target, targetName);
            default -> sender.sendMessage(ChatColor.RED + "Unknown subcommand. Try ban|mute|kick|unban|unmute|check.");
        }
        return true;
    }

    private void showHistory(CommandSender sender, PunishmentManager pm, UUID target, String name) {
        if (pm.isLiteBansBackend()) {
            sender.sendMessage(ChatColor.YELLOW + "LiteBans backend is active; use LiteBans history commands.");
            return;
        }
        List<Punishment> history = pm.getHistory(target);
        sender.sendMessage(ChatColor.GOLD + "Punishment history for " + name + " (" + history.size() + ")");
        if (history.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "No punishments on record.");
            return;
        }
        for (Punishment p : history) {
            String status = p.isActive() && !p.isExpired() ? ChatColor.RED + "ACTIVE" : ChatColor.DARK_GRAY + "ended";
            sender.sendMessage(ChatColor.GRAY + "#" + p.getId() + " " + ChatColor.WHITE + p.getType()
                    + ChatColor.GRAY + " by " + p.getStaff() + " " + status);
            sender.sendMessage(ChatColor.DARK_GRAY + "  " + fmt.format(new Date(p.getStart()))
                    + " | " + (p.isPermanent() ? "Permanent" : p.durationString()) + " | " + p.getReason());
        }
    }

    private void noManage(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "You need staffmode.punish.manage to remove punishments.");
    }

    private String join(String[] args, int from) {
        return String.join(" ", Arrays.copyOfRange(args, from, args.length));
    }
}
