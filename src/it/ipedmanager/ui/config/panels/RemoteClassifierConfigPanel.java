package it.ipedmanager.ui.config.panels;

import it.ipedmanager.config.PropertiesConfigFile;
import it.ipedmanager.utils.BundleManager;

import javax.swing.*;
import java.awt.*;

/**
 * Pannello per la configurazione del Classificatore Remoto (RemoteImageClassifierConfig.txt)
 */
public class RemoteClassifierConfigPanel extends BaseConfigPanel {

    private PropertiesConfigFile config;

    private JTextField txtUrl;
    private JSpinner spnBatchSize;
    private JSpinner spnCatThreshold;
    private JSpinner spnSkipSize;
    private JSpinner spnSkipDim;
    private JCheckBox chkSkipHashDB;
    private JCheckBox chkValidateSSL;

    public RemoteClassifierConfigPanel() {
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
        JPanel paramsPanel = createSectionPanel(BundleManager.getString("panel.remote.section.params"));

        txtUrl = createStyledTextField();
        addFullWidthComponent(paramsPanel, createStyledLabel(BundleManager.getString("panel.remote.label.url")), 0);
        addFullWidthComponent(paramsPanel, txtUrl, 1);

        spnBatchSize = createStyledSpinner();
        spnBatchSize.setModel(new SpinnerNumberModel(50, 1, 1000, 10));

        spnCatThreshold = createStyledSpinner();
        spnCatThreshold.setModel(new SpinnerNumberModel(50, 0, 100, 5));

        spnSkipSize = createStyledSpinner();
        spnSkipSize.setModel(new SpinnerNumberModel(2048, 0, 1000000, 1024));

        spnSkipDim = createStyledSpinner();
        spnSkipDim.setModel(new SpinnerNumberModel(48, 0, 10000, 10));

        addCompactRow(paramsPanel, 2,
                createStyledLabel(BundleManager.getString("panel.remote.label.batch")), spnBatchSize,
                createStyledLabel(BundleManager.getString("panel.remote.label.threshold")), spnCatThreshold);

        addCompactRow(paramsPanel, 3,
                createStyledLabel(BundleManager.getString("panel.remote.label.skipSize")), spnSkipSize,
                createStyledLabel(BundleManager.getString("panel.remote.label.skipDim")), spnSkipDim);

        content.add(paramsPanel);
        content.add(Box.createVerticalStrut(15));

        // === OPZIONI ===
        JPanel optPanel = createSectionPanel(BundleManager.getString("panel.remote.section.options"));

        chkSkipHashDB = createStyledCheckBox(BundleManager.getString("panel.remote.check.skipHash"));
        addFullWidthComponent(optPanel, chkSkipHashDB, 0);

        chkValidateSSL = createStyledCheckBox(BundleManager.getString("panel.remote.check.ssl"));
        addFullWidthComponent(optPanel, chkValidateSSL, 1);

        content.add(optPanel);

        // Info
        content.add(Box.createVerticalStrut(15));
        JComponent info = createStyledInfoArea(BundleManager.getString("panel.remote.info"));
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

        txtUrl.setText(config.get("url", "10.61.86.148:30603/new"));
        spnBatchSize.setValue(config.getInt("batchSize", 50));
        spnCatThreshold.setValue(config.getInt("labelingThreshold", 50));
        spnSkipSize.setValue(config.getInt("skipSize", 2048));
        spnSkipDim.setValue(config.getInt("skipDimension", 48));
        chkSkipHashDB.setSelected(config.getBoolean("skipHashDBFiles", true));
        chkValidateSSL.setSelected(config.getBoolean("validateSSL", false));
    }

    @Override
    public void saveConfig() {
        if (config == null) return;

        config.set("url", txtUrl.getText().trim());
        config.setInt("batchSize", (Integer) spnBatchSize.getValue());
        config.setInt("labelingThreshold", (Integer) spnCatThreshold.getValue());
        config.setInt("skipSize", (Integer) spnSkipSize.getValue());
        config.setInt("skipDimension", (Integer) spnSkipDim.getValue());
        config.setBoolean("skipHashDBFiles", chkSkipHashDB.isSelected());
        config.setBoolean("validateSSL", chkValidateSSL.isSelected());
    }
}
