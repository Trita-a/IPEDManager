package it.ipedmanager.utils;

import java.util.Locale;
import java.util.ResourceBundle;

public class BundleManager {

    private static final String BUNDLE_NAME = "it.ipedmanager.resources.messages";
    private static ResourceBundle resourceBundle;
    private static Locale currentLocale = Locale.ITALIAN; // Default

    static {
        // Initial load with default (Italian)
        loadBundle();
    }

    public static void loadFromConfig(String localeStr) {
        if (localeStr == null || localeStr.isEmpty())
            return;
        // Handle "pt-BR" -> "pt" if needed, or let ResourceBundle handle it.
        // Locale.forLanguageTag handles "pt-BR" correctly.
        try {
            Locale l = Locale.forLanguageTag(localeStr);
            setLocale(l);
        } catch (Exception e) {
            System.err.println("Invalid locale: " + localeStr);
        }
    }

    private static void loadBundle() {
        try {
            resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, currentLocale);
        } catch (Exception e) {
            System.err.println("Error loading resource bundle: " + e.getMessage());
            // Fallback to default locale if specific fails, or ensure at least base exists
            resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.ROOT);
        }
    }

    public static void setLocale(Locale locale) {
        currentLocale = locale;
        loadBundle();
    }

    public static Locale getLocale() {
        return currentLocale;
    }

    public static String getString(String key) {
        try {
            return resourceBundle.getString(key);
        } catch (Exception e) {
            return "!" + key + "!";
        }
    }

    public static String getString(String key, Object... args) {
        try {
            String pattern = resourceBundle.getString(key);
            return java.text.MessageFormat.format(pattern, args);
        } catch (Exception e) {
            return "!" + key + "!";
        }
    }
}
