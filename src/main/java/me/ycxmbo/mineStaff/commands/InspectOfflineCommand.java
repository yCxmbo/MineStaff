package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.offline.OfflineInventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InspectOfflineCommand implements CommandExecutor {
    private final OfflineInventoryManager offInv;

    public InspectOfflineCommand(OfflineInventoryManager offInv) { this.offInv = offInv; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        var cfg = MineStaff.getInstance().getConfigManager();
        if (!(sender instanceof Player p)) { sender.sendMessage(cfg.getMessage("only_players", "Only players can use this.")); return true; }
        if (!p.hasPermission("staffmode.inspect")) { p.sendMessage(cfg.getMessage("no_permission", "No permission.")); return true; }
        if (args.length < 1) { p.sendMessage(cfg.getMessage("inspectoffline_usage", "Usage: /inspectoffline <player> [ec]")); return true; }
        OfflinePlayer off = Bukkit.getOfflinePlayer(args[0]);
        boolean ec = args.length >= 2 && args[1].equalsIgnoreCase("ec");
        if (ec) offInv.openEnderChest(p, off); else offInv.openInventory(p, off);
        return true;
    }
}
