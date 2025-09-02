package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.gui.RollbackGUI;
import me.ycxmbo.mineStaff.managers.RollbackManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RollbackCommand implements CommandExecutor {
    private final RollbackManager rb;
    private final RollbackGUI gui;

    public RollbackCommand(MineStaff plugin) {
        this.rb = plugin.getRollbackManager();           // reuse the singleton
        this.gui = new RollbackGUI(this.rb);             // constructor takes RollbackManager
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Only players."); return true; }
        if (!p.hasPermission("staffmode.rollback")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
        if (args.length < 1) { p.sendMessage(ChatColor.YELLOW + "Usage: /rollback <player>"); return true; }

        OfflinePlayer t = Bukkit.getOfflinePlayer(args[0]);
        gui.open(p, t.getUniqueId());
        return true;
    }
}

