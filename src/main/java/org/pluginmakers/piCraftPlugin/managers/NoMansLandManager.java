package org.pluginmakers.piCraftPlugin.managers;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.utils.ColorUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NoMansLandManager implements Listener {
    private final PiCraftPlugin plugin;
    private final Map<UUID, Boolean> playersInNoMansLand = new HashMap<>();
    private Location corner1;
    private Location corner2;
    
    public NoMansLandManager(PiCraftPlugin plugin) {
        this.plugin = plugin;
        loadNoMansLandArea();
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (corner1 == null || corner2 == null) return;
        
        Player player = event.getPlayer();
        Location to = event.getTo();
        if (to == null) return;
        
        boolean wasInNoMansLand = playersInNoMansLand.getOrDefault(player.getUniqueId(), false);
        boolean isInNoMansLand = isInNoMansLand(to);
        
        if (!wasInNoMansLand && isInNoMansLand) {
            // Entering No Man's Land
            playersInNoMansLand.put(player.getUniqueId(), true);
            plugin.getCombatTagManager().setExtendedCombatTime(player, true);
            
            player.sendTitle(
                "§4§l⚠ DANGER ZONE ⚠",
                "§cYou are entering No Man's Land!",
                10, 40, 10
            );
            player.sendMessage(ColorUtil.colorize("&c&lYou are now entering No Man's Land - Beware!"));
            player.sendMessage(ColorUtil.colorize("&c&lEnter at your own risk!"));
            player.sendMessage(ColorUtil.colorize("&e⏰ Combat timer extended to &c60 seconds&e!"));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.5f);
            
        } else if (wasInNoMansLand && !isInNoMansLand) {
            // Exiting No Man's Land
            playersInNoMansLand.put(player.getUniqueId(), false);
            plugin.getCombatTagManager().setExtendedCombatTime(player, false);
            
            player.sendTitle(
                "§a§lSAFE ZONE",
                "§aYou have left No Man's Land",
                10, 30, 10
            );
            player.sendMessage(ColorUtil.colorize("&a&lYou are now exiting No Man's Land"));
            player.sendMessage(ColorUtil.colorize("&a&lCome back if you dare!"));
            player.sendMessage(ColorUtil.colorize("&e⏰ Combat timer reduced to &a15 seconds&e"));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        }
    }
    
    public void setCorner1(Location location) {
        this.corner1 = location;
        saveNoMansLandArea();
    }
    
    public void setCorner2(Location location) {
        this.corner2 = location;
        saveNoMansLandArea();
    }
    
    public boolean isInNoMansLand(Location location) {
        if (corner1 == null || corner2 == null) return false;
        if (!location.getWorld().equals(corner1.getWorld())) return false;
        
        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        
        int x = location.getBlockX();
        int z = location.getBlockZ();
        
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }
    
    public boolean isPlayerInNoMansLand(Player player) {
        return playersInNoMansLand.getOrDefault(player.getUniqueId(), false);
    }
    
    private void saveNoMansLandArea() {
        if (corner1 != null && corner2 != null) {
            plugin.getConfig().set("nomansland.corner1.x", corner1.getBlockX());
            plugin.getConfig().set("nomansland.corner1.z", corner1.getBlockZ());
            plugin.getConfig().set("nomansland.corner1.world", corner1.getWorld().getName());
            plugin.getConfig().set("nomansland.corner2.x", corner2.getBlockX());
            plugin.getConfig().set("nomansland.corner2.z", corner2.getBlockZ());
            plugin.getConfig().set("nomansland.corner2.world", corner2.getWorld().getName());
            plugin.saveConfig();
        }
    }
    
    private void loadNoMansLandArea() {
        if (plugin.getConfig().contains("nomansland.corner1.x")) {
            String worldName = plugin.getConfig().getString("nomansland.corner1.world");
            if (worldName != null && plugin.getServer().getWorld(worldName) != null) {
                corner1 = new Location(
                    plugin.getServer().getWorld(worldName),
                    plugin.getConfig().getInt("nomansland.corner1.x"),
                    0,
                    plugin.getConfig().getInt("nomansland.corner1.z")
                );
                corner2 = new Location(
                    plugin.getServer().getWorld(worldName),
                    plugin.getConfig().getInt("nomansland.corner2.x"),
                    0,
                    plugin.getConfig().getInt("nomansland.corner2.z")
                );
            }
        }
    }
    
    public Location getCorner1() { return corner1; }
    public Location getCorner2() { return corner2; }
}