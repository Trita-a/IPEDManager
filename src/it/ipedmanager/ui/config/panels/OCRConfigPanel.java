package it.ipedmanager.ui.config.panels;

import it.ipedmanager.config.PropertiesConfigFile;
import it.ipedmanager.utils.BundleManager;

import javax.swing.*;
import java.awt.*;

/**
 * Pannello per configurazione OCR (OCRConfig.txt)
 * 
 * @author William Tritapepe
 */
public class OCRConfigPanel extends BaseConfigPanel {

    private PropertiesConfigFile config;

    // Componenti
    private JComboBox<String> cmbLanguage;
    private JSpinner spnMinSize;
    private JSpinner spnMaxSize;
    private JSpinner spnPdfResolution;
    private JSpinner spnPageSegMode;
    private JSpinner spnMaxPdfTextSize;
    private JCheckBox chkSkipKnown;
    private JCheckBox chkExternalConv;
    private JCheckBox chkProcessNonStandard;
    private JTextField txtExternalMaxMem;
    private JSpinner spnMaxConvImageSize;
    private JComboBox<String> cmbPdfLib;

    private static final String[] LANGUAGES = {
            "ita", "eng", "por", "deu", "fra", "spa", "rus", "chi_sim", "jpn", "ara"
    };

    private static final String[] PDF_LIBS = { "icepdf", "pdfbox" };

