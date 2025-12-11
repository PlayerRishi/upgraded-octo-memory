package org.pluginmakers.piCraftPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.utils.ColorUtil;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

public class MOTDManager implements Listener {
    private final PiCraftPlugin plugin;
    private YamlConfiguration motdConfig;
    private List<String> motds;
    private int sequentialIndex = 0;
    private String lastDailyMotd = "";
    private LocalDate lastDate = LocalDate.now().minusDays(1);
    
    public MOTDManager(PiCraftPlugin plugin) {
        this.plugin = plugin;
        loadMOTDConfig();
    }
    
    private void loadMOTDConfig() {
        if (!plugin.getConfig().getBoolean("motd.enabled", true)) {
            return;
        }
        
        File motdFile = new File(plugin.getDataFolder(), plugin.getConfig().getString("motd.file", "motd.yml"));
        
        if (!motdFile.exists()) {
            plugin.saveResource("motd.yml", false);
        }
        
        motdConfig = YamlConfiguration.loadConfiguration(motdFile);
        motds = motdConfig.getStringList("motds");
        
        if (motds.isEmpty()) {
            plugin.getLogger().warning("No MOTDs found in motd.yml!");
        } else {
            plugin.getLogger().info("Loaded " + motds.size() + " MOTDs for rotation");
        }
    }
    
    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        if (!plugin.getConfig().getBoolean("motd.enabled", true) || motds == null || motds.isEmpty()) {
            return;
        }
        
        String motd = getRotatedMOTD();
        if (motd != null && !motd.isEmpty()) {
            // Replace variables
            motd = motd.replace("{players}", String.valueOf(event.getNumPlayers()))
                      .replace("{max}", String.valueOf(event.getMaxPlayers()))
                      .replace("{version}", Bukkit.getVersion());
            
            event.setMotd(ColorUtil.colorizeToString(motd));
        }
    }
    
    private String getRotatedMOTD() {
        String rotationType = plugin.getConfig().getString("motd.rotation", "daily").toLowerCase();
        
        return switch (rotationType) {
            case "daily" -> getDailyMOTD();
            case "random" -> getRandomMOTD();
            case "sequential" -> getSequentialMOTD();
            default -> {
                plugin.getLogger().warning("Invalid MOTD rotation type: " + rotationType + ". Using daily.");
                yield getDailyMOTD();
            }
        };
    }
    
    private String getDailyMOTD() {
        LocalDate today = LocalDate.now();
        
        // Check if we need a new daily MOTD
        if (!today.equals(lastDate)) {
            lastDate = today;
            // Use day of year as seed for consistent daily rotation
            Random random = new Random(today.getDayOfYear() + today.getYear());
            lastDailyMotd = motds.get(random.nextInt(motds.size()));
        }
        
        return lastDailyMotd;
    }
    
    private String getRandomMOTD() {
        Random random = new Random();
        return motds.get(random.nextInt(motds.size()));
    }
    
    private String getSequentialMOTD() {
        String motd = motds.get(sequentialIndex);
        sequentialIndex = (sequentialIndex + 1) % motds.size();
        return motd;
    }
    
    public void reloadMOTDs() {
        loadMOTDConfig();
        plugin.getLogger().info("MOTD configuration reloaded");
    }
}