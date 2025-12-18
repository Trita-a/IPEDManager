package it.ipedmanager.ui.config.panels;

import it.ipedmanager.config.PropertiesConfigFile;
import it.ipedmanager.utils.BundleManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ElasticConfigPanel extends BaseConfigPanel {

    private PropertiesConfigFile config;

    private JTextField txtHost;
    private JSpinner spnPort;
    private JTextField txtProtocol;
    private JTextField txtIndexName;
    private JTextField txtTypeName;
    private JCheckBox chkUseAuth;
    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public ElasticConfigPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(BG_COLOR);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_COLOR);

        // === CONNESSIONE ===
        JPanel connPanel = createSectionPanel(BundleManager.getString("panel.elastic.section.connection"));

        txtHost = createStyledTextField();
        spnPort = createStyledSpinner();
        spnPort.setModel(new SpinnerNumberModel(9200, 1, 65535, 1));
        txtProtocol = createStyledTextField();
        txtProtocol = createStyledTextField();
        txtProtocol.setToolTipText(BundleManager.getString("panel.elastic.tooltip.protocol"));

        // Custom Layout: Host (big) | Port (small) | Protocol (small)
        JPanel cRow = new JPanel(new GridBagLayout());
        cRow.setBackground(BG_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 5); // Zero left inset
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Host
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        cRow.add(createStyledLabel(BundleManager.getString("panel.elastic.label.host")), gbc);
        gbc.gridx = 1;
        gbc.weightx = 2.0;
        cRow.add(txtHost, gbc);

        // Port
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(0, 10, 0, 5); // Gap between Host and Port
        cRow.add(createStyledLabel(BundleManager.getString("panel.elastic.label.port")), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, 0, 5);
        cRow.add(spnPort, gbc);

        // Protocol
        gbc.gridx = 4;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(0, 10, 0, 5);
        cRow.add(createStyledLabel(BundleManager.getString("panel.elastic.label.protocol")), gbc);
        gbc.gridx = 5;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, 0, 0);
        cRow.add(txtProtocol, gbc);

        addFullWidthComponent(connPanel, cRow, 0);

        content.add(connPanel);
        content.add(Box.createVerticalStrut(10));

        // === INDICE & AUTH ===
        JPanel configPanel = createSectionPanel(BundleManager.getString("panel.elastic.section.index"));

        txtIndexName = createStyledTextField();
        txtTypeName = createStyledTextField();
        chkUseAuth = createStyledCheckBox(BundleManager.getString("panel.elastic.check.auth"));

        // Row 1: Index [__] Type [__] Auth [x]
        // We use addCompactRow but we need to squeeze 3 elements (2 fields + 1
        // checkbox)

        JPanel row1 = new JPanel(new GridBagLayout());
        row1.setBackground(BG_COLOR);
        GridBagConstraints g1 = new GridBagConstraints();
        g1.fill = GridBagConstraints.HORIZONTAL;
        g1.insets = new Insets(0, 0, 0, 5);

        // Index
        g1.gridx = 0;
        g1.weightx = 0.0;
        row1.add(createStyledLabel(BundleManager.getString("panel.elastic.label.index")), g1);
        g1.gridx = 1;
        g1.weightx = 1.0;
        row1.add(txtIndexName, g1);

        // Type
        g1.gridx = 2;
        g1.weightx = 0.0;
        g1.insets = new Insets(0, 10, 0, 5);
        row1.add(createStyledLabel(BundleManager.getString("panel.elastic.label.type")), g1);
        g1.gridx = 3;
        g1.weightx = 0.5;
        g1.insets = new Insets(0, 0, 0, 5);
        row1.add(txtTypeName, g1);

        // Checkbox
        g1.gridx = 4;
        g1.weightx = 0.0;
        g1.insets = new Insets(0, 10, 0, 0);
        row1.add(chkUseAuth, g1);

        addFullWidthComponent(configPanel, row1, 0);

        // Row 2: User + Pass
        txtUsername = createStyledTextField();
        txtPassword = new JPasswordField();
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 5, 4, 5))); // Use compact padding

        addCompactRow(configPanel, 1,
                createStyledLabel(BundleManager.getString("panel.elastic.label.user")), txtUsername,
                createStyledLabel(BundleManager.getString("panel.elastic.label.pass")), txtPassword);

        content.add(configPanel);

        // Listener
        chkUseAuth.addActionListener(e -> {
            txtUsername.setEnabled(chkUseAuth.isSelected());
            txtPassword.setEnabled(chkUseAuth.isSelected());
        });

        // Warning
        content.add(Box.createVerticalStrut(10));
        JComponent warning = createStyledInfoArea(
                BundleManager.getString("panel.elastic.info"));
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
        spnPort.setValue(config.getInt("port", 9200));
        txtProtocol.setText(getOr(config.getString("protocol"), "http"));
        txtIndexName.setText(getOr(config.getString("index"), "iped"));
        txtTypeName.setText(getOr(config.getString("type"), "item"));

        boolean useAuth = !getOr(config.getString("user"), "").isEmpty();
        chkUseAuth.setSelected(useAuth);
        txtUsername.setText(getOr(config.getString("user"), ""));
        txtPassword.setText(getOr(config.getString("password"), ""));
        txtUsername.setEnabled(useAuth);
        txtPassword.setEnabled(useAuth);
    }

    @Override
    public void saveConfig() {
        if (config == null)
            return;

        config.setString("host", txtHost.getText());
        config.setInt("port", (Integer) spnPort.getValue());
        config.setString("protocol", txtProtocol.getText());
        config.setString("index", txtIndexName.getText());
        config.setString("type", txtTypeName.getText());

        if (chkUseAuth.isSelected()) {
            config.setString("user", txtUsername.getText());
            config.setString("password", new String(txtPassword.getPassword()));
        } else {
            config.setString("user", "");
            config.setString("password", "");
        }
    }
}
