package me.ycxmbo.mineStaff.managers;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class StaffLoginManager {
    private final MineStaff plugin;
    private final Set<UUID> loggedIn = new HashSet<>();

    public StaffLoginManager(MineStaff plugin) {
        this.plugin = plugin;
    }

    public boolean isLoggedIn(Player p) {
        return loggedIn.contains(p.getUniqueId());
    }

    public void setLoggedIn(Player p, boolean v) {
        if (v) loggedIn.add(p.getUniqueId());
        else loggedIn.remove(p.getUniqueId());
    }

    public void clearLoginStatus(Player p) {
        loggedIn.remove(p.getUniqueId());
    }

    public boolean checkPassword(Player p, String password) {
        String stored = plugin.getConfigManager().getStaffAccounts().getString(p.getUniqueId().toString() + ".password");
        return stored != null && stored.equals(password);
    }

    public void setPassword(Player p, String password) {
        plugin.getConfigManager().getStaffAccounts().set(p.getUniqueId().toString() + ".password", password);
    }

    public void saveAccounts() {
        plugin.getConfigManager().saveStaffAccounts();
    }
}
