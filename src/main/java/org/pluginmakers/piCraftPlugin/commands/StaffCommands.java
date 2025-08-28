package org.pluginmakers.piCraftPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.model.Report;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class StaffCommands implements CommandExecutor {
    private final PiCraftPlugin plugin;
    private static final int REPORTS_PER_PAGE = 10;
    
    public StaffCommands(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();
        
        switch (cmd) {
            case "reports":
                return handleReports(sender, args);
            case "reportlist":
                return handleReportList(sender, args);
            case "reportview":
                return handleReportView(sender, args);
            case "reportread":
                return handleReportRead(sender, args);
            case "reportclear":
            case "reportclose":
                return handleReportClear(sender, args);
            case "reportassign":
                return handleReportAssign(sender, args);
            case "reportnotify":
                return handleReportNotify(sender, args);
            case "reporttp":
                return handleReportTp(sender, args);
        }
        
        return false;
    }
    
    private boolean handleReports(CommandSender sender, String[] args) {
        if (!sender.hasPermission("picraft.report.view")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no_permission")));
            return true;
        }
        
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                page = 1;
            }
        }
        
        try {
            int offset = (page - 1) * REPORTS_PER_PAGE;
            List<Report> reports = plugin.getDatabaseManager().getUnreadReports(REPORTS_PER_PAGE, offset);
            
            sender.sendMessage(ChatColor.GOLD + "=== Unread Reports (Page " + page + ") ===");
            
            if (reports.isEmpty()) {
                sender.sendMessage(ChatColor.GREEN + "No unread reports!");
                return true;
            }
            
            boolean canViewRealName = sender.hasPermission("picraft.report.view.realname");
            
            for (Report report : reports) {
                String status = report.getStatus() == Report.Status.OPEN ? 
                    ChatColor.RED + "OPEN" : ChatColor.YELLOW + "READ";
                
                sender.sendMessage(String.format("%s#%d %s[%s] %sby %s %s- %s", 
                    ChatColor.YELLOW, report.getId(),
                    status, ChatColor.WHITE,
                    report.getCategory() != null ? "[" + report.getCategory() + "] " : "",
                    report.getDisplayReporter(canViewRealName),
                    ChatColor.GRAY,
                    report.getMessage().length() > 50 ? 
                        report.getMessage().substring(0, 50) + "..." : report.getMessage()));
            }
            
            sender.sendMessage(ChatColor.GRAY + "Use /reportview <id> for details");
            
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Error loading reports.");
            e.printStackTrace();
        }
        
        return true;
    }
    
    private boolean handleReportList(CommandSender sender, String[] args) {
        if (!sender.hasPermission("picraft.report.list")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no_permission")));
            return true;
        }
        
        Report.Status filter = null;
        int page = 1;
        
        if (args.length > 0) {
            String filterArg = args[0].toLowerCase();
            switch (filterArg) {
                case "open":
                    filter = Report.Status.OPEN;
                    break;
                case "read":
                    filter = Report.Status.READ;
                    break;
                case "closed":
                    filter = Report.Status.CLOSED;
                    break;
                case "unread":
                    filter = Report.Status.OPEN;
                    break;
                case "all":
                    filter = null;
                    break;
                default:
                    try {
                        page = Integer.parseInt(filterArg);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Usage: /reportlist [all|open|closed|unread] [page]");
                        return true;
                    }
            }
        }
        
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                page = 1;
            }
        }
        
        try {
            int offset = (page - 1) * REPORTS_PER_PAGE;
            List<Report> reports = plugin.getDatabaseManager().getReports(filter, REPORTS_PER_PAGE, offset);
            
            String filterName = filter == null ? "All" : filter.name();
            sender.sendMessage(ChatColor.GOLD + "=== " + filterName + " Reports (Page " + page + ") ===");
            
            if (reports.isEmpty()) {
                sender.sendMessage(ChatColor.GREEN + "No reports found!");
                return true;
            }
            
            boolean canViewRealName = sender.hasPermission("picraft.report.view.realname");
            
            for (Report report : reports) {
                String status = getStatusColor(report.getStatus()) + report.getStatus().name();
                
                sender.sendMessage(String.format("%s#%d %s[%s] %sby %s %s- %s", 
                    ChatColor.YELLOW, report.getId(),
                    status, ChatColor.WHITE,
                    report.getCategory() != null ? "[" + report.getCategory() + "] " : "",
                    report.getDisplayReporter(canViewRealName),
                    ChatColor.GRAY,
                    report.getMessage().length() > 50 ? 
                        report.getMessage().substring(0, 50) + "..." : report.getMessage()));
            }
            
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Error loading reports.");
            e.printStackTrace();
        }
        
        return true;
    }
    
    private boolean handleReportView(CommandSender sender, String[] args) {
        if (!sender.hasPermission("picraft.report.view")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no_permission")));
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /reportview <id>");
            return true;
        }
        
        try {
            int id = Integer.parseInt(args[0]);
            Report report = plugin.getDatabaseManager().getReport(id);
            
            if (report == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("not_found")));
                return true;
            }
            
            boolean canViewRealName = sender.hasPermission("picraft.report.view.realname");
            
            sender.sendMessage(ChatColor.GOLD + "=== Report #" + report.getId() + " ===");
            sender.sendMessage(ChatColor.YELLOW + "Reporter: " + ChatColor.WHITE + report.getDisplayReporter(canViewRealName));
            sender.sendMessage(ChatColor.YELLOW + "Category: " + ChatColor.WHITE + (report.getCategory() != null ? report.getCategory() : "None"));
            sender.sendMessage(ChatColor.YELLOW + "Status: " + getStatusColor(report.getStatus()) + report.getStatus().name());
            sender.sendMessage(ChatColor.YELLOW + "Location: " + ChatColor.WHITE + 
                report.getWorld() + " " + report.getX() + " " + report.getY() + " " + report.getZ());
            sender.sendMessage(ChatColor.YELLOW + "Time: " + ChatColor.WHITE + 
                report.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            if (report.getAssignedTo() != null) {
                sender.sendMessage(ChatColor.YELLOW + "Assigned to: " + ChatColor.WHITE + report.getAssignedTo());
            }
            sender.sendMessage(ChatColor.YELLOW + "Message: " + ChatColor.WHITE + report.getMessage());
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid report ID.");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Error loading report.");
            e.printStackTrace();
        }
        
        return true;
    }
    
    private boolean handleReportRead(CommandSender sender, String[] args) {
        if (!sender.hasPermission("picraft.report.read")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no_permission")));
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /reportread <id>");
            return true;
        }
        
        try {
            int id = Integer.parseInt(args[0]);
            Report report = plugin.getDatabaseManager().getReport(id);
            
            if (report == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("not_found")));
                return true;
            }
            
            plugin.getDatabaseManager().updateReportStatus(id, Report.Status.READ);
            
            String message = plugin.getConfigManager().getMessage("viewed").replace("{id}", String.valueOf(id));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getPrefix() + message));
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid report ID.");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Error updating report.");
            e.printStackTrace();
        }
        
        return true;
    }
    
    private boolean handleReportClear(CommandSender sender, String[] args) {
        if (!sender.hasPermission("picraft.report.clear")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no_permission")));
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /reportclear <id>");
            return true;
        }
        
        try {
            int id = Integer.parseInt(args[0]);
            Report report = plugin.getDatabaseManager().getReport(id);
            
            if (report == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("not_found")));
                return true;
            }
            
            plugin.getDatabaseManager().updateReportStatus(id, Report.Status.CLOSED);
            
            String message = plugin.getConfigManager().getMessage("cleared").replace("{id}", String.valueOf(id));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getPrefix() + message));
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid report ID.");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Error updating report.");
            e.printStackTrace();
        }
        
        return true;
    }
    
    private boolean handleReportAssign(CommandSender sender, String[] args) {
        if (!sender.hasPermission("picraft.report.assign")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no_permission")));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /reportassign <id> <staff>");
            return true;
        }
        
        try {
            int id = Integer.parseInt(args[0]);
            String staffName = args[1];
            
            Report report = plugin.getDatabaseManager().getReport(id);
            
            if (report == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("not_found")));
                return true;
            }
            
            plugin.getDatabaseManager().assignReport(id, staffName);
            
            String message = plugin.getConfigManager().getMessage("assigned")
                .replace("{id}", String.valueOf(id))
                .replace("{staff}", staffName);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getPrefix() + message));
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid report ID.");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Error assigning report.");
            e.printStackTrace();
        }
        
        return true;
    }
    
    private boolean handleReportNotify(CommandSender sender, String[] args) {
        if (!sender.hasPermission("picraft.report.notify")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no_permission")));
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        
        Player player = (Player) sender;
        boolean currentState = plugin.getReportManager().isNotificationEnabled(player.getUniqueId());
        
        if (args.length > 0) {
            String arg = args[0].toLowerCase();
            switch (arg) {
                case "on":
                    plugin.getReportManager().setNotificationEnabled(player.getUniqueId(), true);
                    break;
                case "off":
                    plugin.getReportManager().setNotificationEnabled(player.getUniqueId(), false);
                    break;
                case "toggle":
                    plugin.getReportManager().setNotificationEnabled(player.getUniqueId(), !currentState);
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "Usage: /reportnotify [on|off|toggle]");
                    return true;
            }
        } else {
            plugin.getReportManager().setNotificationEnabled(player.getUniqueId(), !currentState);
        }
        
        boolean newState = plugin.getReportManager().isNotificationEnabled(player.getUniqueId());
        String message = newState ? 
            plugin.getConfigManager().getMessage("notify_on") :
            plugin.getConfigManager().getMessage("notify_off");
        
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
            plugin.getConfigManager().getPrefix() + message));
        
        return true;
    }
    
    private boolean handleReportTp(CommandSender sender, String[] args) {
        if (!sender.hasPermission("picraft.report.tp")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no_permission")));
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /reporttp <id>");
            return true;
        }
        
        try {
            int id = Integer.parseInt(args[0]);
            Report report = plugin.getDatabaseManager().getReport(id);
            
            if (report == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("not_found")));
                return true;
            }
            
            Player player = (Player) sender;
            World world = Bukkit.getWorld(report.getWorld());
            
            if (world == null) {
                sender.sendMessage(ChatColor.RED + "World not found: " + report.getWorld());
                return true;
            }
            
            Location location = new Location(world, report.getX(), report.getY(), report.getZ());
            player.teleport(location);
            
            String message = plugin.getConfigManager().getMessage("teleported").replace("{id}", String.valueOf(id));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getPrefix() + message));
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid report ID.");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Error teleporting to report location.");
            e.printStackTrace();
        }
        
        return true;
    }
    
    private ChatColor getStatusColor(Report.Status status) {
        switch (status) {
            case OPEN:
                return ChatColor.RED;
            case READ:
                return ChatColor.YELLOW;
            case CLOSED:
                return ChatColor.GREEN;
            default:
                return ChatColor.WHITE;
        }
    }
}