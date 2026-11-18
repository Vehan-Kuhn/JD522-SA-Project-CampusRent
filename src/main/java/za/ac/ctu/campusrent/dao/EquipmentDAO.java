package za.ac.ctu.campusrent.dao;
import za.ac.ctu.campusrent.model.Equipment;
import za.ac.ctu.campusrent.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipmentDAO {

    // Add equipment
    public int addEquipment(Equipment equipment) throws SQLException {
        String sql = """
                INSERT INTO equipment
                (campus_id, serial_number, equipment_name, description,
                 category, daily_rate, condition_status, availability_status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, equipment.getCampusId());
            statement.setString(2, equipment.getSerialNumber());
            statement.setString(3, equipment.getEquipmentName());
            statement.setString(4, equipment.getDescription());
            statement.setString(5, equipment.getCategory());
            statement.setDouble(6, equipment.getDailyRate());
            statement.setString(7, equipment.getConditionStatus());
            statement.setString(8, equipment.getAvailabilityStatus());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    equipment.setEquipmentId(id);
                    return id;
                }
            }
        }

        throw new SQLException("Adding equipment failed: no ID returned.");
    }

    // Retrieve equipment by ID
    public Equipment getEquipmentById(int equipmentId) throws SQLException {
        String sql = "SELECT * FROM equipment WHERE equipment_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, equipmentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapEquipment(resultSet);
                }
            }
        }

        return null;
    }

    // Retrieve all equipment
    public List<Equipment> getAllEquipment() throws SQLException {
        List<Equipment> equipmentList = new ArrayList<>();
        String sql = "SELECT * FROM equipment";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                equipmentList.add(mapEquipment(resultSet));
            }
        }
        return equipmentList;
    }

    // Update equipment
    public boolean updateEquipment(Equipment equipment) throws SQLException {
        String sql = """
                UPDATE equipment
                SET campus_id = ?, serial_number = ?, equipment_name = ?,
                    description = ?, category = ?, daily_rate = ?,
                    condition_status = ?, availability_status = ?
                WHERE equipment_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, equipment.getCampusId());
            statement.setString(2, equipment.getSerialNumber());
            statement.setString(3, equipment.getEquipmentName());
            statement.setString(4, equipment.getDescription());
            statement.setString(5, equipment.getCategory());
            statement.setDouble(6, equipment.getDailyRate());
            statement.setString(7, equipment.getConditionStatus());
            statement.setString(8, equipment.getAvailabilityStatus());
            statement.setInt(9, equipment.getEquipmentId());

            return statement.executeUpdate() > 0;
        }
    }

    // Delete equipment
    public boolean deleteEquipment(int equipmentId) throws SQLException {
        String sql = "DELETE FROM equipment WHERE equipment_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, equipmentId);
            return statement.executeUpdate() > 0;
        }
    }

    // Convert a database row into an Equipment object
    private Equipment mapEquipment(ResultSet resultSet) throws SQLException {

        Equipment equipment = new Equipment();
        equipment.setEquipmentId(resultSet.getInt("equipment_id"));
        equipment.setCampusId(resultSet.getInt("campus_id"));
        equipment.setSerialNumber(resultSet.getString("serial_number"));
        equipment.setEquipmentName(resultSet.getString("equipment_name"));
        equipment.setDescription(resultSet.getString("description"));
        equipment.setCategory(resultSet.getString("category"));
        equipment.setDailyRate(resultSet.getDouble("daily_rate"));
        equipment.setConditionStatus(resultSet.getString("condition_status"));
        equipment.setAvailabilityStatus(resultSet.getString("availability_status"));
        Timestamp timestamp = resultSet.getTimestamp("created_at");
        if (timestamp != null) {
            equipment.setCreatedAt(timestamp.toLocalDateTime());
        }
        return equipment;
    }
}
