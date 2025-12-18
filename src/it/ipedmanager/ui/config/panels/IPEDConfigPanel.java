package it.ipedmanager.ui.config.panels;

import it.ipedmanager.config.PropertiesConfigFile;

import it.ipedmanager.utils.BundleManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pannello per la configurazione di IPEDConfig.txt
 * Layout ottimizzato con 3 colonne per massimizzare lo spazio.
 */
public class IPEDConfigPanel extends BaseConfigPanel {

    private PropertiesConfigFile config;
    private PropertiesConfigFile hashConfig; // HashTaskConfig.txt
    private final Map<String, JCheckBox> checkboxes = new LinkedHashMap<>();

    // Hash algorithm checkboxes (separate config file)
    private JCheckBox chkMd5;
    private JCheckBox chkSha1;
    private JCheckBox chkSha256;
    private JCheckBox chkSha512;
    private JCheckBox chkEdonkey;

    // Definizione delle sezioni - raggruppate logicamente
    private String[][] getSections() {
        return new String[][] {
                // Sezione Hash & Lookup (panel.iped.section.hash)
                { BundleManager.getString("panel.iped.section.hash"),
                        "enableHash", BundleManager.getString("panel.iped.option.enableHash.label"),
                        BundleManager.getString("panel.iped.option.enableHash.desc"),
                        "enableHashDBLookup", BundleManager.getString("panel.iped.option.enableHashDBLookup.label"),
                        BundleManager.getString("panel.iped.option.enableHashDBLookup.desc"),
                        "enablePhotoDNALookup", BundleManager.getString("panel.iped.option.enablePhotoDNALookup.label"),
                        BundleManager.getString("panel.iped.option.enablePhotoDNALookup.desc")
                },

                // Sezione Carving (panel.iped.section.carving)
                { BundleManager.getString("panel.iped.section.carving"),
                        "enableCarving", BundleManager.getString("panel.iped.option.enableCarving.label"),
                        BundleManager.getString("panel.iped.option.enableCarving.desc"),
                        "enableLedCarving", BundleManager.getString("panel.iped.option.enableLedCarving.label"),
                        BundleManager.getString("panel.iped.option.enableLedCarving.desc"),
                        "enableKnownMetCarving",
                        BundleManager.getString("panel.iped.option.enableKnownMetCarving.label"),
                        BundleManager.getString("panel.iped.option.enableKnownMetCarving.desc")
                },

                // Sezione Rilevamento (panel.iped.section.detection)
                { BundleManager.getString("panel.iped.section.detection"),
                        "enableLedDie", BundleManager.getString("panel.iped.option.enableLedDie.label"),
                        BundleManager.getString("panel.iped.option.enableLedDie.desc"),
                        "enableYahooNSFWDetection",
                        BundleManager.getString("panel.iped.option.enableYahooNSFWDetection.label"),
                        BundleManager.getString("panel.iped.option.enableYahooNSFWDetection.desc"),
                        "enableQRCode", BundleManager.getString("panel.iped.option.enableQRCode.label"),
                        BundleManager.getString("panel.iped.option.enableQRCode.desc"),
                        "enableLanguageDetect", BundleManager.getString("panel.iped.option.enableLanguageDetect.label"),
                        BundleManager.getString("panel.iped.option.enableLanguageDetect.desc")
                },

                // Sezione Elaborazione File (panel.iped.section.processing)
                { BundleManager.getString("panel.iped.section.processing"),
                        "ignoreDuplicates", BundleManager.getString("panel.iped.option.ignoreDuplicates.label"),
                        BundleManager.getString("panel.iped.option.ignoreDuplicates.desc"),
                        "exportFileProps", BundleManager.getString("panel.iped.option.exportFileProps.label"),
                        BundleManager.getString("panel.iped.option.exportFileProps.desc"),
                        "processFileSignatures",
                        BundleManager.getString("panel.iped.option.processFileSignatures.label"),
                        BundleManager.getString("panel.iped.option.processFileSignatures.desc"),
                        "enableFileParsing", BundleManager.getString("panel.iped.option.enableFileParsing.label"),
                        BundleManager.getString("panel.iped.option.enableFileParsing.desc"),
                        "expandContainers", BundleManager.getString("panel.iped.option.expandContainers.label"),
                        BundleManager.getString("panel.iped.option.expandContainers.desc"),
                        "processEmbeddedDisks", BundleManager.getString("panel.iped.option.processEmbeddedDisks.label"),
                        BundleManager.getString("panel.iped.option.processEmbeddedDisks.desc"),
                        "indexFileContents", BundleManager.getString("panel.iped.option.indexFileContents.label"),
                        BundleManager.getString("panel.iped.option.indexFileContents.desc"),
                        "entropyTest", BundleManager.getString("panel.iped.option.entropyTest.label"),
                        BundleManager.getString("panel.iped.option.entropyTest.desc")
                },

                // Sezione Analisi Avanzata (panel.iped.section.advanced)
                { BundleManager.getString("panel.iped.section.advanced"),
                        "enableRegexSearch", BundleManager.getString("panel.iped.option.enableRegexSearch.label"),
                        BundleManager.getString("panel.iped.option.enableRegexSearch.desc"),
                        "enableNamedEntityRecogniton",
                        BundleManager.getString("panel.iped.option.enableNamedEntityRecogniton.label"),
                        BundleManager.getString("panel.iped.option.enableNamedEntityRecogniton.desc"),
                        "enableGraphGeneration",
                        BundleManager.getString("panel.iped.option.enableGraphGeneration.label"),
                        BundleManager.getString("panel.iped.option.enableGraphGeneration.desc"),
                        "enableImageSimilarity",
                        BundleManager.getString("panel.iped.option.enableImageSimilarity.label"),
                        BundleManager.getString("panel.iped.option.enableImageSimilarity.desc"),
                        "enableSearchHardwareWallets",
                        BundleManager.getString("panel.iped.option.enableSearchHardwareWallets.label"),
                        BundleManager.getString("panel.iped.option.enableSearchHardwareWallets.desc")
                }
        };
    }