    public OCRConfigPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_COLOR);

        // === SEZIONE 1: GENERALE ===
        content.add(createGeneralSection());
        content.add(Box.createVerticalStrut(10));

        // === SEZIONE 2: PDF & MOTORE ===
        content.add(createPdfEngineSection());
        content.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Info Panel
        add(createStyledInfoArea(
                BundleManager.getString("panel.ocr.info")),
                BorderLayout.SOUTH);
    }

    private JPanel createGeneralSection() {
        JPanel panel = createSectionPanel(BundleManager.getString("panel.ocr.section.general"));

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.anchor = GridBagConstraints.WEST;

        // Combo boxes
        cmbLanguage = createStyledComboBox(LANGUAGES);
        cmbPdfLib = createStyledComboBox(PDF_LIBS);

        // Spinners
        spnMinSize = createStyledSpinner();
        spnMinSize.setModel(new SpinnerNumberModel(1000, 0, 1000000, 100));
        spnMinSize.setToolTipText(BundleManager.getString("panel.ocr.tooltip.min"));

        spnMaxSize = createStyledSpinner();
        spnMaxSize.setModel(new SpinnerNumberModel(200, 1, 10000, 10));
        spnMaxSize.setToolTipText(BundleManager.getString("panel.ocr.tooltip.max"));

        // Row 0: Lingua + Libreria PDF + Min + Max
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0;
        grid.add(createStyledLabel(BundleManager.getString("panel.ocr.label.language")), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.15;
        grid.add(cmbLanguage, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        grid.add(createStyledLabel(BundleManager.getString("panel.ocr.label.pdflib")), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.15;
        grid.add(cmbPdfLib, gbc);
        gbc.gridx = 4;
        gbc.weightx = 0;
        grid.add(createStyledLabel(BundleManager.getString("panel.ocr.label.min")), gbc);
        gbc.gridx = 5;
        gbc.weightx = 0.2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        grid.add(spnMinSize, gbc);
        gbc.gridx = 6;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        grid.add(createStyledLabel(BundleManager.getString("panel.ocr.label.max")), gbc);
        gbc.gridx = 7;
        gbc.weightx = 0.2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        grid.add(spnMaxSize, gbc);

        // Checkbox
        // Checkbox
        chkSkipKnown = createStyledCheckBox(BundleManager.getString("panel.ocr.check.skipKnown"));
        chkProcessNonStandard = createStyledCheckBox(BundleManager.getString("panel.ocr.check.nonStandard"));

        // Row 1: Checkboxes
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 0.5;
        grid.add(chkSkipKnown, gbc);
        gbc.gridx = 4;
        gbc.gridwidth = 4;
        grid.add(chkProcessNonStandard, gbc);
        gbc.gridwidth = 1;

        GridBagConstraints panelGbc = new GridBagConstraints();
        panelGbc.gridx = 0;
        panelGbc.gridy = 0;
        panelGbc.weightx = 1.0;
        panelGbc.weighty = 1.0;
        panelGbc.fill = GridBagConstraints.HORIZONTAL;
        panelGbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(grid, panelGbc);
        return panel;
    }

    private JPanel createPdfEngineSection() {
        JPanel panel = createSectionPanel(BundleManager.getString("panel.ocr.section.engine"));

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.anchor = GridBagConstraints.WEST;

        // Spinners
        spnPdfResolution = createStyledSpinner();
        spnPdfResolution.setModel(new SpinnerNumberModel(250, 72, 600, 10));
        spnPdfResolution.setToolTipText(BundleManager.getString("panel.ocr.tooltip.dpi"));

        spnMaxPdfTextSize = createStyledSpinner();
        spnMaxPdfTextSize.setModel(new SpinnerNumberModel(100, 0, 10000, 10));
        spnMaxPdfTextSize.setToolTipText(BundleManager.getString("panel.ocr.tooltip.maxtext"));

        spnPageSegMode = createStyledSpinner();
        spnPageSegMode.setModel(new SpinnerNumberModel(1, 0, 13, 1));
        spnPageSegMode.setToolTipText(BundleManager.getString("panel.ocr.tooltip.pageseg"));

        spnMaxConvImageSize = createStyledSpinner();
        spnMaxConvImageSize.setModel(new SpinnerNumberModel(3000, 500, 10000, 100));
        spnMaxConvImageSize.setToolTipText(BundleManager.getString("panel.ocr.tooltip.maximg"));

        // Row 0: DPI + MaxText + PageSeg + MaxImg
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0;
        grid.add(createStyledLabel(BundleManager.getString("panel.ocr.label.dpi")), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        grid.add(spnPdfResolution, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        grid.add(createStyledLabel(BundleManager.getString("panel.ocr.label.maxtext")), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        grid.add(spnMaxPdfTextSize, gbc);
        gbc.gridx = 4;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        grid.add(createStyledLabel(BundleManager.getString("panel.ocr.label.pageseg")), gbc);
        gbc.gridx = 5;
        gbc.weightx = 0.2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        grid.add(spnPageSegMode, gbc);
        gbc.gridx = 6;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        grid.add(createStyledLabel(BundleManager.getString("panel.ocr.label.maximg")), gbc);
        gbc.gridx = 7;
        gbc.weightx = 0.2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        grid.add(spnMaxConvImageSize, gbc);

        // External conversion
        chkExternalConv = createStyledCheckBox(BundleManager.getString("panel.ocr.check.external"));
        chkExternalConv.setToolTipText(BundleManager.getString("panel.ocr.tooltip.external"));
        txtExternalMaxMem = createStyledTextField();
        txtExternalMaxMem.setPreferredSize(new Dimension(60, 28));
        txtExternalMaxMem.setToolTipText(BundleManager.getString("panel.ocr.tooltip.mem"));

        // Row 1: External conversion checkbox + memory field
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        grid.add(chkExternalConv, gbc);
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        grid.add(createStyledLabel(BundleManager.getString("panel.ocr.label.mem")), gbc);
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0.1;
        grid.add(txtExternalMaxMem, gbc);
        gbc.gridwidth = 1;

        GridBagConstraints panelGbc = new GridBagConstraints();
        panelGbc.gridx = 0;
        panelGbc.gridy = 0;
        panelGbc.weightx = 1.0;
        panelGbc.weighty = 1.0;
        panelGbc.fill = GridBagConstraints.HORIZONTAL;
        panelGbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(grid, panelGbc);
        return panel;
    }

    public void setConfig(PropertiesConfigFile config) {
        this.config = config;
        loadConfig();
    }

    @Override
    public void loadConfig() {
        if (config == null || !config.isLoaded())
            return;

        cmbLanguage.setSelectedItem(getOr(config.getString("OCRLanguage"), "eng"));
        chkSkipKnown.setSelected(config.getBoolean("skipKnownFiles", true));
        spnPageSegMode.setValue(config.getInt("pageSegMode", 1));
        spnMinSize.setValue(config.getInt("minFileSize2OCR", 1000));
        spnMaxSize.setValue(config.getInt("maxFileSize2OCR", 200000000) / 1000000);
        spnPdfResolution.setValue(config.getInt("pdfToImgResolution", 250));
        spnMaxPdfTextSize.setValue(config.getInt("maxPDFTextSize2OCR", 100));
        cmbPdfLib.setSelectedItem(getOr(config.getString("pdfToImgLib"), "icepdf"));
        chkExternalConv.setSelected(config.getBoolean("externalPdfToImgConv", true));
        txtExternalMaxMem.setText(getOr(config.getString("externalConvMaxMem"), "512M"));
        chkProcessNonStandard.setSelected(config.getBoolean("processNonStandard", true));
        spnMaxConvImageSize.setValue(config.getInt("maxConvImageSize", 3000));
    }

    @Override
    public void saveConfig() {
        if (config == null)
            return;

        config.setString("OCRLanguage", (String) cmbLanguage.getSelectedItem());
        config.setBoolean("skipKnownFiles", chkSkipKnown.isSelected());
        config.setInt("pageSegMode", (Integer) spnPageSegMode.getValue());
        config.setInt("minFileSize2OCR", (Integer) spnMinSize.getValue());
        config.setInt("maxFileSize2OCR", (Integer) spnMaxSize.getValue() * 1000000);
        config.setInt("pdfToImgResolution", (Integer) spnPdfResolution.getValue());
        config.setInt("maxPDFTextSize2OCR", (Integer) spnMaxPdfTextSize.getValue());
        config.setString("pdfToImgLib", (String) cmbPdfLib.getSelectedItem());
        config.setBoolean("externalPdfToImgConv", chkExternalConv.isSelected());
        config.setString("externalConvMaxMem", txtExternalMaxMem.getText());
        config.setBoolean("processNonStandard", chkProcessNonStandard.isSelected());
        config.setInt("maxConvImageSize", (Integer) spnMaxConvImageSize.getValue());
    }
}
