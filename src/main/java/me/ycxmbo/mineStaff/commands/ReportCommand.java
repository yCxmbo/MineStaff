package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ConfigManager;
import me.ycxmbo.mineStaff.managers.ReportManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ReportCommand implements CommandExecutor {
    private final ReportManager reports;
    private final ConfigManager cfg;

    public ReportCommand(MineStaff plugin) {
        this.reports = plugin.getReportManager();
        this.cfg = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(cfg.getMessage("only_players", "Only players can use this."));
            return true;
        }
        if (args.length < 2) {
            p.sendMessage(cfg.getMessage("report_usage", "Usage: /report <player> <reason>"));
            return true;
        }

        OfflinePlayer t = Bukkit.getOfflinePlayer(args[0]);
        String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        UUID id = reports.add(p.getUniqueId(), t.getUniqueId(), reason);

        p.sendMessage(cfg.getMessage("report_submitted", "Report submitted. ID: {id}").replace("{id}", id.toString()));
        return true;
    }
}

