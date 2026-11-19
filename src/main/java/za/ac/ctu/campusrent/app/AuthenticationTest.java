package za.ac.ctu.campusrent.app;
import za.ac.ctu.campusrent.model.User;
import za.ac.ctu.campusrent.service.UserService;
import za.ac.ctu.campusrent.util.PasswordUtil;

public class AuthenticationTest {

    private static final UserService userService = new UserService();

    public static void main(String[] args) throws Exception {

        System.out.println("======================================");
        System.out.println("     CAMPUSRENT AUTHENTICATION TESTS");
        System.out.println("======================================");

        // Unique email addresses so tests can be run repeatedly.
        String uniqueId = String.valueOf(System.currentTimeMillis());

        String activeEmail = "active" + uniqueId + "@test.com";
        String inactiveEmail = "inactive" + uniqueId + "@test.com";

        int activeUserId = 0;
        int inactiveUserId = 0;

        try {
            // TEST 1: Register a new active user.
            User activeUser = createTestUser(activeEmail, "ACTIVE");

            activeUserId = userService.registerUser(
                    activeUser, "CampusRent123");

            test("Register a new user", activeUserId > 0);

            // TEST 2: Confirm that the password was hashed.
            boolean passwordHashed =
                    activeUser.getPasswordHash() != null
                            && !activeUser.getPasswordHash().equals("CampusRent123")
                            && PasswordUtil.verifyPassword(
                            "CampusRent123", activeUser.getPasswordHash());

            test("Password is securely hashed", passwordHashed);

            // TEST 3: Reject duplicate email addresses.
            boolean duplicateRejected = false;

            try {
                userService.registerUser(
                        createTestUser(activeEmail, "ACTIVE"),
                        "AnotherPassword123");
            } catch (IllegalArgumentException e) {
                duplicateRejected = true;
            }

            test("Reject duplicate email", duplicateRejected);

            // TEST 4: Login with the correct password.
            User loggedInUser = userService.authenticate(
                    activeEmail, "CampusRent123");

            test("Login with correct password",
                    loggedInUser != null
                            && loggedInUser.getPasswordHash() == null);

            // TEST 5: Reject an incorrect password.
            User incorrectLogin = userService.authenticate(
                    activeEmail, "WrongPassword123");

            test("Reject incorrect password",
                    incorrectLogin == null);

            // TEST 6: Register an inactive user.
            User inactiveUser = createTestUser(
                    inactiveEmail, "INACTIVE");

            inactiveUserId = userService.registerUser(
                    inactiveUser, "InactivePass123");

            test("Register inactive user", inactiveUserId > 0);

            // TEST 7: Reject login for inactive account.
            User inactiveLogin = userService.authenticate(
                    inactiveEmail, "InactivePass123");

            test("Reject inactive account login",
                    inactiveLogin == null);

            // TEST 8: Change password using the correct current password.
            boolean passwordChanged = userService.changePassword(
                    activeUserId,
                    "CampusRent123",
                    "NewCampusPass123");

            test("Change password", passwordChanged);

            // TEST 9: Reject the old password.
            User oldPasswordLogin = userService.authenticate(
                    activeEmail, "CampusRent123");

            test("Reject old password",
                    oldPasswordLogin == null);

            // TEST 10: Accept the new password.
            User newPasswordLogin = userService.authenticate(
                    activeEmail, "NewCampusPass123");

            test("Accept new password",
                    newPasswordLogin != null);

        } finally {

            // Clean up test users so the database stays tidy.
            if (activeUserId > 0) {
                userService.deleteUser(activeUserId);
            }

            if (inactiveUserId > 0) {
                userService.deleteUser(inactiveUserId);
            }
        }

        System.out.println("======================================");
        System.out.println("AUTHENTICATION TESTS COMPLETED");
        System.out.println("======================================");
    }

    private static User createTestUser(
            String email, String accountStatus) {

        User user = new User();

        user.setCampusId(1);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail(email);
        user.setPhone("0123456789");
        user.setUserRole("STUDENT");
        user.setAccountStatus(accountStatus);

        return user;
    }

    private static void test(String testName, boolean passed) {

        System.out.println("\nTEST: " + testName);

        if (passed) {
            System.out.println("RESULT: PASSED");
        } else {
            System.out.println("RESULT: FAILED");
            throw new AssertionError(testName + " failed.");
        }
    }
}
