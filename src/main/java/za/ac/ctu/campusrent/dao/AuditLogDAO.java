package za.ac.ctu.campusrent.dao;
import za.ac.ctu.campusrent.model.AuditLog;
import za.ac.ctu.campusrent.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {

    // Add an audit log entry
    public int addAuditLog(AuditLog log) throws SQLException {

        String sql = """
                INSERT INTO audit_log
                (user_id, action, entity_type, entity_id, details)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            if (log.getUserId() != null) {
                statement.setInt(1, log.getUserId());
            } else {
                statement.setNull(1, Types.INTEGER);
            }

            statement.setString(2, log.getAction());
            statement.setString(3, log.getEntityType());

            if (log.getEntityId() != null) {
                statement.setInt(4, log.getEntityId());
            } else {
                statement.setNull(4, Types.INTEGER);
            }

            statement.setString(5, log.getDetails());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    log.setLogId(id);
                    return id;
                }
            }
        }

        throw new SQLException("Creating audit log failed: no ID returned.");
    }

    // Retrieve an audit log by ID
    public AuditLog getAuditLogById(int logId) throws SQLException {

        String sql = "SELECT * FROM audit_log WHERE log_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, logId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapAuditLog(resultSet);
                }
            }
        }

        return null;
    }

    // Retrieve all audit logs
    public List<AuditLog> getAllAuditLogs() throws SQLException {

        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_log ORDER BY created_at DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                logs.add(mapAuditLog(resultSet));
            }
        }

        return logs;
    }

    // Retrieve audit logs for a specific user
    public List<AuditLog> getAuditLogsByUserId(int userId) throws SQLException {

        List<AuditLog> logs = new ArrayList<>();

        String sql = """
                SELECT * FROM audit_log
                WHERE user_id = ?
                ORDER BY created_at DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    logs.add(mapAuditLog(resultSet));
                }
            }
        }

        return logs;
    }

    // Convert a database row into an AuditLog object
    private AuditLog mapAuditLog(ResultSet resultSet) throws SQLException {

        AuditLog log = new AuditLog();

        log.setLogId(resultSet.getInt("log_id"));

        int userId = resultSet.getInt("user_id");
        log.setUserId(resultSet.wasNull() ? null : userId);

        log.setAction(resultSet.getString("action"));
        log.setEntityType(resultSet.getString("entity_type"));

        int entityId = resultSet.getInt("entity_id");
        log.setEntityId(resultSet.wasNull() ? null : entityId);

        log.setDetails(resultSet.getString("details"));

        Timestamp timestamp = resultSet.getTimestamp("created_at");
        if (timestamp != null) {
            log.setCreatedAt(timestamp.toLocalDateTime());
        }

        return log;
    }
}
