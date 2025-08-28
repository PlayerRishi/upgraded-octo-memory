package org.pluginmakers.piCraftPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.model.Report;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReportManager {
    private final PiCraftPlugin plugin;
    private final Map<UUID, Boolean> notificationSettings = new HashMap<>();
    
    public ReportManager(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void notifyNewReport(Report report) {
        // Notify online staff
        notifyOnlineStaff(report);
        
        // Send to Discord if enabled
        if (plugin.getConfigManager().isDiscordEnabled()) {
            sendDiscordNotification(report);
        }
    }
    
    private void notifyOnlineStaff(Report report) {
        String message = ChatColor.translateAlternateColorCodes('&', 
            plugin.getConfigManager().getPrefix() + 
            "&eNew report #" + report.getId() + " from " + 
            report.getDisplayReporter(false) + ". Use &6/reports&e to view.");
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("picraft.report.view") && 
                isNotificationEnabled(player.getUniqueId())) {
                player.sendMessage(message);
            }
        }
    }
    
    private void sendDiscordNotification(Report report) {
        if (!Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) {
            return;
        }
        
        try {
            // Use reflection to avoid hard dependency on DiscordSRV
            Class<?> discordSRVClass = Class.forName("github.scarsz.discordsrv.DiscordSRV");
            Object discordSRV = discordSRVClass.getMethod("getPlugin").invoke(null);
            Object jda = discordSRVClass.getMethod("getJda").invoke(discordSRV);
            
            if (jda != null) {
                String channelId = plugin.getConfigManager().getDiscordChannelId();
                if (!channelId.isEmpty()) {
                    String message = formatDiscordMessage(report);
                    
                    // Send message to Discord channel
                    Class<?> jdaClass = jda.getClass();
                    Object textChannel = jdaClass.getMethod("getTextChannelById", String.class)
                        .invoke(jda, channelId);
                    
                    if (textChannel != null) {
                        textChannel.getClass().getMethod("sendMessage", String.class)
                            .invoke(textChannel, message);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send Discord notification: " + e.getMessage());
        }
    }
    
    private String formatDiscordMessage(Report report) {
        String template = plugin.getConfigManager().getDiscordMessage();
        
        return template
            .replace("{id}", String.valueOf(report.getId()))
            .replace("{player}", report.getReporterName())
            .replace("{displayReporter}", report.getDisplayReporter(false))
            .replace("{category}", report.getCategory() != null ? report.getCategory() : "None")
            .replace("{message}", report.getMessage())
            .replace("{world}", report.getWorld())
            .replace("{x}", String.valueOf(report.getX()))
            .replace("{y}", String.valueOf(report.getY()))
            .replace("{z}", String.valueOf(report.getZ()))
            .replace("{time}", report.getCreatedAt().toString());
    }
    
    public void notifyStaffJoin(Player player) {
        if (!plugin.getConfigManager().isStaffJoinNotificationEnabled()) {
            return;
        }
        
        if (!player.hasPermission("picraft.report.view")) {
            return;
        }
        
        if (!isNotificationEnabled(player.getUniqueId())) {
            return;
        }
        
        try {
            int unreadCount = plugin.getDatabaseManager().getUnreadCount();
            if (unreadCount > 0) {
                String message = plugin.getConfigManager().getMessage("staff_notify")
                    .replace("{count}", String.valueOf(unreadCount));
                
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfigManager().getPrefix() + message));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to notify staff on join: " + e.getMessage());
        }
    }
    
    public boolean isNotificationEnabled(UUID playerId) {
        return notificationSettings.getOrDefault(playerId, true);
    }
    
    public void setNotificationEnabled(UUID playerId, boolean enabled) {
        notificationSettings.put(playerId, enabled);
    }
}