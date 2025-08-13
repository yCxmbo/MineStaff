package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.gui.StaffListGUI;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class StaffListGUICommand implements CommandExecutor {
    private final StaffListGUI gui;
    public StaffListGUICommand(MineStaff plugin) { this.gui = new StaffListGUI(plugin); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Only players."); return true; }
        gui.open(p);
        return true;
    }
}
