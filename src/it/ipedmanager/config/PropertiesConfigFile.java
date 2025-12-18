package it.ipedmanager.config;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Gestisce un file di configurazione IPED (formato properties).
 */
public class PropertiesConfigFile {

    private Path filePath;
    private Properties properties;
    private boolean loaded;

    public PropertiesConfigFile(Path path) {
        this.filePath = path;
        this.properties = new Properties();
        this.loaded = false;
    }

    public boolean load() {
        if (!Files.exists(filePath)) {
            return false;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            properties.clear();
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                // Remove UTF-8 BOM from first line if present
                if (firstLine) {
                    if (line.startsWith("\uFEFF")) {
                        line = line.substring(1);
                    }
                    firstLine = false;
                }

                line = line.trim();
                if (line.isEmpty() || line.startsWith("#"))
                    continue;

                int eq = line.indexOf('=');
                if (eq > 0) {
                    String key = line.substring(0, eq).trim();
                    String value = line.substring(eq + 1).trim();
                    properties.setProperty(key, value);
                }
            }
            loaded = true;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean save() {
        try {
            // Leggi tutto il file originale per preservare commenti
            List<String> lines = Files.readAllLines(filePath);
            List<String> output = new ArrayList<>();
            Set<String> written = new HashSet<>();

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    output.add(line);
                } else {
                    int eq = trimmed.indexOf('=');
                    if (eq > 0) {
                        String key = trimmed.substring(0, eq).trim();
                        if (properties.containsKey(key)) {
                            output.add(key + " = " + properties.getProperty(key));
                            written.add(key);
                        } else {
                            output.add(line);
                        }
                    } else {
                        output.add(line);
                    }
                }
            }

            // Aggiungi nuove proprieta' non esistenti
            for (String key : properties.stringPropertyNames()) {
                if (!written.contains(key)) {
                    output.add(key + " = " + properties.getProperty(key));
                }
            }

            Files.write(filePath, output);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void backup() {
        try {
            Path backupPath = Paths.get(filePath.toString() + ".bak");
            Files.copy(filePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String get(String key) {
        return properties.getProperty(key, "");
    }

    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    // Alias per compatibilità
    public String getString(String key) {
        String val = properties.getProperty(key);
        return val != null ? val : "";
    }

    public void set(String key, String value) {
        properties.setProperty(key, value);
    }

    // Alias per compatibilità
    public void setString(String key, String value) {
        properties.setProperty(key, value != null ? value : "");
    }

    public boolean getBoolean(String key) {
        String val = get(key).toLowerCase();
        return "true".equals(val) || "yes".equals(val) || "1".equals(val);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String val = properties.getProperty(key);
        if (val == null || val.isEmpty())
            return defaultValue;
        val = val.toLowerCase().trim();
        return "true".equals(val) || "yes".equals(val) || "1".equals(val);
    }

    public void setBoolean(String key, boolean value) {
        set(key, value ? "true" : "false");
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public void setInt(String key, int value) {
        set(key, String.valueOf(value));
    }

    public Path getPath() {
        return filePath;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public Set<String> getKeys() {
        return properties.stringPropertyNames();
    }
}
