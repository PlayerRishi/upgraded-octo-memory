package org.pluginmakers.piCraftPlugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.pluginmakers.piCraftPlugin.commands.EvidenceCommand;
import org.pluginmakers.piCraftPlugin.commands.HomeCommand;
import org.pluginmakers.piCraftPlugin.commands.ReportCommand;
import org.pluginmakers.piCraftPlugin.commands.ReportTabCompleter;
import org.pluginmakers.piCraftPlugin.commands.RulesCommand;
import org.pluginmakers.piCraftPlugin.commands.SpawnCommand;
import org.pluginmakers.piCraftPlugin.commands.StaffCommands;
import org.pluginmakers.piCraftPlugin.config.ConfigManager;
import org.pluginmakers.piCraftPlugin.database.DatabaseManager;
import org.pluginmakers.piCraftPlugin.detection.BaseRadiusEnforcer;
import org.pluginmakers.piCraftPlugin.detection.CombatLogDetector;
import org.pluginmakers.piCraftPlugin.detection.DragonEggTracker;
import org.pluginmakers.piCraftPlugin.detection.ReplayModDetector;
import org.pluginmakers.piCraftPlugin.detection.SeedAbuseDetector;
import org.pluginmakers.piCraftPlugin.detection.VillagerKillDetector;
import org.pluginmakers.piCraftPlugin.detection.WeaknessPotionDetector;
import org.pluginmakers.piCraftPlugin.listeners.PlayerJoinListener;
import org.pluginmakers.piCraftPlugin.managers.BaseTracker;
import org.pluginmakers.piCraftPlugin.managers.ReportManager;
import org.pluginmakers.piCraftPlugin.web.WebDashboard;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;

public final class PiCraftPlugin extends JavaPlugin {
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private ReportManager reportManager;
    private BaseTracker baseTracker;
    private WebDashboard webDashboard;
    
