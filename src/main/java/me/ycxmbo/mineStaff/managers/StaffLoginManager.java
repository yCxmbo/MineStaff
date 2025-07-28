package me.ycxmbo.mineStaff.managers;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StaffLoginManager {

    private final MineStaff plugin;

    private File staffAccountsFile;
    private FileConfiguration staffAccountsConfig;

    // Store login status: UUID -> logged in or not
    private final Map<UUID, Boolean> loggedInPlayers = new HashMap<>();
    // Cache passwords in memory
    private final Map<UUID, String> cachedPasswords = new HashMap<>();

    public StaffLoginManager(MineStaff plugin) {
        this.plugin = plugin;
        createStaffAccountsFile();
        loadPasswords();
    }

    private void createStaffAccountsFile() {
        staffAccountsFile = new File(plugin.getDataFolder(), "staffaccounts.yml");
        if (!staffAccountsFile.exists()) {
            try {
                staffAccountsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create staffaccounts.yml!");
                e.printStackTrace();
            }
        }
        staffAccountsConfig = YamlConfiguration.loadConfiguration(staffAccountsFile);
    }

    private void loadPasswords() {
        if (!staffAccountsConfig.isConfigurationSection("passwords")) return;

        for (String key : staffAccountsConfig.getConfigurationSection("passwords").getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            String password = staffAccountsConfig.getString("passwords." + key);
            cachedPasswords.put(uuid, password);
        }
    }

    private void savePasswords() {
        // Clear old entries first
        if (staffAccountsConfig.isConfigurationSection("passwords")) {
            for (String key : staffAccountsConfig.getConfigurationSection("passwords").getKeys(false)) {
                staffAccountsConfig.set("passwords." + key, null);
            }
        }

        for (Map.Entry<UUID, String> entry : cachedPasswords.entrySet()) {
            staffAccountsConfig.set("passwords." + entry.getKey().toString(), entry.getValue());
        }

        try {
            staffAccountsConfig.save(staffAccountsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save staffaccounts.yml!");
            e.printStackTrace();
        }
    }

    public boolean isLoggedIn(Player player) {
        return loggedInPlayers.getOrDefault(player.getUniqueId(), false);
    }

    public boolean hasPassword(Player player) {
        return cachedPasswords.containsKey(player.getUniqueId());
    }

    public void setPassword(Player player, String password) {
        cachedPasswords.put(player.getUniqueId(), password);
        savePasswords();
    }

    public boolean checkPassword(Player player, String password) {
        String stored = cachedPasswords.get(player.getUniqueId());
        if (stored != null && stored.equals(password)) {
            loggedInPlayers.put(player.getUniqueId(), true);
            return true;
        }
        return false;
    }

    public void logout(Player player) {
        loggedInPlayers.remove(player.getUniqueId());
    }

    public void logoutAll() {
        loggedInPlayers.clear();
    }

    public void forceLogin(Player player) {
        loggedInPlayers.put(player.getUniqueId(), true);
    }
}
