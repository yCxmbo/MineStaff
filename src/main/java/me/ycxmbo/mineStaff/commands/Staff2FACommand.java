package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ConfigManager;
import me.ycxmbo.mineStaff.security.TwoFactorManager;
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
        ConfigManager cfg = plugin.getConfigManager();
        if (!(sender instanceof Player p)) { sender.sendMessage(cfg.getMessage("only_players", "Only players can use this.")); return true; }
        if (!p.hasPermission("staffmode.login")) { p.sendMessage(cfg.getMessage("no_permission", "No permission.")); return true; }
        if (!plugin.getConfigManager().getConfig().getBoolean("security.2fa.enabled", false)) { p.sendMessage(cfg.getMessage("fa2_disabled", "2FA is not enabled.")); return true; }
        if (args.length == 0) {
            p.sendMessage(cfg.getMessage("fa2_usage", "Usage: /staff2fa enable | confirm <otp> | disable | status"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "enable": {
                String secret = TwoFactorManager.generateSecret();
                pending.put(p.getUniqueId(), secret);
                String issuer = URLEncoder.encode("MineStaff", StandardCharsets.UTF_8);
                String acct = URLEncoder.encode(p.getName(), StandardCharsets.UTF_8);
                String url = "otpauth://totp/" + issuer + ":" + acct + "?secret=" + secret + "&issuer=" + issuer + "&digits=6&period=30";
                p.sendMessage(cfg.getMessage("fa2_enable_secret", "2FA Secret: {secret}").replace("{secret}", secret));
                p.sendMessage(cfg.getMessage("fa2_enable_instructions", "Add to authenticator app, then run /staff2fa confirm <code>."));
                p.sendMessage(cfg.getMessage("fa2_enable_url", "{url}").replace("{url}", url));
                return true;
            }
            case "confirm": {
                if (args.length < 2) { p.sendMessage(cfg.getMessage("fa2_confirm_usage", "Usage: /staff2fa confirm <otp>")); return true; }
                String secret = pending.get(p.getUniqueId());
                if (secret == null) { p.sendMessage(cfg.getMessage("fa2_no_pending", "No pending 2FA. Run /staff2fa enable first.")); return true; }
                if (TwoFactorManager.verify(secret, args[1], 1)) {
                    plugin.getStaffLoginManager().setTwoFactor(p, true, secret);
                    pending.remove(p.getUniqueId());
                    p.sendMessage(cfg.getMessage("fa2_enabled", "2FA enabled."));
                } else {
                    p.sendMessage(cfg.getMessage("fa2_invalid_code", "Invalid code."));
                }
                return true;
            }
            case "disable": {
                plugin.getStaffLoginManager().setTwoFactor(p, false, null);
                p.sendMessage(cfg.getMessage("fa2_disabled_success", "2FA disabled."));
                return true;
            }
            case "status": {
                boolean on = plugin.getStaffLoginManager().isTwoFactorEnabled(p);
                p.sendMessage(cfg.getMessage(on ? "fa2_status_on" : "fa2_status_off", on ? "2FA: ENABLED" : "2FA: DISABLED"));
                return true;
            }
        }
        p.sendMessage(cfg.getMessage("fa2_usage", "Usage: /staff2fa enable | confirm <otp> | disable | status"));
        return true;
    }
}
