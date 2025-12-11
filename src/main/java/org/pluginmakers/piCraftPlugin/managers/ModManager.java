package org.pluginmakers.piCraftPlugin.managers;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.utils.ColorUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ModManager implements Listener {
    private final PiCraftPlugin plugin;
    private final Set<UUID> verifiedPlayers = new HashSet<>();
    private final Set<String> requiredMods = Set.of("picraft");
    
    public ModManager(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Skip if mod check is disabled
        if (!plugin.getConfigManager().getConfig().getBoolean("mod_check.enabled", true)) {
            return;
        }
        
        // Skip check for ops if configured
        if (player.isOp() && plugin.getConfigManager().getConfig().getBoolean("mod_check.skip_ops", true)) {
            return;
        }
        
        // Skip check for whitelisted players
        if (player.hasPermission("picraft.modcheck.bypass")) {
            return;
        }
        
        // Delay check to allow client to fully load
        new BukkitRunnable() {
            @Override
            public void run() {
                checkPlayerMods(player);
            }
        }.runTaskLater(plugin, 60L); // 3 second delay
    }
    
    private void checkPlayerMods(Player player) {
        if (!player.isOnline()) return;
        
        boolean hasRequiredMod = hasRequiredMod(player);
        
        if (!hasRequiredMod) {
            String kickMessage = plugin.getConfigManager().getConfig().getString(
                "mod_check.kick_message", 
                "&cYou must install the PiCraft mod to play on this server!\n&eDownload: &fhttps://github.com/YourRepo/PiCraftMod"
            );
            
            player.kick(ColorUtil.colorize(kickMessage));
            plugin.getLogger().info("Kicked " + player.getName() + " for missing required PiCraft mod");
        } else {
            verifiedPlayers.add(player.getUniqueId());
        }
    }
    
    public boolean hasRequiredMod(Player player) {
        try {
            String clientBrand = player.getClientBrandName();
            plugin.getLogger().info("Player " + player.getName() + " client brand: " + clientBrand);
            
            if (clientBrand == null) {
                plugin.getLogger().info("Player " + player.getName() + " has null client brand - VANILLA");
                return false;
            }
            
            String brand = clientBrand.toLowerCase();
            
            // STRICT CHECK: Only allow if client brand contains "picraft"
            if (brand.contains("picraft")) {
                plugin.getLogger().info("Player " + player.getName() + " has PiCraft mod (brand: " + brand + ") - ALLOWED");
                return true;
            }
            
            // Reject all other clients
            plugin.getLogger().info("Player " + player.getName() + " missing PiCraft mod (brand: " + brand + ") - REJECTED");
            return false;
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error checking client brand for " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    public boolean isPlayerVerified(Player player) {
        return verifiedPlayers.contains(player.getUniqueId());
    }
    
    public void removePlayer(UUID uuid) {
        verifiedPlayers.remove(uuid);
    }
}