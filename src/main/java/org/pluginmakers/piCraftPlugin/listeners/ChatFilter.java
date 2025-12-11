package org.pluginmakers.piCraftPlugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.utils.ColorUtil;

import java.util.List;

public class ChatFilter implements Listener {
    private final PiCraftPlugin plugin;
    
    public ChatFilter(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!plugin.getConfig().getBoolean("chat_filter.enabled", true)) {
            return;
        }
        
        Player player = event.getPlayer();
        String message = event.getMessage().toLowerCase();
        
        List<String> blacklistedWords = plugin.getConfig().getStringList("chat_filter.blacklisted_words");
        
        if (blacklistedWords == null || blacklistedWords.isEmpty()) {
            return;
        }
        
        for (String word : blacklistedWords) {
            if (word != null && !word.trim().isEmpty() && message.contains(word.toLowerCase().trim())) {
                event.setCancelled(true);
                String filterMessage = plugin.getConfig().getString("chat_filter.message", "&cThe word in your message is blacklisted.");
                player.sendMessage(ColorUtil.colorize(filterMessage));
                return;
            }
        }
    }
}