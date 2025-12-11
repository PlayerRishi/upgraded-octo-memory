package org.pluginmakers.piCraftPlugin.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;

public class GhastFireballPrevention implements Listener {
    private final PiCraftPlugin plugin;
    
    public GhastFireballPrevention(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() == EntityType.GHAST) {
            // Cancel the interaction to prevent triggering fireball
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntityType() == EntityType.FIREBALL && 
            event.getEntity().getShooter() instanceof Ghast) {
            
            Ghast ghast = (Ghast) event.getEntity().getShooter();
            
            // Check if a player recently interacted with this ghast
            // Cancel if it seems like player-triggered fireball
            if (ghast.getTarget() == null) {
                event.setCancelled(true);
            }
        }
    }
}