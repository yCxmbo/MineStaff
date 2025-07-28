package me.ycxmbo.mineStaff;

import me.ycxmbo.mineStaff.commands.*;
import me.ycxmbo.mineStaff.listeners.*;
import me.ycxmbo.mineStaff.managers.*;
import me.ycxmbo.mineStaff.tools.*;
import me.ycxmbo.mineStaff.util.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MineStaff extends JavaPlugin {

    private static MineStaff instance;

    private StaffDataManager staffDataManager;
    private ConfigManager configManager;
    private ToolManager toolManager;
    private InspectorGUI inspectorGUI;
    private StaffLoginManager staffLoginManager;
    private ActionLogger actionLogger;
    private CPSManager cpsManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        configManager = new ConfigManager(this);
        staffDataManager = new StaffDataManager(this);
        toolManager = new ToolManager(this);
        inspectorGUI = new InspectorGUI();
        staffLoginManager = new StaffLoginManager(this);
        actionLogger = new ActionLogger(this);
        cpsManager = new CPSManager(this);

        // Register commands
        getCommand("staffmode").setExecutor(new StaffModeCommand(this));
        getCommand("sm").setExecutor(new StaffModeCommand(this));
        getCommand("stafflist").setExecutor(new StaffListCommand(this));
        getCommand("stafflogin").setExecutor(new StaffLoginCommand(this));
        getCommand("cpscheck").setExecutor(new CPSCheckCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new AlertListener(this), this);
        getServer().getPluginManager().registerEvents(new StaffModeListener(this), this);
        getServer().getPluginManager().registerEvents(new InspectorGUIListener(this), this);
        getServer().getPluginManager().registerEvents(new StaffAlertListener(this), this);

        getLogger().info("MineStaff has been enabled.");
    }

    @Override
    public void onDisable() {
        // Force logout all on disable (optional)
        staffLoginManager.logoutAll();
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

    public StaffLoginManager getStaffLoginManager() {
        return staffLoginManager;
    }

    public ActionLogger getActionLogger() {
        return actionLogger;
    }

    public CPSManager getCPSManager() {
        return cpsManager;
    }
}
