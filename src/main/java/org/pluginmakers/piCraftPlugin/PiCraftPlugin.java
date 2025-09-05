package org.pluginmakers.piCraftPlugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.pluginmakers.piCraftPlugin.commands.EvidenceCommand;
import org.pluginmakers.piCraftPlugin.commands.ReportCommand;
import org.pluginmakers.piCraftPlugin.commands.ReportTabCompleter;
import org.pluginmakers.piCraftPlugin.commands.RulesCommand;
import org.pluginmakers.piCraftPlugin.commands.StaffCommands;
import org.pluginmakers.piCraftPlugin.config.ConfigManager;
import org.pluginmakers.piCraftPlugin.database.DatabaseManager;
import org.pluginmakers.piCraftPlugin.detection.BaseRadiusEnforcer;
import org.pluginmakers.piCraftPlugin.detection.CombatLogDetector;
import org.pluginmakers.piCraftPlugin.detection.SeedAbuseDetector;
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
        if (configManager.getConfig().getBoolean("reports.auto_detection.combat_logging.enabled", true)) {
            getServer().getPluginManager().registerEvents(new CombatLogDetector(this), this);
        }
        if (configManager.getConfig().getBoolean("reports.auto_detection.seed_abuse.enabled", true)) {
            getServer().getPluginManager().registerEvents(new SeedAbuseDetector(this), this);
        }
        if (configManager.getConfig().getBoolean("reports.base_tracking.enforce_radius", true)) {
            getServer().getPluginManager().registerEvents(new BaseRadiusEnforcer(this), this);
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
        getCommand("report").setExecutor(new ReportCommand(this));
        getCommand("report").setTabCompleter(new ReportTabCompleter(this));
        getCommand("rules").setExecutor(new RulesCommand(this));
        
        // Staff commands
        StaffCommands staffCommands = new StaffCommands(this);
        getCommand("reports").setExecutor(staffCommands);
        getCommand("reportlist").setExecutor(staffCommands);
        getCommand("reportview").setExecutor(staffCommands);
        getCommand("reportread").setExecutor(staffCommands);
        getCommand("reportclear").setExecutor(staffCommands);
        getCommand("reportassign").setExecutor(staffCommands);
        getCommand("reportnotify").setExecutor(staffCommands);
        getCommand("reporttp").setExecutor(staffCommands);
        getCommand("evidence").setExecutor(new EvidenceCommand(this));
    }
    
    private void createDefaultRulesFile() {
        File rulesFile = new File(getDataFolder(), configManager.getRulesFile());
        
        if (!rulesFile.exists()) {
            try {
                if (!getDataFolder().exists()) {
                    getDataFolder().mkdirs();
                }
                
                String defaultRules = "# Rules of the Server:\n\n" +
                    "## Hard Enforced Rules:\n" +
                    "### - No exploiting\n" +
                    "### - No Duping\n" +
                    "### - No Xray or Hacking\n" +
                    "### - Be respectful and Family Friendly\n" +
                    "### - Have fun!\n" +
                    "- Breaking these will result in a 3 day ban\n" +
                    "- If anyone finds dupe stashes IMMEDIATELY REPORT to staff or any of the server mods!\n\n" +
                    "## Soft Enforced Rules:\n" +
                    "### - No Combat Logging\n" +
                    "### - This includes randomly PvP-ing at spawn\n" +
                    "### - Replay Mod/Flashback may not be used during gameplay or events to find traps and etc.\n" +
                    "### - These mods may only be used after logging off after a session, and they may not be used for base hunting\n" +
                    "### - No F3+A to reload chunks\n" +
                    "### - All bases need to be built within a 2500 block radius of Spawn\n" +
                    "### - No Stream Sniping\n" +
                    "### - No crazy griefing (like destroying whole bases, taking a block or two or leaving a message is fine)\n" +
                    "### - No crazy stealing (Like taking a lot of important things at once)\n" +
                    "### - No Dragon Egg inside of Ender Chest\n" +
                    "### - DO NOT abuse the seed to get an unfair advantage!\n" +
                    "### - No Toxicity\n\n" +
                    "## Spawn Rules:\n" +
                    "### - No crazy insane battles right at spawn.\n" +
                    "### - No griefing at all\n\n" +
                    "## Farm Rules:\n" +
                    "### - No auto AFK raid farms\n" +
                    "### - No auto AFK wither-skeleton farms\n" +
                    "### - No auto AFK wither/obsidian farms\n\n" +
                    "## Item Rules:\n" +
                    "### - No Elytra (During Combat)\n" +
                    "### - No End Crystals (During Combat)\n" +
                    "### - No Respawn Anchors (During Combat)\n" +
                    "### - No Beds (During Combat)\n" +
                    "### - No Restocking of items while in combat with use of Shulkers/Ender-Chests/Chests\n" +
                    "### - No weakness potions or arrows\n\n" +
                    "## Remember to always be friendly and HAVE FUN!";
                
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
