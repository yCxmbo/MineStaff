package me.ycxmbo.mineStaff;/*
 * me.ycxmbo.mineStaff.MineStaff - A comprehensive Staff Mode plugin for Minecraft Paper 1.20+
 * Group ID: me.ycxmbo
 * Artifact ID: me.ycxmbo.mineStaff.MineStaff
 *
 * Core class structure:
 * - MineStaff.java (plugin entry point)
 * - me.ycxmbo.mineStaff.commands/StaffModeCommand.java (handles /staffmode and /sm)
 * - me.ycxmbo.mineStaff.listeners/StaffModeListener.java (handles interaction, inventory, and restrictions)
 * - me.ycxmbo.mineStaff.tools/ToolManager.java (generates custom staff me.ycxmbo.mineStaff.tools)
 * - me.ycxmbo.mineStaff.managers/StaffDataManager.java (stores and restores player states)
 * - me.ycxmbo.mineStaff.util/ConfigManager.java (loads and manages config)
 */

// Main plugin class

import me.ycxmbo.mineStaff.commands.StaffListCommand;
import me.ycxmbo.mineStaff.commands.StaffModeCommand;
import me.ycxmbo.mineStaff.listeners.StaffModeListener;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import me.ycxmbo.mineStaff.util.ConfigManager;
import me.ycxmbo.mineStaff.tools.ToolManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MineStaff extends JavaPlugin {

    private static MineStaff instance;
    private StaffDataManager staffDataManager;
    private ConfigManager configManager;
    private ToolManager toolManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        configManager = new ConfigManager(this);
        staffDataManager = new StaffDataManager(this); // ✅ Correct
        toolManager = new ToolManager(this);

        getCommand("staffmode").setExecutor(new StaffModeCommand(this));
        getCommand("sm").setExecutor(new StaffModeCommand(this));
        getCommand("stafflist").setExecutor(new StaffListCommand(this));

        getServer().getPluginManager().registerEvents(new StaffModeListener(this), this);

        getLogger().info("MineStaff has been enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("MineStaff has been disabled.");
    }

    public static MineStaff getInstance() {
        return instance;
    }

    public StaffDataManager getStaffDataManager() {
        return staffDataManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ToolManager getToolManager() {
        return toolManager;
    }
}
