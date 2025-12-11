package org.pluginmakers.piCraftPlugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.utils.ColorUtil;

public class HeartWithdrawCommand implements CommandExecutor {
    private final PiCraftPlugin plugin;
    
    public HeartWithdrawCommand(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.colorize("&cOnly players can use this command."));
            return true;
        }
        
        Player player = (Player) sender;
        int currentHearts = plugin.getLifestealManager().getPlayerHearts(player);
        int minHearts = plugin.getConfig().getInt("lifesteal.min_hearts", 5);
        
        if (currentHearts <= minHearts) {
            player.sendMessage(ColorUtil.colorize(plugin.getConfig().getString("messages.heart_no_withdraw", "&cYou don't have enough hearts to withdraw!")));
            return true;
        }
        
        // Check inventory space
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ColorUtil.colorize("&cInventory full! Make space first."));
            return true;
        }
        
        // Remove heart from player
        plugin.getLifestealManager().setPlayerHearts(player, currentHearts - 1);
        
        // Give heart item
        ItemStack heart = plugin.getLifestealManager().createHeartItem();
        player.getInventory().addItem(heart);
        
        player.sendMessage(ColorUtil.colorize(
            plugin.getConfig().getString("messages.heart_withdrawn", "&aWithdrew a heart!")
            .replace("{hearts}", String.valueOf(currentHearts - 1))
        ));
        
        return true;
    }
}