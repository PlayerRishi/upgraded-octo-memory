package org.pluginmakers.piCraftPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.utils.ColorUtil;

import java.util.Map;
import java.util.UUID;

public class SusCommand implements CommandExecutor {
    private final PiCraftPlugin plugin;
    
    public SusCommand(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("picraft.sus")) {
            sender.sendMessage(ColorUtil.colorize("&cYou do not have permission."));
            return true;
        }
        
        if (args.length != 1) {
            sender.sendMessage(ColorUtil.colorize("&cUsage: /sus <player>"));
            return true;
        }
        
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore()) {
            sender.sendMessage(ColorUtil.colorize("&cPlayer not found."));
            return true;
        }
        
        UUID playerUUID = target.getUniqueId();
        String playerName = target.getName();
        
        // Get mining statistics
        Map<String, Integer> oreStats = plugin.getDatabaseManager().getOreMiningStats(playerUUID, 7); // Last 7 days
        Map<String, Integer> locationStats = plugin.getDatabaseManager().getOreMiningByLocation(playerUUID, 7);
        
        sender.sendMessage(ColorUtil.colorize("&6&l=== SUS REPORT: " + playerName + " ==="));
        sender.sendMessage(ColorUtil.colorize("&e&lORE MINING (Last 7 days):"));
        
        if (oreStats.isEmpty()) {
            sender.sendMessage(ColorUtil.colorize("&7No ore mining data found."));
        } else {
            for (Map.Entry<String, Integer> entry : oreStats.entrySet()) {
                String ore = entry.getKey().replace("_ORE", "").replace("DEEPSLATE_", "");
                int count = entry.getValue();
                String color = getOreColor(entry.getKey());
                sender.sendMessage(ColorUtil.colorize(color + ore + ": &f" + count));
            }
        }
        
        sender.sendMessage(ColorUtil.colorize("&e&lMINING HEATMAP:"));
        if (locationStats.isEmpty()) {
            sender.sendMessage(ColorUtil.colorize("&7No location data found."));
        } else {
            for (Map.Entry<String, Integer> entry : locationStats.entrySet()) {
                String[] coords = entry.getKey().split(",");
                int count = entry.getValue();
                String intensity = getHeatmapColor(count);
                sender.sendMessage(ColorUtil.colorize(intensity + "▓ &7(" + coords[0] + ", " + coords[1] + ", " + coords[2] + ") &f" + count + " ores"));
            }
        }
        
        // Movement analysis
        boolean isOnline = target.isOnline();
        sender.sendMessage(ColorUtil.colorize("&e&lSTATUS:"));
        sender.sendMessage(ColorUtil.colorize("&7Online: " + (isOnline ? "&aYes" : "&cNo")));
        
        if (isOnline) {
            Player onlinePlayer = (Player) target;
            sender.sendMessage(ColorUtil.colorize("&7World: &f" + onlinePlayer.getWorld().getName()));
            sender.sendMessage(ColorUtil.colorize("&7Location: &f" + 
                onlinePlayer.getLocation().getBlockX() + ", " + 
                onlinePlayer.getLocation().getBlockY() + ", " + 
                onlinePlayer.getLocation().getBlockZ()));
        }
        
        return true;
    }
    
    private String getOreColor(String ore) {
        return switch (ore) {
            case "DIAMOND_ORE", "DEEPSLATE_DIAMOND_ORE" -> "&b";
            case "EMERALD_ORE", "DEEPSLATE_EMERALD_ORE" -> "&a";
            case "GOLD_ORE", "DEEPSLATE_GOLD_ORE", "NETHER_GOLD_ORE" -> "&6";
            case "IRON_ORE", "DEEPSLATE_IRON_ORE" -> "&7";
            case "ANCIENT_DEBRIS" -> "&4";
            case "REDSTONE_ORE", "DEEPSLATE_REDSTONE_ORE" -> "&c";
            case "LAPIS_ORE", "DEEPSLATE_LAPIS_ORE" -> "&9";
            default -> "&f";
        };
    }
    
    private String getHeatmapColor(int count) {
        if (count >= 20) return "&4"; // Dark red - very suspicious
        if (count >= 10) return "&c"; // Red - suspicious
        if (count >= 5) return "&6";  // Orange - moderate
        if (count >= 2) return "&e";  // Yellow - low
        return "&7";                  // Gray - minimal
    }
}