package it.ipedmanager.ui.config.panels;

import it.ipedmanager.config.PropertiesConfigFile;
import it.ipedmanager.utils.BundleManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Panel for local environment configuration.
 * Clean layout with logical groupings.
 */
public class LocalConfigPanel extends BaseConfigPanel {

    private JTextField txtHashesDB;
    private JTextField txtMPlayer;
    private JTextField txtIndexTemp;
    private JCheckBox chkIndexTempOnSSD;
    private JCheckBox chkOutputOnSSD;
    private JSpinner spnThreads;
    private JSpinner spnMemoryGB;
    private JComboBox<LanguageItem> cmbLocale;
    private PropertiesConfigFile config;

    // Helper class for language selection
    private static class LanguageItem {
        String code;
        String name;

        LanguageItem(String code, String name) {
            this.code = code;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            LanguageItem that = (LanguageItem) o;
            return code.equals(that.code);
        }
    }

    public LocalConfigPanel() {
        initComponents();
    }

    public void setConfig(PropertiesConfigFile config) {
        this.config = config;
        loadConfig();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(BG_COLOR);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_COLOR);

        // === SECTION 1: PERCORSI ===
        JPanel pathsSection = createSectionPanel(BundleManager.getString("panel.local.paths"));

        txtHashesDB = createStyledTextField();
        txtHashesDB.setToolTipText(BundleManager.getString("panel.local.tooltip.hashesDB"));
        addFileField(pathsSection, BundleManager.getString("panel.local.hashesDB"), txtHashesDB, true, 0);

        txtMPlayer = createStyledTextField();
        txtMPlayer.setToolTipText(BundleManager.getString("panel.local.tooltip.mplayer"));
        addFileField(pathsSection, BundleManager.getString("panel.local.mplayer"), txtMPlayer, false, 1);

        txtIndexTemp = createStyledTextField();
        txtIndexTemp.setToolTipText(BundleManager.getString("panel.local.tooltip.indexTemp"));
        addFileField(pathsSection, BundleManager.getString("panel.local.indexTemp"), txtIndexTemp, true, 2);

        content.add(pathsSection);
        content.add(Box.createVerticalStrut(15));

        // === SECTION 2: PRESTAZIONI ===
        JPanel perfSection = createSectionPanel(BundleManager.getString("panel.local.performance"));

        // Row 0: Thread + Memory
        spnThreads = createStyledSpinner();
        spnThreads.setModel(new SpinnerNumberModel(4, 1, 256, 1));
        spnThreads.setToolTipText(BundleManager.getString("panel.local.tooltip.threads"));

        spnMemoryGB = createStyledSpinner();
        spnMemoryGB.setModel(new SpinnerNumberModel(8, 1, 512, 1));
        spnMemoryGB.setToolTipText(BundleManager.getString("panel.local.tooltip.memory"));

