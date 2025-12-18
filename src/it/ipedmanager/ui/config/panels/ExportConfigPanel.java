package it.ipedmanager.ui.config.panels;

import it.ipedmanager.config.ConfigManager;
import it.ipedmanager.utils.BundleManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Pannello per configurazione Export automatico
 * (CategoriesToExport.txt + KeywordsToExport.txt)
 */
public class ExportConfigPanel extends BaseConfigPanel {

    private Path categoriesFile;
    private Path keywordsFile;

    private JPanel categoriesPanel;
    private JTextArea keywordsArea;
    private final Map<String, JCheckBox> categoryCheckboxes = new LinkedHashMap<>();

    public ExportConfigPanel() {
        initComponents();
    }

    private static final String DEFAULT_INSTRUCTIONS = "# Keywords da esportare automaticamente\n# Una keyword per riga";

    // ...

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel content = new JPanel(new BorderLayout());
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        splitPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        splitPane.setBackground(BG_COLOR);

        // Left: Categories
        categoriesPanel = new JPanel();
        categoriesPanel.setLayout(new BoxLayout(categoriesPanel, BoxLayout.Y_AXIS));
        categoriesPanel.setBackground(Color.WHITE);

        JScrollPane catScroll = new JScrollPane(categoriesPanel);
        catScroll.getVerticalScrollBar().setUnitIncrement(16); // Fix slow scroll
        catScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR),
                        BundleManager.getString("panel.export.title.categories"), TitledBorder.LEFT, TitledBorder.TOP,
                        TITLE_FONT, ACCENT_COLOR),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        catScroll.setBackground(BG_COLOR);
        splitPane.setLeftComponent(catScroll);

        // Right: Keywords
        keywordsArea = new JTextArea();
        keywordsArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        keywordsArea.setToolTipText(BundleManager.getString("panel.export.tooltip.keywords"));
        keywordsArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Placeholder Logic
        keywordsArea.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (keywordsArea.getText().trim().equals(DEFAULT_INSTRUCTIONS)) {
                    keywordsArea.setText("");
                    keywordsArea.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (keywordsArea.getText().trim().isEmpty()) {
                    keywordsArea.setForeground(Color.GRAY);
                    keywordsArea.setText(DEFAULT_INSTRUCTIONS);
                }
            }
        });

        JScrollPane keyScroll = new JScrollPane(keywordsArea);
        keyScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR),
                        BundleManager.getString("panel.export.title.keywords"), TitledBorder.LEFT, TitledBorder.TOP,
                        TITLE_FONT, ACCENT_COLOR),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        keyScroll.setBackground(BG_COLOR);
        splitPane.setRightComponent(keyScroll);
        splitPane.setDividerLocation(250); // Default wider for categories
        splitPane.setResizeWeight(0.5);

        content.add(splitPane, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    // Override loadConfig/saveConfig empty as we use custom logic here
    @Override
    public void loadConfig() {
    }

    @Override
    public void saveConfig() {
        saveCategories();
        saveKeywords();
    }

    public void setConfigManager(ConfigManager configManager) {
        if (configManager.getConfPath() != null) {
            this.categoriesFile = configManager.getConfPath().resolve("CategoriesToExport.txt");
            this.keywordsFile = configManager.getConfPath().resolve("KeywordsToExport.txt");
            loadFiles();
        }
    }

    public void loadFiles() {
        loadCategories();
        loadKeywords();
    }

    private void loadKeywords() {
        if (keywordsFile == null || !Files.exists(keywordsFile))
            return;

        try {
            StringBuilder sb = new StringBuilder();
            java.util.List<String> lines = Files.readAllLines(keywordsFile);
            for (String line : lines) {
                String trimmed = line.trim();
                // Skip empty or comment lines
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                sb.append(line).append("\n");
            }

            String content = sb.toString().trim();
            // Check if it matches default instruction (ignoring newlines diffs)

            if (content.isEmpty()) {
                keywordsArea.setForeground(Color.GRAY);
                keywordsArea.setText(DEFAULT_INSTRUCTIONS);
            } else {
                keywordsArea.setForeground(Color.BLACK);
                keywordsArea.setText(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ...

    private void saveKeywords() {
        if (keywordsFile == null)
            return;

        try (BufferedWriter writer = Files.newBufferedWriter(keywordsFile)) {
            String content = keywordsArea.getText();
            // If placeholder is active, save it back to keep file instructions valid
            // Or if empty, save instructions.
            if (content.trim().isEmpty() || content.trim().equals(DEFAULT_INSTRUCTIONS)) {
                writer.write(DEFAULT_INSTRUCTIONS);
            } else {
                writer.write(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCategories() {
        categoriesPanel.removeAll();
        categoryCheckboxes.clear();

        if (categoriesFile == null || !Files.exists(categoriesFile)) {
            categoriesPanel.add(createStyledLabel(BundleManager.getString("panel.export.label.fileNotFound")));
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(categoriesFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();

                // Skip empty lines
                if (trimmed.isEmpty())
                    continue;

                // Skip pure comment lines (e.g. headers with multiple #### or instructions)
                if (trimmed.startsWith("##") || trimmed.contains("uncomment") || trimmed.contains("automaticamente")) {
                    continue;
                }

                boolean isCommented = trimmed.startsWith("#");
                String potentialCategory = isCommented ? trimmed.substring(1).trim() : trimmed;

                // Secondary cleanup: if after removing # it's empty or still starts with #
                if (potentialCategory.isEmpty() || potentialCategory.startsWith("#")) {
                    continue;
                }

                // Heuristic: IPED categories are usually CamelCase or single words.
                // Sentences are comments.
                if (potentialCategory.contains(" ") && potentialCategory.length() > 20) {
                    continue;
                }

                JCheckBox chk = createStyledCheckBox(potentialCategory);
                chk.setSelected(!isCommented);
                chk.setBackground(Color.WHITE);
                categoryCheckboxes.put(potentialCategory, chk);
                categoriesPanel.add(chk);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveCategories() {
        if (categoriesFile == null)
            return;

        try (BufferedWriter writer = Files.newBufferedWriter(categoriesFile)) {
            writer.write("# Categorie da esportare automaticamente\n");
            writer.write("# Rimuovi # per abilitare una categoria\n\n");

            for (Map.Entry<String, JCheckBox> entry : categoryCheckboxes.entrySet()) {
                String category = entry.getKey();
                boolean enabled = entry.getValue().isSelected();

                writer.write(enabled ? category : "#" + category);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- Validation Helpers ---

    public boolean hasSelectedCategories() {
        for (JCheckBox chk : categoryCheckboxes.values()) {
            if (chk.isSelected())
                return true;
        }
        return false;
    }

    public void selectDefaultCategory() {
        // Try to select "Documenti" or similar if exists
        JCheckBox defaultChk = categoryCheckboxes.get("Documenti");
        if (defaultChk == null && !categoryCheckboxes.isEmpty()) {
            // Fallback to first available
            defaultChk = categoryCheckboxes.values().iterator().next();
        }

        if (defaultChk != null) {
            defaultChk.setSelected(true);
            saveCategories(); // Save immediately to persist fix
        }
    }
}
