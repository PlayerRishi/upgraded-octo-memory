package org.pluginmakers.piCraftPlugin.detection;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.model.Report;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SeedAbuseDetector implements Listener {
    private final PiCraftPlugin plugin;
    private final Map<UUID, PlayerStats> playerStats = new HashMap<>();
    
    public SeedAbuseDetector(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("reports.auto_detection.seed_abuse.enabled", true)) {
            return;
        }
        
        Player player = event.getPlayer();
        Material block = event.getBlock().getType();
        
        if (block == Material.DIAMOND_ORE || block == Material.DEEPSLATE_DIAMOND_ORE) {
            PlayerStats stats = getPlayerStats(player.getUniqueId());
            stats.incrementDiamonds();
            
            int limit = plugin.getConfigManager().getConfig().getInt("reports.auto_detection.seed_abuse.diamond_limit", 64);
            if (stats.getDiamondsThisHour() > limit) {
                createSeedAbuseReport(player, "Excessive diamond mining: " + stats.getDiamondsThisHour() + " diamonds in 1 hour");
            }
        }
    }
    
    @EventHandler
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("reports.auto_detection.seed_abuse.enabled", true)) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // Check for nearby structures (simplified detection)
        if (isNearStructure(player)) {
            PlayerStats stats = getPlayerStats(player.getUniqueId());
            if (stats.shouldCheckStructure()) {
                stats.incrementStructures();
                
                int limit = plugin.getConfigManager().getConfig().getInt("reports.auto_detection.seed_abuse.structure_limit", 3);
                if (stats.getStructuresThisHour() > limit) {
                    createSeedAbuseReport(player, "Suspicious structure finding: " + stats.getStructuresThisHour() + " structures in 1 hour");
                }
            }
        }
    }
    
    private boolean isNearStructure(@NotNull Player player) {
        // Only detect naturally generated structure blocks
        int radius = 10;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                org.bukkit.block.Block block = player.getLocation().add(x, 0, z).getBlock();
                Material material = block.getType();
                
                // Check for structure blocks that are naturally generated
                if ((material == Material.MOSSY_COBBLESTONE || material == Material.SPAWNER || 
                     material == Material.END_PORTAL_FRAME || material == Material.NETHER_BRICKS ||
                     material == Material.GILDED_BLACKSTONE) && isNaturallyGenerated(block)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean isNaturallyGenerated(@NotNull org.bukkit.block.Block block) {
        // Check if block is in a naturally generated structure
        // Spawners and End Portal Frames can only be naturally generated
        if (block.getType() == Material.SPAWNER || block.getType() == Material.END_PORTAL_FRAME) {
            return true;
        }
        
        // For other blocks, check if they're part of a structure pattern
        // (This is a simplified check - in a real implementation you'd want more sophisticated detection)
        return hasStructurePattern(block);
    }
    
    private boolean hasStructurePattern(@NotNull org.bukkit.block.Block block) {
        // Simple pattern detection - check for multiple structure blocks nearby
        int structureBlocks = 0;
        int checkRadius = 3;
        
        for (int x = -checkRadius; x <= checkRadius; x++) {
            for (int y = -checkRadius; y <= checkRadius; y++) {
                for (int z = -checkRadius; z <= checkRadius; z++) {
                    Material nearby = block.getRelative(x, y, z).getType();
                    if (nearby == Material.MOSSY_COBBLESTONE || nearby == Material.NETHER_BRICKS ||
                        nearby == Material.GILDED_BLACKSTONE || nearby == Material.BLACKSTONE) {
                        structureBlocks++;
                    }
                }
            }
        }
        
        // If there are multiple structure blocks nearby, likely natural generation
        return structureBlocks >= 5;
    }
    
    private PlayerStats getPlayerStats(UUID playerId) {
        return playerStats.computeIfAbsent(playerId, k -> new PlayerStats());
    }
    
    private void createSeedAbuseReport(@NotNull Player player, @NotNull String reason) {
        try {
            Report report = new Report(
                UUID.fromString("00000000-0000-0000-0000-000000000000"),
                "SYSTEM",
                false,
                "exploit",
                String.format("Suspected seed abuse by %s: %s", player.getName(), reason),
                player.getWorld().getName(),
                player.getLocation().getBlockX(),
                player.getLocation().getBlockY(),
                player.getLocation().getBlockZ()
            );
            
            report.setAutoDetected(true);
            
            int reportId = plugin.getDatabaseManager().createReport(report);
            plugin.getLogger().info("Auto-detected seed abuse: " + player.getName() + " (Report #" + reportId + ")");
            
            plugin.getReportManager().notifyNewReport(report);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create seed abuse report: " + e.getMessage());
        }
    }
    
    private static class PlayerStats {
        private long lastHourReset = System.currentTimeMillis();
        private int diamondsThisHour = 0;
        private int structuresThisHour = 0;
        private long lastStructureCheck = 0;
        
        public void incrementDiamonds() {
            checkHourReset();
            diamondsThisHour++;
        }
        
        public void incrementStructures() {
            checkHourReset();
            structuresThisHour++;
            lastStructureCheck = System.currentTimeMillis();
        }
        
        public boolean shouldCheckStructure() {
            return System.currentTimeMillis() - lastStructureCheck > 300000; // 5 minutes
        }
        
        private void checkHourReset() {
            long now = System.currentTimeMillis();
            if (now - lastHourReset > 3600000) { // 1 hour
                diamondsThisHour = 0;
                structuresThisHour = 0;
                lastHourReset = now;
            }
        }
        
        public int getDiamondsThisHour() {
            checkHourReset();
            return diamondsThisHour;
        }
        
        public int getStructuresThisHour() {
            checkHourReset();
            return structuresThisHour;
        }
    }
}