        JPanel perfRow = new JPanel(new GridBagLayout());
        perfRow.setBackground(BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 12);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.weightx = 0;
        perfRow.add(createStyledLabel(BundleManager.getString("panel.local.threads")), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        perfRow.add(spnThreads, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        perfRow.add(createStyledLabel(BundleManager.getString("panel.local.memory")), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        perfRow.add(spnMemoryGB, gbc);

        gbc.gridx = 4;
        gbc.weightx = 1.0; // Spacer
        perfRow.add(Box.createHorizontalGlue(), gbc);

        addFullWidthComponent(perfSection, perfRow, 0);

        // Row 1: SSD Options
        chkIndexTempOnSSD = createStyledCheckBox(BundleManager.getString("panel.local.indexTempSSD"));
        chkIndexTempOnSSD.setToolTipText(BundleManager.getString("panel.local.tooltip.indexTempSSD"));
        chkOutputOnSSD = createStyledCheckBox(BundleManager.getString("panel.local.outputSSD"));
        chkOutputOnSSD.setToolTipText(BundleManager.getString("panel.local.tooltip.outputSSD"));

        JPanel ssdRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        ssdRow.setBackground(BG_COLOR);
        ssdRow.add(chkIndexTempOnSSD);
        ssdRow.add(chkOutputOnSSD);
        addFullWidthComponent(perfSection, ssdRow, 1);

        content.add(perfSection);
        content.add(Box.createVerticalStrut(15));

        // === SECTION 3: LINGUA ===
        JPanel langSection = createSectionPanel(BundleManager.getString("panel.local.language"));

        cmbLocale = createStyledComboBox(new LanguageItem[] {
                new LanguageItem("en", "English"),
                new LanguageItem("it-IT", "Italiano"),
                new LanguageItem("pt-BR", "Português (Brasil)"),
                new LanguageItem("es-AR", "Español"),
                new LanguageItem("de-DE", "Deutsch"),
                new LanguageItem("fr-FR", "Français")
        });
        cmbLocale.setToolTipText(BundleManager.getString("panel.local.tooltip.language"));

        JPanel langRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        langRow.setBackground(BG_COLOR);
        langRow.add(createStyledLabel(BundleManager.getString("panel.local.ipedInterface")));
        langRow.add(Box.createHorizontalStrut(8));
        langRow.add(cmbLocale);
        addFullWidthComponent(langSection, langRow, 0);

        content.add(langSection);
        content.add(Box.createVerticalStrut(15));

        // === INFO BOX ===
        JComponent info = createStyledInfoArea(BundleManager.getString("panel.local.filesInfo"));
        // Add to SOUTH to fix sizing and layout issues
        add(info, BorderLayout.SOUTH);

        content.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    @Override
    public void loadConfig() {
        if (config == null || !config.isLoaded())
            return;

        txtHashesDB.setText(getOr(config.getString("hashesDB"), ""));
        txtMPlayer.setText(getOr(config.getString("mplayerPath"), ""));
        txtIndexTemp.setText(getOr(config.getString("indexTemp"), "default"));

        // Threads
        String threads = config.getString("numThreads");
        if (threads != null && !threads.equalsIgnoreCase("default")) {
            try {
                spnThreads.setValue(Integer.parseInt(threads));
            } catch (NumberFormatException e) {
                setThreadsDefault();
            }
        } else {
            setThreadsDefault();
        }

        spnMemoryGB.setValue(config.getInt("maxMemoryGB", 8));
        chkIndexTempOnSSD.setSelected(config.getBoolean("indexTempOnSSD", false));
        chkOutputOnSSD.setSelected(config.getBoolean("outputOnSSD", false));

        // Locale Loading
        String currentLocale = getOr(config.getString("locale"), "en");

        // Try to match strict first, then loose (if needed)
        boolean found = false;
        for (int i = 0; i < cmbLocale.getItemCount(); i++) {
            LanguageItem item = cmbLocale.getItemAt(i);
            if (item.code.equalsIgnoreCase(currentLocale)) {
                cmbLocale.setSelectedIndex(i);
                found = true;
                break;
            }
        }

        // Fallback: match slightly looser (e.g. "it" matches "it-IT")
        if (!found && currentLocale.contains("-")) {
            String shortCode = currentLocale.split("-")[0];
            for (int i = 0; i < cmbLocale.getItemCount(); i++) {
                if (cmbLocale.getItemAt(i).code.startsWith(shortCode)) {
                    cmbLocale.setSelectedIndex(i);
                    found = true;
                    break;
                }
            }
        }
    }

    private void setThreadsDefault() {
        try {
            spnThreads.setValue(Runtime.getRuntime().availableProcessors());
        } catch (Exception ex) {
            spnThreads.setValue(4);
        }
    }

    @Override
    public void saveConfig() {
        if (config == null)
            return;

        config.setString("hashesDB", txtHashesDB.getText());
        config.setString("mplayerPath", txtMPlayer.getText());
        config.setString("indexTemp", txtIndexTemp.getText());
        config.setInt("numThreads", (Integer) spnThreads.getValue());
        config.setInt("maxMemoryGB", (Integer) spnMemoryGB.getValue());
        config.setBoolean("indexTempOnSSD", chkIndexTempOnSSD.isSelected());
        config.setBoolean("outputOnSSD", chkOutputOnSSD.isSelected());

        LanguageItem selected = (LanguageItem) cmbLocale.getSelectedItem();
        if (selected != null) {
            // Store simple language code for now to match Bundles
            config.setString("locale", selected.code);
        }
    }
}
