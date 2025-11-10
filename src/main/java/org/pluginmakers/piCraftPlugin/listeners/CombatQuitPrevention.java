package org.pluginmakers.piCraftPlugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;

public class CombatQuitPrevention implements Listener {
    private final PiCraftPlugin plugin;
    
    public CombatQuitPrevention(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getCombatTagManager().isInCombat(player)) {
            // Set custom quit message for combat loggers
            event.setQuitMessage("§c" + player.getName() + " combat logged!");
            
            // Kill the player (they lose their items)
            player.setHealth(0);
            
            plugin.getLogger().info("Player " + player.getName() + " combat logged and was killed");
        }
    }
}