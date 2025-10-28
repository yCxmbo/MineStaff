package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.notes.PlayerNotesManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NotesCommand implements CommandExecutor {
    private final PlayerNotesManager notes;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public NotesCommand(MineStaff plugin) { this.notes = plugin.getPlayerNotesManager(); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("staffmode.notes")) { sender.sendMessage(ChatColor.RED + "No permission."); return true; }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /notes <player> add <note> | list | remove <index>");
            return true;
        }
        OfflinePlayer t = Bukkit.getOfflinePlayer(args[0]);
        String sub = args[1].toLowerCase();
        switch (sub) {
            case "add":
                if (args.length < 3) { sender.sendMessage(ChatColor.RED + "Usage: /notes <player> add <note>"); return true; }
                String note = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                notes.add(t.getUniqueId(), (sender instanceof Player p) ? p.getUniqueId() : java.util.UUID.randomUUID(), note);
                sender.sendMessage(ChatColor.GREEN + "Note added.");
                MineStaff.getInstance().getAuditLogger().log(java.util.Map.of(
                        "type","note","action","add","actor",(sender instanceof Player p)?p.getUniqueId().toString():"console","target",t.getUniqueId().toString(),"text",note
                ));
                return true;
            case "list":
                List<PlayerNotesManager.Note> list = notes.get(t.getUniqueId());
                sender.sendMessage(ChatColor.GOLD + "Notes for " + (t.getName() != null ? t.getName() : t.getUniqueId()) + ":");
                for (int i=0;i<list.size();i++) {
                    var n = list.get(i);
                    sender.sendMessage(ChatColor.YELLOW + "#" + i + ChatColor.GRAY + " [" + sdf.format(new Date(n.ts)) + "] " + ChatColor.WHITE + n.text);
                }
                return true;
            case "remove":
                if (args.length < 3) { sender.sendMessage(ChatColor.RED + "Usage: /notes <player> remove <index>"); return true; }
                int idx;
                try { idx = Integer.parseInt(args[2]); } catch (NumberFormatException ex) { sender.sendMessage(ChatColor.RED + "Index must be a number."); return true; }
                if (notes.remove(t.getUniqueId(), idx)) {
                    sender.sendMessage(ChatColor.GREEN + "Note removed.");
                    MineStaff.getInstance().getAuditLogger().log(java.util.Map.of(
                            "type","note","action","remove","actor",(sender instanceof Player p)?p.getUniqueId().toString():"console","target",t.getUniqueId().toString(),"index",String.valueOf(idx)
                    ));
                } else sender.sendMessage(ChatColor.RED + "Invalid index.");
                return true;
        }
        sender.sendMessage(ChatColor.YELLOW + "Usage: /notes <player> add <note> | list | remove <index>");
        return true;
    }
}

