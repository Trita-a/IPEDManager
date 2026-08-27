package it.ipedmanager.ui.config.panels;

import it.ipedmanager.service.RamDiskService;
import it.ipedmanager.utils.BundleManager;
import it.ipedmanager.ui.config.VectorIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class RamDiskConfigPanel extends BaseConfigPanel {

    private static final Color PRIMARY_BLUE = new Color(30, 58, 138);

    private JComboBox<String> cmbEngine;
    private JTextField txtOsfPath;
    private JTextField txtArsenalPath;
    private JSpinner spnSize;
    private JComboBox<String> cmbLetter;
    private JCheckBox chkAutoMode;

    private JButton btnMount;
    private JButton btnUnmount;

    public RamDiskConfigPanel() {
        initComponents();
        loadConfig();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_COLOR);

        // === MOTORE ===
        JPanel enginePanel = createSectionPanel(BundleManager.getString("panel.ramdisk.section.engine"));
        cmbEngine = createStyledComboBox(new String[]{RamDiskService.ENGINE_OSF, RamDiskService.ENGINE_ARSENAL});
        cmbEngine.addActionListener(e -> updateEngineVisibility());
        addCompactRow(enginePanel, 0, createStyledLabel(BundleManager.getString("panel.ramdisk.label.engine")), cmbEngine);
        content.add(enginePanel);
        content.add(Box.createVerticalStrut(15));

        // === PERCORSI ===
        JPanel pathsPanel = createSectionPanel(BundleManager.getString("panel.ramdisk.section.paths"));
        txtOsfPath = createStyledTextField();
        addFileField(pathsPanel, BundleManager.getString("panel.ramdisk.label.osfPath"), txtOsfPath, false, 0);

        txtArsenalPath = createStyledTextField();
        addFileField(pathsPanel, BundleManager.getString("panel.ramdisk.label.arsenalPath"), txtArsenalPath, false, 1);
        content.add(pathsPanel);
        content.add(Box.createVerticalStrut(15));

        // === PARAMETRI ===
        JPanel paramsPanel = createSectionPanel(BundleManager.getString("panel.ramdisk.section.params"));
        spnSize = createStyledSpinner();
        spnSize.setModel(new SpinnerNumberModel(32, 1, 1024, 1));
        
        String[] letters = new String[26];
        for (int i = 0; i < 26; i++) {
            letters[i] = (char)('A' + i) + ":";
        }
        cmbLetter = createStyledComboBox(letters);

        addCompactRow(paramsPanel, 0, 
            createStyledLabel(BundleManager.getString("panel.ramdisk.label.size")), spnSize,
            createStyledLabel(BundleManager.getString("panel.ramdisk.label.letter")), cmbLetter
        );
        content.add(paramsPanel);
        content.add(Box.createVerticalStrut(15));

        // === OPZIONI ===
        JPanel optPanel = createSectionPanel(BundleManager.getString("panel.ramdisk.section.options"));
        chkAutoMode = createStyledCheckBox(BundleManager.getString("panel.ramdisk.check.auto"));
        addFullWidthComponent(optPanel, chkAutoMode, 0);
        content.add(optPanel);
        content.add(Box.createVerticalStrut(15));

        // === AZIONI MANUALI ===
        JPanel actionPanel = createSectionPanel(BundleManager.getString("panel.ramdisk.section.actions"));
        btnMount = createStyledButton(BundleManager.getString("panel.ramdisk.btn.mount"));
        btnMount.setIcon(new VectorIcon("play", 16, PRIMARY_BLUE));
        btnMount.addActionListener(this::handleMount);

        btnUnmount = createStyledButton(BundleManager.getString("panel.ramdisk.btn.unmount"));
        btnUnmount.setIcon(new VectorIcon("trash", 16, new Color(220, 38, 38)));
        btnUnmount.addActionListener(this::handleUnmount);

        addCompactRow(actionPanel, 0, btnMount, btnUnmount);
        content.add(actionPanel);
        content.add(Box.createVerticalStrut(15));

        // === INFO ===
        content.add(createInfoPanel(java.util.Collections.singletonList(
            BundleManager.getString("panel.ramdisk.info")
        )));
        content.add(Box.createVerticalGlue());

        add(content, BorderLayout.NORTH);
    }

    private void updateEngineVisibility() {
        boolean isOsf = RamDiskService.ENGINE_OSF.equals(cmbEngine.getSelectedItem());
        cmbLetter.setEnabled(isOsf);
        // We could also show/hide the path fields based on the engine, but keeping them both is fine.
    }

    @Override
    public void loadConfig() {
        RamDiskService svc = RamDiskService.getInstance();
        cmbEngine.setSelectedItem(svc.getEngine());
        txtOsfPath.setText(svc.getOsfPath());
        txtArsenalPath.setText(svc.getArsenalPath());
        spnSize.setValue(svc.getSizeGb());
        cmbLetter.setSelectedItem(svc.getDriveLetter());
        chkAutoMode.setSelected(svc.isAutoMode());
        updateEngineVisibility();
    }

    @Override
    public void saveConfig() {
        RamDiskService svc = RamDiskService.getInstance();
        svc.setEngine((String) cmbEngine.getSelectedItem());
        svc.setOsfPath(txtOsfPath.getText());
        svc.setArsenalPath(txtArsenalPath.getText());
        svc.setSizeGb((Integer) spnSize.getValue());
        svc.setDriveLetter((String) cmbLetter.getSelectedItem());
        svc.setAutoMode(chkAutoMode.isSelected());
    }

    public boolean save() {
        saveConfig();
        return true;
    }

    private void handleMount(ActionEvent e) {
        save(); // Ensure settings are saved before running
        btnMount.setEnabled(false);
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return RamDiskService.getInstance().mount();
            }
            @Override
            protected void done() {
                btnMount.setEnabled(true);
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(RamDiskConfigPanel.this, 
                            BundleManager.getString("panel.ramdisk.msg.mountSuccess"),
                            "Info", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(RamDiskConfigPanel.this, 
                            BundleManager.getString("panel.ramdisk.msg.mountFail"),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void handleUnmount(ActionEvent e) {
        save();
        btnUnmount.setEnabled(false);
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return RamDiskService.getInstance().unmount();
            }
            @Override
            protected void done() {
                btnUnmount.setEnabled(true);
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(RamDiskConfigPanel.this, 
                            BundleManager.getString("panel.ramdisk.msg.unmountSuccess"),
                            "Info", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(RamDiskConfigPanel.this, 
                            BundleManager.getString("panel.ramdisk.msg.unmountFail"),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }
}
