package it.ipedmanager.ui.config.panels;

import it.ipedmanager.config.PropertiesConfigFile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MinIOConfigPanel extends BaseConfigPanel {

    private PropertiesConfigFile config;

    private JTextField txtHost;
    private JSpinner spnPort;
    private JTextField txtBucket;
    private JTextField txtAccessKey;
    private JPasswordField txtSecretKey;
    private JCheckBox chkSecure;

    public MinIOConfigPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(BG_COLOR);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_COLOR);

        // === CONNESSIONE & CREDENZIALI ===
        // Merging everything into one main block?
        JPanel mainPanel = createSectionPanel(
                it.ipedmanager.utils.BundleManager.getString("panel.minio.section.config"));

        txtHost = createStyledTextField();
        spnPort = createStyledSpinner();
        spnPort.setModel(new SpinnerNumberModel(9000, 1, 65535, 1));
        txtBucket = createStyledTextField();

        // Row 1: Host | Port | Bucket
        JPanel r1 = new JPanel(new GridBagLayout());
        r1.setBackground(BG_COLOR);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 0, 5);

        // Host
        g.gridx = 0;
        g.weightx = 0.0;
        r1.add(createStyledLabel(it.ipedmanager.utils.BundleManager.getString("panel.minio.label.host")), g);
        g.gridx = 1;
        g.weightx = 1.5;
        r1.add(txtHost, g);

        // Port
        g.gridx = 2;
        g.weightx = 0.0;
        g.insets = new Insets(0, 10, 0, 5);
        r1.add(createStyledLabel(it.ipedmanager.utils.BundleManager.getString("panel.minio.label.port")), g);
        g.gridx = 3;
        g.weightx = 0.4;
        g.insets = new Insets(0, 0, 0, 5);
        r1.add(spnPort, g);

        // Bucket
        g.gridx = 4;
        g.weightx = 0.0;
        g.insets = new Insets(0, 10, 0, 5);
        r1.add(createStyledLabel(it.ipedmanager.utils.BundleManager.getString("panel.minio.label.bucket")), g);
        g.gridx = 5;
        g.weightx = 0.8;
        g.insets = new Insets(0, 0, 0, 0);
        r1.add(txtBucket, g);

        addFullWidthComponent(mainPanel, r1, 0);

        // Row 2: Secure [x] | Access Key | Secret Key
        chkSecure = createStyledCheckBox(it.ipedmanager.utils.BundleManager.getString("panel.minio.check.secure"));
        txtAccessKey = createStyledTextField();
        txtSecretKey = new JPasswordField();
        txtSecretKey.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 5, 4, 5)));

        // We use addCompactRow but Chk needs wrapper to behave as a component
        // Actually addCompactRow logic: [Comp] [Comp] ...
        // If we want [Chk] [Label] [Field] [Label] [Field]
        // addCompactRow handles Label specially (no grow).
        // Let's create a wrapper panel for row 2

        JPanel r2 = new JPanel(new GridBagLayout());
        r2.setBackground(BG_COLOR);
        g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 0, 5);

        // Checkbox Secure
        g.gridx = 0;
        g.weightx = 0.0;
        r2.add(chkSecure, g);

        // Access
        g.gridx = 1;
        g.weightx = 0.0;
        g.insets = new Insets(0, 10, 0, 5);
        r2.add(createStyledLabel(it.ipedmanager.utils.BundleManager.getString("panel.minio.label.accessKey")), g);
        g.gridx = 2;
        g.weightx = 1.0;
        g.insets = new Insets(0, 0, 0, 5);
        r2.add(txtAccessKey, g);

        // Secret
        g.gridx = 3;
        g.weightx = 0.0;
        g.insets = new Insets(0, 10, 0, 5);
        r2.add(createStyledLabel(it.ipedmanager.utils.BundleManager.getString("panel.minio.label.secretKey")), g);
        g.gridx = 4;
        g.weightx = 1.0;
        g.insets = new Insets(0, 0, 0, 0);
        r2.add(txtSecretKey, g);

        addFullWidthComponent(mainPanel, r2, 1);

        content.add(mainPanel);

        // Warning
        content.add(Box.createVerticalStrut(10));
        // Warning
        content.add(Box.createVerticalStrut(10));
        JComponent warning = createStyledInfoArea(
                it.ipedmanager.utils.BundleManager.getString("panel.minio.info"));
        // Add to SOUTH to fix sizing and layout issues
        add(warning, BorderLayout.SOUTH);

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

        txtHost.setText(getOr(config.getString("host"), ""));
        spnPort.setValue(config.getInt("port", 9000));
        txtBucket.setText(getOr(config.getString("bucket"), "iped"));
        txtAccessKey.setText(getOr(config.getString("accessKey"), ""));
        txtSecretKey.setText(getOr(config.getString("secretKey"), ""));
        chkSecure.setSelected(config.getBoolean("secure", false));
    }

    @Override
    public void saveConfig() {
        if (config == null)
            return;

        config.setString("host", txtHost.getText());
        config.setInt("port", (Integer) spnPort.getValue());
        config.setString("bucket", txtBucket.getText());
        config.setString("accessKey", txtAccessKey.getText());
        config.setString("secretKey", new String(txtSecretKey.getPassword()));
        config.setBoolean("secure", chkSecure.isSelected());
    }
}
