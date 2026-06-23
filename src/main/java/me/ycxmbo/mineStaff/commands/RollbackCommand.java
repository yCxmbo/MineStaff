package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.gui.RollbackGUI;
import me.ycxmbo.mineStaff.managers.ConfigManager;
import me.ycxmbo.mineStaff.managers.RollbackManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RollbackCommand implements CommandExecutor {
    private final MineStaff plugin;
    private final RollbackManager rb;
    private final RollbackGUI gui;

    public RollbackCommand(MineStaff plugin) {
        this.plugin = plugin;
        this.rb = plugin.getRollbackManager();
        this.gui = new RollbackGUI(this.rb);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        if (!(sender instanceof Player p)) { sender.sendMessage(cfg.getMessage("only_players", "Only players can use this.")); return true; }
        if (!p.hasPermission("staffmode.rollback")) { p.sendMessage(cfg.getMessage("no_permission", "No permission.")); return true; }
        if (args.length < 1) { p.sendMessage(cfg.getMessage("rollback_usage", "Usage: /rollback <player>")); return true; }

        OfflinePlayer t = Bukkit.getOfflinePlayer(args[0]);
        gui.open(p, t.getUniqueId());
        return true;
    }
}
