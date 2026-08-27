package it.ipedmanager.ui.config.panels;

import it.ipedmanager.config.PropertiesConfigFile;
import it.ipedmanager.utils.BundleManager;

import javax.swing.*;
import java.awt.*;

/**
 * Pannello per la configurazione della Stima dell'Età (AgeEstimationConfig.txt)
 */
public class AgeEstimationConfigPanel extends BaseConfigPanel {

    private PropertiesConfigFile config;

    private JSpinner spnBatchSize;
    private JSpinner spnCatThreshold;
    private JCheckBox chkSkipHashDB;
    private JComboBox<String> cmbDevice;

    public AgeEstimationConfigPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_COLOR);

        // === PARAMETRI ===
        JPanel paramsPanel = createSectionPanel(BundleManager.getString("panel.age.section.params"));

        spnBatchSize = createStyledSpinner();
        spnBatchSize.setModel(new SpinnerNumberModel(50, 1, 1000, 10));

        spnCatThreshold = createStyledSpinner();
        spnCatThreshold.setModel(new SpinnerNumberModel(0, 0, 100, 5));

        cmbDevice = createStyledComboBox(new String[]{"cpu", "gpu"});

        addCompactRow(paramsPanel, 0,
                createStyledLabel(BundleManager.getString("panel.age.label.batchSize")), spnBatchSize,
                createStyledLabel(BundleManager.getString("panel.age.label.threshold")), spnCatThreshold,
                createStyledLabel(BundleManager.getString("panel.age.label.device")), cmbDevice);

        content.add(paramsPanel);
        content.add(Box.createVerticalStrut(15));

        // === OPZIONI ===
        JPanel optPanel = createSectionPanel(BundleManager.getString("panel.age.section.options"));

        chkSkipHashDB = createStyledCheckBox(BundleManager.getString("panel.age.check.skipHashDB"));
        addFullWidthComponent(optPanel, chkSkipHashDB, 0);

        content.add(optPanel);

        // Info
        content.add(Box.createVerticalStrut(15));
        JComponent info = createStyledInfoArea(BundleManager.getString("panel.age.info"));
        add(info, BorderLayout.SOUTH);

        content.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setConfig(PropertiesConfigFile config) {
        this.config = config;
        loadConfig();
    }

    @Override
    public void loadConfig() {
        if (config == null || !config.isLoaded()) return;

        spnBatchSize.setValue(config.getInt("batchSize", 50));
        spnCatThreshold.setValue(config.getInt("categorizationThreshold", 0));
        chkSkipHashDB.setSelected(config.getBoolean("skipHashDBFiles", true));
        cmbDevice.setSelectedItem(config.get("device", "cpu"));
    }

    @Override
    public void saveConfig() {
        if (config == null) return;

        config.setInt("batchSize", (Integer) spnBatchSize.getValue());
        config.setInt("categorizationThreshold", (Integer) spnCatThreshold.getValue());
        config.setBoolean("skipHashDBFiles", chkSkipHashDB.isSelected());
        config.set("device", (String) cmbDevice.getSelectedItem());
    }
}
