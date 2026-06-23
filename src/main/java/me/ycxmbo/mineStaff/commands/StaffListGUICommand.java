package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.gui.StaffListGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffListGUICommand implements CommandExecutor {
    private final MineStaff plugin;
    private final StaffListGUI gui;

    public StaffListGUICommand(MineStaff plugin) {
        this.plugin = plugin;
        this.gui = new StaffListGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage(plugin.getConfigManager().getMessage("only_players", "Only players can use this.")); return true; }
        if (!p.hasPermission("staffmode.stafflist.gui")) {
            p.sendMessage(plugin.getConfigManager().getMessage("no_permission", "No permission."));
            return true;
        }
        gui.open(p);
        return true;
    }
}
