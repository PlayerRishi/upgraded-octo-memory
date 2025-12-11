package org.pluginmakers.piCraftPlugin.detection;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;

import java.util.List;

public class OreTracker implements Listener {
    private final PiCraftPlugin plugin;
    
    public OreTracker(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        if (!plugin.getConfig().getBoolean("ore_tracking.enabled", true)) {
            return;
        }
        
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();
        
        List<String> trackedOres = plugin.getConfig().getStringList("ore_tracking.track_ores");
        if (!trackedOres.contains(blockType.toString())) {
            return;
        }
        
        // Log ore mining to database
        plugin.getDatabaseManager().logOreMining(
            player.getUniqueId(),
            player.getName(),
            blockType.toString(),
            event.getBlock().getWorld().getName(),
            event.getBlock().getX(),
            event.getBlock().getY(),
            event.getBlock().getZ(),
            System.currentTimeMillis()
        );
    }
}