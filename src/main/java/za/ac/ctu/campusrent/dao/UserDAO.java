package za.ac.ctu.campusrent.dao;
import za.ac.ctu.campusrent.model.User;
import za.ac.ctu.campusrent.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // Add a user.
    public int addUser(User user) throws SQLException {
        String sql = """
                INSERT INTO app_user
                (campus_id, first_name, last_name, email, phone,
                 password_hash, user_role, account_status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, user.getCampusId());
            statement.setString(2, user.getFirstName());
            statement.setString(3, user.getLastName());
            statement.setString(4, user.getEmail());
            statement.setString(5, user.getPhone());
            statement.setString(6, user.getPasswordHash());
            statement.setString(7, user.getUserRole());
            statement.setString(8, user.getAccountStatus());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    user.setUserId(id);
                    return id;
                }
            }
        }

        throw new SQLException("Creating user failed: no ID returned.");
    }

    // Retrieve a user by ID.
    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT * FROM app_user WHERE user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapUser(resultSet);
                }
            }
        }

        return null;
    }

    // Retrieve a user by email.
    public User getUserByEmail(String email) throws SQLException {
        String sql = """
                SELECT * FROM app_user
                WHERE LOWER(email) = LOWER(?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapUser(resultSet);
                }
            }
        }

        return null;
    }

    // Retrieve all users.
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM app_user";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
        }

        return users;
    }

    // Update a user.
    public boolean updateUser(User user) throws SQLException {
        String sql = """
                UPDATE app_user
                SET campus_id = ?, first_name = ?, last_name = ?,
                    email = ?, phone = ?, password_hash = ?,
                    user_role = ?, account_status = ?
                WHERE user_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, user.getCampusId());
            statement.setString(2, user.getFirstName());
            statement.setString(3, user.getLastName());
            statement.setString(4, user.getEmail());
            statement.setString(5, user.getPhone());
            statement.setString(6, user.getPasswordHash());
            statement.setString(7, user.getUserRole());
            statement.setString(8, user.getAccountStatus());
            statement.setInt(9, user.getUserId());

            return statement.executeUpdate() > 0;
        }
    }

    // Delete a user.
    public boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM app_user WHERE user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            return statement.executeUpdate() > 0;
        }
    }

    // Convert a database row into a User object.
    private User mapUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setUserId(resultSet.getInt("user_id"));
        user.setCampusId(resultSet.getInt("campus_id"));
        user.setFirstName(resultSet.getString("first_name"));
        user.setLastName(resultSet.getString("last_name"));
        user.setEmail(resultSet.getString("email"));
        user.setPhone(resultSet.getString("phone"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setUserRole(resultSet.getString("user_role"));
        user.setAccountStatus(resultSet.getString("account_status"));

        Timestamp timestamp = resultSet.getTimestamp("created_at");
        if (timestamp != null) {
            user.setCreatedAt(timestamp.toLocalDateTime());
        }

        return user;
    }
}
