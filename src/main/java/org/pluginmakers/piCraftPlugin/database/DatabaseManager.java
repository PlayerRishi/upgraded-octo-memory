package org.pluginmakers.piCraftPlugin.database;

import org.pluginmakers.piCraftPlugin.model.Report;
import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {
    private Connection connection;
    private final File dataFolder;
    
    public DatabaseManager(File dataFolder) {
        this.dataFolder = dataFolder;
    }
    
    public void initialize() throws SQLException {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        String url = "jdbc:sqlite:" + new File(dataFolder, "reports.db").getAbsolutePath();
        connection = DriverManager.getConnection(url);
        createTables();
    }
    
    private void createTables() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS reports (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "created_at TEXT NOT NULL," +
                    "reporter_uuid TEXT NOT NULL," +
                    "reporter_name TEXT NOT NULL," +
                    "anonymous BOOLEAN NOT NULL," +
                    "category TEXT," +
                    "message TEXT NOT NULL," +
                    "world TEXT NOT NULL," +
                    "x INTEGER NOT NULL," +
                    "y INTEGER NOT NULL," +
                    "z INTEGER NOT NULL," +
                    "status TEXT NOT NULL DEFAULT 'OPEN'," +
                    "assigned_to TEXT" +
                    ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    public int createReport(Report report) throws SQLException {
        String sql = "INSERT INTO reports (created_at, reporter_uuid, reporter_name, anonymous, category, " +
                    "message, world, x, y, z, status, assigned_to) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, report.getCreatedAt().toString());
            stmt.setString(2, report.getReporterUuid().toString());
            stmt.setString(3, report.getReporterName());
            stmt.setBoolean(4, report.isAnonymous());
            stmt.setString(5, report.getCategory());
            stmt.setString(6, report.getMessage());
            stmt.setString(7, report.getWorld());
            stmt.setInt(8, report.getX());
            stmt.setInt(9, report.getY());
            stmt.setInt(10, report.getZ());
            stmt.setString(11, report.getStatus().name());
            stmt.setString(12, report.getAssignedTo());
            
            stmt.executeUpdate();
            
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    report.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }
    
    public Report getReport(int id) throws SQLException {
        String sql = "SELECT * FROM reports WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReport(rs);
                }
            }
        }
        return null;
    }
    
    public List<Report> getReports(Report.Status status, int limit, int offset) throws SQLException {
        String sql = status == null ? 
            "SELECT * FROM reports ORDER BY created_at DESC LIMIT ? OFFSET ?" :
            "SELECT * FROM reports WHERE status = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if (status == null) {
                stmt.setInt(1, limit);
                stmt.setInt(2, offset);
            } else {
                stmt.setString(1, status.name());
                stmt.setInt(2, limit);
                stmt.setInt(3, offset);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                List<Report> reports = new ArrayList<>();
                while (rs.next()) {
                    reports.add(mapResultSetToReport(rs));
                }
                return reports;
            }
        }
    }
    
    public List<Report> getUnreadReports(int limit, int offset) throws SQLException {
        String sql = "SELECT * FROM reports WHERE status IN ('OPEN') ORDER BY created_at DESC LIMIT ? OFFSET ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            
            try (ResultSet rs = stmt.executeQuery()) {
                List<Report> reports = new ArrayList<>();
                while (rs.next()) {
                    reports.add(mapResultSetToReport(rs));
                }
                return reports;
            }
        }
    }
    
    public void updateReportStatus(int id, Report.Status status) throws SQLException {
        String sql = "UPDATE reports SET status = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }
    
    public void assignReport(int id, String staffName) throws SQLException {
        String sql = "UPDATE reports SET assigned_to = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, staffName);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }
    
    public int getUnreadCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM reports WHERE status = 'OPEN'";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    private Report mapResultSetToReport(ResultSet rs) throws SQLException {
        Report report = new Report();
        report.setId(rs.getInt("id"));
        report.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
        report.setReporterUuid(UUID.fromString(rs.getString("reporter_uuid")));
        report.setReporterName(rs.getString("reporter_name"));
        report.setAnonymous(rs.getBoolean("anonymous"));
        report.setCategory(rs.getString("category"));
        report.setMessage(rs.getString("message"));
        report.setWorld(rs.getString("world"));
        report.setX(rs.getInt("x"));
        report.setY(rs.getInt("y"));
        report.setZ(rs.getInt("z"));
        report.setStatus(Report.Status.valueOf(rs.getString("status")));
        report.setAssignedTo(rs.getString("assigned_to"));
        return report;
    }
    
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}