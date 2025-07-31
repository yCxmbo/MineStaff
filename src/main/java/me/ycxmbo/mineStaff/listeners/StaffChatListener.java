package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.commands.StaffChatCommand;
import me.ycxmbo.mineStaff.util.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class StaffChatListener implements Listener {

    private final MineStaff plugin;
    private final ConfigManager configManager;

    public StaffChatListener(MineStaff plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        StaffChatCommand chatCommand = plugin.getStaffChatCommand();

        if (chatCommand.isToggled(player.getUniqueId())) {
            event.setCancelled(true);
            String format = ChatColor.translateAlternateColorCodes('&',
                            configManager.getMessage("staff-chat.format", "&8[&bStaff&8] &7%player%: %message%"))
                    .replace("%player%", player.getName())
                    .replace("%message%", event.getMessage());

            plugin.getServer().getOnlinePlayers().forEach(p -> {
                if (p.hasPermission("staffmode.chat")) {
                    p.sendMessage(format);
                }
            });
        }
    }
}
