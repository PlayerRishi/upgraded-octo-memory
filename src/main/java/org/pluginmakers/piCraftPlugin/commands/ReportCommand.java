package org.pluginmakers.piCraftPlugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.model.Report;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReportCommand implements CommandExecutor {
    private final PiCraftPlugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    
    public ReportCommand(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("picraft.report.use")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no_permission")));
            return true;
        }
        
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /report [-anon] [category] <message>");
            return true;
        }
        
        // Check cooldown
        if (!player.hasPermission("picraft.report.cooldown.bypass")) {
            long cooldownTime = plugin.getConfigManager().getReportCooldown() * 1000L;
            long lastReport = cooldowns.getOrDefault(player.getUniqueId(), 0L);
            long timeLeft = (lastReport + cooldownTime) - System.currentTimeMillis();
            
            if (timeLeft > 0) {
                String message = plugin.getConfigManager().getMessage("report_cooldown")
                    .replace("{time}", String.valueOf(timeLeft / 1000));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfigManager().getPrefix() + message));
                return true;
            }
        }
        
        // Parse arguments
        boolean anonymous = false;
        String category = null;
        StringBuilder messageBuilder = new StringBuilder();
        
        int startIndex = 0;
        
        // Check for -anon flag
        if (args[0].equalsIgnoreCase("-anon")) {
            if (!player.hasPermission("picraft.report.use.anon")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no_permission")));
                return true;
            }
            anonymous = true;
            startIndex = 1;
        }
        
        if (startIndex >= args.length) {
            player.sendMessage(ChatColor.RED + "Usage: /report [-anon] [category] <message>");
            return true;
        }
        
        // Check if next argument is a category (common categories)
        String[] commonCategories = {"griefing", "hacking", "toxicity", "cheating", "spam", "other"};
        if (startIndex < args.length) {
            String potentialCategory = args[startIndex].toLowerCase();
            for (String cat : commonCategories) {
                if (cat.equals(potentialCategory)) {
                    category = potentialCategory;
                    startIndex++;
                    break;
                }
            }
        }
        
        // Build message
        for (int i = startIndex; i < args.length; i++) {
            if (messageBuilder.length() > 0) {
                messageBuilder.append(" ");
            }
            messageBuilder.append(args[i]);
        }
        
        String message = messageBuilder.toString();
        if (message.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Please provide a message for your report.");
            return true;
        }
        
        // Create report
        Location loc = player.getLocation();
        Report report = new Report(
            player.getUniqueId(),
            player.getName(),
            anonymous,
            category,
            message,
            loc.getWorld().getName(),
            loc.getBlockX(),
            loc.getBlockY(),
            loc.getBlockZ()
        );
        
        try {
            int reportId = plugin.getDatabaseManager().createReport(report);
            
            // Update cooldown
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            
            // Send confirmation
            String confirmMessage = anonymous ? 
                plugin.getConfigManager().getMessage("report_submitted_anon") :
                plugin.getConfigManager().getMessage("report_submitted");
            
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getPrefix() + confirmMessage));
            
            // Notify staff and Discord
            plugin.getReportManager().notifyNewReport(report);
            
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Failed to submit report. Please try again.");
            e.printStackTrace();
        }
        
        return true;
    }
}