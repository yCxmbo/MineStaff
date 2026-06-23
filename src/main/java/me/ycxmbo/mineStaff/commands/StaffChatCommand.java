package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ConfigManager;
import me.ycxmbo.mineStaff.managers.StaffChatManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffChatCommand implements CommandExecutor {
    private final MineStaff plugin;
    private final StaffChatManager scm;

    public StaffChatCommand(MineStaff plugin) {
        this.plugin = plugin;
        this.scm = plugin.getStaffChatManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        if (!(sender instanceof Player p)) { sender.sendMessage(cfg.getMessage("only_players", "Only players can use this.")); return true; }
        if (!p.hasPermission("staffmode.chat")) { p.sendMessage(cfg.getMessage("no_permission", "No permission.")); return true; }

        if (args.length == 0) {
            boolean now = !scm.isToggled(p);
            scm.setToggled(p, now);
            p.sendMessage(cfg.getMessage(now ? "staffchat_on" : "staffchat_off",
                    now ? "StaffChat ON." : "StaffChat OFF."));
            return true;
        }

        String msg = String.join(" ", args);
        scm.broadcast(p, msg);
        return true;
    }
}
