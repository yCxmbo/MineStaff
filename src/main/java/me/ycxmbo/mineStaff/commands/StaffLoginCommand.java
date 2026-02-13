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
        if (!cfg.isStaffLoginEnabled()) {
            p.sendMessage(cfg.getMessage("staff_login_disabled", "Staff login is disabled."));
            return true;
        }
        if (args.length == 0) {
            p.sendMessage(ChatColor.YELLOW + "Usage: /stafflogin <password> [otp] | setpassword <password> | change <oldPassword> <newPassword>");
            return true;
        }
        if (args[0].equalsIgnoreCase("setpassword")) {
            if (args.length < 2) {
                p.sendMessage(ChatColor.RED + "Usage: /stafflogin setpassword <password>");
                return true;
            }
            if (login.hasPassword(p)) {
                p.sendMessage(ChatColor.RED + "You already have a password. Use /stafflogin change <oldPassword> <newPassword>");
                return true;
            }
            login.setPassword(p, args[1]);
            cfg.saveStaffAccounts();
            p.sendMessage(cfg.getMessage("password_set", "Password set."));
            return true;
        } else if (args[0].equalsIgnoreCase("change")) {
            if (args.length < 3) {
                p.sendMessage(ChatColor.RED + "Usage: /stafflogin change <oldPassword> <newPassword>");
                return true;
            }
            if (!login.verifyPassword(p, args[1])) {
                p.sendMessage(cfg.getMessage("login_failure", "Incorrect password."));
                return true;
            }
            login.setPassword(p, args[2]);
            cfg.saveStaffAccounts();
            p.sendMessage(cfg.getMessage("password_changed", "&aPassword changed successfully."));
            return true;
        } else {
            String password = args[0];

            // Check for lockout before attempting password check
            long lockoutSeconds = login.getRemainingLockoutSeconds(p.getUniqueId());
            if (lockoutSeconds > 0) {
                String msg = cfg.getMessage("login_locked_out",
                    "&cToo many failed attempts. Try again in {seconds} seconds.")
                    .replace("{seconds}", String.valueOf(lockoutSeconds));
                p.sendMessage(msg);
                return true;
            }

            if (!login.checkPassword(p, password)) {
                p.sendMessage(cfg.getMessage("login_failure", "Incorrect password."));
                return true;
            }
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
