package org.pluginmakers.piCraftPlugin.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.model.Report;
import org.pluginmakers.piCraftPlugin.utils.ColorUtil;

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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.colorize("&cOnly players can use this command."));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("picraft.report.use")) {
            player.sendMessage(ColorUtil.colorize(
                plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no_permission")));
            return true;
        }
        
        if (args.length == 0) {
            java.util.List<String> categories = plugin.getConfigManager().getReportCategories();
            player.sendMessage(ColorUtil.colorize("&cUsage: /report [-anon] <category> <message>"));
            player.sendMessage(ColorUtil.colorize("&eCategories: " + String.join(", ", categories)));
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
                player.sendMessage(ColorUtil.colorize(
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
                player.sendMessage(ColorUtil.colorize(
                    plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no_permission")));
                return true;
            }
            anonymous = true;
            startIndex = 1;
        }
        
        if (startIndex >= args.length) {
            java.util.List<String> categories = plugin.getConfigManager().getReportCategories();
            player.sendMessage(ColorUtil.colorize("&cUsage: /report [-anon] <category> <message>"));
            player.sendMessage(ColorUtil.colorize("&eCategories: " + String.join(", ", categories)));
            return true;
        }
        
        // Category is now required
        java.util.List<String> validCategories = plugin.getConfigManager().getReportCategories();
        String potentialCategory = args[startIndex].toLowerCase();
        
        boolean categoryFound = false;
        for (String cat : validCategories) {
            if (cat.toLowerCase().equals(potentialCategory)) {
                category = cat;
                categoryFound = true;
                startIndex++;
                break;
            }
        }
        
        if (!categoryFound) {
            player.sendMessage(ColorUtil.colorize("&cInvalid category: " + args[startIndex]));
            player.sendMessage(ColorUtil.colorize("&eValid categories: " + String.join(", ", validCategories)));
            return true;
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
            player.sendMessage(ColorUtil.colorize("&cPlease provide a message for your report."));
            return true;
        }
        
        if (startIndex >= args.length) {
            player.sendMessage(ColorUtil.colorize("&cPlease provide a message for your report."));
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
            
            // Log coordinates for base tracking
            plugin.getBaseTracker().logReportLocation(report);
            
            // Check if outside spawn radius
            if (plugin.getBaseTracker().isOutsideSpawnRadius(loc)) {
                String distance = plugin.getBaseTracker().getDistanceFromSpawn(loc);
                plugin.getLogger().info("Report #" + reportId + " filed outside spawn radius: " + distance);
            }
            
            // Send confirmation
            String confirmMessage = anonymous ? 
                plugin.getConfigManager().getMessage("report_submitted_anon") :
                plugin.getConfigManager().getMessage("report_submitted");
            
            player.sendMessage(ColorUtil.colorize(
                plugin.getConfigManager().getPrefix() + confirmMessage));
            
            // Notify staff and Discord
            plugin.getReportManager().notifyNewReport(report);
            
        } catch (Exception e) {
            player.sendMessage(ColorUtil.colorize("&cFailed to submit report. Please try again."));
            e.printStackTrace();
        }
        
        return true;
    }
}