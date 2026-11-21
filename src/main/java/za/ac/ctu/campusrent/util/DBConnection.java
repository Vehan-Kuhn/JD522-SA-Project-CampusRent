package za.ac.ctu.campusrent.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConnection {

    private static final String DB_URL_VARIABLE =
            "CAMPUSRENT_DB_URL";

    private static final String DB_USER_VARIABLE =
            "CAMPUSRENT_DB_USER";

    private static final String DB_PASSWORD_VARIABLE =
            "CAMPUSRENT_DB_PASSWORD";

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {

        Properties properties = loadProperties();

        String url = firstNonBlank(
                System.getenv(DB_URL_VARIABLE),
                properties.getProperty("db.url"));

        String username = firstNonBlank(
                System.getenv(DB_USER_VARIABLE),
                properties.getProperty("db.username"));

        String password = System.getenv(DB_PASSWORD_VARIABLE);

        if (password == null) {
            password = properties.getProperty("db.password", "");
        }

        if (url == null || username == null || password == null
                || url.isBlank() || username.isBlank()) {
            throw new IllegalStateException(
                    "Configure db.url, db.username and db.password "
                            + "in db.properties or set the CampusRent "
                            + "database environment variables.");
        }

        return DriverManager.getConnection(
                url, username, password);
    }

    private static Properties loadProperties()
            throws SQLException {

        Properties properties = new Properties();

        try (InputStream input =
                     DBConnection.class.getClassLoader()
                             .getResourceAsStream("db.properties")) {

            if (input != null) {
                properties.load(input);
            }

        } catch (IOException ex) {
            throw new SQLException(
                    "Could not read db.properties.", ex);
        }

        return properties;
    }

    private static String firstNonBlank(
            String preferred, String fallback) {

        return preferred != null && !preferred.isBlank()
                ? preferred
                : fallback;
    }
}