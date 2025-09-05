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
                    "id INTEGER PRIMARY KEY," +
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
                    "assigned_to TEXT," +
                    "evidence TEXT," +
                    "auto_detected BOOLEAN NOT NULL DEFAULT 0" +
                    ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    public int createReport(Report report) throws SQLException {
        int nextId = getNextAvailableId();
        
        String sql = "INSERT INTO reports (id, created_at, reporter_uuid, reporter_name, anonymous, category, " +
                    "message, world, x, y, z, status, assigned_to, evidence, auto_detected) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, nextId);
            stmt.setString(2, report.getCreatedAt().toString());
            stmt.setString(3, report.getReporterUuid().toString());
            stmt.setString(4, report.getReporterName());
            stmt.setBoolean(5, report.isAnonymous());
            stmt.setString(6, report.getCategory());
            stmt.setString(7, report.getMessage());
            stmt.setString(8, report.getWorld());
            stmt.setInt(9, report.getX());
            stmt.setInt(10, report.getY());
            stmt.setInt(11, report.getZ());
            stmt.setString(12, report.getStatus().name());
            stmt.setString(13, report.getAssignedTo());
            stmt.setString(14, report.getEvidence());
            stmt.setBoolean(15, report.isAutoDetected());
            
            stmt.executeUpdate();
            report.setId(nextId);
            return nextId;
        }
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
        
        // If closing a report, renumber remaining open reports
        if (status == Report.Status.CLOSED) {
            renumberReports(id);
        }
    }
    
    private void renumberReports(int closedId) throws SQLException {
        // Get all open/read reports with ID > closedId
        String selectSql = "SELECT id FROM reports WHERE status IN ('OPEN', 'READ') AND id > ? ORDER BY id";
        
        try (PreparedStatement selectStmt = connection.prepareStatement(selectSql)) {
            selectStmt.setInt(1, closedId);
            
            try (ResultSet rs = selectStmt.executeQuery()) {
                int newId = closedId;
                
                while (rs.next()) {
                    int oldId = rs.getInt("id");
                    
                    // Update this report to the next available ID
                    String updateSql = "UPDATE reports SET id = ? WHERE id = ?";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, newId);
                        updateStmt.setInt(2, oldId);
                        updateStmt.executeUpdate();
                    }
                    
                    newId++;
                }
            }
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
    
    public void updateEvidence(int id, String evidence) throws SQLException {
        String sql = "UPDATE reports SET evidence = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, evidence);
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
        report.setEvidence(rs.getString("evidence"));
        report.setAutoDetected(rs.getBoolean("auto_detected"));
        return report;
    }
    
    public int getNextAvailableId() throws SQLException {
        String sql = "SELECT MIN(id + 1) as next_id FROM reports r1 WHERE NOT EXISTS (SELECT 1 FROM reports r2 WHERE r2.id = r1.id + 1 AND r2.status IN ('OPEN', 'READ'))";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int nextId = rs.getInt("next_id");
                return nextId > 0 ? nextId : 1;
            }
        }
        return 1;
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