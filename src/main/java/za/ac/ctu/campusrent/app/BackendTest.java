package za.ac.ctu.campusrent.app;
import za.ac.ctu.campusrent.dao.*;
import za.ac.ctu.campusrent.model.*;
import za.ac.ctu.campusrent.service.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class BackendTest {

    private static int passed = 0;

    private static Campus campus;
    private static User user;
    private static Equipment equipment;
    private static Booking booking;
    private static AuditLog auditLog;

    private static final CampusDAO campusDAO = new CampusDAO();
    private static final UserDAO userDAO = new UserDAO();
    private static final EquipmentDAO equipmentDAO = new EquipmentDAO();
    private static final BookingDAO bookingDAO = new BookingDAO();
    private static final AuditLogDAO auditLogDAO = new AuditLogDAO();

    private static final CampusService campusService = new CampusService();
    private static final UserService userService = new UserService();
    private static final EquipmentService equipmentService =
            new EquipmentService();
    private static final BookingService bookingService =
            new BookingService();
    private static final AuditLogService auditLogService =
            new AuditLogService();

    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println("       CAMPUSRENT BACKEND TESTS");
        System.out.println("======================================");

        try {

            // CAMPUS TESTS
            test("Campus: Retrieve all campuses", () -> {
                List<Campus> campuses = campusDAO.getAllCampuses();

                check(!campuses.isEmpty(),
                        "No campus records found.");

                campus = campuses.get(0);

                System.out.println("Found: " + campus);
            });

            test("Campus: Retrieve campus by ID", () -> {
                Campus result =
                        campusService.getCampusById(campus.getCampusId());

                check(result != null,
                        "Campus was not found.");
            });

            // USER TESTS
            String uniqueId = UUID.randomUUID().toString()
                    .substring(0, 8);

            test("User: Add a new user", () -> {

                user = new User();

                user.setCampusId(campus.getCampusId());
                user.setFirstName("Test");
                user.setLastName("Student");
                user.setEmail("test." + uniqueId + "@campusrent.co.za");
                user.setPhone("0123456789");
                user.setPasswordHash(createTestPasswordHash());
                user.setUserRole("STUDENT");
                user.setAccountStatus("ACTIVE");

                int id = userService.addUser(user);

                check(id > 0, "User ID was not generated.");

                System.out.println("Created user ID: " + id);
            });

            test("User: Retrieve user by ID", () -> {
                User result = userService.getUserById(user.getUserId());

                check(result != null, "User was not found.");
                check(result.getEmail().equals(user.getEmail()),
                        "User email does not match.");
            });

            test("User: Retrieve user by email", () -> {
                User result = userService.getUserByEmail(user.getEmail());

                check(result != null, "User was not found by email.");
            });

            test("User: Retrieve all users", () -> {
                List<User> users = userService.getAllUsers();

                check(!users.isEmpty(), "No users were returned.");
            });

            test("User: Update user", () -> {
                user.setPhone("0119876543");

                boolean updated = userService.updateUser(user);

                check(updated, "User update failed.");
            });

            test("User: Reject duplicate email", () -> {
                User duplicate = new User();

                duplicate.setCampusId(campus.getCampusId());
                duplicate.setFirstName("Duplicate");
                duplicate.setLastName("Student");
                duplicate.setEmail(user.getEmail());
                duplicate.setPasswordHash(createTestPasswordHash());
                duplicate.setUserRole("STUDENT");
                duplicate.setAccountStatus("ACTIVE");

                expectIllegalArgument(
                        () -> userService.addUser(duplicate)
                );
            });

            // EQUIPMENT TESTS
            test("Equipment: Add equipment", () -> {

                equipment = new Equipment();

                equipment.setCampusId(campus.getCampusId());
                equipment.setSerialNumber("TEST-" + uniqueId);
                equipment.setEquipmentName("Test Laptop");
                equipment.setDescription(
                        "CampusRent automated test equipment"
                );
                equipment.setCategory("LAPTOP");
                equipment.setDailyRate(150.00);
                equipment.setConditionStatus("GOOD");
                equipment.setAvailabilityStatus("AVAILABLE");

                int id = equipmentService.addEquipment(equipment);

                check(id > 0,
                        "Equipment ID was not generated.");

                System.out.println("Created equipment ID: " + id);
            });

            test("Equipment: Retrieve by ID", () -> {
                Equipment result =
                        equipmentService.getEquipmentById(
                                equipment.getEquipmentId()
                        );

                check(result != null,
                        "Equipment was not found.");
            });

            test("Equipment: Retrieve all equipment", () -> {
                List<Equipment> items =
                        equipmentService.getAllEquipment();

                check(!items.isEmpty(),
                        "No equipment records found.");
            });

            test("Equipment: Retrieve available equipment", () -> {
                List<Equipment> items =
                        equipmentService.getAvailableEquipment();

                check(items.stream().anyMatch(
                        item -> item.getEquipmentId()
                                == equipment.getEquipmentId()
                ), "Test equipment was not in the available list.");
            });

            test("Equipment: Update equipment", () -> {
                equipment.setDailyRate(175.00);

                boolean updated =
                        equipmentService.updateEquipment(equipment);

                check(updated,
                        "Equipment update failed.");
            });

            // BOOKING TESTS
            test("Booking: Create booking", () -> {

                booking = new Booking();

                booking.setUserId(user.getUserId());
                booking.setEquipmentId(equipment.getEquipmentId());
                booking.setStartDate(LocalDate.now().plusDays(2));
                booking.setDueDate(LocalDate.now().plusDays(4));
                booking.setNotes("CampusRent automated test booking");

                int id = bookingService.addBooking(booking);

                check(id > 0,
                        "Booking ID was not generated.");

                check(booking.getTotalCost() == 525.00,
                        "Booking total cost was incorrect.");

                System.out.println("Created booking ID: " + id);
                System.out.println(
                        "Calculated cost: R" + booking.getTotalCost()
                );
            });

            test("Booking: Retrieve by ID", () -> {
                Booking result =
                        bookingService.getBookingById(
                                booking.getBookingId()
                        );

                check(result != null,
                        "Booking was not found.");
            });

            test("Booking: Retrieve all bookings", () -> {
                List<Booking> bookings =
                        bookingService.getAllBookings();

                check(!bookings.isEmpty(),
                        "No bookings were returned.");
            });

            test("Booking: Retrieve bookings by user", () -> {
                List<Booking> bookings =
                        bookingService.getBookingsByUserId(
                                user.getUserId()
                        );

                check(bookings.stream().anyMatch(
                        item -> item.getBookingId()
                                == booking.getBookingId()
                ), "User booking was not found.");
            });

            test("Booking: Update booking", () -> {
                booking.setNotes("Updated automated test booking");

                boolean updated =
                        bookingService.updateBooking(booking);

                check(updated,
                        "Booking update failed.");
            });

            test("Booking: Cancel booking", () -> {
                boolean cancelled =
                        bookingService.cancelBooking(
                                booking.getBookingId()
                        );

                check(cancelled,
                        "Booking cancellation failed.");

                Booking result =
                        bookingDAO.getBookingById(
                                booking.getBookingId()
                        );

                check("CANCELLED".equals(result.getBookingStatus()),
                        "Booking status was not changed to CANCELLED.");
            });

            test("Booking: Reject invalid dates", () -> {
                Booking invalid = new Booking();

                invalid.setUserId(user.getUserId());
                invalid.setEquipmentId(equipment.getEquipmentId());
                invalid.setStartDate(LocalDate.now().plusDays(5));
                invalid.setDueDate(LocalDate.now().plusDays(2));

                expectIllegalArgument(
                        () -> bookingService.addBooking(invalid)
                );
            });

            // AUDIT LOG TESTS
            test("Audit log: Add audit record", () -> {

                auditLog = new AuditLog();

                auditLog.setUserId(user.getUserId());
                auditLog.setAction("TEST_CREATE");
                auditLog.setEntityType("BOOKING");
                auditLog.setEntityId(booking.getBookingId());
                auditLog.setDetails(
                        "Automated CampusRent backend test"
                );

                int id = auditLogService.addAuditLog(auditLog);

                check(id > 0,
                        "Audit log ID was not generated.");

                System.out.println("Created audit log ID: " + id);
            });

            test("Audit log: Retrieve by ID", () -> {
                AuditLog result =
                        auditLogService.getAuditLogById(
                                auditLog.getLogId()
                        );

                check(result != null,
                        "Audit log was not found.");
            });

            test("Audit log: Retrieve all records", () -> {
                List<AuditLog> logs =
                        auditLogService.getAllAuditLogs();

                check(!logs.isEmpty(),
                        "No audit records were returned.");
            });

            test("Audit log: Retrieve by user ID", () -> {
                List<AuditLog> logs =
                        auditLogService.getAuditLogsByUserId(
                                user.getUserId()
                        );

                check(logs.stream().anyMatch(
                        log -> log.getLogId() == auditLog.getLogId()
                ), "User audit log was not found.");
            });

            // VALIDATION TESTS
            test("Campus: Reject invalid campus ID", () -> {
                expectIllegalArgument(
                        () -> campusService.getCampusById(-1)
                );
            });

            test("User: Reject invalid user ID", () -> {
                expectIllegalArgument(
                        () -> userService.getUserById(-1)
                );
            });

            test("Equipment: Reject invalid equipment ID", () -> {
                expectIllegalArgument(
                        () -> equipmentService.getEquipmentById(-1)
                );
            });

            test("Booking: Reject invalid booking ID", () -> {
                expectIllegalArgument(
                        () -> bookingService.getBookingById(-1)
                );
            });

            test("Audit log: Reject invalid log ID", () -> {
                expectIllegalArgument(
                        () -> auditLogService.getAuditLogById(-1)
                );
            });

            System.out.println("\n======================================");
            System.out.println("ALL BACKEND TESTS COMPLETED");
            System.out.println("PASSED: " + passed);
            System.out.println("FAILED: 0");
            System.out.println("======================================");

        } catch (Exception e) {

            System.err.println("\n======================================");
            System.err.println("BACKEND TESTS STOPPED");
            System.err.println("PASSED BEFORE FAILURE: " + passed);
            System.err.println("ERROR: " + e.getMessage());
            System.err.println("======================================");

            e.printStackTrace();
        }
    }

    // Run an individual test
    private static void test(String name, TestAction action)
            throws Exception {

        System.out.println("\nTEST: " + name);

        action.run();

        passed++;

        System.out.println("RESULT: PASSED");
    }

    // Check whether a condition is true
    private static void check(boolean condition, String message) {

        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    // Check whether invalid information is rejected
    private static void expectIllegalArgument(
            TestAction action) throws Exception {

        try {
            action.run();
        } catch (IllegalArgumentException e) {
            return;
        }

        throw new IllegalStateException(
                "Expected validation to reject the information."
        );
    }

    // Generate a test-only PBKDF2 password hash
    private static String createTestPasswordHash() throws Exception {

        char[] password = UUID.randomUUID().toString().toCharArray();

        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        PBEKeySpec spec = new PBEKeySpec(
                password, salt, 210000, 256
        );

        byte[] hash = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .getEncoded();

        spec.clearPassword();

        return "PBKDF2$210000$"
                + Base64.getEncoder().encodeToString(salt)
                + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    @FunctionalInterface
    private interface TestAction {
        void run() throws Exception;
    }
}
