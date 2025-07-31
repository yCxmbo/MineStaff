package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import me.ycxmbo.mineStaff.util.SoundUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;

public class FreezeListener implements Listener {

    private final MineStaff plugin;
    private final StaffDataManager dataManager;

    public FreezeListener(MineStaff plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getStaffDataManager();
    }

    @EventHandler
    public void onMoveWhileFrozen(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (dataManager.isFrozen(uuid)) {
            if (event.getFrom().getX() != event.getTo().getX()
                    || event.getFrom().getY() != event.getTo().getY()
                    || event.getFrom().getZ() != event.getTo().getZ()) {

                event.setTo(event.getFrom());
                player.sendActionBar(ChatColor.RED + "You are frozen by a staff member.");
                SoundUtil.playFreezeEffect(player);
            }
        }
    }
}
