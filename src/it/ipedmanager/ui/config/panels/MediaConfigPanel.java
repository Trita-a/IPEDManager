package it.ipedmanager.ui.config.panels;

import it.ipedmanager.config.PropertiesConfigFile;
import it.ipedmanager.ui.components.ToggleSwitch;
import it.ipedmanager.ui.config.VectorIcon;
import it.ipedmanager.utils.BundleManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Pannello unificato per configurazione Media (Immagini + Video)
 * Unisce ImageThumbsConfig.txt e VideoThumbsConfig.txt
 */
public class MediaConfigPanel extends BaseConfigPanel {

    private PropertiesConfigFile imageConfig;
    private PropertiesConfigFile videoConfig;
    private PropertiesConfigFile ipedConfig; // Main config for enable flags

    // === IMMAGINI ===
    private ToggleSwitch switchEnableImageThumbs;
    private JSpinner spnImgThumbSize;
    private JSpinner spnImgGallerySize;
    private JCheckBox chkExtractExif;
    private JCheckBox chkLogGPS;
    private JCheckBox chkImgSkipKnown;

    // === VIDEO ===
    private JSpinner spnVidRows;
    private JSpinner spnVidColumns;
    private JSpinner spnVidThumbWidth;
    private JSpinner spnVidTimeout;
    private JSpinner spnVidMinDuration;
    private JSpinner spnVidMaxDuration;
    private JCheckBox chkVidGalleryThumbs;
    private JCheckBox chkVidSkipKnown;
    private ToggleSwitch switchEnableVideoThumbs;

