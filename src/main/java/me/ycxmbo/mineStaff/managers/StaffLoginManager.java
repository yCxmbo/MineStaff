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
            try {
                UUID uuid = UUID.fromString(key);
                String passwordHash = staffAccountsConfig.getString("passwords." + key);

                // Migrate plain text passwords to BCrypt
                if (!passwordHash.startsWith("$2a$")) {
                    plugin.getLogger().info("Migrating plain text password for " + key + " to BCrypt");
                    String newHash = BCrypt.hashpw(passwordHash, BCrypt.gensalt());
                    staffAccountsConfig.set("passwords." + key, newHash);
                    savePasswords();
                }

                cachedPasswords.put(uuid, passwordHash);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().severe("Invalid UUID in staffaccounts.yml: " + key);
            }
        }
    }

    private void savePasswords() {
        staffAccountsConfig.set("passwords", null); // Clear old entries
        staffAccountsConfig.createSection("passwords", cachedPasswords); // Save all cached passwords

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
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        cachedPasswords.put(player.getUniqueId(), hashed);
        savePasswords();
    }

    public boolean checkPassword(Player player, String password) {
        String storedHash = cachedPasswords.get(player.getUniqueId());
        if (storedHash != null && BCrypt.checkpw(password, storedHash)) {
            loggedInPlayers.put(player.getUniqueId(), true);
            return true;
        }
        return false;
    }

    public void logout(Player player) {
        loggedInPlayers.remove(player.getUniqueId());
        plugin.getStaffDataManager().unVanishPlayer(player); // Ensure vanish is reset
    }

    public void logoutAll() {
        loggedInPlayers.clear();
        plugin.getStaffDataManager().getStaffMap().keySet().forEach(uuid -> {
            Player player = org.bukkit.Bukkit.getPlayer(uuid);
            if (player != null) {
                plugin.getStaffDataManager().unVanishPlayer(player);
            }
        });
    }

    public void forceLogin(Player player) {
        loggedInPlayers.put(player.getUniqueId(), true);
    }
}
