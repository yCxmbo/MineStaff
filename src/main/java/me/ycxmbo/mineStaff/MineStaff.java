package me.ycxmbo.mineStaff;

import me.ycxmbo.mineStaff.api.internal.MineStaffApiProvider;
import me.ycxmbo.mineStaff.commands.*;
import me.ycxmbo.mineStaff.listeners.*;
import me.ycxmbo.mineStaff.listeners.StaffAlertListener;
import me.ycxmbo.mineStaff.managers.*;
import me.ycxmbo.mineStaff.papi.MineStaffExpansion;
import me.ycxmbo.mineStaff.storage.VanishStore;
import me.ycxmbo.mineStaff.tools.InspectorGUI;
import me.ycxmbo.mineStaff.tools.ToolManager;
import me.ycxmbo.mineStaff.messaging.ProxyMessenger;
import me.ycxmbo.mineStaff.messaging.RedisBridge;
import me.ycxmbo.mineStaff.messaging.DiscordBridge;
import me.ycxmbo.mineStaff.spy.SpyManager;
import me.ycxmbo.mineStaff.services.FreezeService;
import me.ycxmbo.mineStaff.audit.JsonAuditLogger;
import me.ycxmbo.mineStaff.util.ActivityTracker;
import me.ycxmbo.mineStaff.storage.SqlStorage;
import me.ycxmbo.mineStaff.notes.PlayerNotesManager;
import me.ycxmbo.mineStaff.offline.OfflineInventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.Set;

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
    private StaffDutyManager staffDutyManager;

    // Feature
    private ReportManager reportManager;
    private InfractionManager infractionManager;
    private RollbackManager rollbackManager;
    private CPSCheckManager cpsCheckManager;
    private StaffChatManager staffChatManager;
    private ProxyMessenger proxyMessenger;
    private RedisBridge redisBridge;
    private DiscordBridge discordBridge;
    private SpyManager spyManager;
    private JsonAuditLogger auditLogger;
    private ActivityTracker activityTracker;
    private SqlStorage sqlStorage;
    private me.ycxmbo.mineStaff.evidence.EvidenceManager evidenceManager;
    private FreezeService freezeService;
    private PlayerNotesManager playerNotesManager;
    private OfflineInventoryManager offlineInventoryManager;

    // GUIs/Commands singletons
    private InspectorGUI inspectorGUI;
    private StaffChatCommand staffChatCommand;
    private StaffListGUICommand staffListGUICommand;
    private StaffListCommand staffListCommand;

    // Getters
    public ConfigManager getConfigManager() { return configManager; }
    public StaffDataManager getStaffDataManager() { return staffDataManager; }
    public StaffLoginManager getStaffLoginManager() { return staffLoginManager; }
    public ToolManager getToolManager() { return toolManager; }
    public ActionLogger getActionLogger() { return actionLogger; }
    public VanishStore getVanishStore() { return vanishStore; }
    public StaffDutyManager getStaffDutyManager() { return staffDutyManager; }
    public ReportManager getReportManager() { return reportManager; }
    public InfractionManager getInfractionManager() { return infractionManager; }
    public RollbackManager getRollbackManager() { return rollbackManager; }
    public CPSCheckManager getCPSManager() { return cpsCheckManager; } // legacy name
    public CPSCheckManager getCpsCheckManager() { return cpsCheckManager; }
    public StaffChatManager getStaffChatManager() { return staffChatManager; }
    public ProxyMessenger getProxyMessenger() { return proxyMessenger; }
    public RedisBridge getRedisBridge() { return redisBridge; }
    public DiscordBridge getDiscordBridge() { return discordBridge; }
    public FreezeService getFreezeService() { return freezeService; }
    public SpyManager getSpyManager() { return spyManager; }
    public JsonAuditLogger getAuditLogger() { return auditLogger; }
    public ActivityTracker getActivityTracker() { return activityTracker; }
    public SqlStorage getStorage() { return sqlStorage; }
    public InspectorGUI getInspectorGUI() { return inspectorGUI; }
    public StaffChatCommand getStaffChatCommand() { return staffChatCommand; }
    public me.ycxmbo.mineStaff.evidence.EvidenceManager getEvidenceManager() { return evidenceManager; }
    public PlayerNotesManager getPlayerNotesManager() { return playerNotesManager; }
    public OfflineInventoryManager getOfflineInventoryManager() { return offlineInventoryManager; }

    private static final Set<String> SUPPORTED_SERVER_BRANDS = Set.of("Paper", "Purpur", "Spigot", "CraftBukkit");
    private static final String SUPPORTED_VERSION_RANGE = "1.20.x-1.21.x";

    @Override
    public void onEnable() {
        instance = this;

        logServerCompatibility();

        // Instantiate managers
        this.configManager     = new ConfigManager(this);
        this.staffDataManager  = new StaffDataManager(this);
        this.staffLoginManager = new StaffLoginManager(this);
        this.toolManager       = new ToolManager(this);
        this.actionLogger      = new ActionLogger(this);
        this.vanishStore       = new VanishStore(this);
        this.staffDutyManager  = new StaffDutyManager(this);

        this.reportManager     = new ReportManager(this);
        this.infractionManager = new InfractionManager(this);
        this.rollbackManager   = new RollbackManager(this);
        this.cpsCheckManager   = new CPSCheckManager(this);
        this.staffChatManager  = new StaffChatManager(this);
        this.proxyMessenger    = new ProxyMessenger(this);
        this.redisBridge       = new RedisBridge(this);
        this.discordBridge     = new DiscordBridge(this);
        this.freezeService     = new FreezeService(this);
        this.spyManager        = new SpyManager();
        this.auditLogger       = new JsonAuditLogger(this);
        this.activityTracker   = new ActivityTracker();
        this.evidenceManager   = new me.ycxmbo.mineStaff.evidence.EvidenceManager(this);
        // Optional SQL storage
        try {
            String mode = getConfigManager().getConfig().getString("storage.mode", "yaml");
            if (!"yaml".equalsIgnoreCase(mode)) {
                this.sqlStorage = new SqlStorage(this);
                getLogger().info("SQL storage initialized using mode: " + mode);
            }
        } catch (Throwable t) {
            getLogger().warning("SQL storage init failed; falling back to YAML: " + t.getMessage());
            this.sqlStorage = null;
        }

        this.playerNotesManager = new PlayerNotesManager(this);
        this.offlineInventoryManager = new OfflineInventoryManager(this);

        // GUIs
        this.inspectorGUI      = new InspectorGUI(this);

        boolean staffLoginEnabled = configManager.isStaffLoginEnabled();
        boolean loginRequired = configManager.isLoginRequired();

        // Commands
        if (getCommand("staffmode") != null) getCommand("staffmode").setExecutor(new StaffModeCommand(this));
        if (getCommand("stafflogin") != null) {
            if (staffLoginEnabled) {
                getCommand("stafflogin").setExecutor(new StaffLoginCommand(this));
            } else {
                getCommand("stafflogin").setExecutor((sender, command, label, args) -> {
                    sender.sendMessage(configManager.getMessage("staff_login_disabled", "Staff login is disabled."));
                    return true;
                });
            }
        }
        if (getCommand("report") != null) getCommand("report").setExecutor(new ReportCommand(this));
        if (getCommand("infractions") != null) {
            getCommand("infractions").setExecutor(new InfractionsCommand(this));
            getCommand("infractions").setTabCompleter(new InfractionsTabCompleter());
        }
        if (getCommand("rollback") != null) getCommand("rollback").setExecutor(new RollbackCommand(this));
        if (getCommand("cpscheck") != null) getCommand("cpscheck").setExecutor(new CPSCheckCommand(this));
        if (getCommand("inspect") != null) getCommand("inspect").setExecutor(new InspectCommand(this));
        if (getCommand("freeze") != null) getCommand("freeze").setExecutor(new FreezeCommand(this));
        if (getCommand("staffreload") != null) getCommand("staffreload").setExecutor(new StaffReloadCommand(this));
        if (getCommand("reports") != null) getCommand("reports").setExecutor(new me.ycxmbo.mineStaff.commands.ReportsGUICommand(this));
        StaffDutyCommand staffDutyCommand = new StaffDutyCommand(this.staffDutyManager);
        if (getCommand("staffduty") != null) getCommand("staffduty").setExecutor(staffDutyCommand);
        if (getCommand("duty") != null) getCommand("duty").setExecutor(staffDutyCommand);
        if (getCommand("commandspy") != null) getCommand("commandspy").setExecutor(new me.ycxmbo.mineStaff.commands.CommandSpyCommand(this));
        if (getCommand("socialspy") != null) getCommand("socialspy").setExecutor(new me.ycxmbo.mineStaff.commands.SocialSpyCommand(this));
        if (getCommand("notes") != null) getCommand("notes").setExecutor(new me.ycxmbo.mineStaff.commands.NotesCommand(this));
        if (getCommand("profile") != null) getCommand("profile").setExecutor(new me.ycxmbo.mineStaff.commands.ProfileCommand(this));
        if (getCommand("inspectoffline") != null) getCommand("inspectoffline").setExecutor(new me.ycxmbo.mineStaff.commands.InspectOfflineCommand(offlineInventoryManager));
        if (getCommand("staff2fa") != null) getCommand("staff2fa").setExecutor(new me.ycxmbo.mineStaff.commands.Staff2FACommand(this));
        if (getCommand("evidence") != null) getCommand("evidence").setExecutor(new me.ycxmbo.mineStaff.commands.EvidenceCommand(this));

        this.staffListGUICommand = new StaffListGUICommand(this);
        this.staffListCommand = new StaffListCommand(this);
        if (getCommand("stafflistgui") != null) getCommand("stafflistgui").setExecutor(staffListGUICommand);
        if (getCommand("stafflist") != null) getCommand("stafflist").setExecutor(staffListCommand);

        this.staffChatCommand = new StaffChatCommand(this);
        if (getCommand("staffchat") != null) getCommand("staffchat").setExecutor(staffChatCommand);
        if (getCommand("sc") != null) getCommand("sc").setExecutor(staffChatCommand);
        if (getCommand("inspect") != null) getCommand("inspect").setExecutor(new InspectCommand(this));
        if (getCommand("freeze") != null) getCommand("freeze").setExecutor(new FreezeCommand(this));
        if (getCommand("staffreload") != null) getCommand("staffreload").setExecutor(new StaffReloadCommand(this));
        if (getCommand("reports") != null) getCommand("reports").setExecutor(new me.ycxmbo.mineStaff.commands.ReportsGUICommand(this));

        // Listeners
        Bukkit.getPluginManager().registerEvents(new ToolListener(this), this);
        Bukkit.getPluginManager().registerEvents(new StaffToolGuardListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SilentChestListener(this), this);
        if (loginRequired) {
            Bukkit.getPluginManager().registerEvents(new LoginGuardListener(this), this);
        }
        Bukkit.getPluginManager().registerEvents(new CPSClickListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ReportsGUIListener(this), this);
        Bukkit.getPluginManager().registerEvents(new RollbackGUIListener(this), this);
        Bukkit.getPluginManager().registerEvents(new InspectorGUIListener(this), this);
        Bukkit.getPluginManager().registerEvents(new StaffChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new StaffAlertListener(this), this);
        Bukkit.getPluginManager().registerEvents(new VanishEffectListener(this), this);
        Bukkit.getPluginManager().registerEvents(new AlertListener(this), this);
        Bukkit.getPluginManager().registerEvents(new StaffGameModeGuardListener(this), this);
        Bukkit.getPluginManager().registerEvents(new StaffListGUIListener(), this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(getRollbackManager()), this);
        Bukkit.getPluginManager().registerEvents(new FreezeListener(this), this);
        Bukkit.getPluginManager().registerEvents(new FreezeQuitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CreativeBlockerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new StaffModeListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SpyListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ProfileGUIListener(offlineInventoryManager), this);
        Bukkit.getPluginManager().registerEvents(offlineInventoryManager, this);
        Bukkit.getPluginManager().registerEvents(new ActivityListener(this), this);
        Bukkit.getPluginManager().registerEvents(new LuckPermsContextListener(this), this);

        if (Bukkit.getPluginManager().getPlugin("LiteBans") != null) {
            Bukkit.getPluginManager().registerEvents(new LiteBansBridgeListener(this), this);
        }

        // Optional: initialize reflection-based bridges for Vulcan/LiteBans alerts
        try { me.ycxmbo.mineStaff.bridge.BridgeManager.initialize(this); } catch (Throwable ignored) {}

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                new MineStaffExpansion(this).register();
                getLogger().info("Registered PlaceholderAPI expansion.");
            } catch (Throwable t) {
                getLogger().warning("Failed to register PlaceholderAPI expansion: " + t.getMessage());
            }
        }

        // Init cross-server messaging (Bungee/Velocity plugin messaging)
        try { proxyMessenger.init(); } catch (Throwable t) { getLogger().warning("Proxy messenger init failed: " + t.getMessage()); }
        try { redisBridge.init(); } catch (Throwable t) { getLogger().warning("Redis bridge init failed: " + t.getMessage()); }

        // Register API service
        getServer().getServicesManager().register(
                me.ycxmbo.mineStaff.api.MineStaffAPI.class,
                new MineStaffApiProvider(this),
                this,
                ServicePriority.Normal
        );

        // Re-apply persisted vanish state for online players (e.g., on /reload)
        for (org.bukkit.entity.Player p : Bukkit.getOnlinePlayers()) {
            if (vanishStore.isVanished(p.getUniqueId())) {
                staffDataManager.setVanished(p, true);
                me.ycxmbo.mineStaff.util.VanishUtil.applyVanish(p, true);
                toolManager.updateVanishDye(p, true);
            }
        }

        getLogger().info("MineStaff enabled.");
    }

    @Override
    public void onDisable() {
        // Persist and cleanup
        try { vanishStore.save(); } catch (Throwable ignored) {}
        try { configManager.saveStaffAccounts(); } catch (Throwable ignored) {}
        try { if (sqlStorage != null) sqlStorage.close(); } catch (Throwable ignored) {}
        try { if (redisBridge != null) redisBridge.close(); } catch (Throwable ignored) {}
        try { if (freezeService != null) freezeService.stop(); } catch (Throwable ignored) {}
        // Unregister API service(s)
        getServer().getServicesManager().unregisterAll(this);
        getLogger().info("MineStaff disabled.");
    }

    private void logServerCompatibility() {
        String versionInfo = Bukkit.getVersion();
        String minecraftVersion = Bukkit.getMinecraftVersion();

        boolean supportedBrand = SUPPORTED_SERVER_BRANDS.stream()
            .anyMatch(brand -> versionInfo.toLowerCase(Locale.ROOT).contains(brand.toLowerCase(Locale.ROOT)));
        boolean supportedVersion = minecraftVersion.startsWith("1.20") || minecraftVersion.startsWith("1.21");

        if (supportedBrand && supportedVersion) {
            getLogger().info("Detected supported server environment: " + versionInfo + " (Minecraft " + minecraftVersion + ")");
            return;
        }

        if (!supportedBrand) {
            getLogger().warning("Detected server implementation '" + versionInfo + "'. MineStaff officially supports Paper, Purpur, and Spigot.");
        }

        if (supportedVersion) {
            getLogger().info("Minecraft version " + minecraftVersion + " is within the supported range (" + SUPPORTED_VERSION_RANGE + ").");
        } else {
            getLogger().severe("Minecraft version " + minecraftVersion + " is outside the supported range (" + SUPPORTED_VERSION_RANGE + ").");
        }
    }
}
