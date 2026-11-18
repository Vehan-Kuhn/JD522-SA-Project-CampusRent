
package za.ac.ctu.campusrent.service;
import za.ac.ctu.campusrent.dao.AuditLogDAO;
import za.ac.ctu.campusrent.model.AuditLog;
import java.sql.SQLException;
import java.util.List;

/** Handles audit log business logic. **/
public class AuditLogService {

    private final AuditLogDAO auditLogDAO;

    // Default constructor
    public AuditLogService() {
        this.auditLogDAO = new AuditLogDAO();
    }

    // Add a new audit log entry
    public int addAuditLog(AuditLog log) throws SQLException {

        validateAuditLog(log);

        return auditLogDAO.addAuditLog(log);
    }

    // Retrieve an audit log by ID
    public AuditLog getAuditLogById(int logId) throws SQLException {

        if (logId <= 0) {
            throw new IllegalArgumentException("Invalid audit log ID.");
        }

        return auditLogDAO.getAuditLogById(logId);
    }

    // Retrieve all audit logs
    public List<AuditLog> getAllAuditLogs() throws SQLException {

        return auditLogDAO.getAllAuditLogs();
    }

    // Retrieve audit logs belonging to a specific user
    public List<AuditLog> getAuditLogsByUserId(int userId)
            throws SQLException {

        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID.");
        }

        return auditLogDAO.getAuditLogsByUserId(userId);
    }

    // Validate audit log information
    private void validateAuditLog(AuditLog log) {

        if (log == null) {
            throw new IllegalArgumentException(
                    "Audit log cannot be empty."
            );
        }

        if (log.getAction() == null || log.getAction().isBlank()) {
            throw new IllegalArgumentException(
                    "Audit action is required."
            );
        }

        if (log.getEntityType() == null
                || log.getEntityType().isBlank()) {
            throw new IllegalArgumentException(
                    "Entity type is required."
            );
        }

        if (log.getAction().length() > 100) {
            throw new IllegalArgumentException(
                    "Action cannot exceed 100 characters."
            );
        }

        if (log.getEntityType().length() > 50) {
            throw new IllegalArgumentException(
                    "Entity type cannot exceed 50 characters."
            );
        }
    }
}
