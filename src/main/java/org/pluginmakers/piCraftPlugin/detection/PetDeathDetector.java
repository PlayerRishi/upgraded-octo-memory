package org.pluginmakers.piCraftPlugin.detection;

import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.model.Report;
import org.pluginmakers.piCraftPlugin.utils.ColorUtil;

import java.util.UUID;

public class PetDeathDetector implements Listener {
    private final PiCraftPlugin plugin;
    
    public PetDeathDetector(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onEntityDeath(@NotNull EntityDeathEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("reports.auto_detection.pet_deaths.enabled", true)) {
            return;
        }
        
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        
        LivingEntity livingEntity = (LivingEntity) entity;
        Player killer = livingEntity.getKiller();
        if (killer == null) {
            return;
        }
        
        // Check if it's a tamed pet
        boolean isPet = entity instanceof Tameable && ((Tameable) entity).isTamed() && ((Tameable) entity).getOwner() != null;
        
        // Check if it has a nametag
        boolean hasNametag = entity.getCustomName() != null;
        
        if (!isPet && !hasNametag) {
            return;
        }
        
        // Don't report if owner kills their own pet
        if (isPet && plugin.getConfigManager().getConfig().getBoolean("reports.auto_detection.pet_deaths.exclude_owner", true)) {
            Tameable pet = (Tameable) entity;
            if (pet.getOwner().equals(killer)) {
                return;
            }
        }
        
        createPetDeathReport(killer, livingEntity, isPet, hasNametag);
    }
    
    private void createPetDeathReport(@NotNull Player killer, @NotNull LivingEntity entity, boolean isPet, boolean hasNametag) {
        try {
            String entityType = entity.getType().toString().toLowerCase().replace("_", " ");
            String entityName = hasNametag ? entity.getCustomName().toString() : entityType;
            String ownerName = null;
            
            if (isPet) {
                Tameable pet = (Tameable) entity;
                ownerName = pet.getOwner() instanceof Player ? 
                    ((Player) pet.getOwner()).getName() : "Unknown";
            }
            
            // Create report message
            String reportMessage;
            String chatMessage;
            
            if (isPet && ownerName != null) {
                reportMessage = String.format("Player %s killed %s's %s%s", 
                    killer.getName(), ownerName, entityType,
                    hasNametag ? " named '" + entityName + "'" : "");
                chatMessage = String.format("&c💀 %s killed %s's %s%s", 
                    killer.getName(), ownerName, entityType,
                    hasNametag ? " named '" + entityName + "'" : "");
            } else {
                reportMessage = String.format("Player %s killed %s named '%s'", 
                    killer.getName(), entityType, entityName);
                chatMessage = String.format("&c💀 %s killed %s named '%s'", 
                    killer.getName(), entityType, entityName);
            }
            
            // Send public chat message
            Bukkit.broadcast(ColorUtil.colorize(chatMessage));
            
            // Create report
            Report report = new Report(
                UUID.fromString("00000000-0000-0000-0000-000000000000"),
                "SYSTEM",
                false,
                "grief",
                reportMessage,
                entity.getWorld().getName(),
                entity.getLocation().getBlockX(),
                entity.getLocation().getBlockY(),
                entity.getLocation().getBlockZ()
            );
            
            report.setAutoDetected(true);
            
            int reportId = plugin.getDatabaseManager().createReport(report);
            plugin.getLogger().info("Auto-detected animal/pet death: " + reportMessage + " (Report #" + reportId + ")");
            
            plugin.getReportManager().notifyNewReport(report);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create pet death report: " + e.getMessage());
        }
    }
}