package me.ycxmbo.mineStaff;

import me.ycxmbo.mineStaff.commands.*;
import me.ycxmbo.mineStaff.listeners.*;
import me.ycxmbo.mineStaff.managers.*;
import me.ycxmbo.mineStaff.tools.*;
import me.ycxmbo.mineStaff.util.ConfigManager;
import org.bukkit.Bukkit;
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
    private StaffChatCommand staffChatCommand;

    @Override
    public void onEnable() {
        instance = this;

        // Config
        saveDefaultConfig();
        configManager = new ConfigManager(this);

        // Managers
        staffDataManager = new StaffDataManager(this);
        toolManager = new ToolManager(this);
        inspectorGUI = new InspectorGUI();
        staffLoginManager = new StaffLoginManager(this);
        actionLogger = new ActionLogger(this);
        cpsManager = new CPSManager(this);
        staffChatCommand = new StaffChatCommand(this);

        // Commands
        getCommand("staffmode").setExecutor(new StaffModeCommand(this));
        getCommand("stafflist").setExecutor(new StaffListCommand(this));
        getCommand("stafflogin").setExecutor(new StaffLoginCommand(this));
        getCommand("cpscheck").setExecutor(new CPSCheckCommand(this));
        getCommand("staffinspect").setExecutor(new StaffInspectCommand(this));
        getCommand("staffchat").setExecutor(staffChatCommand); // Toggles or sends message

        // Listeners
        getServer().getPluginManager().registerEvents(new AlertListener(this), this);
        getServer().getPluginManager().registerEvents(new StaffModeListener(this), this);
        getServer().getPluginManager().registerEvents(new InspectorGUIListener(this), this);
        getServer().getPluginManager().registerEvents(new StaffAlertListener(this), this);
        getServer().getPluginManager().registerEvents(new FreezeListener(this), this);

        // Register Vulcan alerts only if available
        if (Bukkit.getPluginManager().isPluginEnabled("Vulcan")) {
            getLogger().info("Vulcan Anti-Cheat detected. Enabling cheat detection alerts.");
            getServer().getPluginManager().registerEvents(new VulcanListener(this), this);
        } else {
            getLogger().warning("Vulcan Anti-Cheat not found. Cheat alerts are disabled.");
        }

        getLogger().info("MineStaff has been enabled.");
    }

    @Override
    public void onDisable() {
        // Optionally force logout all staff
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

    public StaffChatCommand getStaffChatCommand() {
        return staffChatCommand;
    }
}
