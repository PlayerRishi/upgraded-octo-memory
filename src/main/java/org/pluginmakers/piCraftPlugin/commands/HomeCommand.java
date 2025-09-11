package org.pluginmakers.piCraftPlugin.commands;

import org.bukkit.Location;
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
        Location bedLocation = player.getBedSpawnLocation();
        
        if (bedLocation == null) {
            player.sendMessage(ColorUtil.colorize("&cYou have not set a respawn point. Sleep in a bed first!"));
            return true;
        }
        
        // Check if bed still exists
        if (!bedLocation.getBlock().getType().name().contains("BED")) {
            player.sendMessage(ColorUtil.colorize("&cYour bed has been destroyed! Set a new respawn point."));
            return true;
        }
        
        // Teleport to bed location (slightly above to avoid suffocation)
        Location teleportLocation = bedLocation.clone().add(0, 1, 0);
        player.teleport(teleportLocation);
        player.sendMessage(ColorUtil.colorize("&aTeleported to your bed!"));
        
        return true;
    }
}