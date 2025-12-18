package it.ipedmanager.ui.config.panels;

import it.ipedmanager.config.PropertiesConfigFile;
import it.ipedmanager.utils.BundleManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Pannello generico che crea UI basandosi sulle proprietà trovate nel file.
 * Utile per importare "tutto" senza scrivere 10 classi a mano.
 */
public class GenericConfigPanel extends BaseConfigPanel {

    private String configFileName;
    private JPanel fieldsPanel;
    private List<ConfigEntry> entries;

    private static class ConfigEntry {
        String key;
        JComponent component;

        ConfigEntry(String k, JComponent c) {
            key = k;
            component = c;
        }
    }

    public GenericConfigPanel(String configFileName) {
        this.configFileName = configFileName;
        this.entries = new ArrayList<>();
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane();
        scroll.setBorder(null);

        fieldsPanel = new JPanel(new GridBagLayout());
        scroll.setViewportView(fieldsPanel);

        add(scroll, BorderLayout.CENTER);
    }

    @Override
    public void loadConfig() {
        PropertiesConfigFile cfg = configManager.getConfigFile(configFileName);
        fieldsPanel.removeAll();
        entries.clear();

        if (cfg == null || !cfg.isLoaded()) {
            fieldsPanel.add(new JLabel(BundleManager.getString("panel.generic.error.notFound", configFileName)));
            return;
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        // Ordina le chiavi
        java.util.List<String> keys = new ArrayList<>(cfg.getKeys());
        java.util.Collections.sort(keys);

        for (String key : keys) {
            String value = cfg.get(key);

            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0.0;
            fieldsPanel.add(new JLabel(key + ":"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            JComponent comp;

            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                JCheckBox chk = new JCheckBox();
                chk.setSelected(Boolean.parseBoolean(value));
                comp = chk;
            } else {
                JTextField txt = new JTextField(value);
                comp = txt;
            }

            fieldsPanel.add(comp, gbc);
            entries.add(new ConfigEntry(key, comp));
            row++;
        }

        // Push everything up
        gbc.gridy = row;
        gbc.weighty = 1.0;
        fieldsPanel.add(new JPanel(), gbc);

        revalidate();
        repaint();
    }

    @Override
    public void saveConfig() {
        PropertiesConfigFile cfg = configManager.getConfigFile(configFileName);
        if (cfg == null)
            return;

        for (ConfigEntry entry : entries) {
            String val;
            if (entry.component instanceof JCheckBox) {
                val = String.valueOf(((JCheckBox) entry.component).isSelected());
            } else {
                val = ((JTextField) entry.component).getText();
            }
            cfg.set(entry.key, val);
        }
    }
}