    @Override
    public void onEnable() {
        // Initialize configuration
        configManager = new ConfigManager(this);
        
        // Initialize database
        databaseManager = new DatabaseManager(getDataFolder());
        try {
            databaseManager.initialize();
        } catch (SQLException e) {
            getLogger().severe("Failed to initialize database: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Initialize managers
        reportManager = new ReportManager(this);
        baseTracker = new BaseTracker(this);
        webDashboard = new WebDashboard(this);
        
        // Register commands
        registerCommands();
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        
        // Register detection systems
        if (configManager != null && configManager.getConfig() != null) {
            if (configManager.getConfig().getBoolean("reports.auto_detection.combat_logging.enabled", true)) {
                getServer().getPluginManager().registerEvents(new CombatLogDetector(this), this);
            }
            if (configManager.getConfig().getBoolean("reports.auto_detection.seed_abuse.enabled", true)) {
                getServer().getPluginManager().registerEvents(new SeedAbuseDetector(this), this);
            }
            if (configManager.getConfig().getBoolean("reports.base_tracking.enforce_radius", true)) {
                getServer().getPluginManager().registerEvents(new BaseRadiusEnforcer(this), this);
            }
            if (configManager.getConfig().getBoolean("reports.auto_detection.replay_mod.enabled", true)) {
                getServer().getPluginManager().registerEvents(new ReplayModDetector(this), this);
            }
            if (configManager.getConfig().getBoolean("reports.auto_detection.dragon_egg.enabled", true)) {
                getServer().getPluginManager().registerEvents(new DragonEggTracker(this), this);
            }
            if (configManager.getConfig().getBoolean("reports.auto_detection.weakness_potions.enabled", true)) {
                getServer().getPluginManager().registerEvents(new WeaknessPotionDetector(this), this);
            }
            if (configManager.getConfig().getBoolean("reports.auto_detection.villager_kills.enabled", true)) {
                getServer().getPluginManager().registerEvents(new VillagerKillDetector(this), this);
            }
        }
        
        // Create default rules file if it doesn't exist
        createDefaultRulesFile();
        
        // Start web dashboard
        webDashboard.start();
        
        getLogger().info("PiCraft Plugin has been enabled!");
    }
    
    @Override
    public void onDisable() {
        if (webDashboard != null) {
            webDashboard.stop();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("PiCraft Plugin has been disabled!");
    }
    
    private void registerCommands() {
        // Player commands
        if (getCommand("report") != null) {
            getCommand("report").setExecutor(new ReportCommand(this));
            getCommand("report").setTabCompleter(new ReportTabCompleter(this));
        }
        if (getCommand("rules") != null) {
            getCommand("rules").setExecutor(new RulesCommand(this));
        }
        
        // Staff commands
        StaffCommands staffCommands = new StaffCommands(this);
        if (getCommand("reports") != null) getCommand("reports").setExecutor(staffCommands);
        if (getCommand("reportlist") != null) getCommand("reportlist").setExecutor(staffCommands);
        if (getCommand("reportview") != null) getCommand("reportview").setExecutor(staffCommands);
        if (getCommand("reportread") != null) getCommand("reportread").setExecutor(staffCommands);
        if (getCommand("reportclear") != null) getCommand("reportclear").setExecutor(staffCommands);
        if (getCommand("reportassign") != null) getCommand("reportassign").setExecutor(staffCommands);
        if (getCommand("reportnotify") != null) getCommand("reportnotify").setExecutor(staffCommands);
        if (getCommand("reporttp") != null) getCommand("reporttp").setExecutor(staffCommands);
        if (getCommand("evidence") != null) getCommand("evidence").setExecutor(new EvidenceCommand(this));
        if (getCommand("home") != null) getCommand("home").setExecutor(new HomeCommand(this));
        if (getCommand("spawn") != null) getCommand("spawn").setExecutor(new SpawnCommand(this));
    }
    
    private void createDefaultRulesFile() {
        File rulesFile = new File(getDataFolder(), configManager != null ? configManager.getRulesFile() : "rules.txt");
        
        if (!rulesFile.exists()) {
            try {
                if (!getDataFolder().exists()) {
                    if (!getDataFolder().mkdirs()) {
                        getLogger().warning("Failed to create plugin data folder");
                    }
                }
                
                String defaultRules = """
                    # Rules of the Server:
                    
                    ## Hard Enforced Rules:
                    ### - No exploiting
                    ### - No Duping
                    ### - No Xray or Hacking
                    ### - Be respectful and Family Friendly
                    ### - Have fun!
                    - Breaking these will result in a 3 day ban
                    - If anyone finds dupe stashes IMMEDIATELY REPORT to <@1313691983126069289> or <@873986014937481346>, or any of the server mods!
                    
                    ## Soft Enforced Rules:
                    ### - No Combat Logging
                          ### - This includes randomly PvP-ing at spawn
                    ### - Replay Mod/Flashback may not be used during gameplay or events to find traps and etc.
                          ### - These mods may only be used after logging off after a session, and they may not be used for base hunting
                    ### - No F3+A to reload chunks
                    ### - All bases need to be built within a 2500 block radius of Spawn
                    ### - No Stream Sniping (IDK if anyone actually streams)
                    ### - No crazy ahh griefing (like destroying whole bases, taking a block or two or leaving a message is fine. you get the idea)
                    ### - DO NOT steal more than 64x of any item from any player, you can steal upon a kill.
                    ### - No Dragon Egg inside of Ender Chest
                    ### - DO NOT abuse the seed to get an unfair advantage!
                    ### - No Toxicity
                    ### - No mass killing of pets
                    ### - No killing of villagers
                    
                    ## Spawn Rules:
                    ### - No crazy insane battles right at spawn.
                    ### - No griefing at all
                    ### - No bases right at spawn
                    ### - Spawn is a 160 by 160 block radius from the spawn point/a decided location by everyone near the original spawn point
                    
                    ## Farm Rules:
                    ### - No auto AFK raid farms
                    ### - No auto AFK wither-skeleton farms
                    
                    ## Item Rules:
                    ### - No Elytra (During Combat)
                    ### - Only 2 netherite armour pieces and any netherite tools are allowed
                    ### - Maximum of 8 End Crystals/Respawn Anchors, per fight
                    ### - No Restocking of items while in combat with use of Shulkers/Ender-Chests/Chests
                    ### - No weakness potions or arrows
                    
                    ## How to join:
                    ### Name: PiCraft Season 2
                    ### IP: 71.187.21.145
                    ### Any Version
                    
                    ## Remember to always be friendly and HAVE FUN!
                    """;

                
                Files.write(rulesFile.toPath(), defaultRules.getBytes());
                getLogger().info("Created default rules.txt file");
                
            } catch (IOException e) {
                getLogger().warning("Failed to create default rules file: " + e.getMessage());
            }
        }
    }
    
    // Getters for other classes
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public ReportManager getReportManager() {
        return reportManager;
    }
    
    public BaseTracker getBaseTracker() {
        return baseTracker;
    }
}
