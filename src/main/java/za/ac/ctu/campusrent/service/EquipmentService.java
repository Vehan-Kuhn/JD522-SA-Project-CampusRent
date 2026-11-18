package za.ac.ctu.campusrent.service;
import za.ac.ctu.campusrent.dao.EquipmentDAO;
import za.ac.ctu.campusrent.model.Equipment;
import za.ac.ctu.campusrent.dao.BookingDAO;
import za.ac.ctu.campusrent.util.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Handles equipment business logic and validation. **/
public class EquipmentService {

    private final EquipmentDAO equipmentDAO;
    private final BookingDAO bookingDAO;

    private static final Set<String> VALID_CATEGORIES = Set.of(
            "LAPTOP", "PROJECTOR", "CAMERA",
            "TABLET", "NETWORK_KIT", "OTHER"
    );

    private static final Set<String> VALID_CONDITIONS = Set.of(
            "NEW", "GOOD", "FAIR", "DAMAGED"
    );

    private static final Set<String> VALID_AVAILABILITY = Set.of(
            "AVAILABLE", "BOOKED", "MAINTENANCE", "RETIRED"
    );

    public EquipmentService() {
        this.equipmentDAO = new EquipmentDAO();
        this.bookingDAO = new BookingDAO();
    }

    // Add equipment
    public int addEquipment(Equipment equipment) throws SQLException {

        validateEquipment(equipment);
        normaliseEquipment(equipment);

        if (serialNumberExists(equipment.getSerialNumber(), 0)) {
            throw new IllegalArgumentException(
                    "Equipment with this serial number already exists."
            );
        }

        return equipmentDAO.addEquipment(equipment);
    }

    // Retrieve equipment by ID
    public Equipment getEquipmentById(int equipmentId) throws SQLException {

        validateEquipmentId(equipmentId);

        return equipmentDAO.getEquipmentById(equipmentId);
    }

    // Retrieve all equipment
    public List<Equipment> getAllEquipment() throws SQLException {

        return equipmentDAO.getAllEquipment();
    }

    // Retrieve available equipment
    public List<Equipment> getAvailableEquipment() throws SQLException {

        return equipmentDAO.getAllEquipment().stream()
                .filter(equipment ->
                        "AVAILABLE".equals(equipment.getAvailabilityStatus()))
                .toList();
    }

    // Update equipment
    public boolean updateEquipment(Equipment equipment) throws SQLException {

        if (equipment == null || equipment.getEquipmentId() <= 0) {
            throw new IllegalArgumentException(
                    "Invalid equipment information."
            );
        }

        validateEquipment(equipment);
        normaliseEquipment(equipment);

        Equipment existing = equipmentDAO.getEquipmentById(
                equipment.getEquipmentId()
        );

        if (existing == null) {
            throw new IllegalArgumentException(
                    "Equipment could not be found."
            );
        }

        if (serialNumberExists(
                equipment.getSerialNumber(),
                equipment.getEquipmentId())) {

            throw new IllegalArgumentException(
                    "This serial number is already in use."
            );
        }

        return equipmentDAO.updateEquipment(equipment);
    }

    // Check whether a serial number is already being used
    private boolean serialNumberExists(
            String serialNumber,
            int excludedEquipmentId) throws SQLException {

        if (serialNumber == null || serialNumber.isBlank()) {
            return false;
        }

        String searchSerial = serialNumber.trim();

        return equipmentDAO.getAllEquipment().stream()
                .anyMatch(existing ->
                        existing.getEquipmentId() != excludedEquipmentId
                                && existing.getSerialNumber() != null
                                && existing.getSerialNumber().trim()
                                .equalsIgnoreCase(searchSerial));
    }
    // Mark equipment for maintenance.
    public boolean markForMaintenance(int equipmentId) throws SQLException {

        validateEquipmentId(equipmentId);

        return changeAvailabilitySafely(
                equipmentId,
                "MAINTENANCE"
        );
    }

    // Restore equipment from maintenance.
    public boolean restoreEquipment(int equipmentId) throws SQLException {

        validateEquipmentId(equipmentId);

        return changeAvailabilitySafely(
                equipmentId,
                "AVAILABLE"
        );
    }

