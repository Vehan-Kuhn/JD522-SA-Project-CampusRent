package za.ac.ctu.campusrent.model;
import java.time.LocalDateTime;
import java.time.LocalDate;

/** Represents Bookings in CampusRent **/

public class Booking {
    private int bookingId;
    private int userId;
    private int equipmentId;
    private LocalDateTime bookingDate;
    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDate actualReturnDate;
    private double dailyRate;
    private double totalCost;
    private String bookingStatus;
    private String notes;


    // Default constructor
    public Booking() {
        this.bookingDate = LocalDateTime.now();
    }
    //Parameterized Constructor
    public Booking(int BookingId, int userId, int equipmentId, LocalDateTime bookingDate, LocalDate startDate,LocalDate dueDate,  LocalDate actualReturnDate, double dailyRate, double totalCost, String bookingStatus, String notes){
        this.bookingId = BookingId;
        this.userId = userId;
        this.equipmentId = equipmentId;
        this.bookingDate = bookingDate;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.actualReturnDate = actualReturnDate;
        this.dailyRate = dailyRate;
        this.totalCost = totalCost;
        this.bookingStatus = bookingStatus;
        this.notes = notes;
    }
    public int getBookingId() {
        return bookingId;
    }
    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public int getEquipmentId() {
        return equipmentId;
    }
    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }
    public LocalDateTime getBookingDate() {
        return bookingDate;
    }
    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }
    public LocalDate getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    public LocalDate getDueDate() {
        return dueDate;
    }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    public LocalDate getActualReturnDate() {
        return actualReturnDate;
    }
    public void setActualReturnDate(LocalDate actualReturnDate) {
        this.actualReturnDate = actualReturnDate;
    }
    public double getDailyRate() {
        return dailyRate;
    }
    public void setDailyRate(double dailyRate) {
        this.dailyRate = dailyRate;
    }
    public double getTotalCost() {
        return totalCost;
    }
    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }
    public String getBookingStatus() {
        return bookingStatus;
    }
    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }

    //Displays Booking Information
    @Override
    public String toString() {
        return "Booking{" +
                "bookingId=" + bookingId +
                ", userId=" + userId +
                ", equipmentId=" + equipmentId +
                ", startDate=" + startDate +
                ", dueDate=" + dueDate +
                ", totalCost=" + totalCost +
                ", bookingStatus='" + bookingStatus + '\'' +
                '}';
    }
}