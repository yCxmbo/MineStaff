package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.commands.StaffChatCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class StaffChatListener implements Listener {

    private final MineStaff plugin;

    public StaffChatListener(MineStaff plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        StaffChatCommand chatCommand = plugin.getStaffChatCommand();

        if (chatCommand.isToggled(player.getUniqueId())) {
            event.setCancelled(true);
            String format = ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfig().getString("staff-chat.format", "&8[&bStaff&8] &7%player%: %message%"))
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
