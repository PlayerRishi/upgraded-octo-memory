package org.pluginmakers.piCraftPlugin.detection;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.model.Report;

import java.util.UUID;

public class BaseRadiusEnforcer implements Listener {
    private final PiCraftPlugin plugin;
    
    public BaseRadiusEnforcer(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockPlace(@NotNull BlockPlaceEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("reports.base_tracking.enforce_radius", true)) {
            return;
        }
        
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();
        
        if (isBaseBlock(event.getBlock().getType()) && isOutsideRadius(location)) {
            createRadiusViolationReport(player, location);
        }
    }
    
    private boolean isBaseBlock(@NotNull Material material) {
        return material == Material.CHEST || material == Material.FURNACE || 
               material == Material.CRAFTING_TABLE || material == Material.BED;
    }
    
    private boolean isOutsideRadius(@NotNull Location location) {
        Location spawn = location.getWorld().getSpawnLocation();
        double distance = location.distance(spawn);
        int limit = plugin.getConfigManager().getConfig().getInt("reports.base_tracking.radius_limit", 2500);
        return distance > limit;
    }
    
    private void createRadiusViolationReport(@NotNull Player player, @NotNull Location location) {
        try {
            double distance = location.distance(location.getWorld().getSpawnLocation());
            
            Report report = new Report(
                UUID.fromString("00000000-0000-0000-0000-000000000000"),
                "SYSTEM",
                false,
                "other",
                String.format("Player %s building outside radius: %.0f blocks from spawn (limit: %d)", 
                    player.getName(), distance, plugin.getConfigManager().getConfig().getInt("reports.base_tracking.radius_limit", 2500)),
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
            );
            
            report.setAutoDetected(true);
            
            int reportId = plugin.getDatabaseManager().createReport(report);
            plugin.getLogger().info("Base radius violation: " + player.getName() + " at " + distance + " blocks (Report #" + reportId + ")");
            
            plugin.getReportManager().notifyNewReport(report);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create radius violation report: " + e.getMessage());
        }
    }
}