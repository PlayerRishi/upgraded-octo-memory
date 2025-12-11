package org.pluginmakers.piCraftPlugin.detection;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.model.Report;

import java.util.UUID;

public class VillagerKillDetector implements Listener {
    private final PiCraftPlugin plugin;
    
    public VillagerKillDetector(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onEntityDeath(@NotNull EntityDeathEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("reports.auto_detection.villager_kills.enabled", true)) {
            return;
        }
        
        if (event.getEntity().getType() != EntityType.VILLAGER) {
            return;
        }
        
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }
        Villager villager = (Villager) event.getEntity();
        
        // Only report if villager has a job (not NONE or NITWIT)
        if (plugin.getConfigManager().getConfig().getBoolean("reports.auto_detection.villager_kills.job_only", true)) {
            if (villager.getProfession() == Villager.Profession.NONE || 
                villager.getProfession() == Villager.Profession.NITWIT) {
                return;
            }
        }
        
        createVillagerKillReport(killer, villager);
    }
    
    private void createVillagerKillReport(@NotNull Player killer, @NotNull Villager villager) {
        try {
            String profession = villager.getProfession().toString().toLowerCase();
            
            Report report = new Report(
                UUID.fromString("00000000-0000-0000-0000-000000000000"),
                "SYSTEM",
                false,
                "other",
                String.format("Player %s killed a villager (%s)", 
                    killer.getName(), profession),
                villager.getWorld().getName(),
                villager.getLocation().getBlockX(),
                villager.getLocation().getBlockY(),
                villager.getLocation().getBlockZ()
            );
            
            report.setAutoDetected(true);
            
            int reportId = plugin.getDatabaseManager().createReport(report);
            plugin.getLogger().info("Auto-detected villager kill: " + killer.getName() + " (Report #" + reportId + ")");
            
            plugin.getReportManager().notifyNewReport(report);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create villager kill report: " + e.getMessage());
        }
    }
}