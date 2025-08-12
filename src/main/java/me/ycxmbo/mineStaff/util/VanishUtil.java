package me.ycxmbo.mineStaff.util;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VanishUtil {
    public static void applyVanish(Player p, boolean vanish) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(p)) continue;
            if (vanish) {
                if (!other.hasPermission("staffmode.see")) other.hidePlayer(MineStaff.getInstance(), p);
            } else {
                other.showPlayer(MineStaff.getInstance(), p);
            }
        }
        p.setCollidable(!vanish);
        p.setInvisible(vanish);
        p.setSilent(vanish);
    }

    public static void reapplyForJoin(Player joiner) {
        // make sure vanished staff remain hidden to new players without see permission
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.equals(joiner)) continue;
            if (p.isInvisible()) {
                if (!joiner.hasPermission("staffmode.see")) joiner.hidePlayer(MineStaff.getInstance(), p);
            }
        }
    }
}
