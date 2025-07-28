package me.ycxmbo.mineStaff;

import me.ycxmbo.mineStaff.commands.StaffListCommand;
import me.ycxmbo.mineStaff.commands.StaffModeCommand;
import me.ycxmbo.mineStaff.listeners.AlertListener;
import me.ycxmbo.mineStaff.listeners.InspectorGUIListener;
import me.ycxmbo.mineStaff.listeners.StaffAlertListener;
import me.ycxmbo.mineStaff.listeners.StaffModeListener;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import me.ycxmbo.mineStaff.tools.InspectorGUI;
import me.ycxmbo.mineStaff.tools.ToolManager;
import me.ycxmbo.mineStaff.util.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MineStaff extends JavaPlugin {

    private static MineStaff instance;
    private StaffDataManager staffDataManager;
    private ConfigManager configManager;
    private ToolManager toolManager;
    private InspectorGUI inspectorGUI;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        configManager = new ConfigManager(this);
        staffDataManager = new StaffDataManager(this);
        toolManager = new ToolManager(this);
        inspectorGUI = new InspectorGUI(this);

        // Register commands
        getCommand("staffmode").setExecutor(new StaffModeCommand(this));
        getCommand("sm").setExecutor(new StaffModeCommand(this));
        getCommand("stafflist").setExecutor(new StaffListCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new AlertListener(this), this);
        getServer().getPluginManager().registerEvents(new me.ycxmbo.mineStaff.listeners.AlertListener(this), this);
        getServer().getPluginManager().registerEvents(new StaffModeListener(this), this);
        getServer().getPluginManager().registerEvents(new InspectorGUIListener(this), this);
        getServer().getPluginManager().registerEvents(new StaffAlertListener(this), this); // ✅ NEW

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

    public InspectorGUI getInspectorGUI() {
        return inspectorGUI;
    }
}
