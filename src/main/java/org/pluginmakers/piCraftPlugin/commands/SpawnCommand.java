package org.pluginmakers.piCraftPlugin.commands;

import org.bukkit.Location;
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
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.colorize("&cOnly players can use this command."));
            return true;
        }
        
        Player player = (Player) sender;
        Location spawnLocation = player.getWorld().getSpawnLocation();
        
        player.teleport(spawnLocation);
        player.sendMessage(ColorUtil.colorize("&aTeleported to spawn!"));
        
        return true;
    }
}