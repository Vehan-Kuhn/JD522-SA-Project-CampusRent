
package za.ac.ctu.campusrent.service;

import za.ac.ctu.campusrent.model.AppPreferences;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class PreferencesService {

    private final Path preferencesFile;

    public PreferencesService() {
        this(Path.of("campusrent-data", "preferences.dat"));
    }

    public PreferencesService(Path preferencesFile) {
        this.preferencesFile = preferencesFile;
    }

    public void save(AppPreferences preferences) throws IOException {

        if (preferences == null) {
            throw new IllegalArgumentException(
                    "Preferences cannot be null."
            );
        }

        Path parent = preferencesFile.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (ObjectOutputStream output = new ObjectOutputStream(
                Files.newOutputStream(preferencesFile))) {

            output.writeObject(preferences);
        }
    }

    public AppPreferences load() throws IOException {

        if (!Files.exists(preferencesFile)) {
            return new AppPreferences();
        }

        try (ObjectInputStream input = new ObjectInputStream(
                Files.newInputStream(preferencesFile))) {

            Object object = input.readObject();

            if (object instanceof AppPreferences preferences) {
                return preferences;
            }

            throw new IOException(
                    "The preferences file has an invalid format."
            );

        } catch (ClassNotFoundException ex) {
            throw new IOException(
                    "Could not read saved preferences.", ex
            );
        }
    }

    public Path getPreferencesFile() {
        return preferencesFile;
    }
}
