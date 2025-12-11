package org.pluginmakers.piCraftPlugin.managers;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.utils.ColorUtil;

import java.util.Arrays;

public class LifestealManager implements Listener {
    private final PiCraftPlugin plugin;
    
    public LifestealManager(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            setPlayerHearts(player, plugin.getConfig().getInt("lifesteal.default_hearts", 10));
        }
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.getConfig().getBoolean("lifesteal.enabled", true)) return;
        
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        int minHearts = plugin.getConfig().getInt("lifesteal.min_hearts", 5);
        int maxHearts = plugin.getConfig().getInt("lifesteal.max_hearts", 20);
        
        int victimHearts = getPlayerHearts(victim);
        
        // Only process if victim has more than minimum hearts
        if (victimHearts > minHearts) {
            // Remove heart from victim
            setPlayerHearts(victim, victimHearts - 1);
            victim.sendMessage(ColorUtil.colorize(
                plugin.getConfig().getString("messages.heart_lost", "&cYou lost a heart!")
                .replace("{hearts}", String.valueOf(victimHearts - 1))
            ));
            
            if (killer != null) {
                // Player kill - try to give heart to killer
                int killerHearts = getPlayerHearts(killer);
                if (killerHearts < maxHearts) {
                    // Killer can receive heart - direct transfer
                    setPlayerHearts(killer, killerHearts + 1);
                    killer.sendMessage(ColorUtil.colorize(
                        plugin.getConfig().getString("messages.heart_gained", "&aYou gained a heart!")
                        .replace("{hearts}", String.valueOf(killerHearts + 1))
                    ));
                } else {
                    // Killer at max hearts - drop heart item
                    ItemStack heart = createHeartItem();
                    event.getDrops().add(heart);
                }
            } else {
                // Non-player death (fall, lava, etc.) - always drop heart
                ItemStack heart = createHeartItem();
                event.getDrops().add(heart);
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;
        
        ItemStack item = event.getItem();
        if (!isHeartItem(item)) return;
        
        Player player = event.getPlayer();
        int currentHearts = getPlayerHearts(player);
        int maxHearts = plugin.getConfig().getInt("lifesteal.max_hearts", 20);
        
        if (currentHearts >= maxHearts) {
            player.sendMessage(ColorUtil.colorize(plugin.getConfig().getString("messages.heart_max", "&cYou already have maximum hearts!")));
            return;
        }
        
        event.setCancelled(true);
        item.setAmount(item.getAmount() - 1);
        
        setPlayerHearts(player, currentHearts + 1);
        player.sendMessage(ColorUtil.colorize(
            plugin.getConfig().getString("messages.heart_used", "&aYou used a heart!")
            .replace("{hearts}", String.valueOf(currentHearts + 1))
        ));
    }
    
    public ItemStack createHeartItem() {
        ItemStack heart = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = heart.getItemMeta();
        
        meta.setDisplayName(ColorUtil.colorizeToString("&c❤ Heart"));
        meta.setLore(Arrays.asList(
            ColorUtil.colorizeToString("&7Right-click to use"),
            ColorUtil.colorizeToString("&7Increases your max health")
        ));
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setCustomModelData(12345); // Makes them stackable
        
        heart.setItemMeta(meta);
        return heart;
    }
    
    public boolean isHeartItem(ItemStack item) {
        if (item == null || item.getType() != Material.NETHER_STAR) return false;
        if (!item.hasItemMeta()) return false;
        
        ItemMeta meta = item.getItemMeta();
        return meta.hasCustomModelData() && meta.getCustomModelData() == 12345;
    }
    
    public int getPlayerHearts(Player player) {
        return (int) (player.getAttribute(Attribute.MAX_HEALTH).getValue() / 2);
    }
    
    public void setPlayerHearts(Player player, int hearts) {
        int minHearts = plugin.getConfig().getInt("lifesteal.min_hearts", 5);
        int maxHearts = plugin.getConfig().getInt("lifesteal.max_hearts", 20);
        
        hearts = Math.max(minHearts, Math.min(maxHearts, hearts));
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(hearts * 2);
        
        // Heal player if their current health exceeds new max
        if (player.getHealth() > hearts * 2) {
            player.setHealth(hearts * 2);
        }
    }
}