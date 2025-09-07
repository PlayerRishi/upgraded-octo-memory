package org.pluginmakers.piCraftPlugin.detection;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.model.Report;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReplayModDetector implements Listener {
    private final PiCraftPlugin plugin;
    private final Map<UUID, ChunkReloadTracker> reloadTrackers = new HashMap<>();
    
    public ReplayModDetector(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerCommand(@NotNull PlayerCommandPreprocessEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("reports.auto_detection.replay_mod.enabled", true)) {
            return;
        }
        
        String command = event.getMessage().toLowerCase();
        
        // Detect F3+A chunk reload attempts (players might try /reload or similar)
        if (command.contains("reload") || command.contains("refresh")) {
            Player player = event.getPlayer();
            ChunkReloadTracker tracker = getTracker(player.getUniqueId());
            
            tracker.addReload();
            
            int limit = plugin.getConfigManager().getConfig().getInt("reports.auto_detection.replay_mod.chunk_reload_limit", 5);
            if (tracker.getReloadsThisMinute() > limit) {
                createReplayModReport(player, "Excessive chunk reload attempts: " + tracker.getReloadsThisMinute() + " in 1 minute");
            }
        }
    }
    
    private ChunkReloadTracker getTracker(UUID playerId) {
        return reloadTrackers.computeIfAbsent(playerId, k -> new ChunkReloadTracker());
    }
    
    private void createReplayModReport(@NotNull Player player, @NotNull String reason) {
        try {
            Report report = new Report(
                UUID.fromString("00000000-0000-0000-0000-000000000000"),
                "SYSTEM",
                false,
                "exploit",
                String.format("Suspected replay mod usage by %s: %s", player.getName(), reason),
                player.getWorld().getName(),
                player.getLocation().getBlockX(),
                player.getLocation().getBlockY(),
                player.getLocation().getBlockZ()
            );
            
            report.setAutoDetected(true);
            
            int reportId = plugin.getDatabaseManager().createReport(report);
            plugin.getLogger().info("Auto-detected replay mod usage: " + player.getName() + " (Report #" + reportId + ")");
            
            plugin.getReportManager().notifyNewReport(report);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create replay mod report: " + e.getMessage());
        }
    }
    
    private static class ChunkReloadTracker {
        private long lastMinuteReset = System.currentTimeMillis();
        private int reloadsThisMinute = 0;
        
        public void addReload() {
            checkMinuteReset();
            reloadsThisMinute++;
        }
        
        private void checkMinuteReset() {
            long now = System.currentTimeMillis();
            if (now - lastMinuteReset > 60000) { // 1 minute
                reloadsThisMinute = 0;
                lastMinuteReset = now;
            }
        }
        
        public int getReloadsThisMinute() {
            checkMinuteReset();
            return reloadsThisMinute;
        }
    }
}