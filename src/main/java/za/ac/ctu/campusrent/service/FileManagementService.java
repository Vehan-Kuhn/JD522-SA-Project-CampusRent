
package za.ac.ctu.campusrent.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class FileManagementService {

    public List<FileInformation> listFiles(Path directory)
            throws IOException {

        List<FileInformation> files = new ArrayList<>();

        if (!Files.exists(directory)) {
            return files;
        }

        try (Stream<Path> paths = Files.walk(directory)) {

            paths.filter(Files::isRegularFile)
                    .forEach(path -> {

                        try {
                            BasicFileAttributes attributes =
                                    Files.readAttributes(
                                            path,
                                            BasicFileAttributes.class
                                    );

                            files.add(new FileInformation(
                                    path,
                                    attributes.size(),
                                    attributes.creationTime().toMillis(),
                                    attributes.lastModifiedTime().toMillis()
                            ));

                        } catch (IOException ex) {
                            throw new FileManagementException(ex);
                        }
                    });

        } catch (FileManagementException ex) {
            throw ex.getCause();
        }

        files.sort(
                Comparator.comparingLong(
                        FileInformation::getLastModified
                ).reversed()
        );

        return files;
    }

    public static class FileInformation {

        private final Path path;
        private final long size;
        private final long created;
        private final long lastModified;

        public FileInformation(
                Path path,
                long size,
                long created,
                long lastModified) {

            this.path = path;
            this.size = size;
            this.created = created;
            this.lastModified = lastModified;
        }

        public Path getPath() {
            return path;
        }

        public long getSize() {
            return size;
        }

        public long getCreated() {
            return created;
        }

        public long getLastModified() {
            return lastModified;
        }
    }

    private static class FileManagementException
            extends RuntimeException {

        public FileManagementException(IOException cause) {
            super(cause);
        }

        @Override
        public synchronized IOException getCause() {
            return (IOException) super.getCause();
        }
    }
}
