
package za.ac.ctu.campusrent.gui;

import za.ac.ctu.campusrent.model.AppPreferences;
import za.ac.ctu.campusrent.service.PreferencesService;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class LanguageManager {

    private static final String BASE_NAME =
            "za.ac.ctu.campusrent.i18n.Messages";

    private static Locale currentLocale = Locale.forLanguageTag("en-ZA");

    private static ResourceBundle currentBundle;
    private static ResourceBundle englishBundle;

    static {
        loadBundles();
    }

    private LanguageManager() {
    }

    private static void loadBundles() {

        englishBundle = ResourceBundle.getBundle(
                BASE_NAME,
                Locale.forLanguageTag("en-ZA")
        );

        currentBundle = ResourceBundle.getBundle(
                BASE_NAME,
                currentLocale
        );
    }

    public static void setLanguage(String language) {

        if (language == null || language.isBlank()) {
            language = "en";
        }

        currentLocale = switch (language.toLowerCase(Locale.ROOT)) {
            case "af" -> Locale.forLanguageTag("af-ZA");
            case "zu" -> Locale.forLanguageTag("zu-ZA");
            default -> Locale.forLanguageTag("en-ZA");
        };

        loadBundles();
    }

    public static String getLanguage() {
        return currentLocale.getLanguage();
    }

    public static Locale getLocale() {
        return currentLocale;
    }

    public static String text(String key) {

        try {
            return currentBundle.getString(key);

        } catch (MissingResourceException ex) {

            try {
                return englishBundle.getString(key);

            } catch (MissingResourceException ignored) {
                return "[" + key + "]";
            }
        }
    }

    public static void loadSavedLanguage() {

        try {
            AppPreferences preferences =
                    new PreferencesService().load();

            setLanguage(preferences.getLanguage());

        } catch (Exception ex) {
            setLanguage("en");
        }
    }
}
