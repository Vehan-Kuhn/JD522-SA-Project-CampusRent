package za.ac.ctu.campusrent.model;
import java.io.Serializable;
public class AppPreferences implements Serializable {

    private static final long serialVersionUID = 1L;

    private String language = "en";
    private String theme = "LIGHT";
    private String exportDirectory = "exports";
    private String backupDirectory = "backups";
    private boolean automaticBackup = false;

    // Sensitive information must never be serialized.
    private transient String password;

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
