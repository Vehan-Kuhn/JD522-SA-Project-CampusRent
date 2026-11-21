package za.ac.ctu.campusrent.dao;

import za.ac.ctu.campusrent.util.DBConnection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class DatabaseMetadataDemo {

    private DatabaseMetadataDemo() {
    }

    public static void printDatabaseDetails()
            throws SQLException {

        try (Connection connection =
                     DBConnection.getConnection()) {

            DatabaseMetaData metadata =
                    connection.getMetaData();

            System.out.println(
                    "Database: "
                            + metadata.getDatabaseProductName());

            System.out.println(
                    "Database version: "
                            + metadata.getDatabaseProductVersion());

            System.out.println(
                    "JDBC driver: "
                            + metadata.getDriverName());

            try (ResultSet tables = metadata.getTables(
                    connection.getCatalog(),
                    null,
                    "%",
                    new String[]{"TABLE"})) {

                while (tables.next()) {
                    System.out.println(
                            "Table: "
                                    + tables.getString("TABLE_NAME"));
                }
            }
        }
    }
}