    public MediaConfigPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(8, 10, 8, 10));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_COLOR);

        // === SEZIONE IMMAGINI ===
        content.add(createImageSection());
        content.add(Box.createVerticalStrut(12));

        // === SEZIONE VIDEO ===
        content.add(createVideoSection());
        content.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Info Panel
        add(createStyledInfoArea(
                BundleManager.getString("panel.media.info")),
                BorderLayout.SOUTH);
    }

    private JPanel createImageSection() {
        switchEnableImageThumbs = new ToggleSwitch();
        switchEnableImageThumbs.addActionListener(enabled -> updateImageSectionState());

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.anchor = GridBagConstraints.WEST;

        // Spinner dimensioni
        spnImgThumbSize = createStyledSpinner();
        spnImgThumbSize.setModel(new SpinnerNumberModel(160, 50, 500, 10));
        spnImgThumbSize.setToolTipText(BundleManager.getString("panel.media.tooltip.imgThumb"));

        spnImgGallerySize = createStyledSpinner();
        spnImgGallerySize.setModel(new SpinnerNumberModel(64, 32, 256, 8));
        spnImgGallerySize.setToolTipText(BundleManager.getString("panel.media.tooltip.galleryThumb"));

        // Row 0: Dimensioni
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.15;
        grid.add(createStyledLabel(BundleManager.getString("panel.media.label.thumb")), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        grid.add(spnImgThumbSize, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0.15;
        gbc.fill = GridBagConstraints.NONE;
        grid.add(createStyledLabel(BundleManager.getString("panel.media.label.gallery")), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.35;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        grid.add(spnImgGallerySize, gbc);

        // Checkbox opzioni
        chkExtractExif = createStyledCheckBox(BundleManager.getString("panel.media.check.exif"));
        chkLogGPS = createStyledCheckBox(BundleManager.getString("panel.media.check.gps"));
        chkImgSkipKnown = createStyledCheckBox(BundleManager.getString("panel.media.check.skipKnown"));

        // Row 1: Checkbox
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.weightx = 0.25;
        grid.add(chkExtractExif, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.25;
        grid.add(chkLogGPS, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0.25;
        grid.add(chkImgSkipKnown, gbc);

        // Create the card wrapping the grid
        return createCardPanel(BundleManager.getString("panel.media.title.images"), "image", switchEnableImageThumbs,
                grid);
    }

    private JPanel createVideoSection() {
        switchEnableVideoThumbs = new ToggleSwitch();
        switchEnableVideoThumbs.addActionListener(enabled -> updateVideoSectionState());

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.anchor = GridBagConstraints.WEST;

        // Spinner griglia
        spnVidRows = createStyledSpinner();
        spnVidRows.setModel(new SpinnerNumberModel(3, 1, 10, 1));
        spnVidRows.setToolTipText(BundleManager.getString("panel.media.tooltip.rows"));

        spnVidColumns = createStyledSpinner();
        spnVidColumns.setModel(new SpinnerNumberModel(8, 1, 20, 1));
        spnVidColumns.setToolTipText(BundleManager.getString("panel.media.tooltip.columns"));

        spnVidThumbWidth = createStyledSpinner();
        spnVidThumbWidth.setModel(new SpinnerNumberModel(200, 50, 500, 10));
        spnVidThumbWidth.setToolTipText(BundleManager.getString("panel.media.tooltip.vidWidth"));

        // Row 0: Griglia
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        grid.add(createStyledLabel(BundleManager.getString("panel.media.label.rows")), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        grid.add(spnVidRows, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        grid.add(createStyledLabel(BundleManager.getString("panel.media.label.columns")), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        grid.add(spnVidColumns, gbc);
        gbc.gridx = 4;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        grid.add(createStyledLabel(BundleManager.getString("panel.media.label.width")), gbc);
        gbc.gridx = 5;
        gbc.weightx = 0.2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        grid.add(spnVidThumbWidth, gbc);

        // Spinner limiti
        spnVidTimeout = createStyledSpinner();
        spnVidTimeout.setModel(new SpinnerNumberModel(180, 30, 600, 30));
        spnVidTimeout.setToolTipText(BundleManager.getString("panel.media.tooltip.vidTimeout"));

        spnVidMinDuration = createStyledSpinner();
        spnVidMinDuration.setModel(new SpinnerNumberModel(3, 0, 60, 1));
        spnVidMinDuration.setToolTipText(BundleManager.getString("panel.media.tooltip.minDur"));

        spnVidMaxDuration = createStyledSpinner();
        spnVidMaxDuration.setModel(new SpinnerNumberModel(0, 0, 36000, 60));
        spnVidMaxDuration.setToolTipText(BundleManager.getString("panel.media.tooltip.maxDur"));

        // Row 1: Limiti
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        grid.add(createStyledLabel(BundleManager.getString("panel.media.label.timeout")), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        grid.add(spnVidTimeout, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        grid.add(createStyledLabel(BundleManager.getString("panel.media.label.minDur")), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        grid.add(spnVidMinDuration, gbc);
        gbc.gridx = 4;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        grid.add(createStyledLabel(BundleManager.getString("panel.media.label.maxDur")), gbc);
        gbc.gridx = 5;
        gbc.weightx = 0.2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        grid.add(spnVidMaxDuration, gbc);

        // Checkbox
        chkVidGalleryThumbs = createStyledCheckBox(BundleManager.getString("panel.media.check.galleryThumbs"));
        chkVidSkipKnown = createStyledCheckBox(BundleManager.getString("panel.media.check.skipKnown"));

        // Row 2: Checkbox
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.weightx = 0.25;
        gbc.gridwidth = 2;
        grid.add(chkVidGalleryThumbs, gbc);
        gbc.gridx = 2;
        gbc.gridwidth = 2;
        grid.add(chkVidSkipKnown, gbc);
        gbc.gridwidth = 1;

        // Create the card wrapping the grid
        return createCardPanel(BundleManager.getString("panel.media.title.video"), "video", switchEnableVideoThumbs,
                grid);
    }

    /**
     * Creates a styled card panel with Icon, Title, Switch and Content.
     */
    private JPanel createCardPanel(String title, String iconName, ToggleSwitch toggle, JComponent content) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(BG_COLOR);
        // Subtle border to frame the card
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)), // Light separator
                new EmptyBorder(12, 4, 16, 4)));

        // Header
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(BG_COLOR);

        // Left: Icon + Title + Switch (now all together)
        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftBox.setBackground(BG_COLOR);

        JLabel iconLbl = new JLabel(new VectorIcon(iconName, 20, ACCENT_COLOR));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(ACCENT_COLOR);

        leftBox.add(iconLbl);
        leftBox.add(titleLbl);

        // Add a spacer between title and switch
        leftBox.add(Box.createHorizontalStrut(10));
        leftBox.add(toggle);

        header.add(leftBox, BorderLayout.WEST);

        card.add(header, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    public void setImageConfig(PropertiesConfigFile config) {
        this.imageConfig = config;
        loadImageConfig();
    }

    public void setVideoConfig(PropertiesConfigFile config) {
        this.videoConfig = config;
        loadVideoConfig();
    }

    public void setIPEDConfig(PropertiesConfigFile config) {
        this.ipedConfig = config;
        loadIPEDConfig();
    }

    private void loadIPEDConfig() {
        if (ipedConfig == null || !ipedConfig.isLoaded())
            return;

        switchEnableImageThumbs.setSelected(ipedConfig.getBoolean("enableImageThumbs", true));
        switchEnableVideoThumbs.setSelected(ipedConfig.getBoolean("enableVideoThumbs", true));

        // Update UI states
        updateImageSectionState();
        updateVideoSectionState();
    }

    private void updateImageSectionState() {
        boolean enabled = switchEnableImageThumbs.isSelected();
        spnImgThumbSize.setEnabled(enabled);
        spnImgGallerySize.setEnabled(enabled);
        chkExtractExif.setEnabled(enabled);
        chkLogGPS.setEnabled(enabled);
        chkImgSkipKnown.setEnabled(enabled);
    }

    private void updateVideoSectionState() {
        boolean enabled = switchEnableVideoThumbs.isSelected();
        spnVidRows.setEnabled(enabled);
        spnVidColumns.setEnabled(enabled);
        spnVidThumbWidth.setEnabled(enabled);
        spnVidTimeout.setEnabled(enabled);
        spnVidMinDuration.setEnabled(enabled);
        spnVidMaxDuration.setEnabled(enabled);
        chkVidGalleryThumbs.setEnabled(enabled);
        chkVidSkipKnown.setEnabled(enabled);
    }

    private void loadImageConfig() {
        if (imageConfig == null || !imageConfig.isLoaded())
            return;

        spnImgThumbSize.setValue(imageConfig.getInt("thumbSize", 160));
        spnImgGallerySize.setValue(imageConfig.getInt("galleryThumbSize", 64));
        chkExtractExif.setSelected(imageConfig.getBoolean("extractExif", true));
        chkLogGPS.setSelected(imageConfig.getBoolean("logGPS", true));
        chkImgSkipKnown.setSelected(imageConfig.getBoolean("skipKnownFiles", false));
    }

    private void loadVideoConfig() {
        if (videoConfig == null || !videoConfig.isLoaded())
            return;

        spnVidRows.setValue(videoConfig.getInt("rows", 3));
        spnVidColumns.setValue(videoConfig.getInt("columns", 8));
        spnVidThumbWidth.setValue(videoConfig.getInt("thumbWidth", 200));
        spnVidTimeout.setValue(videoConfig.getInt("timeout", 180));
        spnVidMinDuration.setValue(videoConfig.getInt("minDuration", 3));
        spnVidMaxDuration.setValue(videoConfig.getInt("maxDuration", 0));
        chkVidGalleryThumbs.setSelected(videoConfig.getBoolean("galleryThumbs", true));
        chkVidSkipKnown.setSelected(videoConfig.getBoolean("skipKnownFiles", true));
    }

    @Override
    public void loadConfig() {
        loadImageConfig();
        loadVideoConfig();
        loadIPEDConfig();
    }

    @Override
    public void saveConfig() {
        saveImageConfig();
        saveVideoConfig();
        saveIPEDConfig();
    }

    private void saveIPEDConfig() {
        if (ipedConfig == null)
            return;
        ipedConfig.setBoolean("enableImageThumbs", switchEnableImageThumbs.isSelected());
        ipedConfig.setBoolean("enableVideoThumbs", switchEnableVideoThumbs.isSelected());
    }

    private void saveImageConfig() {
        if (imageConfig == null)
            return;

        imageConfig.setInt("thumbSize", (Integer) spnImgThumbSize.getValue());
        imageConfig.setInt("galleryThumbSize", (Integer) spnImgGallerySize.getValue());
        imageConfig.setBoolean("extractExif", chkExtractExif.isSelected());
        imageConfig.setBoolean("logGPS", chkLogGPS.isSelected());
        imageConfig.setBoolean("skipKnownFiles", chkImgSkipKnown.isSelected());
    }

    private void saveVideoConfig() {
        if (videoConfig == null)
            return;

        videoConfig.setInt("rows", (Integer) spnVidRows.getValue());
        videoConfig.setInt("columns", (Integer) spnVidColumns.getValue());
        videoConfig.setInt("thumbWidth", (Integer) spnVidThumbWidth.getValue());
        videoConfig.setInt("timeout", (Integer) spnVidTimeout.getValue());
        videoConfig.setInt("minDuration", (Integer) spnVidMinDuration.getValue());
        videoConfig.setInt("maxDuration", (Integer) spnVidMaxDuration.getValue());
        videoConfig.setBoolean("galleryThumbs", chkVidGalleryThumbs.isSelected());
        videoConfig.setBoolean("skipKnownFiles", chkVidSkipKnown.isSelected());
    }
}
