package me.ycxmbo.mineStaff;

import me.ycxmbo.mineStaff.commands.*;
import me.ycxmbo.mineStaff.listeners.*;
import me.ycxmbo.mineStaff.managers.*;
import me.ycxmbo.mineStaff.tools.InspectorGUI;
import me.ycxmbo.mineStaff.tools.ToolManager;
import me.ycxmbo.mineStaff.util.CPSManager;
import me.ycxmbo.mineStaff.storage.VanishStore;
import me.ycxmbo.mineStaff.util.VanishUtil;
import me.ycxmbo.mineStaff.bridge.BridgeManager;
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
    private VanishStore vanishStore;
    private StaffChatCommand staffChatCommand; // used by listener to check toggles

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        this.staffDataManager = new StaffDataManager(this);
        this.toolManager = new ToolManager(this);
        this.inspectorGUI = new InspectorGUI(this);
        this.staffLoginManager = new StaffLoginManager(this);
        this.actionLogger = new ActionLogger(this);
        this.cpsManager = new CPSManager(this);
        this.vanishStore = new VanishStore(this);

        // Commands
        getCommand("staffmode").setExecutor(new StaffModeCommand(this));
        getCommand("stafflogin").setExecutor(new StaffLoginCommand(this));
        getCommand("stafflist").setExecutor(new StaffListCommand(this));
        getCommand("staffreload").setExecutor(new StaffReloadCommand(this));
        getCommand("freeze").setExecutor(new FreezeCommand(this));
        getCommand("inspect").setExecutor(new InspectCommand(this));
        getCommand("cpscheck").setExecutor(new CPSCheckCommand(this));

        this.staffChatCommand = new StaffChatCommand(this);
        getCommand("staffchat").setExecutor(staffChatCommand);

        // Listeners
        Bukkit.getPluginManager().registerEvents(new StaffModeListener(this), this);
        Bukkit.getPluginManager().registerEvents(new StaffChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new FreezeListener(this), this);
        Bukkit.getPluginManager().registerEvents(new InspectorGUIListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ToolListener(this), this);
        Bukkit.getPluginManager().registerEvents(new StaffAlertListener(this), this);
        Bukkit.getPluginManager().registerEvents(new AlertListener(this), this);

        // Dynamic bridges (Vulcan/LiteBans)
        BridgeManager.initialize(this);

        // Reapply persisted vanish for online players
        Bukkit.getOnlinePlayers().forEach(p -> {
            if (vanishStore.isVanished(p.getUniqueId())) {
                getStaffDataManager().setVanished(p, true);
                VanishUtil.applyVanish(p, true);
            }
        });

        getLogger().info("MineStaff enabled.");
    }

    @Override
    public void onDisable() {
        staffLoginManager.saveAccounts();
        vanishStore.save();
        getLogger().info("MineStaff disabled.");
    }

    public static MineStaff getInstance() { return instance; }
    public StaffDataManager getStaffDataManager() { return staffDataManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public ToolManager getToolManager() { return toolManager; }
    public InspectorGUI getInspectorGUI() { return inspectorGUI; }
    public StaffLoginManager getStaffLoginManager() { return staffLoginManager; }
    public ActionLogger getActionLogger() { return actionLogger; }
    public CPSManager getCPSManager() { return cpsManager; }
    public VanishStore getVanishStore() { return vanishStore; }
    public StaffChatCommand getStaffChatCommand() { return staffChatCommand; }
}
