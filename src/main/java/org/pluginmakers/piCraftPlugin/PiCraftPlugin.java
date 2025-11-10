package org.pluginmakers.piCraftPlugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.pluginmakers.piCraftPlugin.commands.EvidenceCommand;
import org.pluginmakers.piCraftPlugin.commands.HomeCommand;
import org.pluginmakers.piCraftPlugin.commands.NoMansCommand;
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
import org.pluginmakers.piCraftPlugin.listeners.ChatFilter;
import org.pluginmakers.piCraftPlugin.listeners.CombatQuitPrevention;
import org.pluginmakers.piCraftPlugin.listeners.PlayerJoinListener;
import org.pluginmakers.piCraftPlugin.managers.BaseTracker;
import org.pluginmakers.piCraftPlugin.managers.CombatTagManager;
import org.pluginmakers.piCraftPlugin.managers.NoMansLandManager;
import org.pluginmakers.piCraftPlugin.managers.ReportManager;
import org.pluginmakers.piCraftPlugin.recipes.NametagRecipe;
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
    private CombatTagManager combatTagManager;
    private NoMansLandManager noMansLandManager;
    
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
        combatTagManager = new CombatTagManager(this);
        noMansLandManager = new NoMansLandManager(this);
        
        // Register commands
        registerCommands();
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(combatTagManager, this);
        getServer().getPluginManager().registerEvents(noMansLandManager, this);
        getServer().getPluginManager().registerEvents(new CombatQuitPrevention(this), this);
        
        // Register chat filter
        if (configManager.getConfig().getBoolean("chat_filter.enabled", true)) {
            getServer().getPluginManager().registerEvents(new ChatFilter(this), this);
        }
        
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
            if (configManager.getConfig().getBoolean("reports.auto_detection.villager_kills.enabled", true)) {
                getServer().getPluginManager().registerEvents(new VillagerKillDetector(this), this);
            }
        }
        
        // Create default rules file if it doesn't exist
        createDefaultRulesFile();
        
        // Start web dashboard
        webDashboard.start();
        
        // Register custom recipes
        NametagRecipe.registerRecipe(this);
        
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
        if (getCommand("nomans") != null) getCommand("nomans").setExecutor(new NoMansCommand(this));
        
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
                        🪓・SERVER RULES — PiCraft
                        Welcome to the  PiCraft SMP — a chill server.
                        
                        🌎・CORE RULES
                        Break = Punishment.
                        🚫 No cheating, duping, or exploiting.
                         No Xray, hacking clients, or bug abuse. Play legit or don’t play at all.
                        🔒 No griefing or stealing — unless there’s a really, REALLY valid reason.
                         (“They looked at my cow funny” is not valid, btw.)
                        🧱 Pranks are fine — just not destructive or rage-inducing.
                        💬 Be respectful and family-friendly.
                        🎉 Play fair, have fun, don’t ruin it for others.
                        🧾 Breaking these = 3-day ban minimum.
                         If you find a dupe/exploit → report to <@PlayerRishi>, <@NorthPlace>, or a mod.
                        
                        😜・PRANKS & STEALING
                        Pranks are allowed, but for the laughs — not arguments.
                        🎁 Stealing is only allowed for harmless, funny pranks.
                         You must return or replace all items within 24 hours.
                        ⛔ No malicious or revenge stealing.
                         If it feels like theft, it is theft. (no shi sherlock)
                        🤝 Respect boundaries. If someone says “stop,” you stop.
                         🐣 No pranking new players (under 3 days).
                         🏗️ No pranks in public areas (Spawn, Hub, End) unless approved.
                        ⚖️ Malicious stealing = normal theft = punishment.
                        
                        ⚔️・PVP & PRANK BATTLES
                        PvP = fun, don’t be super sweaty or toxic.
                        ⚔️ Must be mutual (as in the other person needs to agree) unless it’s clearly a prank.
                        😂 Prank kills are okay — but give their stuff back. plz.
                        💣 Fight Limits:
                        Max 8 TNT Crystals/Anchors
                        
                        
                        Max 3 Totems
                        
                        
                        ❌ No weakness or harming!
                        
                        
                        🧪 Potions only if both players agree
                        
                        
                        💰 Fair kills: take some loot, not everything.
                        🧍 Don’t target new or undergeared players.
                        
                        🏗️・BUILDS & COMMUNITY ZONES
                        🚫 No griefing builds. Ever.
                        🏙️ Protected Zones: Spawn, Nether Hub, End Island.
                        ❌ No explosions, PvP, or major pranks here.
                        🚧 No bases within 160 blocks of spawn.
                        🧹 Keep community areas clean — no creeper craters or mess.
                        
                        🌾・FARMS & AUTOMATION
                        🔌 Turn off redstone/farms when logging out.
                        💀 No lag machines or infinite loops.
                        🐄 Clear mobs before leaving farms.
                        🧠 Follow admin/mod instructions if something crashes or lags the server.
                        🌾Don’t use other people’s farms without their consent
                        😴Any farms are fine, but just no afk farms.
                        
                        🥚・DRAGON EGG RULES
                        📦 Don’t put it in an ender chest.
                        ✅ If lost by accident = replaceable. (through events, quests, etc.)
                        ❌ If lost on purpose = gone forever. (unless an admin approves another one being added)
                        ⚔️ Don’t bring it to fights or pranks. (unless you wanna lose it…)
                        
                        📹・REPLAY MOD RULES
                        🎥 Replay Mod SHOULD ONLY BE A CINEMATIC TOOL.
                        ✅ Allowed for:
                        Base tours
                        
                        
                        Time-lapses
                        
                        
                        Cinematic builds & edits
                        
                        
                        🚫 NOT allowed for:
                        Base hunting
                        
                        
                        Structure finding
                        
                        
                        Snooping on players
                        
                        
                        Basically: make youtube vids, don’t create mischief.
                        
                        💬・COMMUNITY CONDUCT
                        ❤️ Be kind, goofy, and cooperative.
                         🤪 Funny kills/pranks are fine if everyone laughs.
                         🐕 No pet or villager killing.
                         📜 Don’t mess with books or signs.
                         🧱 If a prank goes wrong — fix it. Don’t double down.
                        
                        🧑‍💼・ADMINS & MODS
                        🛠️ Admins don’t use Creative Mode or unfair commands.
                        🚀 Teleports only for moderation or mutual consent.
                        📸 Bans need proof (screenshots or clips).
                        ⚖️ Punishments must be fair and transparent.
                        
                        🧼・CHAT RULES
                        💚 Keep chat chill and friendly.
                        🚫 No spam, ragebait, or harassment.
                        🤡 Be funny, not toxic.
                        😅 Drama → take it private, not public.
                        
                        💀・No Man’s Land
                        🫣 Dont hide there 24/7
                        ⌨️ No hacking
                        🤡 Be funny, not toxic.
                        
                        🌟・FINAL RULE
                        If it makes the server less fun, don’t do it.
                        If it’ll make everyone laugh, go for it — but clean up afterwards.
                        And don’t create a toxic environment for anyone.
                        
                        TL;DR (Short Version)
                        🚫 No griefing, dupes, or stealing (unless it’s harmless & funny).
                        🤝 Respect players + builds.
                        🎥 Replay Mod = cinematics only.
                        ⚔️ PvP = fair and mutual.
                        🧱 No base hunting or destruction.
                        ❤️ Be funny, kind, and chill.
                        💀 If it ruins fun → don’t. If it’s hilarious → yes.
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
    
    public CombatTagManager getCombatTagManager() {
        return combatTagManager;
    }
    
    public NoMansLandManager getNoMansLandManager() {
        return noMansLandManager;
    }
}
