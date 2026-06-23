package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ConfigManager;
import me.ycxmbo.mineStaff.spy.SpyManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SocialSpyCommand implements CommandExecutor {
    private final SpyManager spy;
    private final ConfigManager cfg;

    public SocialSpyCommand(MineStaff plugin) {
        this.spy = plugin.getSpyManager();
        this.cfg = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage(cfg.getMessage("only_players", "Only players can use this.")); return true; }
        if (!p.hasPermission("staffmode.spy")) { p.sendMessage(cfg.getMessage("no_permission", "You don't have permission.")); return true; }
        boolean now = !spy.isSocialSpy(p.getUniqueId());
        spy.setSocialSpy(p.getUniqueId(), now);
        p.sendMessage(cfg.getMessage(now ? "socialspy_on" : "socialspy_off", now ? "SocialSpy enabled." : "SocialSpy disabled."));
        return true;
    }
}

