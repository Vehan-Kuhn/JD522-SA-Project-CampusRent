
package za.ac.ctu.campusrent.service;

import za.ac.ctu.campusrent.model.Booking;
import za.ac.ctu.campusrent.model.Equipment;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CsvService {

    private final EquipmentService equipmentService;
    private final BookingService bookingService;

    public CsvService() {
        equipmentService = new EquipmentService();
        bookingService = new BookingService();
    }

    public ImportResult importEquipment(Path file) throws Exception {

        if (file == null || !Files.isRegularFile(file)) {
            throw new IOException("The selected CSV file does not exist.");
        }

        List<String> lines = Files.readAllLines(
                file, StandardCharsets.UTF_8
        );

        if (lines.isEmpty()) {
            throw new IOException("The CSV file is empty.");
        }

        ImportResult result = new ImportResult();

        for (int i = 1; i < lines.size(); i++) {

            String line = lines.get(i);

            if (line == null || line.isBlank()) {
                continue;
            }

            try {
                List<String> values = parseCsvLine(line);

                int campusIndex;

                if (values.size() == 9) {
                    // Exported format includes equipment_id first.
                    campusIndex = 1;
                } else if (values.size() == 8) {
                    // Import format without equipment_id.
                    campusIndex = 0;
                } else {
                    throw new IllegalArgumentException(
                            "Expected 8 or 9 columns, but found "
                                    + values.size()
                    );
                }

                Equipment equipment = new Equipment();

                equipment.setCampusId(
                        Integer.parseInt(values.get(campusIndex).trim())
                );

                equipment.setSerialNumber(
                        values.get(campusIndex + 1).trim()
                );

                equipment.setEquipmentName(
                        values.get(campusIndex + 2).trim()
                );

                equipment.setDescription(
                        values.get(campusIndex + 3).trim()
                );

                equipment.setCategory(
                        values.get(campusIndex + 4).trim()
                );

                equipment.setDailyRate(
                        Double.parseDouble(
                                values.get(campusIndex + 5).trim()
                        )
                );

                equipment.setConditionStatus(
                        values.get(campusIndex + 6).trim()
                );

                equipment.setAvailabilityStatus(
                        values.get(campusIndex + 7).trim()
                );

                equipmentService.addEquipment(equipment);

                result.imported++;

            } catch (Exception ex) {
                result.failed++;

                result.errors.add(
                        "Row " + (i + 1) + ": " + ex.getMessage()
                );
            }
        }

        return result;
    }

    public void exportEquipment(Path file) throws Exception {

        List<Equipment> equipment =
                equipmentService.getAllEquipment();

        try (BufferedWriter writer = Files.newBufferedWriter(
                file, StandardCharsets.UTF_8)) {

            writer.write(
                    "equipment_id,campus_id,serial_number,equipment_name,"
                            + "description,category,daily_rate,"
                            + "condition_status,availability_status"
            );

            writer.newLine();

            for (Equipment item : equipment) {

                writeRow(
                        writer,
                        item.getEquipmentId(),
                        item.getCampusId(),
                        item.getSerialNumber(),
                        item.getEquipmentName(),
                        item.getDescription(),
                        item.getCategory(),
                        item.getDailyRate(),
                        item.getConditionStatus(),
                        item.getAvailabilityStatus()
                );
            }
        }
    }

    public void exportBookings(Path file) throws Exception {

        List<Booking> bookings =
                bookingService.getAllBookings();

        try (BufferedWriter writer = Files.newBufferedWriter(
                file, StandardCharsets.UTF_8)) {

            writer.write(
                    "booking_id,user_id,equipment_id,booking_date,"
                            + "start_date,due_date,actual_return_date,"
                            + "daily_rate,total_cost,booking_status,notes"
            );

            writer.newLine();

            for (Booking booking : bookings) {

                writeRow(
                        writer,
                        booking.getBookingId(),
                        booking.getUserId(),
                        booking.getEquipmentId(),
                        booking.getBookingDate(),
                        booking.getStartDate(),
                        booking.getDueDate(),
                        booking.getActualReturnDate(),
                        booking.getDailyRate(),
                        booking.getTotalCost(),
                        booking.getBookingStatus(),
                        booking.getNotes()
                );
            }
        }
    }

    private void writeRow(
            BufferedWriter writer,
            Object... values) throws IOException {

        for (int i = 0; i < values.length; i++) {

            if (i > 0) {
                writer.write(",");
            }

            String value = values[i] == null
                    ? ""
                    : values[i].toString();

            writer.write(escapeCsv(value));
        }

        writer.newLine();
    }

    private String escapeCsv(String value) {

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private List<String> parseCsvLine(String line) {

        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {

            char character = line.charAt(i);

            if (character == '"') {

                if (insideQuotes
                        && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {

                    current.append('"');
                    i++;

                } else {
                    insideQuotes = !insideQuotes;
                }

            } else if (character == ',' && !insideQuotes) {

                values.add(current.toString());
                current.setLength(0);

            } else {
                current.append(character);
            }
        }

        if (insideQuotes) {
            throw new IllegalArgumentException(
                    "Unclosed quotation mark in CSV row."
            );
        }

        values.add(current.toString());

        return values;
    }

    public static class ImportResult {

        private int imported;
        private int failed;

        private final List<String> errors = new ArrayList<>();

        public int getImported() {
            return imported;
        }

        public int getFailed() {
            return failed;
        }

        public List<String> getErrors() {
            return List.copyOf(errors);
        }
    }
}
