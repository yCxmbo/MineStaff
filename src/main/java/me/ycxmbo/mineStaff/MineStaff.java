package me.ycxmbo.mineStaff;

import me.ycxmbo.mineStaff.api.internal.MineStaffApiProvider;
import me.ycxmbo.mineStaff.commands.*;
import me.ycxmbo.mineStaff.listeners.*;
import me.ycxmbo.mineStaff.managers.*;
import me.ycxmbo.mineStaff.papi.MineStaffExpansion;
import me.ycxmbo.mineStaff.storage.VanishStore;
import me.ycxmbo.mineStaff.tools.InspectorGUI;
import me.ycxmbo.mineStaff.tools.ToolManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class MineStaff extends JavaPlugin {

    private static MineStaff instance;
    public static MineStaff getInstance() { return instance; }

    // Core
    private ConfigManager configManager;
    private StaffDataManager staffDataManager;
    private StaffLoginManager staffLoginManager;
    private ToolManager toolManager;
    private ActionLogger actionLogger;
    private VanishStore vanishStore;

    // Feature
    private ReportManager reportManager;
    private InfractionManager infractionManager;
    private RollbackManager rollbackManager;
    private CPSCheckManager cpsCheckManager;
    private StaffChatManager staffChatManager;

    // GUIs/Commands singletons
    private InspectorGUI inspectorGUI;
    private StaffChatCommand staffChatCommand;
    private StaffListGUICommand staffListGUICommand;

    // Getters
    public ConfigManager getConfigManager() { return configManager; }
    public StaffDataManager getStaffDataManager() { return staffDataManager; }
    public StaffLoginManager getStaffLoginManager() { return staffLoginManager; }
    public ToolManager getToolManager() { return toolManager; }
    public ActionLogger getActionLogger() { return actionLogger; }
    public VanishStore getVanishStore() { return vanishStore; }
    public ReportManager getReportManager() { return reportManager; }
    public InfractionManager getInfractionManager() { return infractionManager; }
    public RollbackManager getRollbackManager() { return rollbackManager; }
    public CPSCheckManager getCPSManager() { return cpsCheckManager; } // legacy name
    public CPSCheckManager getCpsCheckManager() { return cpsCheckManager; }
    public StaffChatManager getStaffChatManager() { return staffChatManager; }
    public InspectorGUI getInspectorGUI() { return inspectorGUI; }
    public StaffChatCommand getStaffChatCommand() { return staffChatCommand; }

    @Override
    public void onEnable() {
        instance = this;

        // Instantiate managers
        this.configManager     = new ConfigManager(this);
        this.staffDataManager  = new StaffDataManager(this);
        this.staffLoginManager = new StaffLoginManager(this);
        this.toolManager       = new ToolManager(this);
        this.actionLogger      = new ActionLogger(this);
        this.vanishStore       = new VanishStore(this);

        this.reportManager     = new ReportManager(this);
        this.infractionManager = new InfractionManager(this);
        this.rollbackManager   = new RollbackManager(this);
        this.cpsCheckManager   = new CPSCheckManager();
        this.staffChatManager  = new StaffChatManager();

        // GUIs
        this.inspectorGUI      = new InspectorGUI(this);

        // Commands
        if (getCommand("staffmode") != null) getCommand("staffmode").setExecutor(new StaffModeCommand(this));
        if (getCommand("stafflogin") != null) getCommand("stafflogin").setExecutor(new StaffLoginCommand(this));
        if (getCommand("report") != null) getCommand("report").setExecutor(new ReportCommand(this));
        if (getCommand("infractions") != null) {
            getCommand("infractions").setExecutor(new InfractionsCommand(this));
            getCommand("infractions").setTabCompleter(new InfractionsTabCompleter());
        }
        if (getCommand("rollback") != null) getCommand("rollback").setExecutor(new RollbackCommand(this));
        if (getCommand("cpscheck") != null) getCommand("cpscheck").setExecutor(new CPSCheckCommand(this));

        this.staffListGUICommand = new StaffListGUICommand(this);
        if (getCommand("stafflistgui") != null) getCommand("stafflistgui").setExecutor(staffListGUICommand);
        if (getCommand("stafflist") != null) getCommand("stafflist").setExecutor(staffListGUICommand);

        this.staffChatCommand = new StaffChatCommand(this);
        if (getCommand("staffchat") != null) getCommand("staffchat").setExecutor(staffChatCommand);
        if (getCommand("sc") != null) getCommand("sc").setExecutor(staffChatCommand);

        // Listeners
        Bukkit.getPluginManager().registerEvents(new ToolListener(this), this);
        Bukkit.getPluginManager().registerEvents(new StaffToolGuardListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SilentChestListener(this), this);
        Bukkit.getPluginManager().registerEvents(new LoginGuardListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CPSClickListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ReportsGUIListener(this), this);
        Bukkit.getPluginManager().registerEvents(new RollbackGUIListener(this), this);
        Bukkit.getPluginManager().registerEvents(new InspectorGUIListener(this), this);
        Bukkit.getPluginManager().registerEvents(new StaffChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new StaffListGUIListener(), this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(this), this);
        Bukkit.getPluginManager().registerEvents(new FreezeListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CreativeBlockerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new StaffModeListener(this), this);

        if (Bukkit.getPluginManager().getPlugin("LiteBans") != null) {
            Bukkit.getPluginManager().registerEvents(new LiteBansBridgeListener(this), this);
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                new MineStaffExpansion(this).register();
                getLogger().info("Registered PlaceholderAPI expansion.");
            } catch (Throwable t) {
                getLogger().warning("Failed to register PlaceholderAPI expansion: " + t.getMessage());
            }
        }

        // Register API service
        getServer().getServicesManager().register(
                me.ycxmbo.mineStaff.api.MineStaffAPI.class,
                new MineStaffApiProvider(this),
                this,
                ServicePriority.Normal
        );

        getLogger().info("MineStaff enabled.");
    }

    @Override
    public void onDisable() {
        // Unregister API service
        getServer().getServicesManager().unregister(me.ycxmbo.mineStaff.api.MineStaffAPI.class, null);
        getLogger().info("MineStaff disabled.");
    }
}
