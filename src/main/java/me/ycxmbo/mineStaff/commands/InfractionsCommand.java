package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.gui.InfractionsGUI;
import me.ycxmbo.mineStaff.managers.InfractionManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class InfractionsCommand implements CommandExecutor {
    private final InfractionManager manager;
    private final InfractionsGUI gui;

    public InfractionsCommand(MineStaff plugin) {
        this.manager = new InfractionManager(plugin);
        this.gui = new InfractionsGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Only players."); return true; }
        if (args.length < 1) { p.sendMessage(ChatColor.YELLOW + "Usage: /infractions <player> [add <type> <reason>]"); return true; }
        OfflinePlayer t = Bukkit.getOfflinePlayer(args[0]);
        if (args.length >= 2 && args[1].equalsIgnoreCase("add")) {
            if (!p.hasPermission("staffmode.infractions.add")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
            if (args.length < 4) { p.sendMessage(ChatColor.YELLOW + "Usage: /infractions <player> add <type> <reason>"); return true; }
            String type = args[2];
            String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
            manager.add(t.getUniqueId(), new InfractionManager.Infraction(p.getUniqueId(), type, reason));
            p.sendMessage(ChatColor.GREEN + "Infraction added.");
            return true;
        }
        gui.open(p, t.getUniqueId());
        return true;
    }
}
