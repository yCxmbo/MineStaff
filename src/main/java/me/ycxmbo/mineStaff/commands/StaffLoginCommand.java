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
            p.sendMessage(ChatColor.YELLOW + "Usage: /stafflogin <password> [otp]| set <newPassword>");
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
            String password = args[0];
            if (!login.checkPassword(p, password)) { p.sendMessage(cfg.getMessage("login_failure", "Incorrect password.")); return true; }
            boolean require2fa = cfg.getConfig().getBoolean("security.2fa.enabled", false) && login.isTwoFactorEnabled(p);
            if (require2fa) {
                if (args.length < 2) { p.sendMessage(ChatColor.RED + "OTP required. Usage: /stafflogin <password> <otp>"); return true; }
                String otp = args[1];
                String secret = login.getTwoFactorSecret(p);
                if (secret == null || !me.ycxmbo.mineStaff.security.TwoFactorManager.verify(secret, otp, 1)) {
                    p.sendMessage(ChatColor.RED + "Invalid OTP.");
                    return true;
                }
            }
            login.setLoggedIn(p, true);
            login.startSession(p);
            p.sendMessage(cfg.getMessage("login_success", "Logged in."));
            return true;
        }
    }
}
