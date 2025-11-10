package org.pluginmakers.piCraftPlugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.utils.ColorUtil;

public class NoMansCommand implements CommandExecutor {
    private final PiCraftPlugin plugin;
    
    public NoMansCommand(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.colorize("&cOnly players can use this command."));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("picraft.nomans.admin")) {
            player.sendMessage(ColorUtil.colorize("&cNo permission."));
            return true;
        }
        
        if (args.length != 1) {
            player.sendMessage(ColorUtil.colorize("&cUsage: /nomans <corner1|corner2>"));
            return true;
        }
        
        if (args[0].equalsIgnoreCase("corner1")) {
            plugin.getNoMansLandManager().setCorner1(player.getLocation());
            player.sendMessage(ColorUtil.colorize("&aNo Man's Land corner 1 set at your location!"));
            player.sendMessage(ColorUtil.colorize("&7X: " + player.getLocation().getBlockX() + ", Z: " + player.getLocation().getBlockZ()));
        } else if (args[0].equalsIgnoreCase("corner2")) {
            plugin.getNoMansLandManager().setCorner2(player.getLocation());
            player.sendMessage(ColorUtil.colorize("&aNo Man's Land corner 2 set at your location!"));
            player.sendMessage(ColorUtil.colorize("&7X: " + player.getLocation().getBlockX() + ", Z: " + player.getLocation().getBlockZ()));
            
            if (plugin.getNoMansLandManager().getCorner1() != null) {
                player.sendMessage(ColorUtil.colorize("&e✓ No Man's Land area is now active!"));
            }
        } else {
            player.sendMessage(ColorUtil.colorize("&cUsage: /nomans <corner1|corner2>"));
        }
        
        return true;
    }
}