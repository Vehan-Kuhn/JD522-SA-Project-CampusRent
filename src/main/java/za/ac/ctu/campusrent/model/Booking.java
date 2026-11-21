package za.ac.ctu.campusrent.model;
import java.time.LocalDateTime;
import java.time.LocalDate;

/** Represents Bookings in CampusRent **/

public class Booking implements Chargeable {
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
    private Person renter;
    private Equipment rentedEquipment;


    // Default constructor
    public Booking() {
        this.bookingDate = LocalDateTime.now();
    }
    public Booking(Person renter, Equipment equipment,
                   LocalDate startDate, LocalDate dueDate, String notes) {

        this();

        if (renter == null || equipment == null || startDate == null
                || dueDate == null || dueDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "Renter, equipment and valid dates are required.");
        }

        setRenter(renter);
        setRentedEquipment(equipment);

        this.startDate = startDate;
        this.dueDate = dueDate;
        this.dailyRate = equipment.getDailyRate();

        long days = equipment.calculateRentalDays(startDate, dueDate);

        // The discount is calculated polymorphically from the Person subtype.
        this.totalCost = this.dailyRate * days;
        this.totalCost = calculateCharge(renter);

        this.bookingStatus = "PENDING";
        this.notes = notes;
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
    public Person getRenter() {
        return renter;
    }

    public void setRenter(Person renter) {
        this.renter = renter;

        if (renter != null) {
            this.userId = renter.getPersonId();
        }
    }

    public Equipment getRentedEquipment() {
        return rentedEquipment;
    }

    public void setRentedEquipment(Equipment equipment) {
        this.rentedEquipment = equipment;

        if (equipment != null) {
            this.equipmentId = equipment.getEquipmentId();
            this.dailyRate = equipment.getDailyRate();
        }
    }

    @Override
    public double getBaseAmount() {
        return totalCost;
    }

    public BookingStatus getStatusEnum() {
        return BookingStatus.fromDatabase(bookingStatus);
    }

    public boolean transitionTo(BookingStatus next) {
        BookingStatus current = getStatusEnum();

        if (!current.canTransitionTo(next)) {
            return false;
        }

        bookingStatus = next.name();
        return true;
    }
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Booking other)) {
            return false;
        }

        return bookingId > 0 && bookingId == other.bookingId;
    }

    @Override
    public int hashCode() {
        return bookingId > 0
                ? Integer.hashCode(bookingId)
                : java.util.Objects.hash(
                userId, equipmentId, startDate, dueDate);
    }
    public static class Builder {
        private int bookingId;
        private int userId;
        private int equipmentId;
        private LocalDateTime bookingDate = LocalDateTime.now();
        private LocalDate startDate;
        private LocalDate dueDate;
        private LocalDate actualReturnDate;
        private double dailyRate;
        private double totalCost;
        private String bookingStatus = "PENDING";
        private String notes;

        public Builder bookingId(int value) {
            bookingId = value;
            return this;
        }

        public Builder userId(int value) {
            userId = value;
            return this;
        }

        public Builder equipmentId(int value) {
            equipmentId = value;
            return this;
        }

        public Builder bookingDate(LocalDateTime value) {
            bookingDate = value;
            return this;
        }

        public Builder startDate(LocalDate value) {
            startDate = value;
            return this;
        }

        public Builder dueDate(LocalDate value) {
            dueDate = value;
            return this;
        }

        public Builder actualReturnDate(LocalDate value) {
            actualReturnDate = value;
            return this;
        }

        public Builder dailyRate(double value) {
            dailyRate = value;
            return this;
        }

        public Builder totalCost(double value) {
            totalCost = value;
            return this;
        }

        public Builder bookingStatus(String value) {
            bookingStatus = value;
            return this;
        }

        public Builder notes(String value) {
            notes = value;
            return this;
        }

        public Booking build() {
            if (userId <= 0 || equipmentId <= 0
                    || startDate == null || dueDate == null
                    || dueDate.isBefore(startDate)
                    || !Double.isFinite(dailyRate) || dailyRate < 0
                    || !Double.isFinite(totalCost) || totalCost < 0) {
                throw new IllegalArgumentException(
                        "Booking builder contains invalid values.");
            }

            return new Booking(
                    bookingId,
                    userId,
                    equipmentId,
                    bookingDate,
                    startDate,
                    dueDate,
                    actualReturnDate,
                    dailyRate,
                    totalCost,
                    bookingStatus,
                    notes
            );
        }
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