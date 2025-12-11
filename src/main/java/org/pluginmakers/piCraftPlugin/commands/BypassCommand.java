package org.pluginmakers.piCraftPlugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.utils.ColorUtil;

public class BypassCommand implements CommandExecutor {
    private final PiCraftPlugin plugin;
    
    public BypassCommand(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("picraft.modcheck.admin")) {
            if (sender instanceof Player) {
                ((Player) sender).sendMessage(ColorUtil.colorize("&cNo permission."));
            } else {
                sender.sendMessage("No permission.");
            }
            return true;
        }
        
        if (args.length == 0) {
            // Toggle current state
            boolean current = plugin.getConfigManager().isModCheckEnabled();
            plugin.getConfigManager().setModCheckEnabled(!current);
            
            String status = !current ? "&aenabled" : "&cdisabled";
            if (sender instanceof Player) {
                ((Player) sender).sendMessage(ColorUtil.colorize("&7Mod checking " + status + "&7."));
            } else {
                sender.sendMessage("Mod checking " + (!current ? "enabled" : "disabled") + ".");
            }
        } else if (args.length == 1) {
            String arg = args[0].toLowerCase();
            if (arg.equals("on") || arg.equals("enable")) {
                plugin.getConfigManager().setModCheckEnabled(true);
                if (sender instanceof Player) {
                    ((Player) sender).sendMessage(ColorUtil.colorize("&aMod checking enabled."));
                } else {
                    sender.sendMessage("Mod checking enabled.");
                }
            } else if (arg.equals("off") || arg.equals("disable")) {
                plugin.getConfigManager().setModCheckEnabled(false);
                if (sender instanceof Player) {
                    ((Player) sender).sendMessage(ColorUtil.colorize("&cMod checking disabled."));
                } else {
                    sender.sendMessage("Mod checking disabled.");
                }
            } else {
                if (sender instanceof Player) {
                    ((Player) sender).sendMessage(ColorUtil.colorize("&cUsage: /modtoggle [on|off]"));
                } else {
                    sender.sendMessage("Usage: /modtoggle [on|off]");
                }
            }
        } else {
            if (sender instanceof Player) {
                ((Player) sender).sendMessage(ColorUtil.colorize("&cUsage: /modtoggle [on|off]"));
            } else {
                sender.sendMessage("Usage: /modtoggle [on|off]");
            }
        }
        
        return true;
    }
}