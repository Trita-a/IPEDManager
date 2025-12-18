/*
 * ConfigValidator - Validates IPED configuration dependencies
 */
package it.ipedmanager.utils;

import it.ipedmanager.config.ConfigManager;
import it.ipedmanager.config.PropertiesConfigFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Porting of legacy ConfigValidator.
 * Validates dependencies between IPED configuration options.
 */
public class ConfigValidator {

    private static ConfigValidator instance;

    private ConfigValidator() {
    }

    public static ConfigValidator getInstance() {
        if (instance == null) {
            instance = new ConfigValidator();
        }
        return instance;
    }

    /**
     * Validates all configuration dependencies.
     * 
     * @return List of errors found (empty if all ok)
     */
    public List<String> validateConfiguration() {
        List<String> errors = new ArrayList<>();
        ConfigManager configManager = ConfigManager.getInstance();

        // Ensure configs are loaded
        PropertiesConfigFile ipedConfig = configManager.getIpedConfig();
        if (ipedConfig == null) {
            errors.add("Errore: IPEDConfig.txt non trovato o non caricato.");
            return errors;
        }

        try {
            // 1. Verify enableAutomaticExportFiles
            if (ipedConfig.getBoolean("enableAutomaticExportFiles", false)) {
                if (!hasConfiguredCategoriesToExport() && !hasConfiguredKeywordsToExport()) {
                    errors.add("ATTENZIONE: 'Esportazione Automatica File' è attiva ma non hai configurato:\n" +
                            "   • Nessuna categoria in CategoriesToExport.txt\n" +
                            "   • Nessuna keyword in KeywordsToExport.txt\n\n" +
                            "Soluzione: Decommentare almeno una categoria in conf/CategoriesToExport.txt\n" +
                            "oppure disabilitare 'Esportazione Automatica' in IPEDConfig.");
                }
            }

            // 2. Verify enableImageSimilarity requires enableImageThumbs
            if (ipedConfig.getBoolean("enableImageSimilarity", false)) {
                if (!ipedConfig.getBoolean("enableImageThumbs", false)) {
                    errors.add("ATTENZIONE: 'Ricerca Immagini Simili' richiede 'Miniature Immagini' attivo.\n" +
                            "Soluzione: Attivare 'Creazione Miniature' (ImageConfig).");
                }
            }

            // 3. Verify Cloud Services (ElasticSearch, MinIO)
            if (ipedConfig.getBoolean("enableIndexToElasticSearch", false)) {
                if (!isElasticSearchConfigured()) {
                    errors.add("ATTENZIONE: 'ElasticSearch' è attivo ma non configurato.\n" +
                            "Soluzione: Configurare host e port in ElasticSearchConfig.");
                }
            }

            if (ipedConfig.getBoolean("enableMinIO", false)) {
                if (!isMinIOConfigured()) {
                    errors.add("ATTENZIONE: 'MinIO' è attivo ma non configurato.\n" +
                            "Soluzione: Configurare host e port in MinIOConfig.");
                }
            }

            // 4. Verify HashDBLookup requires hashesDB configured
            if (ipedConfig.getBoolean("enableHashDBLookup", false) ||
                    ipedConfig.getBoolean("enablePhotoDNALookup", false)) {
                if (!isHashesDBConfigured()) {
                    errors.add("ATTENZIONE: 'Hash DB Lookup' o 'PhotoDNA Lookup' richiede hashesDB configurato.\n" +
                            "Soluzione: Configurare percorso hashesDB in LocalConfig.");
                }
            }

        } catch (Exception e) {
            errors.add("Errore durante la validazione: " + e.getMessage());
        }

        return errors;
    }

    /**
     * Checks if at least one category is uncommented in CategoriesToExport.txt
     */
    public boolean hasConfiguredCategoriesToExport() {
        return checkConfigFileContent("CategoriesToExport.txt");
    }

    /**
     * Checks if at least one keyword is uncommented in KeywordsToExport.txt
     */
    public boolean hasConfiguredKeywordsToExport() {
        return checkConfigFileContent("KeywordsToExport.txt");
    }

    private boolean checkConfigFileContent(String filename) {
        Path confPath = ConfigManager.getInstance().getConfPath();
        if (confPath == null)
            return false;

        File file = confPath.resolve(filename).toFile();
        if (!file.exists())
            return false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks if ElasticSearch is configured with valid host
     */
    public boolean isElasticSearchConfigured() {
        PropertiesConfigFile config = ConfigManager.getInstance().getConfigFile(ConfigManager.ELASTIC_CONFIG);
        if (config == null)
            return false;

        String host = config.getString("host");
        return host != null && !host.trim().isEmpty();
    }

    /**
     * Checks if MinIO is configured with valid host
     */
    public boolean isMinIOConfigured() {
        PropertiesConfigFile config = ConfigManager.getInstance().getConfigFile(ConfigManager.MINIO_CONFIG);
        if (config == null)
            return false;

        String host = config.getString("host");
        return host != null && !host.trim().isEmpty();
    }

    /**
     * Checks if hashesDB is configured in LocalConfig.txt
     */
    public boolean isHashesDBConfigured() {
        PropertiesConfigFile config = ConfigManager.getInstance().getLocalConfig();
        if (config == null)
            return false;

        String path = config.getString("hashesDB");
        return path != null && !path.trim().isEmpty();
    }
}
