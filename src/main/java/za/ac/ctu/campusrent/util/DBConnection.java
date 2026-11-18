package za.ac.ctu.campusrent.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Handles database connectivity for CampusRent. **/
public class DBConnection {

    // Database environment variable names
    private static final String DB_URL_VARIABLE = "CAMPUSRENT_DB_URL";
    private static final String DB_USER_VARIABLE = "CAMPUSRENT_DB_USER";
    private static final String DB_PASSWORD_VARIABLE = "CAMPUSRENT_DB_PASSWORD";

    /**
     * Establishes and returns a connection to the MySQL database.
     * @return an active database connection
     * @throws SQLException if the connection cannot be established
     **/
    public static Connection getConnection() throws SQLException {

        // Retrieve database credentials
        String url = System.getenv(DB_URL_VARIABLE);
        String username = System.getenv(DB_USER_VARIABLE);
        String password = System.getenv(DB_PASSWORD_VARIABLE);

        // Check whether environment variables are configured
        if (url == null || url.isBlank()
                || username == null || username.isBlank()
                || password == null) {

            throw new IllegalStateException(
                    "Database environment variables are not configured."
            );
        }

        // Establish the MySQL connection
        return DriverManager.getConnection(url, username, password);
    }
}
