package org.pluginmakers.piCraftPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.utils.ColorUtil;

public class ModCheckCommand implements CommandExecutor {
    private final PiCraftPlugin plugin;
    
    public ModCheckCommand(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            if (!sender.hasPermission("picraft.modcheck")) {
                if (sender instanceof Player) {
                    ((Player) sender).sendMessage(ColorUtil.colorize("&cNo permission."));
                } else {
                    sender.sendMessage("No permission.");
                }
                return true;
            }
            
            if (args.length == 0) {
                // Show all online players' mod status
                if (sender instanceof Player) {
                    ((Player) sender).sendMessage(ColorUtil.colorize("&6=== Mod Check - All Players ==="));
                } else {
                    sender.sendMessage("=== Mod Check - All Players ===");
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    showPlayerModStatus(sender, player);
                }
                return true;
            }
            
            // Check specific player
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                if (sender instanceof Player) {
                    ((Player) sender).sendMessage(ColorUtil.colorize("&cPlayer not found."));
                } else {
                    sender.sendMessage("Player not found.");
                }
                return true;
            }
            
            if (sender instanceof Player) {
                ((Player) sender).sendMessage(ColorUtil.colorize("&6=== Mod Check - " + target.getName() + " ==="));
            } else {
                sender.sendMessage("=== Mod Check - " + target.getName() + " ===");
            }
            showPlayerModStatus(sender, target);
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Error in modcheck command: " + e.getMessage());
            e.printStackTrace();
            if (sender instanceof Player) {
                ((Player) sender).sendMessage(ColorUtil.colorize("&cCommand error. Check console."));
            } else {
                sender.sendMessage("Command error: " + e.getMessage());
            }
            return true;
        }
    }
    
    private void showPlayerModStatus(CommandSender sender, Player player) {
        try {
            boolean hasPiCraftMod = plugin.getModManager().hasRequiredMod(player);
            
            String clientBrand = "Unknown";
            try {
                clientBrand = player.getClientBrandName();
                if (clientBrand == null) clientBrand = "Vanilla";
            } catch (Exception ignored) {}
            
            String resourcePackStatus = "Unknown";
            try {
                resourcePackStatus = player.getResourcePackStatus().toString();
            } catch (Exception ignored) {}
            
            var modList = plugin.getModListListener().getPlayerMods(player);
            var resourcePacks = plugin.getModListListener().getPlayerResourcePacks(player);
            
            if (sender instanceof Player) {
                Player p = (Player) sender;
                p.sendMessage(ColorUtil.colorize("&e========== " + player.getName() + " =========="));
                
                // Status
                String modStatus = hasPiCraftMod ? "&aHAS PICRAFT MOD ✓" : "&cMISSING PICRAFT MOD ✗";
                p.sendMessage(ColorUtil.colorize("&7Status: " + modStatus));
                
                // Client info
                p.sendMessage(ColorUtil.colorize("&7Client: &f" + clientBrand));
                p.sendMessage(ColorUtil.colorize("&7Resource Pack: &f" + resourcePackStatus));
                
                // Mod list
                boolean hasModData = plugin.getModListListener().hasReceivedModList(player);
                p.sendMessage(ColorUtil.colorize("&7Mods (&f" + modList.size() + "&7)" + (hasModData ? ":" : " &8[No data received]")));
                if (!hasModData) {
                    p.sendMessage(ColorUtil.colorize("  &8Player hasn't sent mod list yet"));
                } else if (modList.isEmpty()) {
                    p.sendMessage(ColorUtil.colorize("  &8No mods installed"));
                } else {
                    int shown = 0;
                    for (String mod : modList) {
                        if (shown >= 15) {
                            p.sendMessage(ColorUtil.colorize("  &8... and " + (modList.size() - shown) + " more"));
                            break;
                        }
                        String color = mod.contains("picraftmod") ? "&a" : 
                                      mod.contains("fabric") ? "&e" : 
                                      mod.contains("sodium") || mod.contains("lithium") || mod.contains("iris") ? "&b" : "&f";
                        p.sendMessage(ColorUtil.colorize("  " + color + mod));
                        shown++;
                    }
                }
                
                // Resource packs
                p.sendMessage(ColorUtil.colorize("&7Resource Packs (&f" + resourcePacks.size() + "&7):"));
                if (resourcePacks.isEmpty()) {
                    p.sendMessage(ColorUtil.colorize("  &8No resource packs active"));
                } else {
                    for (String pack : resourcePacks) {
                        String color = pack.toLowerCase().contains("vanilla") ? "&7" : "&d";
                        p.sendMessage(ColorUtil.colorize("  " + color + pack));
                    }
                }
                
                // Access status
                String accessStatus = hasPiCraftMod ? "&aALLOWED" : "&cWOULD BE KICKED";
                p.sendMessage(ColorUtil.colorize("&7Server Access: " + accessStatus));
                
            } else {
                sender.sendMessage("========== " + player.getName() + " ==========");
                String modStatus = hasPiCraftMod ? "HAS PICRAFT MOD" : "MISSING PICRAFT MOD";
                sender.sendMessage("Status: " + modStatus);
                sender.sendMessage("Client: " + clientBrand);
                sender.sendMessage("Resource Pack: " + resourcePackStatus);
                boolean hasModData = plugin.getModListListener().hasReceivedModList(player);
                sender.sendMessage("Mods (" + modList.size() + ")" + (hasModData ? ":" : " [No data received]"));
                if (!hasModData) {
                    sender.sendMessage("  Player hasn't sent mod list yet");
                } else if (modList.isEmpty()) {
                    sender.sendMessage("  No mods installed");
                } else {
                    int shown = 0;
                    for (String mod : modList) {
                        if (shown >= 20) {
                            sender.sendMessage("  ... and " + (modList.size() - shown) + " more");
                            break;
                        }
                        sender.sendMessage("  " + mod + (mod.contains("picraftmod") ? " [REQUIRED]" : ""));
                        shown++;
                    }
                }
                
                // Resource packs
                sender.sendMessage("Resource Packs (" + resourcePacks.size() + "):");
                if (resourcePacks.isEmpty()) {
                    sender.sendMessage("  No resource packs active");
                } else {
                    for (String pack : resourcePacks) {
                        sender.sendMessage("  " + pack);
                    }
                }
                
                sender.sendMessage("Server Access: " + (hasPiCraftMod ? "ALLOWED" : "WOULD BE KICKED"));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error checking mod status for " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}