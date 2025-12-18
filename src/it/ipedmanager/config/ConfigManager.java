package it.ipedmanager.config;

import java.nio.file.*;
import java.util.*;

/**
 * Singleton che gestisce le configurazioni IPED.
 */
public class ConfigManager {

    private static ConfigManager instance;
    private Path confPath;
    private Path ipedPath;
    private Map<String, PropertiesConfigFile> configFiles;

    // Nomi file configurazione
    public static final String LOCAL_CONFIG = "LocalConfig.txt";
    public static final String IPED_CONFIG = "IPEDConfig.txt";
    public static final String HASH_CONFIG = "HashTaskConfig.txt";
    public static final String OCR_CONFIG = "OCRConfig.txt";
    public static final String AUDIO_CONFIG = "AudioTranscriptConfig.txt";
    public static final String VIDEO_CONFIG = "VideoThumbsConfig.txt";
    public static final String IMAGE_CONFIG = "ImageThumbsConfig.txt";
    public static final String ELASTIC_CONFIG = "ElasticSearchConfig.txt";
    public static final String MINIO_CONFIG = "MinIOConfig.txt";
    public static final String FACE_CONFIG = "FaceRecognitionConfig.txt";
    public static final String PHOTODNA_CONFIG = "PhotoDNAConfig.txt";
    public static final String HTML_REPORT_CONFIG = "HTMLReportConfig.txt";

    private ConfigManager() {
        configFiles = new HashMap<>();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    /**
     * Inizializza con il path di iped.jar
     */
    public void initialize(String ipedJarPath) {
        try {
            this.ipedPath = Paths.get(ipedJarPath).getParent();
            this.confPath = ipedPath.resolve("conf");
            configFiles.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        configFiles.clear();
    }

    public PropertiesConfigFile getConfigFile(String fileName) {
        if (!configFiles.containsKey(fileName)) {
            Path path = confPath.resolve(fileName);
            PropertiesConfigFile config = new PropertiesConfigFile(path);
            if (config.load()) {
                configFiles.put(fileName, config);
            } else {
                return null;
            }
        }
        return configFiles.get(fileName);
    }

    public PropertiesConfigFile getLocalConfig() {
        if (ipedPath == null)
            return null;
        if (!configFiles.containsKey(LOCAL_CONFIG)) {
            // Prima cerca in root IPED
            Path path = ipedPath.resolve(LOCAL_CONFIG);
            PropertiesConfigFile config = new PropertiesConfigFile(path);
            if (config.load()) {
                configFiles.put(LOCAL_CONFIG, config);
            } else {
                // Poi in conf
                path = confPath.resolve(LOCAL_CONFIG);
                config = new PropertiesConfigFile(path);
                if (config.load()) {
                    configFiles.put(LOCAL_CONFIG, config);
                } else {
                    return null;
                }
            }
        }
        return configFiles.get(LOCAL_CONFIG);
    }

    public PropertiesConfigFile getIpedConfig() {
        if (!configFiles.containsKey(IPED_CONFIG)) {
            Path path = ipedPath.resolve(IPED_CONFIG);
            PropertiesConfigFile config = new PropertiesConfigFile(path);
            if (config.load()) {
                configFiles.put(IPED_CONFIG, config);
            } else {
                return null;
            }
        }
        return configFiles.get(IPED_CONFIG);
    }

    // Alias per compatibilità con la nuova API
    public PropertiesConfigFile getIPEDConfig() {
        return getIpedConfig();
    }

    public PropertiesConfigFile getHTMLReportConfig() {
        return getConfigFile(HTML_REPORT_CONFIG);
    }

    public boolean saveAll() {
        boolean success = true;
        for (PropertiesConfigFile config : configFiles.values()) {
            config.backup();
            if (!config.save()) {
                success = false;
            }
        }
        return success;
    }

    public Path getConfPath() {
        return confPath;
    }

    public Path getIpedPath() {
        return ipedPath;
    }

    public boolean isValid() {
        return confPath != null && Files.isDirectory(confPath);
    }

    /**
     * Gets the profiles directory path
     */
    public Path getProfilesPath() {
        return ipedPath.resolve("profiles");
    }

    /**
     * Load configuration from a specific profile.
     * Profile configs override the current config values.
     * 
     * @param profileName Name of the profile (e.g., "forensic", "triage")
     * @return true if profile configs were loaded successfully
     */
    public boolean loadProfileConfig(String profileName) {
        if (profileName == null || profileName.isEmpty()) {
            return false;
        }

        Path profilePath = getProfilesPath().resolve(profileName);
        if (!Files.isDirectory(profilePath)) {
            return false;
        }

        boolean loaded = false;

        // Load IPEDConfig.txt from profile (if exists)
        Path profileIpedConfig = profilePath.resolve(IPED_CONFIG);
        if (Files.exists(profileIpedConfig)) {
            PropertiesConfigFile profileConfig = new PropertiesConfigFile(profileIpedConfig);
            if (profileConfig.load()) {
                // Ensure base IPEDConfig is loaded first
                PropertiesConfigFile currentConfig = getIpedConfig();
                if (currentConfig == null) {
                    // Base config doesn't exist, create it from main iped path
                    Path mainIpedConfig = ipedPath.resolve(IPED_CONFIG);
                    currentConfig = new PropertiesConfigFile(mainIpedConfig);
                    currentConfig.load();
                    configFiles.put(IPED_CONFIG, currentConfig);
                }
                // Merge profile settings into current config
                int changedCount = 0;
                for (String key : profileConfig.getKeys()) {
                    String newValue = profileConfig.get(key);
                    String oldValue = currentConfig.get(key);
                    currentConfig.set(key, newValue);
                    if (!newValue.equals(oldValue)) {
                        changedCount++;
                        System.out.println("[Profile] Changed: " + key + " = " + oldValue + " -> " + newValue);
                    }
                }
                System.out.println("[Profile] Total changed settings: " + changedCount);
                loaded = true;
            }
        }

        // Load config files from profile's conf/ directory (if exists)
        Path profileConfPath = profilePath.resolve("conf");
        if (Files.isDirectory(profileConfPath)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(profileConfPath, "*.txt")) {
                for (Path file : stream) {
                    String fileName = file.getFileName().toString();
                    PropertiesConfigFile profileConfig = new PropertiesConfigFile(file);
                    if (profileConfig.load()) {
                        // Ensure base config is loaded first
                        PropertiesConfigFile currentConfig = getConfigFile(fileName);
                        if (currentConfig == null) {
                            // Base config doesn't exist in cache, load from main conf/
                            Path mainConfFile = confPath.resolve(fileName);
                            if (Files.exists(mainConfFile)) {
                                currentConfig = new PropertiesConfigFile(mainConfFile);
                                currentConfig.load();
                                configFiles.put(fileName, currentConfig);
                            }
                        }
                        // Merge profile settings into current config
                        if (currentConfig != null) {
                            for (String key : profileConfig.getKeys()) {
                                currentConfig.set(key, profileConfig.get(key));
                            }
                            loaded = true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return loaded;
    }
}
