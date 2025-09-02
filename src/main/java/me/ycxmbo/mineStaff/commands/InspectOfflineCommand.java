package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.offline.OfflineInventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InspectOfflineCommand implements CommandExecutor {
    private final OfflineInventoryManager offInv;
    public InspectOfflineCommand(MineStaff plugin) { this.offInv = new OfflineInventoryManager(plugin); }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Only players."); return true; }
        if (!p.hasPermission("staffmode.inspect")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
        if (args.length < 1) { p.sendMessage(ChatColor.YELLOW + "Usage: /inspectoffline <player> [ec]"); return true; }
        OfflinePlayer off = Bukkit.getOfflinePlayer(args[0]);
        boolean ec = args.length >= 2 && args[1].equalsIgnoreCase("ec");
        if (ec) offInv.openEnderChest(p, off); else offInv.openInventory(p, off);
        return true;
    }
}

