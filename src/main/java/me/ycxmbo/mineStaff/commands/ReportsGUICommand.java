package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.gui.ReportsGUI;
import me.ycxmbo.mineStaff.managers.ReportManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReportsGUICommand implements CommandExecutor {
    private final ReportsGUI gui;

    public ReportsGUICommand(MineStaff plugin) {
        this.gui = new ReportsGUI(plugin.getReportManager());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Only players."); return true; }
        if (!p.hasPermission("staffmode.alerts")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
        gui.open(p);
        return true;
    }
}

