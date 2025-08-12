package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ConfigManager;
import me.ycxmbo.mineStaff.managers.StaffLoginManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffLoginCommand implements CommandExecutor {
    private final MineStaff plugin;
    private final StaffLoginManager login;
    private final ConfigManager cfg;

    public StaffLoginCommand(MineStaff plugin) {
        this.plugin = plugin;
        this.login = plugin.getStaffLoginManager();
        this.cfg = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(ChatColor.RED + "Only players.");
            return true;
        }
        if (!p.hasPermission("staffmode.login")) {
            p.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (args.length == 0) {
            p.sendMessage(ChatColor.YELLOW + "Usage: /stafflogin <password>|set <newPassword>");
            return true;
        }
        if (args[0].equalsIgnoreCase("set")) {
            if (args.length < 2) {
                p.sendMessage(ChatColor.RED + "Usage: /stafflogin set <password>");
                return true;
            }
            login.setPassword(p, args[1]);
            cfg.saveStaffAccounts();
            p.sendMessage(cfg.getMessage("password_set", "Password set."));
            return true;
        } else {
            if (login.checkPassword(p, args[0])) {
                login.setLoggedIn(p, true);
                p.sendMessage(cfg.getMessage("login_success", "Logged in."));
            } else {
                p.sendMessage(cfg.getMessage("login_failure", "Incorrect password."));
            }
            return true;
        }
    }
}
