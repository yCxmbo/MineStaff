package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.evidence.EvidenceManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class EvidenceCommand implements CommandExecutor {
    private final MineStaff plugin;
    public EvidenceCommand(MineStaff plugin) { this.plugin = plugin; }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        if (!sender.hasPermission("staffmode.alerts")) { sender.sendMessage(ChatColor.RED + "No permission."); return true; }
        if (args.length < 2) { sender.sendMessage(ChatColor.YELLOW + "Usage: /evidence <reportId> add <url> | list"); return true; }
        UUID id;
        try { id = UUID.fromString(args[0]); } catch (Exception ex) { sender.sendMessage(ChatColor.RED + "Invalid reportId"); return true; }
        String sub = args[1].toLowerCase();
        EvidenceManager ev = plugin.getEvidenceManager();
        switch (sub) {
            case "add":
                if (args.length < 3) { sender.sendMessage(ChatColor.RED + "Usage: /evidence <reportId> add <url>"); return true; }
                String url = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                UUID evid = ev.add(id, (sender instanceof org.bukkit.entity.Player p)?p.getUniqueId():java.util.UUID.randomUUID(), url);
                sender.sendMessage(ChatColor.GREEN + "Evidence added: " + ChatColor.YELLOW + evid);
                return true;
            case "list":
                var list = ev.list(id);
                sender.sendMessage(ChatColor.GOLD + "Evidence for report " + id + ":");
                for (var e : list) sender.sendMessage(ChatColor.YELLOW + "- " + e.url + ChatColor.DARK_GRAY + " (by " + e.staff + ")");
                return true;
        }
        sender.sendMessage(ChatColor.YELLOW + "Usage: /evidence <reportId> add <url> | list");
        return true;
    }
}

