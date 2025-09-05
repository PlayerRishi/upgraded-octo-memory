package org.pluginmakers.piCraftPlugin.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Report {
    public enum Status {
        OPEN, READ, CLOSED
    }
    
    private int id;
    private LocalDateTime createdAt;
    private UUID reporterUuid;
    private String reporterName;
    private boolean anonymous;
    private String category;
    private String message;
    private String world;
    private int x, y, z;
    private Status status;
    private String assignedTo;
    private String evidence;
    private boolean autoDetected;
    
    public Report() {
        this.createdAt = LocalDateTime.now();
        this.status = Status.OPEN;
        this.autoDetected = false;
    }
    
    public Report(UUID reporterUuid, String reporterName, boolean anonymous, String category, 
                  String message, String world, int x, int y, int z) {
        this();
        this.reporterUuid = reporterUuid;
        this.reporterName = reporterName;
        this.anonymous = anonymous;
        this.category = category;
        this.message = message;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public UUID getReporterUuid() { return reporterUuid; }
    public void setReporterUuid(UUID reporterUuid) { this.reporterUuid = reporterUuid; }
    
    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }
    
    public boolean isAnonymous() { return anonymous; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getWorld() { return world; }
    public void setWorld(String world) { this.world = world; }
    
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    
    public int getZ() { return z; }
    public void setZ(int z) { this.z = z; }
    
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    
    public String getEvidence() { return evidence; }
    public void setEvidence(String evidence) { this.evidence = evidence; }
    
    public boolean isAutoDetected() { return autoDetected; }
    public void setAutoDetected(boolean autoDetected) { this.autoDetected = autoDetected; }
    
    public String getDisplayReporter(boolean canViewRealName) {
        if (anonymous && !canViewRealName) {
            return "Anonymous";
        }
        return reporterName;
    }
}