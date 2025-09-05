package org.pluginmakers.piCraftPlugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.model.Report;
import org.pluginmakers.piCraftPlugin.utils.ColorUtil;

public class EvidenceCommand implements CommandExecutor {
    private final PiCraftPlugin plugin;
    
    public EvidenceCommand(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("picraft.report.evidence")) {
            sender.sendMessage(ColorUtil.colorize("&cNo permission."));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.colorize("&cUsage: /evidence <report_id> <url>"));
            return true;
        }
        
        try {
            int id = Integer.parseInt(args[0]);
            String url = args[1];
            
            Report report = plugin.getDatabaseManager().getReport(id);
            if (report == null) {
                sender.sendMessage(ColorUtil.colorize("&cReport not found."));
                return true;
            }
            
            String currentEvidence = report.getEvidence();
            String newEvidence = currentEvidence == null ? url : currentEvidence + ";" + url;
            
            plugin.getDatabaseManager().updateEvidence(id, newEvidence);
            sender.sendMessage(ColorUtil.colorize("&aEvidence added to report #" + id));
            
        } catch (Exception e) {
            sender.sendMessage(ColorUtil.colorize("&cError: " + e.getMessage()));
        }
        
        return true;
    }
}