    public IPEDConfigPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(8, 10, 8, 10));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_COLOR);

        // Crea sezioni con layout compatto
        for (String[] section : getSections()) {
            JPanel sectionPanel = createCompactSection(section);
            contentPanel.add(sectionPanel);
            contentPanel.add(Box.createVerticalStrut(8)); // Spazio ridotto
        }

        // Sezione Algoritmi Hash (da HashTaskConfig.txt)
        contentPanel.add(createHashAlgorithmsSection());
        contentPanel.add(Box.createVerticalStrut(8));

        // Info Area Dinamica (In fondo, fuori dallo scroll)
        add(createInfoPanel(), BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHashAlgorithmsSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(4, 0, 8, 0)));

        JLabel titleLabel = new JLabel(BundleManager.getString("panel.iped.hash.title"));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(ACCENT_COLOR);
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 5, 12, 2));
        grid.setBackground(BG_COLOR);

        chkMd5 = createStyledCheckBox("MD5");
        addHoverHelp(chkMd5, BundleManager.getString("panel.iped.hash.md5.desc"));
        grid.add(chkMd5);

        chkSha1 = createStyledCheckBox("SHA-1");
        addHoverHelp(chkSha1, BundleManager.getString("panel.iped.hash.sha1.desc"));
        grid.add(chkSha1);

        chkSha256 = createStyledCheckBox("SHA-256");
        addHoverHelp(chkSha256, BundleManager.getString("panel.iped.hash.sha256.desc"));
        grid.add(chkSha256);

        chkSha512 = createStyledCheckBox("SHA-512");
        addHoverHelp(chkSha512, BundleManager.getString("panel.iped.hash.sha512.desc"));
        grid.add(chkSha512);

        chkEdonkey = createStyledCheckBox("eDonkey");
        addHoverHelp(chkEdonkey, BundleManager.getString("panel.iped.hash.edonkey.desc"));
        grid.add(chkEdonkey);

        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCompactSection(String[] sectionData) {
        String title = sectionData[0];

        // Pannello sezione con bordo e titolo
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(4, 0, 8, 0)));

        // Titolo sezione
        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(ACCENT_COLOR);
        panel.add(titleLabel, BorderLayout.NORTH);

        // Griglia checkbox - 3 colonne per massimizzare spazio
        // Items are now triplets (key, label, desc), so index 1..len, step 3
        int numItems = (sectionData.length - 1) / 3;
        int cols = numItems <= 3 ? numItems : 3;
        // Avoid division by zero if section is empty
        if (cols == 0)
            cols = 1;

        JPanel grid = new JPanel(new GridLayout(0, cols, 12, 2));
        grid.setBackground(BG_COLOR);

        for (int i = 1; i < sectionData.length; i += 3) {
            String key = sectionData[i];
            String label = sectionData[i + 1];
            String desc = sectionData[i + 2];

            JCheckBox checkbox = new JCheckBox(label);
            checkbox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            checkbox.setBackground(BG_COLOR);
            checkbox.setFocusPainted(false);

            // Stile per opzioni pericolose
            if (label.contains("[!]")) {
                checkbox.setForeground(new Color(220, 38, 38));
                checkbox.setFont(checkbox.getFont().deriveFont(Font.BOLD));
            }

            // Add dynamic info help
            addHoverHelp(checkbox, desc);

            checkboxes.put(key, checkbox);
            grid.add(checkbox);
        }

        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    public void setConfig(PropertiesConfigFile config) {
        this.config = config;
        loadConfig();
    }

    public void setHashConfig(PropertiesConfigFile hashConfig) {
        this.hashConfig = hashConfig;
        loadHashConfig();
    }

    @Override
    public void loadConfig() {
        if (config == null || !config.isLoaded())
            return;

        for (Map.Entry<String, JCheckBox> entry : checkboxes.entrySet()) {
            String key = entry.getKey();
            JCheckBox checkbox = entry.getValue();

            String value = config.getString(key);
            if (value != null) {
                checkbox.setSelected(value.equalsIgnoreCase("true"));
                checkbox.setEnabled(true);
            } else {
                checkbox.setSelected(false);
                checkbox.setEnabled(false);
                checkbox.setToolTipText("Non presente in IPEDConfig.txt");
            }
        }

        handleDependencies();
        loadHashConfig();
    }

    private void loadHashConfig() {
        if (hashConfig == null || !hashConfig.isLoaded())
            return;

        String hashes = hashConfig.get("hashes", "md5;sha-1").toLowerCase();
        if (chkMd5 != null)
            chkMd5.setSelected(hashes.contains("md5"));
        if (chkSha1 != null)
            chkSha1.setSelected(hashes.contains("sha-1"));
        if (chkSha256 != null)
            chkSha256.setSelected(hashes.contains("sha-256"));
        if (chkSha512 != null)
            chkSha512.setSelected(hashes.contains("sha-512"));
        if (chkEdonkey != null)
            chkEdonkey.setSelected(hashes.contains("edonkey"));
    }

    @Override
    public void saveConfig() {
        if (config == null)
            return;

        for (Map.Entry<String, JCheckBox> entry : checkboxes.entrySet()) {
            String key = entry.getKey();
            JCheckBox checkbox = entry.getValue();

            if (checkbox.isEnabled()) {
                config.setBoolean(key, checkbox.isSelected());
            }
        }

        // Save hash algorithms to HashTaskConfig
        saveHashConfig();
    }

    private void saveHashConfig() {
        if (hashConfig == null)
            return;

        StringBuilder sb = new StringBuilder();
        if (chkMd5 != null && chkMd5.isSelected())
            sb.append("md5;");
        if (chkSha1 != null && chkSha1.isSelected())
            sb.append("sha-1;");
        if (chkSha256 != null && chkSha256.isSelected())
            sb.append("sha-256;");
        if (chkSha512 != null && chkSha512.isSelected())
            sb.append("sha-512;");
        if (chkEdonkey != null && chkEdonkey.isSelected())
            sb.append("edonkey;");

        String hashes = sb.toString();
        if (hashes.endsWith(";"))
            hashes = hashes.substring(0, hashes.length() - 1);
        if (hashes.isEmpty())
            hashes = "md5"; // Default minimo

        hashConfig.set("hashes", hashes);
    }

    private void handleDependencies() {
        refreshDependencies();

        JCheckBox imageThumbs = checkboxes.get("enableImageThumbs");
        JCheckBox imageSimilarity = checkboxes.get("enableImageSimilarity");

        if (imageThumbs != null && imageSimilarity != null) {
            imageThumbs.addActionListener(e -> {
                if (!imageThumbs.isSelected()) {
                    imageSimilarity.setSelected(false);
                    imageSimilarity.setEnabled(false);
                    imageSimilarity.setToolTipText("Richiede 'Immagini' attivo");
                } else {
                    imageSimilarity.setEnabled(true);
                    imageSimilarity.setToolTipText(null);
                    refreshDependencies();
                }
            });
        }
    }

    public void refreshDependencies() {
        it.ipedmanager.utils.ConfigValidator validator = it.ipedmanager.utils.ConfigValidator.getInstance();

        boolean hasHashesDB = validator.isHashesDBConfigured();
        checkDependency("enableHashDBLookup", hasHashesDB, "Richiede HashesDB");
        checkDependency("enablePhotoDNALookup", hasHashesDB, "Richiede HashesDB");
        checkDependency("enableLedCarving", hasHashesDB, "Richiede HashesDB");

        JCheckBox imageThumbs = checkboxes.get("enableImageThumbs");
        JCheckBox imageSimilarity = checkboxes.get("enableImageSimilarity");
        if (imageThumbs != null && imageSimilarity != null && !imageThumbs.isSelected()) {
            imageSimilarity.setEnabled(false);
            imageSimilarity.setToolTipText("Richiede 'Immagini' attivo");
        }
    }

    private void checkDependency(String key, boolean condition, String errorMsg) {
        JCheckBox cb = checkboxes.get(key);
        if (cb != null) {
            if (!condition) {
                cb.setEnabled(false);
                cb.setSelected(false);
                cb.setToolTipText("❌ " + errorMsg);
            } else {
                cb.setEnabled(true);
                cb.setToolTipText(null);
            }
        }
    }
}
