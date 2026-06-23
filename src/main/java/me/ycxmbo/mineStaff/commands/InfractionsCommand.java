package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.gui.InfractionsGUI;
import me.ycxmbo.mineStaff.managers.ConfigManager;
import me.ycxmbo.mineStaff.managers.InfractionManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class InfractionsCommand implements CommandExecutor {
    private final MineStaff plugin;
    private final InfractionManager manager;
    private final InfractionsGUI gui;

    public InfractionsCommand(MineStaff plugin) {
        this.plugin = plugin;
        this.manager = plugin.getInfractionManager();
        this.gui = new InfractionsGUI(manager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        if (!(sender instanceof Player p)) { sender.sendMessage(cfg.getMessage("only_players", "Only players can use this.")); return true; }
        if (args.length < 1) { p.sendMessage(cfg.getMessage("infractions_usage", "Usage: /infractions <player> [add <type> <reason>]")); return true; }
        OfflinePlayer t = Bukkit.getOfflinePlayer(args[0]);
        if (args.length >= 2 && args[1].equalsIgnoreCase("add")) {
            if (!p.hasPermission("staffmode.infractions.add")) { p.sendMessage(cfg.getMessage("no_permission", "No permission.")); return true; }
            if (args.length < 4) { p.sendMessage(cfg.getMessage("infractions_add_usage", "Usage: /infractions <player> add <type> <reason>")); return true; }
            String type = args[2];
            String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
            manager.add(t.getUniqueId(), new InfractionManager.Infraction(p.getUniqueId(), type, reason));
            p.sendMessage(cfg.getMessage("infraction_added", "Infraction recorded."));
            return true;
        }
        gui.open(p, t.getUniqueId());
        return true;
    }
}
