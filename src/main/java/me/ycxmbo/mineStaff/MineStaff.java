package me.ycxmbo.mineStaff;

import me.ycxmbo.mineStaff.commands.*;
import me.ycxmbo.mineStaff.tools.InspectorGUI;
import me.ycxmbo.mineStaff.listeners.*;
import me.ycxmbo.mineStaff.managers.*;
import me.ycxmbo.mineStaff.papi.MineStaffExpansion;
import me.ycxmbo.mineStaff.tools.ToolManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MineStaff extends JavaPlugin {

    private static MineStaff instance;
    public static MineStaff getInstance() { return instance; }

    // --- Core managers / stores ---
    private ConfigManager configManager;
    private StaffDataManager staffDataManager;
    private StaffLoginManager staffLoginManager;
    private ToolManager toolManager;
    private ActionLogger actionLogger;
    private VanishStore vanishStore;

    // --- Feature managers ---
    private ReportManager reportManager;
    private InfractionManager infractionManager;
    private RollbackManager rollbackManager;
    private CPSCheckManager cpsCheckManager;
    private StaffChatManager staffChatManager;

    // --- GUIs / Commands exposed to other classes ---
    private InspectorGUI inspectorGUI;
    private StaffChatCommand staffChatCommand;
    private StaffListGUICommand staffListGUICommand;

    // ===== Getters =====
    public ConfigManager getConfigManager() { return configManager; }
    public StaffDataManager getStaffDataManager() { return staffDataManager; }
    public StaffLoginManager getStaffLoginManager() { return staffLoginManager; }
    public ToolManager getToolManager() { return toolManager; }
    public ActionLogger getActionLogger() { return actionLogger; }
    public VanishStore getVanishStore() { return vanishStore; }

    public ReportManager getReportManager() { return reportManager; }
    public InfractionManager getInfractionManager() { return infractionManager; }
    public RollbackManager getRollbackManager() { return rollbackManager; }

    // legacy + camelCase CPS getters (some code references either)
    public CPSCheckManager getCPSManager() { return cpsCheckManager; }
    public CPSCheckManager getCpsCheckManager() { return cpsCheckManager; }

    public StaffChatManager getStaffChatManager() { return staffChatManager; }
    public StaffChatCommand getStaffChatCommand() { return staffChatCommand; }

    public InspectorGUI getInspectorGUI() { return inspectorGUI; }

    @Override
    public void onEnable() {
        instance = this;

        // --- Instantiate managers / stores first ---
        this.configManager     = new ConfigManager(this);
        this.staffDataManager  = new StaffDataManager(this);
        this.staffLoginManager = new StaffLoginManager(this);
        this.toolManager       = new ToolManager(this);
        this.actionLogger      = new ActionLogger(this);
        this.vanishStore       = new VanishStore(this);         // persist vanish to vanish.yml

        this.reportManager     = new ReportManager(this);
        this.infractionManager = new InfractionManager(this);
        this.rollbackManager   = new RollbackManager(this);
        this.cpsCheckManager   = new CPSCheckManager();         // no-arg manager
        this.staffChatManager  = new StaffChatManager();        // no-arg manager

        // --- GUIs ---
        this.inspectorGUI      = new InspectorGUI(this);

        // --- Commands (guard each in case plugin.yml entry is missing) ---
        if (getCommand("staffmode") != null)
            getCommand("staffmode").setExecutor(new StaffModeCommand(this));

        if (getCommand("stafflogin") != null)
            getCommand("stafflogin").setExecutor(new StaffLoginCommand(this));

        if (getCommand("report") != null)
            getCommand("report").setExecutor(new ReportCommand(this));

        if (getCommand("infractions") != null) {
            getCommand("infractions").setExecutor(new InfractionsCommand(this));
            getCommand("infractions").setTabCompleter(new InfractionsTabCompleter());
        }

        if (getCommand("rollback") != null)
            getCommand("rollback").setExecutor(new RollbackCommand(this));

        if (getCommand("cpscheck") != null)
            getCommand("cpscheck").setExecutor(new CPSCheckCommand(this));

        // Staff List GUI command (both names share one executor)
        this.staffListGUICommand = new StaffListGUICommand(this);
        if (getCommand("stafflistgui") != null)
            getCommand("stafflistgui").setExecutor(staffListGUICommand);
        if (getCommand("stafflist") != null)
            getCommand("stafflist").setExecutor(staffListGUICommand);

        // StaffChat command (toggle or one-off message) + alias /sc
        this.staffChatCommand = new StaffChatCommand(this);
        if (getCommand("staffchat") != null)
            getCommand("staffchat").setExecutor(staffChatCommand);
        if (getCommand("sc") != null)
            getCommand("sc").setExecutor(staffChatCommand);

        // --- Listeners ---
        Bukkit.getPluginManager().registerEvents(new ToolListener(this), this);              // tools: teleport/vanish/freeze/inspect
        Bukkit.getPluginManager().registerEvents(new StaffToolGuardListener(this), this);    // block moving staff tools
        Bukkit.getPluginManager().registerEvents(new SilentChestListener(this), this);       // silent chest view (read-only)
        Bukkit.getPluginManager().registerEvents(new LoginGuardListener(this), this);        // require /stafflogin to move/command
        Bukkit.getPluginManager().registerEvents(new CPSClickListener(this), this);          // count left-clicks for CPS
        Bukkit.getPluginManager().registerEvents(new ReportsGUIListener(this), this);        // if you have a reports GUI
        Bukkit.getPluginManager().registerEvents(new RollbackGUIListener(this), this);       // inventory rollback GUI
        Bukkit.getPluginManager().registerEvents(new InspectorGUIListener(this), this);      // inspector GUI click logic
        Bukkit.getPluginManager().registerEvents(new StaffChatListener(this), this);         // route toggled chat to staff only
        Bukkit.getPluginManager().registerEvents(new StaffListGUIListener(), this);          // make /stafflist GUI read-only
        Bukkit.getPluginManager().registerEvents(new DeathListener(this), this);             // rollback capture (uses your ctor)

        // LiteBans bridge (mirror punishments into infractions) — only if present
        if (Bukkit.getPluginManager().getPlugin("LiteBans") != null) {
            Bukkit.getPluginManager().registerEvents(new LiteBansBridgeListener(this), this);
        }

        // PlaceholderAPI (soft-depend)
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                new MineStaffExpansion(this).register();
                getLogger().info("Registered PlaceholderAPI expansion.");
            } catch (Throwable t) {
                getLogger().warning("Failed to register PlaceholderAPI expansion: " + t.getMessage());
            }
        }

        getLogger().info("MineStaff enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("MineStaff disabled.");
    }
}
