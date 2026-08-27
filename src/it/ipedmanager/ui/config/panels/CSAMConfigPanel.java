package it.ipedmanager.ui.config.panels;

import it.ipedmanager.config.PropertiesConfigFile;
import it.ipedmanager.utils.BundleManager;

import javax.swing.*;
import java.awt.*;

/**
 * Pannello per configurazione CSAM Detector (CSAMDetectorConfig.txt)
 */
public class CSAMConfigPanel extends BaseConfigPanel {

    private PropertiesConfigFile config;

    private JTextField txtModelFile;
    private JSpinner spnBatchSize;
    private JSpinner spnMinImageSize;
    private JSpinner spnSkipDim;
    private JCheckBox chkSkipHashDB;
    private JCheckBox chkBookmarks;
    private JSpinner spnCsamThresh;
    private JSpinner spnPornThresh;
    private JSpinner spnCsamMin;
    private JSpinner spnPornMin;

    public CSAMConfigPanel() {
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
        JPanel paramsPanel = createSectionPanel(BundleManager.getString("panel.csam.section.params"));

        txtModelFile = createStyledTextField();
        addFullWidthComponent(paramsPanel, createStyledLabel(BundleManager.getString("panel.csam.label.model")), 0);
        addFullWidthComponent(paramsPanel, txtModelFile, 1);

        spnBatchSize = createStyledSpinner();
        spnBatchSize.setModel(new SpinnerNumberModel(16, 1, 1000, 4));

        spnMinImageSize = createStyledSpinner();
        spnMinImageSize.setModel(new SpinnerNumberModel(2048, 0, 10000000, 1024));

        spnSkipDim = createStyledSpinner();
        spnSkipDim.setModel(new SpinnerNumberModel(10, 0, 10000, 10));

        addCompactRow(paramsPanel, 2,
                createStyledLabel(BundleManager.getString("panel.csam.label.batch")), spnBatchSize,
                createStyledLabel(BundleManager.getString("panel.csam.label.minSize")), spnMinImageSize,
                createStyledLabel(BundleManager.getString("panel.csam.label.skipDim")), spnSkipDim);

        content.add(paramsPanel);
        content.add(Box.createVerticalStrut(15));

        // === THRESHOLDS ===
        JPanel threshPanel = createSectionPanel(BundleManager.getString("panel.csam.section.thresholds"));
        
        spnCsamThresh = createStyledSpinner();
        spnCsamThresh.setModel(new SpinnerNumberModel(0.6, 0.0, 1.0, 0.05));
        
        spnPornThresh = createStyledSpinner();
        spnPornThresh.setModel(new SpinnerNumberModel(0.5, 0.0, 1.0, 0.05));

        spnCsamMin = createStyledSpinner();
        spnCsamMin.setModel(new SpinnerNumberModel(1, 1, 1000, 1));
        
        spnPornMin = createStyledSpinner();
        spnPornMin.setModel(new SpinnerNumberModel(1, 1, 1000, 1));

        addCompactRow(threshPanel, 0,
                createStyledLabel(BundleManager.getString("panel.csam.label.csamThresh")), spnCsamThresh,
                createStyledLabel(BundleManager.getString("panel.csam.label.csamMin")), spnCsamMin);

        addCompactRow(threshPanel, 1,
                createStyledLabel(BundleManager.getString("panel.csam.label.pornThresh")), spnPornThresh,
                createStyledLabel(BundleManager.getString("panel.csam.label.pornMin")), spnPornMin);

        content.add(threshPanel);
        content.add(Box.createVerticalStrut(15));

        // === OPZIONI ===
        JPanel optPanel = createSectionPanel(BundleManager.getString("panel.csam.section.options"));

        chkSkipHashDB = createStyledCheckBox(BundleManager.getString("panel.csam.check.skipHash"));
        addFullWidthComponent(optPanel, chkSkipHashDB, 0);
        
        chkBookmarks = createStyledCheckBox(BundleManager.getString("panel.csam.check.bookmarks"));
        addFullWidthComponent(optPanel, chkBookmarks, 1);

        content.add(optPanel);

        // Info
        content.add(Box.createVerticalStrut(15));
        JComponent info = createStyledInfoArea(BundleManager.getString("panel.csam.info"));
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

        txtModelFile.setText(config.get("ModelFile", "pytorch_B0_v3_1_2_fp32.onnx"));
        spnBatchSize.setValue(config.getInt("BatchSize", 16));
        spnMinImageSize.setValue(config.getInt("MinimumImageSize", 2048));
        spnSkipDim.setValue(config.getInt("SkipDimension", 10));
        
        chkSkipHashDB.setSelected(config.getBoolean("SkipHashDBFiles", true));
        chkBookmarks.setSelected(config.getBoolean("CreateBookmarks", false));
        
        try {
            spnCsamThresh.setValue(Double.parseDouble(config.get("CsamThreshold", "0.6")));
        } catch (NumberFormatException e) {}
        try {
            spnPornThresh.setValue(Double.parseDouble(config.get("PornThreshold", "0.5")));
        } catch (NumberFormatException e) {}
        spnCsamMin.setValue(config.getInt("CsamMinFrames", 1));

        spnPornMin.setValue(config.getInt("PornMinFrames", 1));
    }

    @Override
    public void saveConfig() {
        if (config == null) return;

        config.set("ModelFile", txtModelFile.getText().trim());
        config.setInt("BatchSize", (Integer) spnBatchSize.getValue());
        config.setInt("MinimumImageSize", (Integer) spnMinImageSize.getValue());
        config.setInt("SkipDimension", (Integer) spnSkipDim.getValue());
        
        config.setBoolean("SkipHashDBFiles", chkSkipHashDB.isSelected());
        config.setBoolean("CreateBookmarks", chkBookmarks.isSelected());
        
        config.set("CsamThreshold", String.valueOf(spnCsamThresh.getValue()));
        config.set("PornThreshold", String.valueOf(spnPornThresh.getValue()));
        config.setInt("CsamMinFrames", (Integer) spnCsamMin.getValue());

        config.setInt("PornMinFrames", (Integer) spnPornMin.getValue());
    }
}
