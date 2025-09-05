package org.pluginmakers.piCraftPlugin.detection;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.model.Report;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatLogDetector implements Listener {
    private final PiCraftPlugin plugin;
    private final Map<UUID, Long> combatTimes = new HashMap<>();
    
    public CombatLogDetector(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerDamage(@NotNull EntityDamageByEntityEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("reports.auto_detection.combat_logging.enabled", true)) {
            return;
        }
        
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player attacker = (Player) event.getDamager();
            
            long currentTime = System.currentTimeMillis();
            combatTimes.put(victim.getUniqueId(), currentTime);
            combatTimes.put(attacker.getUniqueId(), currentTime);
        }
    }
    
    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("reports.auto_detection.combat_logging.enabled", true)) {
            return;
        }
        
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        if (combatTimes.containsKey(playerId)) {
            long combatTime = combatTimes.get(playerId);
            long timeSinceCombat = (System.currentTimeMillis() - combatTime) / 1000;
            int combatDuration = plugin.getConfigManager().getConfig().getInt("reports.auto_detection.combat_logging.combat_time", 15);
            
            if (timeSinceCombat < combatDuration) {
                if (plugin.getConfigManager().getConfig().getBoolean("reports.auto_detection.combat_logging.auto_report", true)) {
                    createCombatLogReport(player, timeSinceCombat);
                }
            }
            
            combatTimes.remove(playerId);
        }
    }
    
    private void createCombatLogReport(@NotNull Player player, long timeSinceCombat) {
        try {
            Report report = new Report(
                UUID.fromString("00000000-0000-0000-0000-000000000000"),
                "SYSTEM",
                false,
                "combat",
                String.format("Player %s combat logged after %d seconds in combat", 
                    player.getName(), timeSinceCombat),
                player.getWorld().getName(),
                player.getLocation().getBlockX(),
                player.getLocation().getBlockY(),
                player.getLocation().getBlockZ()
            );
            
            report.setAutoDetected(true);
            
            int reportId = plugin.getDatabaseManager().createReport(report);
            plugin.getLogger().info("Auto-detected combat log: " + player.getName() + " (Report #" + reportId + ")");
            
            plugin.getReportManager().notifyNewReport(report);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create combat log report: " + e.getMessage());
        }
    }
}