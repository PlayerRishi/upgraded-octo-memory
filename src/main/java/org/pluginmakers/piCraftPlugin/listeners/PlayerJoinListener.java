package org.pluginmakers.piCraftPlugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;

public class PlayerJoinListener implements Listener {
    private final PiCraftPlugin plugin;
    
    public PlayerJoinListener(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Delay the notification slightly to ensure the player is fully loaded
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getReportManager().notifyStaffJoin(event.getPlayer());
        }, 20L); // 1 second delay
    }
}