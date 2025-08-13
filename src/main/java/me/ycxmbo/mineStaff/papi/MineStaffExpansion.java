package me.ycxmbo.mineStaff.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class MineStaffExpansion extends PlaceholderExpansion {
    private final MineStaff plugin;

    public MineStaffExpansion(MineStaff plugin) { this.plugin = plugin; }

    @Override public @NotNull String getIdentifier() { return "minestaff"; }
    @Override public @NotNull String getAuthor() { return "ycxmbo"; }
    @Override public @NotNull String getVersion() { return plugin.getDescription().getVersion(); }
    @Override public boolean persist() { return true; }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        StaffDataManager data = plugin.getStaffDataManager();
        switch (params.toLowerCase()) {
            case "staffmode": return (player != null && player.isOnline() && data.isStaffMode(player.getPlayer())) ? "on" : "off";
            case "vanish": return (player != null && player.isOnline() && data.isVanished(player.getPlayer())) ? "on" : "off";
            default: return null;
        }
    }
}
