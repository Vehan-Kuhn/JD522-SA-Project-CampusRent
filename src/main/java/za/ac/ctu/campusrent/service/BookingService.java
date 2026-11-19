package za.ac.ctu.campusrent.service;
import za.ac.ctu.campusrent.dao.BookingDAO;
import za.ac.ctu.campusrent.dao.EquipmentDAO;
import za.ac.ctu.campusrent.dao.UserDAO;
import za.ac.ctu.campusrent.model.Booking;
import za.ac.ctu.campusrent.model.Equipment;
import za.ac.ctu.campusrent.model.User;
import za.ac.ctu.campusrent.util.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Set;
/**
 * Handles booking business logic, validation and transactions.
 *
 * Booking transitions:
 * PENDING -> APPROVED -> ACTIVE -> COMPLETED
 * PENDING/APPROVED -> CANCELLED
 * ACTIVE -> OVERDUE -> COMPLETED
 */
public class BookingService {

    private final BookingDAO bookingDAO;
    private final EquipmentDAO equipmentDAO;
    private final UserDAO userDAO;

    private static final Set<String> EDITABLE_STATUSES =
            Set.of("PENDING", "APPROVED");

    public BookingService() {
        this.bookingDAO = new BookingDAO();
        this.equipmentDAO = new EquipmentDAO();
        this.userDAO = new UserDAO();
    }

    public int addBooking(Booking booking) throws SQLException {
        validateBooking(booking);
        validateActiveUser(booking.getUserId());

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Equipment equipment = equipmentDAO.getEquipmentById(
                        connection, booking.getEquipmentId(), true);

                if (equipment == null) {
                    throw new IllegalArgumentException(
                            "The selected equipment does not exist.");
                }

                validateEquipmentCanBeBooked(equipment);

                if (bookingDAO.hasConflictingBooking(
                        connection, booking.getEquipmentId(),
                        booking.getStartDate(), booking.getDueDate(), 0)) {
                    throw new IllegalArgumentException(
                            "This equipment is already booked for the selected dates.");
                }

                calculateBookingCost(booking, equipment);
                booking.setBookingStatus("PENDING");
                booking.setActualReturnDate(null);

                int bookingId = bookingDAO.addBooking(connection, booking);
                refreshEquipmentAvailability(connection, booking.getEquipmentId());
                connection.commit();
                return bookingId;

            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public Booking getBookingById(int bookingId) throws SQLException {
        validateBookingId(bookingId);
        return bookingDAO.getBookingById(bookingId);
    }

    public List<Booking> getAllBookings() throws SQLException {
        return bookingDAO.getAllBookings();
    }

    public List<Booking> getBookingsByUserId(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID.");
        }
        return bookingDAO.getBookingsByUserId(userId);
    }

