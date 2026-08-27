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
    private String activeProfile;

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
    public static final String CSAM_CONFIG = "CSAMDetectorConfig.txt";
    public static final String AGE_CONFIG = "AgeEstimationConfig.txt";
    public static final String REMOTE_CLASSIFIER_CONFIG = "RemoteImageClassifierConfig.txt";


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

    public void setActiveProfile(String profileName) {
        if (profileName == null || profileName.equals("Personalizzato") || profileName.isEmpty()) {
            this.activeProfile = null;
        } else {
            this.activeProfile = profileName;
        }
        reload(); // Svuota la cache in modo che la GUI ricarichi i file dal percorso giusto
    }

    public String getActiveProfile() {
        return this.activeProfile;
    }

    public PropertiesConfigFile getConfigFile(String fileName) {
        if (!configFiles.containsKey(fileName)) {
            Path path = null;
            
            // Se c'è un profilo attivo, cerca il file nel profilo
            if (activeProfile != null) {
                Path profileConfDir = getProfilesPath().resolve(activeProfile).resolve("conf");
                path = profileConfDir.resolve(fileName);
                
                // Se il file non esiste nel profilo, copialo dalla base in modo che i futuri salvataggi vadano al profilo
                if (!Files.exists(path)) {
                    Path basePath = confPath.resolve(fileName);
                    if (Files.exists(basePath)) {
                        try {
                            if (!Files.exists(profileConfDir)) {
                                Files.createDirectories(profileConfDir);
                            }
                            Files.copy(basePath, path, StandardCopyOption.REPLACE_EXISTING);
                        } catch (Exception e) {
                            e.printStackTrace();
                            path = basePath; // Fallback alla base in caso di errore
                        }
                    } else {
                        path = basePath; // File non esiste nemmeno nella base
                    }
                }
            } else {
                path = confPath.resolve(fileName);
            }
            
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
            Path path = null;
            
            // Se c'è un profilo attivo, cerca IPEDConfig.txt nel profilo
            if (activeProfile != null) {
                Path profileDir = getProfilesPath().resolve(activeProfile);
                path = profileDir.resolve(IPED_CONFIG);
                
                // Se il file non esiste nel profilo, copialo dalla base
                if (!Files.exists(path)) {
                    Path basePath = ipedPath.resolve(IPED_CONFIG);
                    if (Files.exists(basePath)) {
                        try {
                            if (!Files.exists(profileDir)) {
                                Files.createDirectories(profileDir);
                            }
                            Files.copy(basePath, path, StandardCopyOption.REPLACE_EXISTING);
                        } catch (Exception e) {
                            e.printStackTrace();
                            path = basePath;
                        }
                    } else {
                        path = basePath;
                    }
                }
            } else {
                path = ipedPath.resolve(IPED_CONFIG);
            }
            
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
        // Obsoleto: usare setActiveProfile(profileName)
        setActiveProfile(profileName);
        return true;
    }
}
