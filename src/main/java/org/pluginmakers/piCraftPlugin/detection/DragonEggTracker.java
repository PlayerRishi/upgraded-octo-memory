package org.pluginmakers.piCraftPlugin.detection;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.model.Report;

import java.util.UUID;

public class DragonEggTracker implements Listener {
    private final PiCraftPlugin plugin;
    
    public DragonEggTracker(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("reports.auto_detection.dragon_egg.enabled", true)) {
            return;
        }
        
        if (!plugin.getConfigManager().getConfig().getBoolean("reports.auto_detection.dragon_egg.monitor_ender_chest", true)) {
            return;
        }
        
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        // Check if player is trying to put dragon egg in ender chest
        if (event.getInventory().getType() == InventoryType.ENDER_CHEST) {
            ItemStack item = event.getCursor();
            
            if (item != null && item.getType() == Material.DRAGON_EGG) {
                // Cancel the action
                event.setCancelled(true);
                
                // Create violation report
                createDragonEggReport(player, "Attempted to place Dragon Egg in Ender Chest");
                
                player.sendMessage("§c§lVIOLATION: §cDragon Egg cannot be placed in Ender Chest!");
            }
            
            // Also check if they're trying to shift-click dragon egg into ender chest
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() == Material.DRAGON_EGG && event.isShiftClick()) {
                event.setCancelled(true);
                createDragonEggReport(player, "Attempted to shift-click Dragon Egg into Ender Chest");
                player.sendMessage("§c§lVIOLATION: §cDragon Egg cannot be placed in Ender Chest!");
            }
        }
    }
    
    private void createDragonEggReport(@NotNull Player player, @NotNull String reason) {
        try {
            Report report = new Report(
                UUID.fromString("00000000-0000-0000-0000-000000000000"),
                "SYSTEM",
                false,
                "exploit",
                String.format("Dragon Egg violation by %s: %s", player.getName(), reason),
                player.getWorld().getName(),
                player.getLocation().getBlockX(),
                player.getLocation().getBlockY(),
                player.getLocation().getBlockZ()
            );
            
            report.setAutoDetected(true);
            
            int reportId = plugin.getDatabaseManager().createReport(report);
            plugin.getLogger().info("Auto-detected dragon egg violation: " + player.getName() + " (Report #" + reportId + ")");
            
            plugin.getReportManager().notifyNewReport(report);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create dragon egg report: " + e.getMessage());
        }
    }
}