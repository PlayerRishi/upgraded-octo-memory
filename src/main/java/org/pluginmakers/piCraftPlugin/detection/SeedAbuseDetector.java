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
        // Simplified structure detection - check for common structure blocks
        int radius = 10;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Material block = player.getLocation().add(x, 0, z).getBlock().getType();
                if (block == Material.MOSSY_COBBLESTONE || block == Material.SPAWNER || 
                    block == Material.END_PORTAL_FRAME || block == Material.NETHER_BRICKS) {
                    return true;
                }
            }
        }
        return false;
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