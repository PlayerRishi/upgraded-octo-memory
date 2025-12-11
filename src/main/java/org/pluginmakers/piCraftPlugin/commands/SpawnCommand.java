package org.pluginmakers.piCraftPlugin.commands;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.utils.ColorUtil;

public class SpawnCommand implements CommandExecutor {
    private final PiCraftPlugin plugin;
    
    public SpawnCommand(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!plugin.getConfig().getBoolean("commands.spawn.enabled", true)) {
            sender.sendMessage(ColorUtil.colorize("&cThis command is disabled."));
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.colorize("&cOnly players can use this command."));
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check if player is in combat
        if (plugin.getCombatTagManager().isInCombat(player)) {
            int timeLeft = plugin.getCombatTagManager().getRemainingCombatTime(player);
            player.sendMessage(ColorUtil.colorize("&cYou cannot use /spawn while in combat! Wait " + timeLeft + " seconds."));
            return true;
        }
        
        World.Environment environment = player.getWorld().getEnvironment();
        
        // Check if spawn is disabled in Nether
        if (environment == World.Environment.NETHER && plugin.getConfig().getBoolean("commands.spawn.nether_disabled", true)) {
            player.sendMessage(ColorUtil.colorize(plugin.getConfig().getString("messages.spawn_nether_disabled", "&c/spawn is disabled in the Nether!")));
            return true;
        }
        
        Location teleportLocation;
        String message;
        
        if (environment == World.Environment.THE_END) {
            // In the End, teleport to fountain coordinates
            double x = plugin.getConfig().getDouble("commands.spawn.end_fountain_coords.x", 0);
            double y = plugin.getConfig().getDouble("commands.spawn.end_fountain_coords.y", 64);
            double z = plugin.getConfig().getDouble("commands.spawn.end_fountain_coords.z", 0);
            teleportLocation = new Location(player.getWorld(), x, y, z);
            message = plugin.getConfig().getString("messages.spawn_fountain", "&aTeleported to the fountain!");
        } else {
            // In overworld or other dimensions, use world spawn
            teleportLocation = player.getWorld().getSpawnLocation();
            message = plugin.getConfig().getString("messages.spawn_teleported", "&aTeleported to spawn!");
        }
        
        player.teleport(teleportLocation);
        player.sendMessage(ColorUtil.colorize(message));
        
        return true;
    }
}