    // Retire equipment permanently.
    public boolean retireEquipment(int equipmentId) throws SQLException {

        validateEquipmentId(equipmentId);

        return changeAvailabilitySafely(
                equipmentId,
                "RETIRED"
        );
    }
    // Safely change equipment availability using a database transaction.
    private boolean changeAvailabilitySafely(
            int equipmentId,
            String newStatus) throws SQLException {

        try (Connection connection = DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                Equipment equipment = equipmentDAO.getEquipmentById(
                        connection,
                        equipmentId,
                        true
                );

                if (equipment == null) {
                    throw new IllegalArgumentException(
                            "Equipment could not be found."
                    );
                }

                String currentStatus = equipment.getAvailabilityStatus();

                if ("RETIRED".equals(currentStatus)) {
                    throw new IllegalArgumentException(
                            "Retired equipment cannot be changed."
                    );
                }

                if ("MAINTENANCE".equals(newStatus)) {

                    if ("MAINTENANCE".equals(currentStatus)) {
                        throw new IllegalArgumentException(
                                "Equipment is already under maintenance."
                        );
                    }

                    if (bookingDAO.hasUnfinishedBookings(
                            connection, equipmentId)) {

                        throw new IllegalArgumentException(
                                "Equipment with unfinished bookings "
                                        + "cannot be placed into maintenance."
                        );
                    }
                }

                if ("AVAILABLE".equals(newStatus)) {

                    if (!"MAINTENANCE".equals(currentStatus)) {
                        throw new IllegalArgumentException(
                                "Only equipment under maintenance can be restored."
                        );
                    }

                    if (bookingDAO.hasBlockingBookings(
                            connection, equipmentId)) {

                        throw new IllegalArgumentException(
                                "Equipment currently in use cannot be restored."
                        );
                    }
                }

                if ("RETIRED".equals(newStatus)) {

                    if (bookingDAO.hasUnfinishedBookings(
                            connection, equipmentId)) {

                        throw new IllegalArgumentException(
                                "Equipment with unfinished bookings "
                                        + "cannot be retired."
                        );
                    }
                }

                boolean updated = equipmentDAO.updateAvailabilityStatus(
                        connection,
                        equipmentId,
                        newStatus
                );

                if (!updated) {
                    throw new SQLException(
                            "Equipment availability could not be updated."
                    );
                }

                connection.commit();

                return true;

            } catch (SQLException | RuntimeException exception) {

                connection.rollback();
                throw exception;
            }
        }
    }

    // Delete equipment if it has no booking history
    public boolean deleteEquipment(int equipmentId) throws SQLException {

        validateEquipmentId(equipmentId);

        Equipment equipment = equipmentDAO.getEquipmentById(equipmentId);

        if (equipment == null) {
            return false;
        }

        if (equipmentDAO.hasBookingHistory(equipmentId)) {
            throw new IllegalArgumentException(
                    "Equipment with booking history cannot be deleted. "
                            + "Retire the equipment instead."
            );
        }

        return equipmentDAO.deleteEquipment(equipmentId);
    }

    // Update equipment availability
    private boolean updateAvailability(
            int equipmentId,
            String availabilityStatus) throws SQLException {

        // Uses the DAO's existing transaction-aware method.
        try (var connection =
                     za.ac.ctu.campusrent.util.DBConnection.getConnection()) {

            return equipmentDAO.updateAvailabilityStatus(
                    connection,
                    equipmentId,
                    availabilityStatus
            );
        }
    }

    // Validate equipment ID
    private void validateEquipmentId(int equipmentId) {

        if (equipmentId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid equipment ID."
            );
        }
    }

    // Normalise user input
    private void normaliseEquipment(Equipment equipment) {

        equipment.setSerialNumber(equipment.getSerialNumber().trim());

        equipment.setCategory(
                equipment.getCategory().trim().toUpperCase(Locale.ROOT)
        );

        equipment.setConditionStatus(
                equipment.getConditionStatus().trim().toUpperCase(Locale.ROOT)
        );

        equipment.setAvailabilityStatus(
                equipment.getAvailabilityStatus().trim().toUpperCase(Locale.ROOT)
        );
    }

    // Validate equipment information
    private void validateEquipment(Equipment equipment) {

        if (equipment == null) {
            throw new IllegalArgumentException(
                    "Equipment cannot be empty."
            );
        }

        if (equipment.getCampusId() <= 0) {
            throw new IllegalArgumentException(
                    "A valid campus is required."
            );
        }

        if (equipment.getSerialNumber() == null
                || equipment.getSerialNumber().isBlank()) {
            throw new IllegalArgumentException(
                    "Serial number is required."
            );
        }

        if (equipment.getEquipmentName() == null
                || equipment.getEquipmentName().isBlank()) {
            throw new IllegalArgumentException(
                    "Equipment name is required."
            );
        }

        if (equipment.getCategory() == null
                || !VALID_CATEGORIES.contains(
                equipment.getCategory().trim().toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException(
                    "Invalid equipment category."
            );
        }

        if (!Double.isFinite(equipment.getDailyRate())
                || equipment.getDailyRate() < 0) {
            throw new IllegalArgumentException(
                    "Daily rate must be a valid non-negative amount."
            );
        }

        if (equipment.getConditionStatus() == null
                || !VALID_CONDITIONS.contains(
                equipment.getConditionStatus().trim().toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException(
                    "Invalid equipment condition."
            );
        }

        if (equipment.getAvailabilityStatus() == null
                || !VALID_AVAILABILITY.contains(
                equipment.getAvailabilityStatus().trim().toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException(
                    "Invalid equipment availability status."
            );
        }
    }
}
