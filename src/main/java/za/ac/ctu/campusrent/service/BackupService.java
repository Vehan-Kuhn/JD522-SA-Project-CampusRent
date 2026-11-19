
package za.ac.ctu.campusrent.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

public class BackupService {

    private final CsvService csvService;
    private final Path dataDirectory;
    private final Path backupDirectory;

    public BackupService() {
        this(
                new CsvService(),
                Path.of("campusrent-data"),
                Path.of("campusrent-backups")
        );
    }

    public BackupService(
            CsvService csvService,
            Path dataDirectory,
            Path backupDirectory) {

        if (csvService == null
                || dataDirectory == null
                || backupDirectory == null) {
            throw new IllegalArgumentException(
                    "Backup configuration cannot contain null values."
            );
        }

        this.csvService = csvService;
        this.dataDirectory = dataDirectory.toAbsolutePath().normalize();
        this.backupDirectory = backupDirectory.toAbsolutePath().normalize();
    }

    public Path createBackup() throws Exception {

        if (dataDirectory.equals(backupDirectory)
                || backupDirectory.startsWith(dataDirectory)) {
            throw new IOException(
                    "The backup directory cannot be inside the data directory."
            );
        }

        Files.createDirectories(dataDirectory);
        Files.createDirectories(backupDirectory);

        // Export the latest database records.
        csvService.exportEquipment(
                dataDirectory.resolve("equipment.csv")
        );

        csvService.exportBookings(
                dataDirectory.resolve("bookings.csv")
        );

        String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSSSSS")
        );

        Path destination = backupDirectory.resolve(
                "CampusRent_Backup_" + timestamp
        );

        Files.createDirectories(destination);

        copyDirectory(dataDirectory, destination);

        return destination;
    }

    private void copyDirectory(
            Path source,
            Path destination) throws IOException {

        try (Stream<Path> paths = Files.walk(source)) {

            paths.forEach(sourcePath -> {

                try {
                    Path relativePath = source.relativize(sourcePath);
                    Path destinationPath =
                            destination.resolve(relativePath);

                    if (Files.isDirectory(sourcePath)) {

                        Files.createDirectories(destinationPath);

                    } else if (Files.isRegularFile(sourcePath)) {

                        Files.copy(
                                sourcePath,
                                destinationPath,
                                StandardCopyOption.REPLACE_EXISTING,
                                StandardCopyOption.COPY_ATTRIBUTES
                        );
                    }

                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });

        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }

    public Path getBackupDirectory() {
        return backupDirectory;
    }
}
