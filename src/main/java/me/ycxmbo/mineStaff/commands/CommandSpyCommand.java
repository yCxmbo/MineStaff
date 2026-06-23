package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ConfigManager;
import me.ycxmbo.mineStaff.spy.SpyManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSpyCommand implements CommandExecutor {
    private final SpyManager spy;
    private final ConfigManager cfg;

    public CommandSpyCommand(MineStaff plugin) {
        this.spy = plugin.getSpyManager();
        this.cfg = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage(cfg.getMessage("only_players", "Only players can use this.")); return true; }
        if (!p.hasPermission("staffmode.spy")) { p.sendMessage(cfg.getMessage("no_permission", "You don't have permission.")); return true; }
        boolean now = !spy.isCommandSpy(p.getUniqueId());
        spy.setCommandSpy(p.getUniqueId(), now);
        p.sendMessage(cfg.getMessage(now ? "commandspy_on" : "commandspy_off", now ? "CommandSpy enabled." : "CommandSpy disabled."));
        return true;
    }
}

