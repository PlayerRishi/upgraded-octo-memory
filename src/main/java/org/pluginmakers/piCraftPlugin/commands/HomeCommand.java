package org.pluginmakers.piCraftPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.utils.ColorUtil;

public class HomeCommand implements CommandExecutor {
    private final PiCraftPlugin plugin;
    
    public HomeCommand(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.colorize("&cOnly players can use this command."));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Player using /home
            if (plugin.getCombatTagManager().isInCombat(player)) {
                int timeLeft = plugin.getCombatTagManager().getRemainingCombatTime(player);
                player.sendMessage(ColorUtil.colorize("&cYou cannot use /home while in combat! Wait " + timeLeft + " seconds."));
                return true;
            }
            
            Location bedLocation = player.getBedSpawnLocation();
            if (bedLocation == null) {
                player.sendMessage(ColorUtil.colorize("&cYou have not set a respawn point. Sleep in a bed first!"));
                return true;
            }
            
            player.teleport(bedLocation);
            player.sendMessage(ColorUtil.colorize("&aTeleported to your spawn point!"));
            return true;
        }
        
        // Admin using /home <player>
        if (!player.hasPermission("picraft.home.admin")) {
            player.sendMessage(ColorUtil.colorize("&cNo permission."));
            return true;
        }
        
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        Location bedLocation = target.getBedSpawnLocation();
        
        if (bedLocation == null) {
            player.sendMessage(ColorUtil.colorize("&c" + target.getName() + " has no bed spawn set."));
            return true;
        }
        
        player.teleport(bedLocation);
        player.sendMessage(ColorUtil.colorize("&aTeleported to " + target.getName() + "'s bed spawn!"));
        return true;
    }
}