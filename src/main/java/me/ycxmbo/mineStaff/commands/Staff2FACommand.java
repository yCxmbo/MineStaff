package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.security.TwoFactorManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Staff2FACommand implements CommandExecutor {
    private final MineStaff plugin;
    private final Map<UUID, String> pending = new HashMap<>();
    public Staff2FACommand(MineStaff plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Only players."); return true; }
        if (!p.hasPermission("staffmode.login")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
        if (!plugin.getConfigManager().getConfig().getBoolean("security.2fa.enabled", false)) { p.sendMessage(ChatColor.RED + "2FA is not enabled by server."); return true; }
        if (args.length == 0) {
            p.sendMessage(ChatColor.YELLOW + "Usage: /staff2fa enable | confirm <otp> | disable | status");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "enable": {
                String secret = TwoFactorManager.generateSecret();
                pending.put(p.getUniqueId(), secret);
                String issuer = URLEncoder.encode("MineStaff", StandardCharsets.UTF_8);
                String acct = URLEncoder.encode(p.getName(), StandardCharsets.UTF_8);
                String url = "otpauth://totp/" + issuer + ":" + acct + "?secret=" + secret + "&issuer=" + issuer + "&digits=6&period=30";
                p.sendMessage(ChatColor.GREEN + "2FA Secret: " + ChatColor.YELLOW + secret);
                p.sendMessage(ChatColor.GRAY + "Add to your authenticator app. Then run: /staff2fa confirm <code>");
                p.sendMessage(ChatColor.BLUE + url);
                return true;
            }
            case "confirm": {
                if (args.length < 2) { p.sendMessage(ChatColor.RED + "Usage: /staff2fa confirm <otp>"); return true; }
                String secret = pending.get(p.getUniqueId());
                if (secret == null) { p.sendMessage(ChatColor.RED + "No pending secret. Use /staff2fa enable first."); return true; }
                if (TwoFactorManager.verify(secret, args[1], 1)) {
                    plugin.getStaffLoginManager().setTwoFactor(p, true, secret);
                    pending.remove(p.getUniqueId());
                    p.sendMessage(ChatColor.GREEN + "2FA enabled.");
                } else {
                    p.sendMessage(ChatColor.RED + "Invalid code.");
                }
                return true;
            }
            case "disable": {
                plugin.getStaffLoginManager().setTwoFactor(p, false, null);
                p.sendMessage(ChatColor.YELLOW + "2FA disabled.");
                return true;
            }
            case "status": {
                boolean on = plugin.getStaffLoginManager().isTwoFactorEnabled(p);
                p.sendMessage(ChatColor.AQUA + "2FA: " + (on ? ChatColor.GREEN + "ENABLED" : ChatColor.GRAY + "DISABLED"));
                return true;
            }
        }
        p.sendMessage(ChatColor.YELLOW + "Usage: /staff2fa enable | confirm <otp> | disable | status");
        return true;
    }
}

