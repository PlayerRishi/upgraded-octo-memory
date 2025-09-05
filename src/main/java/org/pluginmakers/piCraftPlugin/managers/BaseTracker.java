package org.pluginmakers.piCraftPlugin.managers;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.model.Report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BaseTracker {
    private final PiCraftPlugin plugin;
    private final File coordsFile;
    
    public BaseTracker(PiCraftPlugin plugin) {
        this.plugin = plugin;
        this.coordsFile = new File(plugin.getDataFolder(), "base_coordinates.log");
    }
    
    public void logReportLocation(@NotNull Report report) {
        if (!plugin.getConfigManager().isCoordinateLoggingEnabled()) {
            return;
        }
        
        try {
            if (!coordsFile.exists()) {
                coordsFile.createNewFile();
            }
            
            try (FileWriter writer = new FileWriter(coordsFile, true)) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String line = String.format("[%s] Report #%d - %s at %s %d %d %d (Category: %s) - %s%n",
                    timestamp,
                    report.getId(),
                    report.getReporterName(),
                    report.getWorld(),
                    report.getX(),
                    report.getY(),
                    report.getZ(),
                    report.getCategory() != null ? report.getCategory() : "none",
                    report.getMessage().length() > 50 ? report.getMessage().substring(0, 50) + "..." : report.getMessage()
                );
                writer.write(line);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to log coordinates: " + e.getMessage());
        }
    }
    
    public boolean isOutsideSpawnRadius(@NotNull Location location) {
        if (!plugin.getConfigManager().isBaseTrackingEnabled()) {
            return false;
        }
        
        Location spawn = location.getWorld().getSpawnLocation();
        double distance = location.distance(spawn);
        return distance > plugin.getConfigManager().getRadiusLimit();
    }
    
    @NotNull
    public String getDistanceFromSpawn(@NotNull Location location) {
        Location spawn = location.getWorld().getSpawnLocation();
        double distance = location.distance(spawn);
        return String.format("%.0f blocks from spawn", distance);
    }
}