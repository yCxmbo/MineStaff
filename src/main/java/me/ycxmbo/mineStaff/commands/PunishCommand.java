package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ConfigManager;
import me.ycxmbo.mineStaff.punishments.Punishment;
import me.ycxmbo.mineStaff.punishments.PunishmentManager;
import org.bukkit.Bukkit;
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

public class PunishCommand implements CommandExecutor {
    private final MineStaff plugin;
    private final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public PunishCommand(MineStaff plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        if (!sender.hasPermission("staffmode.punish")) {
            sender.sendMessage(cfg.getMessage("no_permission", "No permission."));
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(cfg.getMessage("punish_usage", "Usage: /punish <player> | ban/mute/kick/unban/unmute/check"));
            return true;
        }

        String targetName = args[0];
        OfflinePlayer off = Bukkit.getOfflinePlayer(targetName);
        UUID target = off.getUniqueId();
        if (off.getName() != null) targetName = off.getName();

        PunishmentManager pm = plugin.getPunishmentManager();

        if (args.length == 1) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(cfg.getMessage("punish_gui_only_players", "Only players can open the GUI."));
                return true;
            }
            plugin.getPunishmentGUI().open(p, targetName, target);
            return true;
        }

        String sub = args[1].toLowerCase();
        String staffName = sender.getName();
        final String finalTargetName = targetName;

        switch (sub) {
            case "ban" -> {
                if (args.length < 4) { sender.sendMessage(cfg.getMessage("punish_ban_usage", "Usage: /punish <player> ban <dur|perm> <reason>")); return true; }
                long dur = PunishmentManager.parseDuration(args[2]);
                if (dur < 0) { sender.sendMessage(cfg.getMessage("punish_invalid_duration", "Invalid duration.")); return true; }
                pm.ban(target, finalTargetName, staffName, join(args, 3), dur);
                sender.sendMessage(cfg.getMessage("punish_banned", "Banned {target}.").replace("{target}", finalTargetName));
            }
            case "mute" -> {
                if (args.length < 4) { sender.sendMessage(cfg.getMessage("punish_mute_usage", "Usage: /punish <player> mute <dur|perm> <reason>")); return true; }
                long dur = PunishmentManager.parseDuration(args[2]);
                if (dur < 0) { sender.sendMessage(cfg.getMessage("punish_invalid_duration", "Invalid duration.")); return true; }
                pm.mute(target, finalTargetName, staffName, join(args, 3), dur);
                sender.sendMessage(cfg.getMessage("punish_muted", "Muted {target}.").replace("{target}", finalTargetName));
            }
            case "kick" -> {
                if (args.length < 3) { sender.sendMessage(cfg.getMessage("punish_kick_usage", "Usage: /punish <player> kick <reason>")); return true; }
                pm.kick(target, finalTargetName, staffName, join(args, 2));
                sender.sendMessage(cfg.getMessage("punish_kicked", "Kicked {target}.").replace("{target}", finalTargetName));
            }
            case "unban" -> {
                if (!sender.hasPermission("staffmode.punish.manage")) { sender.sendMessage(cfg.getMessage("punish_no_manage", "No permission.")); return true; }
                pm.unban(target, finalTargetName, staffName);
                sender.sendMessage(cfg.getMessage("punish_unbanned", "Unbanned {target}.").replace("{target}", finalTargetName));
            }
            case "unmute" -> {
                if (!sender.hasPermission("staffmode.punish.manage")) { sender.sendMessage(cfg.getMessage("punish_no_manage", "No permission.")); return true; }
                pm.unmute(target, finalTargetName, staffName);
                sender.sendMessage(cfg.getMessage("punish_unmuted", "Unmuted {target}.").replace("{target}", finalTargetName));
            }
            case "check" -> showHistory(sender, pm, target, finalTargetName, cfg);
            default -> sender.sendMessage(cfg.getMessage("punish_unknown_sub", "Unknown subcommand."));
        }
        return true;
    }

    private void showHistory(CommandSender sender, PunishmentManager pm, UUID target, String name, ConfigManager cfg) {
        if (pm.isLiteBansBackend()) {
            sender.sendMessage(cfg.getMessage("punish_history_litebans", "LiteBans is active; use LiteBans history commands."));
            return;
        }
        List<Punishment> history = pm.getHistory(target);
        sender.sendMessage(cfg.getMessage("punish_history_header", "Punishment history for {name} ({count})")
                .replace("{name}", name).replace("{count}", String.valueOf(history.size())));
        if (history.isEmpty()) {
            sender.sendMessage(cfg.getMessage("punish_history_empty", "No punishments on record."));
            return;
        }
        for (Punishment p : history) {
            String status = p.isActive() && !p.isExpired() ? "§cACTIVE" : "§8ended";
            sender.sendMessage("§7#" + p.getId() + " §f" + p.getType() + " §7by " + p.getStaff() + " " + status);
            sender.sendMessage("§8  " + fmt.format(new Date(p.getStart()))
                    + " | " + (p.isPermanent() ? "Permanent" : p.durationString()) + " | " + p.getReason());
        }
    }

    private String join(String[] args, int from) {
        return String.join(" ", Arrays.copyOfRange(args, from, args.length));
    }
}
