package za.ac.ctu.campusrent.gui;
import za.ac.ctu.campusrent.model.Booking;
import javax.swing.table.AbstractTableModel;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Read-only table model for CampusRent bookings. */
public class BookingTableModel extends AbstractTableModel {

    private final String[] columns = {
            "Booking ID", "User ID", "Equipment ID", "Start Date",
            "Due Date", "Daily Rate", "Total Cost", "Status"
    };

    private List<Booking> bookings = new ArrayList<>();
    private final NumberFormat currency =
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-ZA"));
    private final DateTimeFormatter dateFormat =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings == null ? new ArrayList<>() : new ArrayList<>(bookings);
        fireTableDataChanged();
    }

    public Booking getBookingAt(int row) {
        if (row < 0 || row >= bookings.size()) {
            return null;
        }
        return bookings.get(row);
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
        return switch (columnIndex) {
            case 0 -> booking.getBookingId();
            case 1 -> booking.getUserId();
            case 2 -> booking.getEquipmentId();
            case 3 -> booking.getStartDate() == null ? "" : booking.getStartDate().format(dateFormat);
            case 4 -> booking.getDueDate() == null ? "" : booking.getDueDate().format(dateFormat);
            case 5 -> currency.format(booking.getDailyRate());
            case 6 -> currency.format(booking.getTotalCost());
            case 7 -> booking.getBookingStatus();
            default -> "";
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}