    /**
     * Edits booking details only. Status changes must use dedicated methods.
     */
    public boolean updateBooking(Booking booking) throws SQLException {
        if (booking == null || booking.getBookingId() <= 0) {
            throw new IllegalArgumentException("Invalid booking information.");
        }

        validateBooking(booking);
        validateActiveUser(booking.getUserId());

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Booking existing = bookingDAO.getBookingById(
                        connection, booking.getBookingId(), true);

                if (existing == null) {
                    throw new IllegalArgumentException("Booking could not be found.");
                }

                if (!EDITABLE_STATUSES.contains(existing.getBookingStatus())) {
                    throw new IllegalArgumentException(
                            "Only pending or approved bookings can be edited.");
                }

                if (booking.getBookingStatus() != null
                        && !existing.getBookingStatus().equals(
                        booking.getBookingStatus().trim().toUpperCase(Locale.ROOT))) {
                    throw new IllegalArgumentException(
                            "Use the dedicated booking action to change its status.");
                }

                int oldEquipmentId = existing.getEquipmentId();
                int newEquipmentId = booking.getEquipmentId();

                Equipment oldEquipment;
                Equipment newEquipment;

                // Lock equipment rows in ascending ID order to reduce deadlocks.
                if (oldEquipmentId == newEquipmentId) {
                    oldEquipment = equipmentDAO.getEquipmentById(
                            connection, oldEquipmentId, true);
                    newEquipment = oldEquipment;
                } else if (oldEquipmentId < newEquipmentId) {
                    oldEquipment = equipmentDAO.getEquipmentById(
                            connection, oldEquipmentId, true);
                    newEquipment = equipmentDAO.getEquipmentById(
                            connection, newEquipmentId, true);
                } else {
                    newEquipment = equipmentDAO.getEquipmentById(
                            connection, newEquipmentId, true);
                    oldEquipment = equipmentDAO.getEquipmentById(
                            connection, oldEquipmentId, true);
                }

                if (oldEquipment == null || newEquipment == null) {
                    throw new IllegalArgumentException(
                            "The original or selected equipment could not be found.");
                }

                validateEquipmentCanBeBooked(newEquipment);

                if (bookingDAO.hasConflictingBooking(
                        connection, newEquipmentId, booking.getStartDate(),
                        booking.getDueDate(), booking.getBookingId())) {
                    throw new IllegalArgumentException(
                            "This equipment is already booked for the selected dates.");
                }

                // Preserve status and return date from the database record.
                booking.setBookingStatus(existing.getBookingStatus());
                booking.setActualReturnDate(existing.getActualReturnDate());
                calculateBookingCost(booking, newEquipment);

                if (!bookingDAO.updateBooking(connection, booking)) {
                    throw new SQLException("Booking update failed.");
                }

                if (oldEquipmentId != newEquipmentId) {
                    refreshEquipmentAvailability(connection, oldEquipmentId);
                }
                refreshEquipmentAvailability(connection, newEquipmentId);

                connection.commit();
                return true;

            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public boolean approveBooking(int bookingId) throws SQLException {
        return changeBookingStatus(bookingId, "PENDING", "APPROVED", false);
    }

    /**
     * Activates an approved booking on or after its start date and no later
     * than its due date. The equipment is locked and date conflicts rechecked.
     */
    public boolean activateBooking(int bookingId) throws SQLException {
        validateBookingId(bookingId);

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Booking booking = bookingDAO.getBookingById(
                        connection, bookingId, true);
                if (booking == null) {
                    throw new IllegalArgumentException("Booking could not be found.");
                }
                if (!"APPROVED".equals(booking.getBookingStatus())) {
                    throw new IllegalArgumentException(
                            "Only approved bookings can be activated.");
                }

                LocalDate today = LocalDate.now();
                if (today.isBefore(booking.getStartDate())
                        || today.isAfter(booking.getDueDate())) {
                    throw new IllegalArgumentException(
                            "A booking can only be activated between its start date and due date.");
                }

                Equipment equipment = equipmentDAO.getEquipmentById(
                        connection, booking.getEquipmentId(), true);
                if (equipment == null) {
                    throw new IllegalArgumentException("Equipment could not be found.");
                }
                validateEquipmentCanBeBooked(equipment);

                booking.setBookingStatus("ACTIVE");
                if (!bookingDAO.updateBooking(connection, booking)) {
                    throw new SQLException("Could not activate booking.");
                }

                refreshEquipmentAvailability(connection, booking.getEquipmentId());
                connection.commit();
                return true;

            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    /**
     * Marks an active booking overdue once its due date has passed.
     */
    public boolean markBookingOverdue(int bookingId) throws SQLException {
        validateBookingId(bookingId);

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Booking booking = bookingDAO.getBookingById(
                        connection, bookingId, true);
                if (booking == null) {
                    throw new IllegalArgumentException("Booking could not be found.");
                }
                if (!"ACTIVE".equals(booking.getBookingStatus())) {
                    throw new IllegalArgumentException(
                            "Only active bookings can be marked overdue.");
                }
                if (!LocalDate.now().isAfter(booking.getDueDate())) {
                    throw new IllegalArgumentException(
                            "The booking due date has not passed.");
                }

                Equipment equipment = equipmentDAO.getEquipmentById(
                        connection, booking.getEquipmentId(), true);
                if (equipment == null) {
                    throw new SQLException("Equipment could not be found.");
                }

                booking.setBookingStatus("OVERDUE");
                if (!bookingDAO.updateBooking(connection, booking)) {
                    throw new SQLException("Could not mark booking overdue.");
                }

                refreshEquipmentAvailability(connection, booking.getEquipmentId());
                connection.commit();
                return true;

            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    /**
     * Marks every active booking past its due date as overdue.
     * Can be called when the application starts or from an admin action.
     */
    public int updateOverdueBookings() throws SQLException {
        int updatedCount = 0;
        for (Booking booking : bookingDAO.getAllBookings()) {
            if ("ACTIVE".equals(booking.getBookingStatus())
                    && booking.getDueDate() != null
                    && LocalDate.now().isAfter(booking.getDueDate())) {
                if (markBookingOverdue(booking.getBookingId())) {
                    updatedCount++;
                }
            }
        }
        return updatedCount;
    }

    public boolean cancelBooking(int bookingId) throws SQLException {
        validateBookingId(bookingId);

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Booking booking = bookingDAO.getBookingById(
                        connection, bookingId, true);
                if (booking == null) {
                    throw new IllegalArgumentException("Booking could not be found.");
                }
                if (!"PENDING".equals(booking.getBookingStatus())
                        && !"APPROVED".equals(booking.getBookingStatus())) {
                    throw new IllegalArgumentException(
                            "Only pending or approved bookings can be cancelled.");
                }

                Equipment equipment = equipmentDAO.getEquipmentById(
                        connection, booking.getEquipmentId(), true);
                if (equipment == null) {
                    throw new SQLException(
                            "The equipment linked to this booking was not found.");
                }

                if (!bookingDAO.cancelBooking(connection, bookingId)) {
                    throw new SQLException("Booking cancellation failed.");
                }

                refreshEquipmentAvailability(connection, booking.getEquipmentId());
                connection.commit();
                return true;

            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public boolean completeBooking(int bookingId, LocalDate returnDate)
            throws SQLException {
        validateBookingId(bookingId);
        if (returnDate == null) {
            throw new IllegalArgumentException("A return date is required.");
        }

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Booking booking = bookingDAO.getBookingById(
                        connection, bookingId, true);
                if (booking == null) {
                    throw new IllegalArgumentException("Booking could not be found.");
                }
                if (!"ACTIVE".equals(booking.getBookingStatus())
                        && !"OVERDUE".equals(booking.getBookingStatus())) {
                    throw new IllegalArgumentException(
                            "Only active or overdue bookings can be completed.");
                }
                if (returnDate.isBefore(booking.getStartDate())) {
                    throw new IllegalArgumentException(
                            "Return date cannot be before the booking start date.");
                }
                if (returnDate.isAfter(LocalDate.now())) {
                    throw new IllegalArgumentException(
                            "Return date cannot be in the future.");
                }

                Equipment equipment = equipmentDAO.getEquipmentById(
                        connection, booking.getEquipmentId(), true);
                if (equipment == null) {
                    throw new SQLException("Equipment could not be found.");
                }

                booking.setActualReturnDate(returnDate);
                booking.setBookingStatus("COMPLETED");

                if (!bookingDAO.updateBooking(connection, booking)) {
                    throw new SQLException("Could not complete booking.");
                }

                refreshEquipmentAvailability(connection, booking.getEquipmentId());
                connection.commit();
                return true;

            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    /**
     * Recalculates current physical availability.
     * Future pending/approved reservations do not mean equipment is currently
     * out with a borrower; overlap checks still protect their reserved dates.
     */
    private void refreshEquipmentAvailability(
            Connection connection, int equipmentId) throws SQLException {

        Equipment equipment = equipmentDAO.getEquipmentById(
                connection, equipmentId, true);
        if (equipment == null) {
            return;
        }

        if ("MAINTENANCE".equals(equipment.getAvailabilityStatus())
                || "RETIRED".equals(equipment.getAvailabilityStatus())) {
            return;
        }

        boolean currentlyInUse =
                bookingDAO.hasBlockingBookings(connection, equipmentId);
        equipmentDAO.updateAvailabilityStatus(
                connection, equipmentId,
                currentlyInUse ? "BOOKED" : "AVAILABLE");
    }

    private boolean changeBookingStatus(
            int bookingId, String requiredStatus, String newStatus,
            boolean requireActiveEquipment) throws SQLException {

        validateBookingId(bookingId);

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Booking booking = bookingDAO.getBookingById(
                        connection, bookingId, true);
                if (booking == null) {
                    throw new IllegalArgumentException("Booking could not be found.");
                }
                if (!requiredStatus.equals(booking.getBookingStatus())) {
                    throw new IllegalArgumentException(
                            "Booking must be in " + requiredStatus + " status.");
                }

                Equipment equipment = equipmentDAO.getEquipmentById(
                        connection, booking.getEquipmentId(), true);
                if (equipment == null) {
                    throw new SQLException("Equipment could not be found.");
                }
                if (requireActiveEquipment) {
                    validateEquipmentCanBeBooked(equipment);
                }

                booking.setBookingStatus(newStatus);
                if (!bookingDAO.updateBooking(connection, booking)) {
                    throw new SQLException("Could not update booking status.");
                }

                refreshEquipmentAvailability(connection, booking.getEquipmentId());
                connection.commit();
                return true;

            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private void validateActiveUser(int userId) throws SQLException {
        User user = userDAO.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("The selected user does not exist.");
        }
        if (!"ACTIVE".equals(user.getAccountStatus())) {
            throw new IllegalArgumentException("This user account is not active.");
        }
    }

    private void validateEquipmentCanBeBooked(Equipment equipment) {
        if ("MAINTENANCE".equals(equipment.getAvailabilityStatus())
                || "RETIRED".equals(equipment.getAvailabilityStatus())) {
            throw new IllegalArgumentException(
                    "This equipment is not available for booking.");
        }
    }

    private void calculateBookingCost(Booking booking, Equipment equipment) {
        booking.setDailyRate(equipment.getDailyRate());
        long rentalDays = ChronoUnit.DAYS.between(
                booking.getStartDate(), booking.getDueDate()) + 1;
        booking.setTotalCost(rentalDays * equipment.getDailyRate());
    }

    private void validateBookingId(int bookingId) {
        if (bookingId <= 0) {
            throw new IllegalArgumentException("Invalid booking ID.");
        }
    }

    private void validateBooking(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be empty.");
        }
        if (booking.getUserId() <= 0) {
            throw new IllegalArgumentException("A valid user is required.");
        }
        if (booking.getEquipmentId() <= 0) {
            throw new IllegalArgumentException("Valid equipment is required.");
        }
        if (booking.getStartDate() == null || booking.getDueDate() == null) {
            throw new IllegalArgumentException(
                    "Booking start and due dates are required.");
        }
        if (booking.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "The booking start date cannot be in the past.");
        }
        if (booking.getDueDate().isBefore(booking.getStartDate())) {
            throw new IllegalArgumentException(
                    "The due date cannot be before the start date.");
        }
    }
}
