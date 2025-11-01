package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.CPSCheckManager;
import me.ycxmbo.mineStaff.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CPSCheckCommand implements CommandExecutor {
    private final MineStaff plugin;

    public CPSCheckCommand(MineStaff plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Only players."); return true; }
        if (!p.hasPermission("staffmode.cpscheck")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
        if (args.length < 1) { p.sendMessage(ChatColor.YELLOW + "Usage: /cpscheck <player>"); return true; }

        OfflinePlayer off = Bukkit.getOfflinePlayer(args[0]);
        if (!off.isOnline()) { p.sendMessage(ChatColor.RED + "Target must be online."); return true; }
        Player target = off.getPlayer();

        ConfigManager configManager = plugin.getConfigManager();
        var cfg = configManager.getConfig();

        CPSCheckManager cps = plugin.getCPSManager();
        if (cps.isChecking(target)) {
            int secs = cfg.getInt("cps.duration_seconds", 10);
            p.sendMessage(format(configManager, "cps_already_running", "&cA CPS test is already running for {target}.", target, secs));
            return true;
        }
        if (cps.begin(p, target)) {
            int secs = cfg.getInt("cps.duration_seconds", 10);
            p.sendMessage(format(configManager, "cps_started", "&aStarted {seconds}s CPS test on {target}.", target, secs));
            target.sendMessage(format(configManager, "cps_target_notify", "&eA staff member is measuring your CPS for {seconds} seconds.", target, secs));
            cps.finishLater(p, target);
        } else {
            int secs = cfg.getInt("cps.duration_seconds", 10);
            p.sendMessage(format(configManager, "cps_already_running", "&cA CPS test is already running for {target}.", target, secs));
        }
        return true;
    }

    private String format(ConfigManager configManager, String key, String def, Player target, int seconds) {
        String msg = configManager.getMessage(key, def);
        if (target != null) {
            msg = msg.replace("{target}", target.getName());
        }
        msg = msg.replace("{seconds}", Integer.toString(seconds));
        return msg;
    }
}
