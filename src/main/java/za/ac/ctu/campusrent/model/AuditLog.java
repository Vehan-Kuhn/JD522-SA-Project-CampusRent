package za.ac.ctu.campusrent.model;
import java.time.LocalDateTime;
/** Represents a AuditLog in CampusRent **/
public class AuditLog {
    private int logId;
    private Integer userId;
    private String action;
    private String entityType;
    private Integer entityId;
    private String details;
    private LocalDateTime createdAt;

    // Default constructor
    public AuditLog() {
        this.createdAt = LocalDateTime.now();
    }
    //Parameterized Constructor
    public AuditLog(int logId, int userId, String auction, String entityType, int entityId, String details, LocalDateTime createdAt) {
        this.logId = logId;
        this.userId = userId;
        this.action = auction;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
        this.createdAt = createdAt;
    }

    public int getLogId() {
        return logId;
    }
    public void setLogId(int logId) {
        this.logId = logId;
    }
    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public String getEntityType() {
        return entityType;
    }
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
    public Integer getEntityId() {
        return entityId;
    }
    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }
    public String getDetails() {
        return details;
    }
    public void setDetails(String details) {
        this.details = details;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    //Displays AuditLog information
    @Override
    public String toString() {
        return "AuditLog{" +
                "logId=" + logId +
                ", userId=" + userId +
                ", action='" + action + '\'' +
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                ", createdAt=" + createdAt +
                '}';
    }

}
