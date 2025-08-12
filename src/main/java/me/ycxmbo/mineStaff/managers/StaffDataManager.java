package me.ycxmbo.mineStaff.managers;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.*;

public class StaffDataManager {
    private final MineStaff plugin;
    private final Set<UUID> staffMode = new HashSet<>();
    private final Set<UUID> frozen = new HashSet<>();
    private final Set<UUID> vanished = new HashSet<>();
    private final Map<UUID, GameMode> previousGamemode = new HashMap<>();

    public StaffDataManager(MineStaff plugin) {
        this.plugin = plugin;
    }

    public boolean isStaffMode(Player p) { return staffMode.contains(p.getUniqueId()); }
    public void enableStaffMode(Player p) { staffMode.add(p.getUniqueId()); }
    public void disableStaffMode(Player p) { staffMode.remove(p.getUniqueId()); }

    public boolean isFrozen(Player p) { return frozen.contains(p.getUniqueId()); }
    public void setFrozen(Player p, boolean v) { if (v) frozen.add(p.getUniqueId()); else frozen.remove(p.getUniqueId()); }

    public boolean isVanished(Player p) { return vanished.contains(p.getUniqueId()); }
    public void setVanished(Player p, boolean v) { if (v) vanished.add(p.getUniqueId()); else vanished.remove(p.getUniqueId()); }

    public void rememberGamemode(Player p) { previousGamemode.put(p.getUniqueId(), p.getGameMode()); }
    public GameMode popPreviousGamemode(Player p) { return previousGamemode.remove(p.getUniqueId()); }
}
