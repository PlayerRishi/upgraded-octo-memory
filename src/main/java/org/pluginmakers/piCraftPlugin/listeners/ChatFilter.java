package org.pluginmakers.piCraftPlugin.listeners;

import org.bukkit.BanList;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.utils.ColorUtil;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ChatFilter implements Listener {
    private final PiCraftPlugin plugin;
    
    public ChatFilter(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is muted
        try {
            if (plugin.getDatabaseManager().isPlayerMuted(player.getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage(ColorUtil.colorize("&cYou are currently muted and cannot chat."));
                return;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to check mute status: " + e.getMessage());
        }
        
        String message = event.getMessage().toLowerCase();
        
        List<String> firstOffenseWords = plugin.getConfigManager().getConfig().getStringList("chat_filter.first_offense_words");
        List<String> secondOffenseWords = plugin.getConfigManager().getConfig().getStringList("chat_filter.second_offense_words");
        List<String> thirdOffenseWords = plugin.getConfigManager().getConfig().getStringList("chat_filter.third_offense_words");
        
        // Check first offense words (most severe)
        for (String word : firstOffenseWords) {
            if (message.contains(word.toLowerCase())) {
                event.setCancelled(true);
                handleViolation(player, 1);
                return;
            }
        }
        
        // Check second offense words
        for (String word : secondOffenseWords) {
            if (message.contains(word.toLowerCase())) {
                event.setCancelled(true);
                handleViolation(player, 2);
                return;
            }
        }
        
        // Check third offense words (least severe)
        for (String word : thirdOffenseWords) {
            if (message.contains(word.toLowerCase())) {
                event.setCancelled(true);
                handleViolation(player, 3);
                return;
            }
        }
    }
    
    private void handleViolation(Player player, int severity) {
        try {
            int violations = plugin.getDatabaseManager().getChatViolations(player.getUniqueId());
            plugin.getDatabaseManager().incrementChatViolations(player.getUniqueId());
            violations++;
            
            String firstMsg = plugin.getConfigManager().getConfig().getString("chat_filter.messages.first_offense", "&cBanned for inappropriate language.");
            String secondMsg = plugin.getConfigManager().getConfig().getString("chat_filter.messages.second_offense", "&cKicked for inappropriate language.");
            String thirdMsg = plugin.getConfigManager().getConfig().getString("chat_filter.messages.third_offense", "&cKicked for inappropriate language.");
            
            if (severity == 1 || violations == 1) {
                // First offense words OR first violation: 3 hour ban
                Date expiry = new Date(System.currentTimeMillis() + (3 * 60 * 60 * 1000));
                plugin.getServer().getBanList(BanList.Type.NAME).addBan(player.getName(), 
                    "Inappropriate language", expiry, "PiCraft AutoMod");
                player.kickPlayer(ColorUtil.colorizeToString(firstMsg));
            } else if (severity == 2 || violations == 2) {
                // Second offense words OR second violation: 10 minute chat mute
                long muteUntil = System.currentTimeMillis() + (10 * 60 * 1000);
                plugin.getDatabaseManager().mutePlayer(player.getUniqueId(), muteUntil);
                player.sendMessage(ColorUtil.colorize(secondMsg));
            } else {
                // Third offense words OR third+ violation: kick
                player.kickPlayer(ColorUtil.colorizeToString(thirdMsg));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to track chat violation: " + e.getMessage());
        }
    }
}