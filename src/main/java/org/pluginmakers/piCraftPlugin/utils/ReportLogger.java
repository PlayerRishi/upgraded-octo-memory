package org.pluginmakers.piCraftPlugin.utils;

import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.model.Report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReportLogger {
    private final PiCraftPlugin plugin;
    private final File logFile;
    
    public ReportLogger(PiCraftPlugin plugin) {
        this.plugin = plugin;
        this.logFile = new File(plugin.getDataFolder(), plugin.getConfig().getString("reports.logging.file", "reports.log"));
    }
    
    public void logReport(Report report) {
        if (!plugin.getConfig().getBoolean("reports.logging.enabled", true)) {
            return;
        }
        
        // Skip auto-detected reports if disabled
        if (report.isAutoDetected() && !plugin.getConfig().getBoolean("reports.logging.log_auto_detected", true)) {
            return;
        }
        
        try {
            String logEntry = formatLogEntry(report);
            writeToFile(logEntry);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to log report #" + report.getId() + ": " + e.getMessage());
        }
    }
    
    private String formatLogEntry(Report report) {
        String format = plugin.getConfig().getString("reports.logging.format", 
            "[{timestamp}] #{id} {reporter} -> {category}: {message} at {world} {x},{y},{z}");
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String reporter = report.isAnonymous() ? "Anonymous" : report.getReporterName();
        if (report.isAutoDetected()) {
            reporter = "AUTO-DETECTED";
        }
        
        return format
            .replace("{timestamp}", timestamp)
            .replace("{id}", String.valueOf(report.getId()))
            .replace("{reporter}", reporter)
            .replace("{category}", report.getCategory())
            .replace("{message}", report.getMessage().replace("\n", " "))
            .replace("{world}", report.getWorld())
            .replace("{x}", String.valueOf(report.getX()))
            .replace("{y}", String.valueOf(report.getY()))
            .replace("{z}", String.valueOf(report.getZ()));
    }
    
    private void writeToFile(String logEntry) throws IOException {
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(logEntry + System.lineSeparator());
        }
    }
}