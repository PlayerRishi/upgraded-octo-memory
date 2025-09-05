package org.pluginmakers.piCraftPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.model.Report;
import org.pluginmakers.piCraftPlugin.utils.ColorUtil;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class StaffCommands implements CommandExecutor {
    private final PiCraftPlugin plugin;
    private static final int REPORTS_PER_PAGE = 10;
    
    public StaffCommands(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
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
            sender.sendMessage(ColorUtil.colorize(
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
            
            sender.sendMessage(ColorUtil.colorize("&6=== Unread Reports (Page " + page + ") ==="));
            
            if (reports.isEmpty()) {
                sender.sendMessage(ColorUtil.colorize("&aNo unread reports!"));
                return true;
            }
            
            boolean canViewRealName = sender.hasPermission("picraft.report.view.realname");
            
            for (Report report : reports) {
                String status = report.getStatus() == Report.Status.OPEN ? "&cOPEN" : "&eREAD";
                String category = report.getCategory() != null ? "[" + report.getCategory() + "] " : "";
                String message = report.getMessage().length() > 50 ? 
                    report.getMessage().substring(0, 50) + "..." : report.getMessage();
                
                String displayText = String.format("&e#%d %s &f%sby %s &7- %s", 
                    report.getId(), status, category,
                    report.getDisplayReporter(canViewRealName), message);
                        
                sender.sendMessage(ColorUtil.colorize(displayText));
            }
            
            sender.sendMessage(ColorUtil.colorize("&7Use /reportview <id> for details"));
            
        } catch (Exception e) {
            sender.sendMessage(ColorUtil.colorize("&cError loading reports."));
            e.printStackTrace();
        }
        
        return true;
    }
    
    private boolean handleReportList(CommandSender sender, String[] args) {
        if (!sender.hasPermission("picraft.report.list")) {
            sender.sendMessage(ColorUtil.colorize(
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
                        sender.sendMessage(ColorUtil.colorize("&cUsage: /reportlist [all|open|closed|unread] [page]"));
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
            sender.sendMessage(ColorUtil.colorize("&6=== " + filterName + " Reports (Page " + page + ") ==="));
            
            if (reports.isEmpty()) {
                sender.sendMessage(ColorUtil.colorize("&aNo reports found!"));
                return true;
            }
            
            boolean canViewRealName = sender.hasPermission("picraft.report.view.realname");
            
            for (Report report : reports) {
                String statusColor = getStatusColorCode(report.getStatus());
                String category = report.getCategory() != null ? "[" + report.getCategory() + "] " : "";
                String message = report.getMessage().length() > 50 ? 
                    report.getMessage().substring(0, 50) + "..." : report.getMessage();
                
                String displayText = String.format("&e#%d %s%s &f%sby %s &7- %s", 
                    report.getId(), statusColor, report.getStatus().name(), category,
                    report.getDisplayReporter(canViewRealName), message);
                        
                sender.sendMessage(ColorUtil.colorize(displayText));
            }
            
        } catch (Exception e) {
            sender.sendMessage(ColorUtil.colorize("&cError loading reports."));
            e.printStackTrace();
        }
        
        return true;
    }
    
    private boolean handleReportView(CommandSender sender, String[] args) {
        if (!sender.hasPermission("picraft.report.view")) {
            sender.sendMessage(ColorUtil.colorize(
                plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no_permission")));
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(ColorUtil.colorize("&cUsage: /reportview <id>"));
            return true;
        }
        
        try {
            int id = Integer.parseInt(args[0]);
            Report report = plugin.getDatabaseManager().getReport(id);
            
            if (report == null) {
                sender.sendMessage(ColorUtil.colorize(
                    plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("not_found")));
                return true;
            }
            
            boolean canViewRealName = sender.hasPermission("picraft.report.view.realname");
            String statusColor = getStatusColorCode(report.getStatus());
            
            sender.sendMessage(ColorUtil.colorize("&6=== Report #" + report.getId() + " ==="));
            sender.sendMessage(ColorUtil.colorize("&eReporter: &f" + report.getDisplayReporter(canViewRealName)));
            sender.sendMessage(ColorUtil.colorize("&eCategory: &f" + (report.getCategory() != null ? report.getCategory() : "None")));
            sender.sendMessage(ColorUtil.colorize("&eStatus: " + statusColor + report.getStatus().name()));
            sender.sendMessage(ColorUtil.colorize("&eLocation: &f" + 
                report.getWorld() + " " + report.getX() + " " + report.getY() + " " + report.getZ()));
            sender.sendMessage(ColorUtil.colorize("&eTime: &f" + 
                report.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            if (report.getAssignedTo() != null) {
                sender.sendMessage(ColorUtil.colorize("&eAssigned to: &f" + report.getAssignedTo()));
            }
            sender.sendMessage(ColorUtil.colorize("&eMessage: &f" + report.getMessage()));
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ColorUtil.colorize("&cInvalid report ID."));
        } catch (Exception e) {
            sender.sendMessage(ColorUtil.colorize("&cError loading report."));
            e.printStackTrace();
        }
        
        return true;
    }
    
    private boolean handleReportRead(CommandSender sender, String[] args) {
        if (!sender.hasPermission("picraft.report.read")) {
            sender.sendMessage(ColorUtil.colorize(
                plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no_permission")));
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(ColorUtil.colorize("&cUsage: /reportread <id>"));
            return true;
        }
        
        try {
            int id = Integer.parseInt(args[0]);
            Report report = plugin.getDatabaseManager().getReport(id);
            
            if (report == null) {
                sender.sendMessage(ColorUtil.colorize(
                    plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("not_found")));
                return true;
            }
            
            plugin.getDatabaseManager().updateReportStatus(id, Report.Status.READ);
            
            String message = plugin.getConfigManager().getMessage("viewed").replace("{id}", String.valueOf(id));
            sender.sendMessage(ColorUtil.colorize(
                plugin.getConfigManager().getPrefix() + message));
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ColorUtil.colorize("&cInvalid report ID."));
        } catch (Exception e) {
            sender.sendMessage(ColorUtil.colorize("&cError updating report."));
            e.printStackTrace();
        }
        
        return true;
    }
    
    private boolean handleReportClear(CommandSender sender, String[] args) {
        if (!sender.hasPermission("picraft.report.clear")) {
            sender.sendMessage(ColorUtil.colorize(
                plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no_permission")));
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(ColorUtil.colorize("&cUsage: /reportclear <id>"));
            return true;
        }
        
        try {
            int id = Integer.parseInt(args[0]);
            Report report = plugin.getDatabaseManager().getReport(id);
            
            if (report == null) {
                sender.sendMessage(ColorUtil.colorize(
                    plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("not_found")));
                return true;
            }
            
            plugin.getDatabaseManager().updateReportStatus(id, Report.Status.CLOSED);
            
            String message = plugin.getConfigManager().getMessage("cleared").replace("{id}", String.valueOf(id));
            sender.sendMessage(ColorUtil.colorize(
                plugin.getConfigManager().getPrefix() + message));
            
            // Notify about renumbering
            sender.sendMessage(ColorUtil.colorize(
                plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("reports_renumbered")));
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ColorUtil.colorize("&cInvalid report ID."));
        } catch (Exception e) {
            sender.sendMessage(ColorUtil.colorize("&cError updating report."));
            e.printStackTrace();
        }
        
        return true;
    }
    
    private boolean handleReportAssign(CommandSender sender, String[] args) {
        if (!sender.hasPermission("picraft.report.assign")) {
            sender.sendMessage(ColorUtil.colorize(
                plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no_permission")));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.colorize("&cUsage: /reportassign <id> <staff>"));
            return true;
        }
        
        try {
            int id = Integer.parseInt(args[0]);
            String staffName = args[1];
            
            Report report = plugin.getDatabaseManager().getReport(id);
            
            if (report == null) {
                sender.sendMessage(ColorUtil.colorize(
                    plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("not_found")));
                return true;
            }
            
            plugin.getDatabaseManager().assignReport(id, staffName);
            
            String message = plugin.getConfigManager().getMessage("assigned")
                .replace("{id}", String.valueOf(id))
                .replace("{staff}", staffName);
            sender.sendMessage(ColorUtil.colorize(
                plugin.getConfigManager().getPrefix() + message));
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ColorUtil.colorize("&cInvalid report ID."));
        } catch (Exception e) {
            sender.sendMessage(ColorUtil.colorize("&cError assigning report."));
            e.printStackTrace();
        }
        
        return true;
    }
    
    private boolean handleReportNotify(CommandSender sender, String[] args) {
        if (!sender.hasPermission("picraft.report.notify")) {
            sender.sendMessage(ColorUtil.colorize(
                plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no_permission")));
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.colorize("&cOnly players can use this command."));
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
                    sender.sendMessage(ColorUtil.colorize("&cUsage: /reportnotify [on|off|toggle]"));
                    return true;
            }
        } else {
            plugin.getReportManager().setNotificationEnabled(player.getUniqueId(), !currentState);
        }
        
        boolean newState = plugin.getReportManager().isNotificationEnabled(player.getUniqueId());
        String message = newState ? 
            plugin.getConfigManager().getMessage("notify_on") :
            plugin.getConfigManager().getMessage("notify_off");
        
        sender.sendMessage(ColorUtil.colorize(
            plugin.getConfigManager().getPrefix() + message));
        
        return true;
    }
    
    private boolean handleReportTp(CommandSender sender, String[] args) {
        if (!sender.hasPermission("picraft.report.tp")) {
            sender.sendMessage(ColorUtil.colorize(
                plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no_permission")));
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.colorize("&cOnly players can use this command."));
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(ColorUtil.colorize("&cUsage: /reporttp <id>"));
            return true;
        }
        
        try {
            int id = Integer.parseInt(args[0]);
            Report report = plugin.getDatabaseManager().getReport(id);
            
            if (report == null) {
                sender.sendMessage(ColorUtil.colorize(
                    plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("not_found")));
                return true;
            }
            
            Player player = (Player) sender;
            World world = Bukkit.getWorld(report.getWorld());
            
            
            if (world == null) {
                sender.sendMessage(ColorUtil.colorize("&cWorld not found: " + report.getWorld()));
                return true;
            }
            
            Location location = new Location(world, report.getX(), report.getY(), report.getZ());
            player.teleport(location);
            
            String message = plugin.getConfigManager().getMessage("teleported").replace("{id}", String.valueOf(id));
            sender.sendMessage(ColorUtil.colorize(
                plugin.getConfigManager().getPrefix() + message));
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ColorUtil.colorize("&cInvalid report ID."));
        } catch (Exception e) {
            sender.sendMessage(ColorUtil.colorize("&cError teleporting to report location."));
            e.printStackTrace();
        }
        
        return true;
    }
    
    private String getStatusColorCode(Report.Status status) {
        switch (status) {
            case OPEN:
                return "&c";
            case READ:
                return "&e";
            case CLOSED:
                return "&a";
            default:
                return "&f";
        }
    }
}