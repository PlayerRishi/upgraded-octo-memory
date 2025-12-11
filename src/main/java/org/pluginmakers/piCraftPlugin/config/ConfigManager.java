package org.pluginmakers.piCraftPlugin.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
    
    // Storage settings
    public String getStorageType() {
        return config.getString("storage.type", "sqlite");
    }
    
    // Cooldown settings
    public int getReportCooldown() {
        return config.getInt("cooldowns.report", 30);
    }
    
    // Notification settings
    public boolean isStaffJoinNotificationEnabled() {
        return config.getBoolean("notifications.on_staff_join", true);
    }
    
    public boolean isUnreadCountEnabled() {
        return config.getBoolean("notifications.unread_count", true);
    }
    
    // Discord settings
    public boolean isDiscordEnabled() {
        return config.getBoolean("discord.enabled", false);
    }
    
    public String getDiscordChannelId() {
        return config.getString("discord.channel_id", "");
    }
    
    public String getDiscordRoleId() {
        return config.getString("discord.ping_role_id", "");
    }
    
    public String getDiscordMessage() {
        return config.getString("discord.message", 
            "**New report #{id}** by {displayReporter} [{category}] at {world} {x} {y} {z}\\n{message}");
    }
    
    // Rules settings
    public String getRulesFile() {
        return config.getString("rules.file", "rules.txt");
    }
    
    public boolean isPaginationEnabled() {
        return config.getBoolean("rules.pagination.enabled", false);
    }
    
    public int getLinesPerPage() {
        return config.getInt("rules.pagination.lines_per_page", 12);
    }
    
    // Report categories
    public java.util.List<String> getReportCategories() {
        return config.getStringList("reports.categories");
    }
    
    // Base tracking settings
    public boolean isBaseTrackingEnabled() {
        return config.getBoolean("reports.base_tracking.enabled", true);
    }
    
    public int getRadiusLimit() {
        return config.getInt("reports.base_tracking.radius_limit", 2500);
    }
    
    public boolean isCoordinateLoggingEnabled() {
        return config.getBoolean("reports.base_tracking.log_coordinates", true);
    }
    
    // Messages
    public String getPrefix() {
        return config.getString("messages.prefix", "&6[PiCraft]&r ");
    }
    
    public String getMessage(String key) {
        return config.getString("messages." + key, "Message not found: " + key);
    }
    
    public org.bukkit.configuration.file.FileConfiguration getConfig() {
        return config;
    }
    
    public boolean isWebDashboardEnabled() {
        return config.getBoolean("web_dashboard.enabled", false);
    }
    
    public int getWebDashboardPort() {
        return config.getInt("web_dashboard.port", 8080);
    }
    
    public String getWebDashboardHost() {
        return config.getString("web_dashboard.host", "localhost");
    }
    
    public String getWebDashboardAuthKey() {
        return config.getString("web_dashboard.auth_key", "change-me");
    }
    
    // Mod check settings
    public boolean isModCheckEnabled() {
        return config.getBoolean("mod_check.enabled", true);
    }
    
    public void setModCheckEnabled(boolean enabled) {
        config.set("mod_check.enabled", enabled);
        plugin.saveConfig();
    }
}