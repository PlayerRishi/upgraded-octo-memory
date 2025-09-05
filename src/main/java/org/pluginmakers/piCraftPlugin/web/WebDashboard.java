package org.pluginmakers.piCraftPlugin.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.model.Report;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;

public class WebDashboard {
    private final PiCraftPlugin plugin;
    private HttpServer server;
    
    public WebDashboard(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void start() {
        if (!plugin.getConfigManager().isWebDashboardEnabled()) {
            return;
        }
        
        try {
            int port = plugin.getConfigManager().getWebDashboardPort();
            server = HttpServer.create(new InetSocketAddress(port), 0);
            
            server.createContext("/", new DashboardHandler());
            server.createContext("/api/reports", new ReportsApiHandler());
            
            server.start();
            plugin.getLogger().info("Web dashboard started on port " + port);
            
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to start web dashboard: " + e.getMessage());
        }
    }
    
    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
    
    private class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = generateDashboardHtml();
            
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, html.length());
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(html.getBytes());
            }
        }
    }
    
    private class ReportsApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                List<Report> reports = plugin.getDatabaseManager().getReports(null, 50, 0);
                String json = generateReportsJson(reports);
                
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, json.length());
                
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(json.getBytes());
                }
            } catch (Exception e) {
                String error = "{\"error\":\"" + e.getMessage() + "\"}";
                exchange.sendResponseHeaders(500, error.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(error.getBytes());
                }
            }
        }
    }
    
    private String generateDashboardHtml() {
        return "<!DOCTYPE html><html><head><title>PiCraft Reports</title>" +
               "<style>body{font-family:Arial;margin:20px;}table{width:100%;border-collapse:collapse;}" +
               "th,td{border:1px solid #ddd;padding:8px;text-align:left;}th{background:#f2f2f2;}</style>" +
               "</head><body><h1>PiCraft Reports Dashboard</h1>" +
               "<div id='reports'>Loading...</div>" +
               "<script>fetch('/api/reports').then(r=>r.json()).then(data=>{" +
               "let html='<table><tr><th>ID</th><th>Reporter</th><th>Category</th><th>Status</th><th>Message</th></tr>';" +
               "data.forEach(r=>html+=`<tr><td>${r.id}</td><td>${r.reporter}</td><td>${r.category}</td><td>${r.status}</td><td>${r.message}</td></tr>`);" +
               "html+='</table>';document.getElementById('reports').innerHTML=html;});</script>" +
               "</body></html>";
    }
    
    private String generateReportsJson(List<Report> reports) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < reports.size(); i++) {
            Report r = reports.get(i);
            if (i > 0) json.append(",");
            json.append("{")
                .append("\"id\":").append(r.getId()).append(",")
                .append("\"reporter\":\"").append(r.getReporterName()).append("\",")
                .append("\"category\":\"").append(r.getCategory() != null ? r.getCategory() : "").append("\",")
                .append("\"status\":\"").append(r.getStatus()).append("\",")
                .append("\"message\":\"").append(r.getMessage().replace("\"", "\\\"")).append("\"")
                .append("}");
        }
        json.append("]");
        return json.toString();
    }
}