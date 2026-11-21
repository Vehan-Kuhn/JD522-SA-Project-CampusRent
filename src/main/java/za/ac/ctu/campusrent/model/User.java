package za.ac.ctu.campusrent.model;
import java.time.LocalDateTime;
/** Represents a user within the CampusRent system. **/
public class User {

    // Instance variables
    private int userId;
    private int campusId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String passwordHash;
    private String userRole;
    private String accountStatus;
    private LocalDateTime createdAt;

    // Default constructor
    public User() {
        this.createdAt = LocalDateTime.now();
    }

    // Parameterized constructor
    public User(int userId, int campusId,
                String firstName, String lastName,
                String email, String phone,
                String passwordHash, String userRole,
                String accountStatus, LocalDateTime createdAt) {

        this.userId = userId;
        this.campusId = campusId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.userRole = userRole;
        this.accountStatus = accountStatus;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public int getCampusId() {
        return campusId;
    }
    public void setCampusId(int campusId) {
        this.campusId = campusId;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    public String getUserRole() {
        return userRole;
    }
    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
    public String getAccountStatus() {
        return accountStatus;
    }
    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Display user information
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", userRole='" + userRole + '\'' +
                ", accountStatus='" + accountStatus + '\'' +
                '}';
    }
}
