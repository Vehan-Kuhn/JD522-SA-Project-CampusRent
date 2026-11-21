package za.ac.ctu.campusrent.model;
import java.io.Serializable;
public class AppPreferences implements Serializable {

    private static final long serialVersionUID = 1L;

    private String language = "en";
    private String theme = "LIGHT";
    private String exportDirectory = "exports";
    private String backupDirectory = "backups";
    private boolean automaticBackup = false;
    private int lastUserId = -1;
    private int windowWidth = 1200;
    private int windowHeight = 800;

    // Sensitive information must never be serialized.
    private transient String password;

    public int getLastUserId() {
        return lastUserId;
    }

    public void setLastUserId(int lastUserId) {
        this.lastUserId = lastUserId;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        if (windowWidth < 640) {
            throw new IllegalArgumentException(
                    "Window width is too small.");
        }

        this.windowWidth = windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        if (windowHeight < 480) {
            throw new IllegalArgumentException(
                    "Window height is too small.");
        }

        this.windowHeight = windowHeight;
    }
    public AppPreferences() {
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getExportDirectory() {
        return exportDirectory;
    }

    public void setExportDirectory(String exportDirectory) {
        this.exportDirectory = exportDirectory;
    }

    public String getBackupDirectory() {
        return backupDirectory;
    }

    public void setBackupDirectory(String backupDirectory) {
        this.backupDirectory = backupDirectory;
    }

    public boolean isAutomaticBackup() {
        return automaticBackup;
    }

    public void setAutomaticBackup(boolean automaticBackup) {
        this.automaticBackup = automaticBackup;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
