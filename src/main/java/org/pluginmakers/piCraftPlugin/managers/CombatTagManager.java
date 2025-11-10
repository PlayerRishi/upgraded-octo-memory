package org.pluginmakers.piCraftPlugin.managers;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatTagManager implements Listener {
    private final PiCraftPlugin plugin;
    private final Map<UUID, Long> combatTags = new HashMap<>();
    private final Map<UUID, BukkitTask> activeTasks = new HashMap<>();
    private final Map<UUID, Boolean> extendedCombatTime = new HashMap<>();
    private final int COMBAT_TIME = 15;
    private final int EXTENDED_COMBAT_TIME = 60;
    
    public CombatTagManager(PiCraftPlugin plugin) {
        this.plugin = plugin;
        startCleanupTask();
    }
    
    @EventHandler
    public void onPlayerDamage(@NotNull EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player attacker = (Player) event.getDamager();
            
            tagPlayer(victim);
            tagPlayer(attacker);
        }
    }
    
    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        combatTags.remove(uuid);
        extendedCombatTime.remove(uuid);
        BukkitTask task = activeTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }
    
    public void tagPlayer(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        combatTags.put(uuid, System.currentTimeMillis());
        
        // Cancel existing task if any
        BukkitTask existingTask = activeTasks.get(uuid);
        if (existingTask != null) {
            existingTask.cancel();
        }
        
        // Start new countdown task
        BukkitTask newTask = new BukkitRunnable() {
            int timeLeft = getCombatTime(player);
            
            @Override
            public void run() {
                if (!isInCombat(player) || timeLeft <= 0) {
                    if (timeLeft <= 0) {
                        player.sendActionBar("§a§lCOMBAT TAG EXPIRED");
                    }
                    activeTasks.remove(uuid);
                    cancel();
                    return;
                }
                
                String zone = extendedCombatTime.getOrDefault(uuid, false) ? " §4[NO MAN'S LAND]" : "";
                player.sendActionBar("§c§lCOMBAT TAGGED §7- §f" + timeLeft + "s" + zone);
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        activeTasks.put(uuid, newTask);
    }
    
    public boolean isInCombat(@NotNull Player player) {
        Long tagTime = combatTags.get(player.getUniqueId());
        if (tagTime == null) return false;
        
        long timeSince = (System.currentTimeMillis() - tagTime) / 1000;
        return timeSince < COMBAT_TIME;
    }
    
    public int getRemainingCombatTime(@NotNull Player player) {
        Long tagTime = combatTags.get(player.getUniqueId());
        if (tagTime == null) return 0;
        
        long timeSince = (System.currentTimeMillis() - tagTime) / 1000;
        return Math.max(0, getCombatTime(player) - (int) timeSince);
    }
    
    public void setExtendedCombatTime(@NotNull Player player, boolean extended) {
        UUID uuid = player.getUniqueId();
        extendedCombatTime.put(uuid, extended);
        
        // If player is currently in combat, restart the timer with new duration
        if (isInCombat(player)) {
            tagPlayer(player);
        }
    }
    
    private int getCombatTime(@NotNull Player player) {
        return extendedCombatTime.getOrDefault(player.getUniqueId(), false) ? EXTENDED_COMBAT_TIME : COMBAT_TIME;
    }
    
    private void startCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                combatTags.entrySet().removeIf(entry -> {
                    UUID uuid = entry.getKey();
                    int combatTime = extendedCombatTime.getOrDefault(uuid, false) ? EXTENDED_COMBAT_TIME : COMBAT_TIME;
                    boolean expired = (now - entry.getValue()) / 1000 >= combatTime;
                    if (expired) {
                        // Also clean up any remaining tasks
                        BukkitTask task = activeTasks.remove(uuid);
                        if (task != null) {
                            task.cancel();
                        }
                    }
                    return expired;
                });
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
}