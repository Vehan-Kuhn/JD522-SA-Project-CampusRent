package za.ac.ctu.campusrent.dao;

import za.ac.ctu.campusrent.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class TransactionRollbackDemo {

    private TransactionRollbackDemo() {
    }

    public static void demonstrate(int equipmentId)
            throws SQLException {

        if (equipmentId <= 0) {
            throw new IllegalArgumentException(
                    "Equipment ID must be positive.");
        }

        String sql = """
                UPDATE equipment
                SET description = ?
                WHERE equipment_id = ?
                """;

        try (Connection connection =
                     DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(
                        1, "TEMPORARY ROLLBACK DEMONSTRATION");

                statement.setInt(2, equipmentId);

                int affected = statement.executeUpdate();

                if (affected != 1) {
                    throw new SQLException(
                            "Equipment record was not found.");
                }

                connection.rollback();

                System.out.println(
                        "Rollback completed. Temporary change was not saved.");

            } catch (SQLException ex) {

                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    ex.addSuppressed(rollbackFailure);
                }

                throw ex;

            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
}