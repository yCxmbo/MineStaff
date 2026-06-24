package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ConfigManager;
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
    private final MineStaff plugin;
    private final PlayerNotesManager notes;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public NotesCommand(MineStaff plugin) {
        this.plugin = plugin;
        this.notes = plugin.getPlayerNotesManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        if (!sender.hasPermission("staffmode.notes")) { sender.sendMessage(cfg.getMessage("no_permission", "No permission.")); return true; }
        if (args.length < 2) {
            sender.sendMessage(cfg.getMessage("notes_usage", "Usage: /notes <player> add <note> | list | remove <index>"));
            return true;
        }
        OfflinePlayer t = Bukkit.getOfflinePlayer(args[0]);
        String name = t.getName() != null ? t.getName() : t.getUniqueId().toString();
        String sub = args[1].toLowerCase();
        switch (sub) {
            case "add":
                if (args.length < 3) { sender.sendMessage(cfg.getMessage("notes_add_usage", "Usage: /notes <player> add <note>")); return true; }
                if (notes.isAtLimit(t.getUniqueId())) {
                    int max = plugin.getConfigManager().getConfig().getInt("notes.max_per_player", 0);
                    sender.sendMessage(cfg.getMessage("notes_limit_reached", "Note limit reached (" + max + " max)."));
                    return true;
                }
                String note = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                notes.add(t.getUniqueId(), (sender instanceof Player p) ? p.getUniqueId() : java.util.UUID.randomUUID(), note);
                sender.sendMessage(cfg.getMessage("notes_note_added", "Note added."));
                MineStaff.getInstance().getAuditLogger().log(java.util.Map.of(
                        "type", "note", "action", "add",
                        "actor", (sender instanceof Player p) ? p.getUniqueId().toString() : "console",
                        "target", t.getUniqueId().toString(), "text", note
                ));
                return true;
            case "list":
                List<PlayerNotesManager.Note> list = notes.get(t.getUniqueId());
                sender.sendMessage(cfg.getMessage("notes_list_header", "Notes for {name}:").replace("{name}", name));
                for (int i = 0; i < list.size(); i++) {
                    var n = list.get(i);
                    sender.sendMessage(ChatColor.YELLOW + "#" + i + ChatColor.GRAY + " [" + sdf.format(new Date(n.ts)) + "] " + ChatColor.WHITE + n.text);
                }
                return true;
            case "remove":
                if (args.length < 3) { sender.sendMessage(cfg.getMessage("notes_remove_usage", "Usage: /notes <player> remove <index>")); return true; }
                int idx;
                try { idx = Integer.parseInt(args[2]); } catch (NumberFormatException ex) { sender.sendMessage(cfg.getMessage("notes_invalid_index_number", "Index must be a number.")); return true; }
                if (notes.remove(t.getUniqueId(), idx)) {
                    sender.sendMessage(cfg.getMessage("notes_note_removed", "Note removed."));
                    MineStaff.getInstance().getAuditLogger().log(java.util.Map.of(
                            "type", "note", "action", "remove",
                            "actor", (sender instanceof Player p) ? p.getUniqueId().toString() : "console",
                            "target", t.getUniqueId().toString(), "index", String.valueOf(idx)
                    ));
                } else sender.sendMessage(cfg.getMessage("notes_invalid_index", "Invalid index."));
                return true;
        }
        sender.sendMessage(cfg.getMessage("notes_usage", "Usage: /notes <player> add <note> | list | remove <index>"));
        return true;
    }
}
