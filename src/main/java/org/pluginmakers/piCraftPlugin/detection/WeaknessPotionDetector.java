package org.pluginmakers.piCraftPlugin.detection;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.TippedArrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.model.Report;

import java.util.UUID;

public class WeaknessPotionDetector implements Listener {
    private final PiCraftPlugin plugin;
    
    public WeaknessPotionDetector(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerConsume(@NotNull PlayerItemConsumeEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("reports.auto_detection.weakness_potions.enabled", true)) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item.getType() == Material.POTION && item.hasItemMeta()) {
            PotionMeta meta = (PotionMeta) item.getItemMeta();
            
            if (hasWeaknessEffect(meta)) {
                // Cancel the consumption
                event.setCancelled(true);
                
                createWeaknessReport(player, "Attempted to consume weakness potion");
                player.sendMessage("§c§lVIOLATION: §cWeakness potions are prohibited!");
            }
        }
    }
    
    @EventHandler
    public void onProjectileLaunch(@NotNull ProjectileLaunchEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("reports.auto_detection.weakness_potions.enabled", true)) {
            return;
        }
        
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity().getShooter();
        
        // Check for weakness arrows
        if (event.getEntity() instanceof TippedArrow) {
            TippedArrow arrow = (TippedArrow) event.getEntity();
            
            for (PotionEffect effect : arrow.getCustomEffects()) {
                if (effect.getType().equals(PotionEffectType.WEAKNESS)) {
                    // Cancel the arrow launch
                    event.setCancelled(true);
                    
                    createWeaknessReport(player, "Attempted to shoot weakness arrow");
                    player.sendMessage("§c§lVIOLATION: §cWeakness arrows are prohibited!");
                    break;
                }
            }
        }
    }
    
    private boolean hasWeaknessEffect(@NotNull PotionMeta meta) {
        if (meta.hasCustomEffects()) {
            for (PotionEffect effect : meta.getCustomEffects()) {
                if (effect.getType().equals(PotionEffectType.WEAKNESS)) {
                    return true;
                }
            }
        }
        
        // Also check base potion data
        if (meta.getBasePotionData() != null) {
            return meta.getBasePotionData().getType().name().contains("WEAKNESS");
        }
        
        return false;
    }
    
    private void createWeaknessReport(@NotNull Player player, @NotNull String reason) {
        try {
            Report report = new Report(
                UUID.fromString("00000000-0000-0000-0000-000000000000"),
                "SYSTEM",
                false,
                "exploit",
                String.format("Weakness potion violation by %s: %s", player.getName(), reason),
                player.getWorld().getName(),
                player.getLocation().getBlockX(),
                player.getLocation().getBlockY(),
                player.getLocation().getBlockZ()
            );
            
            report.setAutoDetected(true);
            
            int reportId = plugin.getDatabaseManager().createReport(report);
            plugin.getLogger().info("Auto-detected weakness potion violation: " + player.getName() + " (Report #" + reportId + ")");
            
            plugin.getReportManager().notifyNewReport(report);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create weakness potion report: " + e.getMessage());
        }
    }
}