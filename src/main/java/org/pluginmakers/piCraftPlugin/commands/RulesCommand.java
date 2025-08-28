package org.pluginmakers.piCraftPlugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class RulesCommand implements CommandExecutor {
    private final PiCraftPlugin plugin;
    
    public RulesCommand(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("picraft.rules")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no_permission")));
            return true;
        }
        
        File rulesFile = new File(plugin.getDataFolder(), plugin.getConfigManager().getRulesFile());
        
        if (!rulesFile.exists()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("rules_not_found")));
            return true;
        }
        
        try {
            List<String> lines = Files.readAllLines(rulesFile.toPath());
            
            if (plugin.getConfigManager().isPaginationEnabled()) {
                int page = 1;
                if (args.length > 0) {
                    try {
                        page = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e) {
                        page = 1;
                    }
                }
                
                int linesPerPage = plugin.getConfigManager().getLinesPerPage();
                int totalPages = (int) Math.ceil((double) lines.size() / linesPerPage);
                
                if (page < 1) page = 1;
                if (page > totalPages) page = totalPages;
                
                int startIndex = (page - 1) * linesPerPage;
                int endIndex = Math.min(startIndex + linesPerPage, lines.size());
                
                sender.sendMessage(ChatColor.GOLD + "=== Rules (Page " + page + "/" + totalPages + ") ===");
                
                for (int i = startIndex; i < endIndex; i++) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lines.get(i)));
                }
                
                if (page < totalPages) {
                    sender.sendMessage(ChatColor.YELLOW + "Use /rules " + (page + 1) + " for the next page.");
                }
            } else {
                // Send all rules at once
                sender.sendMessage(ChatColor.GOLD + "=== Server Rules ===");
                for (String line : lines) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
                }
            }
            
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Error reading rules file.");
            e.printStackTrace();
        }
        
        return true;
    }
}