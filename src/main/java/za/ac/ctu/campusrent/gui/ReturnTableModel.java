package za.ac.ctu.campusrent.gui;
import za.ac.ctu.campusrent.model.Booking;
import javax.swing.table.AbstractTableModel;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Read-only table model for bookings awaiting equipment return. */
public class ReturnTableModel extends AbstractTableModel {

    private static final double LATE_FEE_PER_DAY = 50.00;
    private final String[] columns = {
            "Booking ID", "User ID", "Equipment ID", "Due Date",
            "Status", "Days Overdue", "Estimated Late Fee"
    };

    private List<Booking> bookings = new ArrayList<>();
    private final DateTimeFormatter dateFormat =
            DateTimeFormatter.ofPattern("dd MMM yyyy");
    private final NumberFormat currency =
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-ZA"));

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings == null ? new ArrayList<>() : new ArrayList<>(bookings);
        fireTableDataChanged();
    }

    public Booking getBookingAt(int row) {
        return row < 0 || row >= bookings.size() ? null : bookings.get(row);
    }

    @Override
    public int getRowCount() {
        return bookings.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Booking booking = bookings.get(rowIndex);
        long daysOverdue = getDaysOverdue(booking);

        return switch (columnIndex) {
            case 0 -> booking.getBookingId();
            case 1 -> booking.getUserId();
            case 2 -> booking.getEquipmentId();
            case 3 -> booking.getDueDate() == null ? "" : booking.getDueDate().format(dateFormat);
            case 4 -> booking.getBookingStatus();
            case 5 -> daysOverdue;
            case 6 -> currency.format(daysOverdue * LATE_FEE_PER_DAY);
            default -> "";
        };
    }

    public long getDaysOverdue(Booking booking) {
        if (booking == null || booking.getDueDate() == null) {
            return 0;
        }
        LocalDate comparisonDate = booking.getActualReturnDate() == null
                ? LocalDate.now() : booking.getActualReturnDate();
        return Math.max(0, ChronoUnit.DAYS.between(booking.getDueDate(), comparisonDate));
    }

    public double getEstimatedLateFee(Booking booking) {
        return getDaysOverdue(booking) * LATE_FEE_PER_DAY;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
