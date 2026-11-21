package za.ac.ctu.campusrent.model;

import java.time.LocalDateTime;
import java.util.Objects;

public final class Receipt {
    private final String receiptNumber;
    private final int bookingId;
    private final String customerName;
    private final String equipmentDescription;
    private final double amount;
    private final LocalDateTime issuedAt;

    public Receipt(String receiptNumber, int bookingId, String customerName,
                   String equipmentDescription, double amount,
                   LocalDateTime issuedAt) {

        if (receiptNumber == null || receiptNumber.isBlank()
                || bookingId <= 0
                || !Double.isFinite(amount) || amount < 0) {
            throw new IllegalArgumentException("Invalid receipt details.");
        }

        this.receiptNumber = receiptNumber;
        this.bookingId = bookingId;
        this.customerName = Objects.requireNonNull(customerName);
        this.equipmentDescription =
                Objects.requireNonNull(equipmentDescription);
        this.amount = amount;
        this.issuedAt = Objects.requireNonNull(issuedAt);
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public int getBookingId() {
        return bookingId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getEquipmentDescription() {
        return equipmentDescription;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }
}