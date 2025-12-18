package it.ipedmanager.ui.config.panels;

import it.ipedmanager.config.PropertiesConfigFile;
import it.ipedmanager.utils.BundleManager;

import javax.swing.*;
import java.awt.*;

/**
 * Pannello per configurazione Face Recognition (FaceRecognitionConfig.txt)
 * Layout compatto per parametri di rilevamento.
 */
public class FaceRecognitionConfigPanel extends BaseConfigPanel {

    private PropertiesConfigFile config;

    private JSpinner spnMinFaces;
    private JSpinner spnMaxResolution;
    private JSpinner spnUpsampling;
    private JCheckBox chkSkipKnown;

    public FaceRecognitionConfigPanel() {
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
        JPanel paramsPanel = createSectionPanel(BundleManager.getString("panel.face.section.params"));

        spnMinFaces = createStyledSpinner();
        spnMinFaces.setModel(new SpinnerNumberModel(2, 1, 100, 1));
        spnMinFaces.setToolTipText(BundleManager.getString("panel.face.tooltip.minFaces"));

        spnMaxResolution = createStyledSpinner();
        spnMaxResolution.setModel(new SpinnerNumberModel(1000, 200, 4000, 100));
        spnMaxResolution.setToolTipText(BundleManager.getString("panel.face.tooltip.maxRes"));

        spnUpsampling = createStyledSpinner();
        spnUpsampling.setModel(new SpinnerNumberModel(1, 0, 4, 1));
        spnUpsampling.setToolTipText(BundleManager.getString("panel.face.tooltip.upsampling"));

        // Use compact row [Label, Spin, Label, Spin, Label, Spin]
        addCompactRow(paramsPanel, 0,
                createStyledLabel(BundleManager.getString("panel.face.label.minFaces")), spnMinFaces,
                createStyledLabel(BundleManager.getString("panel.face.label.maxRes")), spnMaxResolution,
                createStyledLabel(BundleManager.getString("panel.face.label.upsampling")), spnUpsampling);

        content.add(paramsPanel);
        content.add(Box.createVerticalStrut(15));

        // === OPZIONI ===
        JPanel optPanel = createSectionPanel(BundleManager.getString("panel.face.section.options"));

        chkSkipKnown = createStyledCheckBox(BundleManager.getString("panel.face.check.skipKnown"));
        addFullWidthComponent(optPanel, chkSkipKnown, 0);

        content.add(optPanel);

        // Info
        content.add(Box.createVerticalStrut(15));
        JComponent info = createStyledInfoArea(
                BundleManager.getString("panel.face.info"));
        // Add to SOUTH to fix sizing and layout issues
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
        if (config == null || !config.isLoaded())
            return;

        spnMinFaces.setValue(config.getInt("minFacesPerGroup", 2));
        spnMaxResolution.setValue(config.getInt("maxResolution", 1000));
        spnUpsampling.setValue(config.getInt("upsampling", 1));
        chkSkipKnown.setSelected(config.getBoolean("skipKnownFiles", false));
    }

    @Override
    public void saveConfig() {
        if (config == null)
            return;

        config.setInt("minFacesPerGroup", (Integer) spnMinFaces.getValue());
        config.setInt("maxResolution", (Integer) spnMaxResolution.getValue());
        config.setInt("upsampling", (Integer) spnUpsampling.getValue());
        config.setBoolean("skipKnownFiles", chkSkipKnown.isSelected());
    }
}
