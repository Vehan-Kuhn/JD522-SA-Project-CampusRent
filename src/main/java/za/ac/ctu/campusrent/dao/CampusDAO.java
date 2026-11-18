
package za.ac.ctu.campusrent.dao;

import za.ac.ctu.campusrent.model.Campus;
import za.ac.ctu.campusrent.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CampusDAO {

    // Add a campus
    public int addCampus(Campus campus) throws SQLException {

        String sql = """
                INSERT INTO campus
                (campus_name, city, province, address, phone, email)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, campus.getCampusName());
            statement.setString(2, campus.getCity());
            statement.setString(3, campus.getProvince());
            statement.setString(4, campus.getAddress());
            statement.setString(5, campus.getPhone());
            statement.setString(6, campus.getEmail());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    campus.setCampusId(id);
                    return id;
                }
            }
        }

        throw new SQLException("Creating campus failed: no ID returned.");
    }

    // Retrieve a campus by ID
    public Campus getCampusById(int campusId) throws SQLException {

        String sql = "SELECT * FROM campus WHERE campus_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, campusId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapCampus(resultSet);
                }
            }
        }

        return null;
    }

    // Retrieve all campuses
    public List<Campus> getAllCampuses() throws SQLException {

        List<Campus> campuses = new ArrayList<>();
        String sql = "SELECT * FROM campus";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                campuses.add(mapCampus(resultSet));
            }
        }

        return campuses;
    }

    // Update a campus
    public boolean updateCampus(Campus campus) throws SQLException {

        String sql = """
                UPDATE campus
                SET campus_name = ?, city = ?, province = ?,
                    address = ?, phone = ?, email = ?
                WHERE campus_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, campus.getCampusName());
            statement.setString(2, campus.getCity());
            statement.setString(3, campus.getProvince());
            statement.setString(4, campus.getAddress());
            statement.setString(5, campus.getPhone());
            statement.setString(6, campus.getEmail());
            statement.setInt(7, campus.getCampusId());

            return statement.executeUpdate() > 0;
        }
    }

    // Delete a campus
    public boolean deleteCampus(int campusId) throws SQLException {

        String sql = "DELETE FROM campus WHERE campus_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, campusId);
            return statement.executeUpdate() > 0;
        }
    }

    // Convert a database row into a Campus object
    private Campus mapCampus(ResultSet resultSet) throws SQLException {

        Campus campus = new Campus();

        campus.setCampusId(resultSet.getInt("campus_id"));
        campus.setCampusName(resultSet.getString("campus_name"));
        campus.setCity(resultSet.getString("city"));
        campus.setProvince(resultSet.getString("province"));
        campus.setAddress(resultSet.getString("address"));
        campus.setPhone(resultSet.getString("phone"));
        campus.setEmail(resultSet.getString("email"));

        return campus;
    }
}
