package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RequestBackupCommand implements CommandExecutor {
    private final MineStaff plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 30000;

    public RequestBackupCommand(MineStaff plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        if (!(sender instanceof Player player)) {
            sender.sendMessage(cfg.getMessage("only_players", "Only players can use this."));
            return true;
        }
        if (!player.hasPermission("staffmode.backup")) {
            player.sendMessage(cfg.getMessage("no_permission", "No permission."));
            return true;
        }

        UUID playerId = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (cooldowns.containsKey(playerId)) {
            long remaining = COOLDOWN_MS - (now - cooldowns.get(playerId));
            if (remaining > 0) {
                player.sendMessage(cfg.getMessage("backup_cooldown", "Please wait {seconds}s before requesting backup again.")
                        .replace("{seconds}", String.valueOf(remaining / 1000)));
                return true;
            }
        }

        cooldowns.put(playerId, now);
        String reason = args.length > 0 ? String.join(" ", args) : "assistance needed";

        int notified = 0;
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("staffmode.backup.receive") && !staff.equals(player)) {
                sendBackupRequest(staff, player, reason);
                notified++;
            }
        }

        if (notified > 0) {
            player.sendMessage(cfg.getMessage("backup_requested", "Backup requested! Notified {count} staff member(s).")
                    .replace("{count}", String.valueOf(notified)));
        } else {
            player.sendMessage(cfg.getMessage("backup_no_staff", "No staff members available."));
        }

        plugin.getActionLogger().log(player, "BACKUP_REQUEST", "Reason: " + reason);
        return true;
    }

    private void sendBackupRequest(Player staff, Player requester, String reason) {
        Component message = Component.text()
                .append(Component.text("⚠ ", NamedTextColor.RED, TextDecoration.BOLD))
                .append(Component.text("BACKUP REQUEST", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(" ⚠\n", NamedTextColor.RED, TextDecoration.BOLD))
                .append(Component.text("From: ", NamedTextColor.GRAY))
                .append(Component.text(requester.getName(), NamedTextColor.YELLOW))
                .append(Component.text("\nReason: ", NamedTextColor.GRAY))
                .append(Component.text(reason, NamedTextColor.WHITE))
                .append(Component.text("\nLocation: ", NamedTextColor.GRAY))
                .append(Component.text(String.format("%s (%d, %d, %d)", requester.getWorld().getName(),
                        requester.getLocation().getBlockX(), requester.getLocation().getBlockY(), requester.getLocation().getBlockZ()), NamedTextColor.AQUA))
                .append(Component.text("\n\n"))
                .append(Component.text("[Click to Teleport]", NamedTextColor.GREEN, TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.runCommand("/tp " + requester.getName()))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to teleport to ", NamedTextColor.GREEN)
                                .append(Component.text(requester.getName(), NamedTextColor.YELLOW)))))
                .build();

        staff.sendMessage(message);
        try {
            staff.playSound(staff.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.0f);
        } catch (Exception e) {
            try { staff.playSound(staff.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f); }
            catch (Exception ex) { staff.playSound(staff.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.5f); }
        }
        staff.sendTitle("§c⚠ BACKUP REQUESTED ⚠", "§7By §e" + requester.getName(), 10, 60, 20);
    }
}
