package za.ac.ctu.campusrent.service;
import za.ac.ctu.campusrent.dao.UserDAO;
import za.ac.ctu.campusrent.model.User;
import za.ac.ctu.campusrent.util.PasswordUtil;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Handles user business logic, validation and authentication. */
public class UserService {

    private final UserDAO userDAO;

    private static final Set<String> VALID_ROLES =
            Set.of("STUDENT", "STAFF", "ADMIN");

    private static final Set<String> VALID_STATUSES =
            Set.of("ACTIVE", "INACTIVE");

    public UserService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Registers a user from a plain-text password.
     * The password is hashed before it reaches the DAO.
     */
    public int registerUser(User user, String password) throws SQLException {
        validateUser(user);
        validatePassword(password);

        String email = normaliseEmail(user.getEmail());
        user.setEmail(email);

        if (userDAO.getUserByEmail(email) != null) {
            throw new IllegalArgumentException(
                    "A user with this email already exists."
            );
        }

        user.setPasswordHash(PasswordUtil.hashPassword(password));
        return userDAO.addUser(user);
    }

    /**
     * Retained for compatibility with existing tests and internal code
     * that already supplies a password hash.
     */
    public int addUser(User user) throws SQLException {
        validateUser(user);

        if (user.getPasswordHash() == null
                || user.getPasswordHash().isBlank()) {
            throw new IllegalArgumentException("A password hash is required.");
        }

        user.setEmail(normaliseEmail(user.getEmail()));

        if (userDAO.getUserByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException(
                    "A user with this email already exists."
            );
        }

        return userDAO.addUser(user);
    }

    /**
     * Authenticates a user. Returns null for invalid credentials or
     * inactive accounts. The returned user has no password hash.
     */
    public User authenticate(String email, String password)
            throws SQLException {

        if (email == null || email.isBlank()
                || password == null || password.isEmpty()) {
            return null;
        }

        User user = userDAO.getUserByEmail(normaliseEmail(email));

        if (user == null) {
            return null;
        }

        if (!"ACTIVE".equals(user.getAccountStatus())) {
            return null;
        }

        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            return null;
        }

        user.setPasswordHash(null);
        return user;
    }

    // Retrieve a user by ID without exposing the stored password hash.
    public User getUserById(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID.");
        }

        User user = userDAO.getUserById(userId);
        hidePasswordHash(user);
        return user;
    }

    // Retrieve a user by email without exposing the stored password hash.
    public User getUserByEmail(String email) throws SQLException {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email address.");
        }

        User user = userDAO.getUserByEmail(normaliseEmail(email));
        hidePasswordHash(user);
        return user;
    }

    // Retrieve all users without exposing password hashes.
    public List<User> getAllUsers() throws SQLException {
        List<User> users = userDAO.getAllUsers();
        users.forEach(this::hidePasswordHash);
        return users;
    }

    // Update an existing user.
    public boolean updateUser(User user) throws SQLException {
        if (user == null || user.getUserId() <= 0) {
            throw new IllegalArgumentException("Invalid user information.");
        }

        validateUser(user);
        user.setEmail(normaliseEmail(user.getEmail()));

        User existingByEmail = userDAO.getUserByEmail(user.getEmail());

        if (existingByEmail != null
                && existingByEmail.getUserId() != user.getUserId()) {
            throw new IllegalArgumentException(
                    "This email address is already in use."
            );
        }

        User existingUser = userDAO.getUserById(user.getUserId());
        if (existingUser == null) {
            return false;
        }

        // A profile edit must not erase a password hash.
        if (user.getPasswordHash() == null
                || user.getPasswordHash().isBlank()) {
            user.setPasswordHash(existingUser.getPasswordHash());
        }

        return userDAO.updateUser(user);
    }

    /**
     * Changes a user's password after verifying the current password.
     */
    public boolean changePassword(
            int userId, String currentPassword, String newPassword)
            throws SQLException {

        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID.");
        }

        validatePassword(newPassword);

        User user = userDAO.getUserById(userId);
        if (user == null) {
            return false;
        }

        if (!PasswordUtil.verifyPassword(
                currentPassword, user.getPasswordHash())) {
            return false;
        }

        user.setPasswordHash(PasswordUtil.hashPassword(newPassword));
        return userDAO.updateUser(user);
    }

    // Delete a user.
    public boolean deleteUser(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID.");
        }

        return userDAO.deleteUser(userId);
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be empty.");
        }

        if (user.getCampusId() <= 0) {
            throw new IllegalArgumentException("A valid campus is required.");
        }

        if (user.getFirstName() == null || user.getFirstName().isBlank()) {
            throw new IllegalArgumentException("First name is required.");
        }

        if (user.getLastName() == null || user.getLastName().isBlank()) {
            throw new IllegalArgumentException("Last name is required.");
        }

        if (user.getEmail() == null || !isValidEmail(user.getEmail())) {
            throw new IllegalArgumentException("A valid email is required.");
        }

        if (user.getUserRole() == null
                || !VALID_ROLES.contains(user.getUserRole().toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Invalid user role.");
        }

        if (user.getAccountStatus() == null
                || !VALID_STATUSES.contains(
                user.getAccountStatus().toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Invalid account status.");
        }

        user.setUserRole(user.getUserRole().toUpperCase(Locale.ROOT));
        user.setAccountStatus(user.getAccountStatus().toUpperCase(Locale.ROOT));
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters."
            );
        }

        if (password.length() > 256) {
            throw new IllegalArgumentException(
                    "Password cannot exceed 256 characters."
            );
        }
    }

    private String normaliseEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isValidEmail(String email) {
        return email != null
                && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private void hidePasswordHash(User user) {
        if (user != null) {
            user.setPasswordHash(null);
        }
    }
}
