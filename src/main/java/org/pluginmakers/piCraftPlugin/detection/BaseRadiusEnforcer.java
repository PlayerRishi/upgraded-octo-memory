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
        
        // Only check player-placed blocks, not naturally generated ones
        if (event.isCancelled()) {
            return;
        }
        
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();
        
        // Only monitor player-placed base blocks outside radius
        if (isBaseBlock(event.getBlock().getType()) && isOutsideRadius(location)) {
            createRadiusViolationReport(player, location);
        }
    }
    
    private boolean isBaseBlock(@NotNull Material material) {
        // Storage blocks
        if (material == Material.CHEST || material == Material.BARREL || 
            material == Material.ENDER_CHEST || material == Material.SHULKER_BOX) {
            return true;
        }
        
        // Utility blocks
        if (material == Material.FURNACE || material == Material.BLAST_FURNACE ||
            material == Material.SMOKER || material == Material.BREWING_STAND ||
            material == Material.CRAFTING_TABLE || material == Material.ANVIL ||
            material == Material.CHIPPED_ANVIL || material == Material.DAMAGED_ANVIL ||
            material == Material.ENCHANTING_TABLE || material == Material.BOOKSHELF) {
            return true;
        }
        
        // Lighting
        if (material == Material.LANTERN || material == Material.SOUL_LANTERN) {
            return true;
        }
        
        // Beds (all colors except yellow and orange)
        if (material == Material.WHITE_BED || material == Material.LIGHT_GRAY_BED ||
            material == Material.GRAY_BED || material == Material.BLACK_BED ||
            material == Material.BROWN_BED || material == Material.RED_BED ||
            material == Material.PINK_BED || material == Material.MAGENTA_BED ||
            material == Material.PURPLE_BED || material == Material.BLUE_BED ||
            material == Material.LIGHT_BLUE_BED || material == Material.CYAN_BED ||
            material == Material.GREEN_BED || material == Material.LIME_BED) {
            return true;
        }
        
        return false;
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