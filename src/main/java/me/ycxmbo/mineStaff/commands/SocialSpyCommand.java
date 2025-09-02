package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.spy.SpyManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SocialSpyCommand implements CommandExecutor {
    private final SpyManager spy;
    public SocialSpyCommand(MineStaff plugin) { this.spy = plugin.getSpyManager(); }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Only players."); return true; }
        if (!p.hasPermission("staffmode.spy")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
        boolean now = !spy.isSocialSpy(p.getUniqueId());
        spy.setSocialSpy(p.getUniqueId(), now);
        p.sendMessage(now ? ChatColor.AQUA + "SocialSpy enabled." : ChatColor.GRAY + "SocialSpy disabled.");
        return true;
    }
}

