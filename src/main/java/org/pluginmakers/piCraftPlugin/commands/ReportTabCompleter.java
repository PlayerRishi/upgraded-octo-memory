package org.pluginmakers.piCraftPlugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;

import java.util.ArrayList;
import java.util.List;

public class ReportTabCompleter implements TabCompleter {
    private final PiCraftPlugin plugin;
    
    public ReportTabCompleter(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument: -anon or category
            completions.add("-anon");
            completions.addAll(plugin.getConfigManager().getReportCategories());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("-anon")) {
            // Second argument after -anon: category
            completions.addAll(plugin.getConfigManager().getReportCategories());
        }
        
        // Filter completions based on what user has typed
        String partial = args[args.length - 1].toLowerCase();
        completions.removeIf(completion -> !completion.toLowerCase().startsWith(partial));
        
        return completions;
    }
}