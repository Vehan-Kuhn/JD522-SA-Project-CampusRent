
package za.ac.ctu.campusrent.dao;

import za.ac.ctu.campusrent.model.Booking;
import za.ac.ctu.campusrent.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    // Add a booking
    public int addBooking(Booking booking) throws SQLException {

        String sql = """
                INSERT INTO booking
                (user_id, equipment_id, start_date, due_date,
                 actual_return_date, daily_rate, total_cost,
                 booking_status, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, booking.getUserId());
            statement.setInt(2, booking.getEquipmentId());
            statement.setDate(3, Date.valueOf(booking.getStartDate()));
            statement.setDate(4, Date.valueOf(booking.getDueDate()));

            if (booking.getActualReturnDate() != null) {
                statement.setDate(5, Date.valueOf(booking.getActualReturnDate()));
            } else {
                statement.setNull(5, Types.DATE);
            }

            statement.setDouble(6, booking.getDailyRate());
            statement.setDouble(7, booking.getTotalCost());
            statement.setString(8, booking.getBookingStatus());
            statement.setString(9, booking.getNotes());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    booking.setBookingId(id);
                    return id;
                }
            }
        }

        throw new SQLException("Creating booking failed: no ID returned.");
    }

    // Retrieve a booking by ID
    public Booking getBookingById(int bookingId) throws SQLException {

        String sql = "SELECT * FROM booking WHERE booking_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, bookingId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapBooking(resultSet);
                }
            }
        }

        return null;
    }

    // Retrieve all bookings
    public List<Booking> getAllBookings() throws SQLException {

        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM booking";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                bookings.add(mapBooking(resultSet));
            }
        }

        return bookings;
    }

    // Retrieve bookings belonging to a specific user
    public List<Booking> getBookingsByUserId(int userId) throws SQLException {

        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM booking WHERE user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    bookings.add(mapBooking(resultSet));
                }
            }
        }

        return bookings;
    }

    // Update a booking
    public boolean updateBooking(Booking booking) throws SQLException {

        String sql = """
                UPDATE booking
                SET user_id = ?, equipment_id = ?, start_date = ?,
                    due_date = ?, actual_return_date = ?, daily_rate = ?,
                    total_cost = ?, booking_status = ?, notes = ?
                WHERE booking_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, booking.getUserId());
            statement.setInt(2, booking.getEquipmentId());
            statement.setDate(3, Date.valueOf(booking.getStartDate()));
            statement.setDate(4, Date.valueOf(booking.getDueDate()));

            if (booking.getActualReturnDate() != null) {
                statement.setDate(5, Date.valueOf(booking.getActualReturnDate()));
            } else {
                statement.setNull(5, Types.DATE);
            }

            statement.setDouble(6, booking.getDailyRate());
            statement.setDouble(7, booking.getTotalCost());
            statement.setString(8, booking.getBookingStatus());
            statement.setString(9, booking.getNotes());
            statement.setInt(10, booking.getBookingId());

            return statement.executeUpdate() > 0;
        }
    }

    // Cancel a booking by changing its status
    public boolean cancelBooking(int bookingId) throws SQLException {

        String sql = """
                UPDATE booking
                SET booking_status = 'CANCELLED'
                WHERE booking_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, bookingId);
            return statement.executeUpdate() > 0;
        }
    }

    // Convert a database row into a Booking object
    private Booking mapBooking(ResultSet resultSet) throws SQLException {

        Booking booking = new Booking();

        booking.setBookingId(resultSet.getInt("booking_id"));
        booking.setUserId(resultSet.getInt("user_id"));
        booking.setEquipmentId(resultSet.getInt("equipment_id"));

        Timestamp bookingTimestamp = resultSet.getTimestamp("booking_date");
        if (bookingTimestamp != null) {
            booking.setBookingDate(bookingTimestamp.toLocalDateTime());
        }

        booking.setStartDate(resultSet.getDate("start_date").toLocalDate());
        booking.setDueDate(resultSet.getDate("due_date").toLocalDate());

        Date returnDate = resultSet.getDate("actual_return_date");
        if (returnDate != null) {
            booking.setActualReturnDate(returnDate.toLocalDate());
        }

        booking.setDailyRate(resultSet.getDouble("daily_rate"));

        double totalCost = resultSet.getDouble("total_cost");
        booking.setTotalCost(resultSet.wasNull() ? 0.0 : totalCost);

        booking.setBookingStatus(resultSet.getString("booking_status"));
        booking.setNotes(resultSet.getString("notes"));

        return booking;
    }
}
