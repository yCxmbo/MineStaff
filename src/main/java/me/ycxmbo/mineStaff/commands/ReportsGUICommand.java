package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.gui.ReportsGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReportsGUICommand implements CommandExecutor {
    private final MineStaff plugin;
    private final ReportsGUI gui;

    public ReportsGUICommand(MineStaff plugin) {
        this.plugin = plugin;
        this.gui = new ReportsGUI(plugin.getReportManager());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage(plugin.getConfigManager().getMessage("only_players", "Only players can use this.")); return true; }
        if (!p.hasPermission("staffmode.alerts")) { p.sendMessage(plugin.getConfigManager().getMessage("no_permission", "No permission.")); return true; }
        gui.open(p);
        return true;
    }
}
