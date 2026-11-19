
package za.ac.ctu.campusrent.dao;

import za.ac.ctu.campusrent.model.Booking;
import za.ac.ctu.campusrent.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookingDAO implements GenericDAO<Booking, Integer> {

    // GenericDAO implementation
    @Override
    public Integer insert(Booking booking) throws SQLException {
        return addBooking(booking);
    }

    @Override
    public Optional<Booking> findById(Integer id) throws SQLException {
        return Optional.ofNullable(getBookingById(id));
    }

    @Override
    public List<Booking> findAll() throws SQLException {
        return getAllBookings();
    }

    @Override
    public boolean update(Booking booking) throws SQLException {
        return updateBooking(booking);
    }

    /**
     * Only cancelled bookings can be physically deleted.
     * This protects active and approved booking records.
     */
    @Override
    public boolean delete(Integer bookingId) throws SQLException {
        String sql = """
                DELETE FROM booking
                WHERE booking_id = ?
                  AND booking_status = 'CANCELLED'
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, bookingId);
            return statement.executeUpdate() > 0;
        }
    }

    public int addBooking(Booking booking) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            return addBooking(connection, booking);
        }
    }

    public int addBooking(Connection connection, Booking booking)
            throws SQLException {

        String sql = """
                INSERT INTO booking
                (user_id, equipment_id, start_date, due_date,
                 actual_return_date, daily_rate, total_cost,
                 booking_status, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, booking.getUserId());
            statement.setInt(2, booking.getEquipmentId());
            statement.setDate(3, Date.valueOf(booking.getStartDate()));
            statement.setDate(4, Date.valueOf(booking.getDueDate()));

            if (booking.getActualReturnDate() == null) {
                statement.setNull(5, Types.DATE);
            } else {
                statement.setDate(
                        5, Date.valueOf(booking.getActualReturnDate()));
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

    public Booking getBookingById(int bookingId) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            return getBookingById(connection, bookingId, false);
        }
    }

    public Booking getBookingById(Connection connection, int bookingId)
            throws SQLException {
        return getBookingById(connection, bookingId, false);
    }

    public Booking getBookingById(
            Connection connection,
            int bookingId,
            boolean forUpdate) throws SQLException {

        String sql = "SELECT * FROM booking WHERE booking_id = ?";

        if (forUpdate) {
            sql += " FOR UPDATE";
        }

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookingId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapBooking(resultSet) : null;
            }
        }
    }

    public List<Booking> getAllBookings() throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM booking ORDER BY booking_date DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                bookings.add(mapBooking(resultSet));
            }
        }

        return bookings;
    }

    public List<Booking> getBookingsByUserId(int userId) throws SQLException {
        List<Booking> bookings = new ArrayList<>();

        String sql = """
                SELECT * FROM booking
                WHERE user_id = ?
                ORDER BY booking_date DESC
                """;

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

    public boolean updateBooking(Booking booking) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            return updateBooking(connection, booking);
        }
    }

    public boolean updateBooking(
            Connection connection,
            Booking booking) throws SQLException {

        String sql = """
                UPDATE booking
                SET user_id = ?, equipment_id = ?, start_date = ?,
                    due_date = ?, actual_return_date = ?, daily_rate = ?,
                    total_cost = ?, booking_status = ?, notes = ?
                WHERE booking_id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, booking.getUserId());
            statement.setInt(2, booking.getEquipmentId());
            statement.setDate(3, Date.valueOf(booking.getStartDate()));
            statement.setDate(4, Date.valueOf(booking.getDueDate()));

            if (booking.getActualReturnDate() == null) {
                statement.setNull(5, Types.DATE);
            } else {
                statement.setDate(
                        5, Date.valueOf(booking.getActualReturnDate()));
            }

            statement.setDouble(6, booking.getDailyRate());
            statement.setDouble(7, booking.getTotalCost());
            statement.setString(8, booking.getBookingStatus());
            statement.setString(9, booking.getNotes());
            statement.setInt(10, booking.getBookingId());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean cancelBooking(int bookingId) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            return cancelBooking(connection, bookingId);
        }
    }

    public boolean cancelBooking(
            Connection connection,
            int bookingId) throws SQLException {

        String sql = """
                UPDATE booking
                SET booking_status = 'CANCELLED'
                WHERE booking_id = ?
                  AND booking_status IN ('PENDING', 'APPROVED')
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookingId);
            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Checks for date overlap.
     * The caller must lock the equipment row first.
     */
    public boolean hasConflictingBooking(
            Connection connection,
            int equipmentId,
            LocalDate startDate,
            LocalDate dueDate,
            int excludedBookingId) throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM booking
                WHERE equipment_id = ?
                  AND booking_id <> ?
                  AND booking_status IN ('PENDING', 'APPROVED', 'ACTIVE', 'OVERDUE')
                  AND start_date <= ?
                  AND due_date >= ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, equipmentId);
            statement.setInt(2, excludedBookingId);
            statement.setDate(3, Date.valueOf(dueDate));
            statement.setDate(4, Date.valueOf(startDate));

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    // Check whether equipment has any unfinished bookings.
    public boolean hasUnfinishedBookings(
            Connection connection,
            int equipmentId) throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM booking
                WHERE equipment_id = ?
                  AND booking_status IN (
                      'PENDING', 'APPROVED', 'ACTIVE', 'OVERDUE'
                  )
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, equipmentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    // True when equipment is physically out with a borrower.
    public boolean hasBlockingBookings(
            Connection connection,
            int equipmentId) throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM booking
                WHERE equipment_id = ?
                  AND booking_status IN ('ACTIVE', 'OVERDUE')
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, equipmentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    // Convert a database row into a Booking object.
    private Booking mapBooking(ResultSet resultSet) throws SQLException {
        Booking booking = new Booking();

        booking.setBookingId(resultSet.getInt("booking_id"));
        booking.setUserId(resultSet.getInt("user_id"));
        booking.setEquipmentId(resultSet.getInt("equipment_id"));

        Timestamp bookingTimestamp =
                resultSet.getTimestamp("booking_date");

        if (bookingTimestamp != null) {
            booking.setBookingDate(
                    bookingTimestamp.toLocalDateTime());
        }

        Date startDate = resultSet.getDate("start_date");

        if (startDate != null) {
            booking.setStartDate(startDate.toLocalDate());
        }

        Date dueDate = resultSet.getDate("due_date");

        if (dueDate != null) {
            booking.setDueDate(dueDate.toLocalDate());
        }

        Date returnDate = resultSet.getDate("actual_return_date");

        if (returnDate != null) {
            booking.setActualReturnDate(returnDate.toLocalDate());
        }

        booking.setDailyRate(resultSet.getDouble("daily_rate"));

        double totalCost = resultSet.getDouble("total_cost");
        booking.setTotalCost(resultSet.wasNull() ? 0.0 : totalCost);

        booking.setBookingStatus(
                resultSet.getString("booking_status"));

        booking.setNotes(resultSet.getString("notes"));

        return booking;
    }
}
