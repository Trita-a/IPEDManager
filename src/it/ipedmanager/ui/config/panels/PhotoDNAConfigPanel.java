package it.ipedmanager.ui.config.panels;

import it.ipedmanager.config.PropertiesConfigFile;
import it.ipedmanager.utils.BundleManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PhotoDNAConfigPanel extends BaseConfigPanel {

    private PropertiesConfigFile config;

    private JCheckBox chkComputeFromThumbnail;
    private JSpinner spnMinFileSize;
    private JCheckBox chkSkipHashDBFiles;

    public PhotoDNAConfigPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(BG_COLOR);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_COLOR);

        // === OPZIONI ===
        JPanel optPanel = createSectionPanel(BundleManager.getString("panel.photodna.section.config"));

        spnMinFileSize = createStyledSpinner();
        spnMinFileSize.setModel(new SpinnerNumberModel(4000, 100, 100000, 500));

        chkComputeFromThumbnail = createStyledCheckBox(BundleManager.getString("panel.photodna.check.thumbnail"));
        chkComputeFromThumbnail.setToolTipText(BundleManager.getString("panel.photodna.tooltip.thumbnail"));

        chkSkipHashDBFiles = createStyledCheckBox(BundleManager.getString("panel.photodna.check.skipHash"));
        chkSkipHashDBFiles.setToolTipText(BundleManager.getString("panel.photodna.tooltip.skipHash"));

        // Single Ultra-Compact Row: Min Size | Checkbox 1 | Checkbox 2
        JPanel row = new JPanel(new GridBagLayout());
        row.setBackground(BG_COLOR);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        // Min Size Label + Spinner
        g.gridx = 0;
        g.weightx = 0.0;
        row.add(createStyledLabel(BundleManager.getString("panel.photodna.label.minSize")), g);
        g.gridx = 1;
        g.weightx = 0.3;
        g.insets = new Insets(0, 5, 0, 10);
        row.add(spnMinFileSize, g);

        // Check 1
        g.gridx = 2;
        g.weightx = 0.0;
        g.insets = new Insets(0, 0, 0, 10);
        row.add(chkComputeFromThumbnail, g);

        // Check 2
        g.gridx = 3;
        g.weightx = 0.0;
        g.insets = new Insets(0, 0, 0, 0);
        row.add(chkSkipHashDBFiles, g);

        // Filler
        g.gridx = 4;
        g.weightx = 1.0;
        row.add(Box.createHorizontalGlue(), g);

        addFullWidthComponent(optPanel, row, 0);

        content.add(optPanel);
        content.add(Box.createVerticalStrut(10));

        // === INFO ===
        // === INFO ===
        JComponent infoText = createStyledInfoArea(
                BundleManager.getString("panel.photodna.info"));
        // Add to SOUTH to fix sizing and layout issues
        add(infoText, BorderLayout.SOUTH);

        content.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
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

        chkComputeFromThumbnail.setSelected(config.getBoolean("computeFromThumbnail", true));
        chkSkipHashDBFiles.setSelected(config.getBoolean("skipHashDBFiles", true));
        spnMinFileSize.setValue(config.getInt("minFileSize", 4000));
    }

    @Override
    public void saveConfig() {
        if (config == null)
            return;

        config.setBoolean("computeFromThumbnail", chkComputeFromThumbnail.isSelected());
        config.setBoolean("skipHashDBFiles", chkSkipHashDBFiles.isSelected());
        config.setInt("minFileSize", (Integer) spnMinFileSize.getValue());
    }
}
