package me.ycxmbo.mineStaff.offline;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class OfflineInventoryManager implements Listener {
    private final MineStaff plugin;
    private final File file;
    private YamlConfiguration yaml;

    public OfflineInventoryManager(MineStaff plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "offlineinv.yml");
        reload();
    }

    public void reload() {
        try { if (!file.exists()) { file.getParentFile().mkdirs(); file.createNewFile(); } } catch (IOException ignored) {}
        yaml = YamlConfiguration.loadConfiguration(file);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        saveSnapshot(e.getPlayer());
    }

    public synchronized void saveSnapshot(Player p) {
        String base = "players." + p.getUniqueId();
        yaml.set(base + ".inv", p.getInventory().getContents());
        yaml.set(base + ".ec", p.getEnderChest().getContents());
        try { yaml.save(file); } catch (IOException ignored) {}
    }

    public void openInventory(Player viewer, OfflinePlayer target) {
        Inventory inv = Bukkit.createInventory(viewer, 54, ChatColor.GOLD + "Offline Inv: " + (target.getName()!=null?target.getName():target.getUniqueId()));
        List<ItemStack> list = (List<ItemStack>) yaml.getList("players." + target.getUniqueId() + ".inv");
        if (list != null) inv.setContents(list.toArray(new ItemStack[0]));
        viewer.openInventory(inv);
    }

    public void openEnderChest(Player viewer, OfflinePlayer target) {
        Inventory inv = Bukkit.createInventory(viewer, 27, ChatColor.LIGHT_PURPLE + "Offline EC: " + (target.getName()!=null?target.getName():target.getUniqueId()));
        List<ItemStack> list = (List<ItemStack>) yaml.getList("players." + target.getUniqueId() + ".ec");
        if (list != null) inv.setContents(list.toArray(new ItemStack[0]));
        viewer.openInventory(inv);
    }
}

