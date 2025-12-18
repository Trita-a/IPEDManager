package it.ipedmanager.ui.config.panels;

import it.ipedmanager.config.ConfigManager;
import it.ipedmanager.config.ConfigManager;
import it.ipedmanager.config.PropertiesConfigFile;
import it.ipedmanager.utils.BundleManager;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicSpinnerUI;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.io.File;

/**
 * Base abstract panel for configuration pages.
 * Phase 13: Final Polish - Modern Sections, Responsive Callouts, Safe Icons.
 */
public abstract class BaseConfigPanel extends JPanel {

    // --- Color Palette (Modern / Filled) ---
    protected static final Color INPUT_BG_IDLE = new Color(241, 245, 249);
    protected static final Color INPUT_BG_HOVER = new Color(226, 232, 240);
    protected static final Color INPUT_BG_FOCUS = new Color(255, 255, 255);

    protected static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    protected static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    protected static final Color ACCENT_COLOR = new Color(30, 58, 138); // Dark Blue (Navy)

    // Semantic Colors for Callouts
    protected static final Color INFO_BG = new Color(239, 246, 255);
    protected static final Color INFO_BORDER = new Color(30, 58, 138); // Navy Blue

    protected static final Color BG_COLOR = new Color(255, 255, 255);
    protected static final Color BORDER_COLOR = new Color(226, 232, 240);

    protected static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 12);
    protected static final Font INPUT_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    protected static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 13);
    // Compatibility alias for subclasses that use TITLE_FONT
    protected static final Font TITLE_FONT = HEADER_FONT;

    protected ConfigManager configManager;

    public BaseConfigPanel() {
        this.configManager = ConfigManager.getInstance();
        setBackground(BG_COLOR);
    }

    public abstract void loadConfig();

    public abstract void saveConfig();

    // --- Helpers ---
    protected String getOr(String value, String defaultValue) {
        return value != null && !value.isEmpty() ? value : defaultValue;
    }

    protected void browseFile(JTextField target, boolean directoriesOnly) {
        JFileChooser chooser = new JFileChooser();
        String currentPath = target.getText().trim();
        if (!currentPath.isEmpty()) {
            chooser.setCurrentDirectory(new File(currentPath));
        }
        chooser.setFileSelectionMode(directoriesOnly ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            target.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    protected PropertiesConfigFile getConfig(String name) {
        return configManager.getConfigFile(name);
    }

    /**
     * Recursively filters the panel's components based on the query.
     * Hides components that do not match and shows those that do.
     * If query is empty, shows all.
     *
     * @param query The search query (will be converted to lower case).
     * @return true if at least one component is visible (match found), false
     *         otherwise.
     */
    public boolean performFilter(String query) {
        if (query == null || query.isEmpty()) {
            setAllVisible(this, true);
            return true;
        }
        String q = query.toLowerCase();
        return filterComponent(this, q);
    }

    private void setAllVisible(Component c, boolean visible) {
        c.setVisible(visible);
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                setAllVisible(child, visible);
            }
        }
    }

    private boolean filterComponent(Component c, String query) {
        // If component is the panel itself, don't hide it here (handled by parent or
        // return value)
        // But we need to traverse it.

        boolean selfMatch = checkComponent(c, query);
        boolean childMatch = false;

        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                boolean childVisible = filterComponent(child, query);
                if (childVisible) {
                    childMatch = true;
                }
            }
        }

        // Logic: Visible if self matches OR has visible children
        boolean visible = selfMatch || childMatch;
        c.setVisible(visible);
        return visible;
    }

    private boolean checkComponent(Component c, String query) {
        String text = null;
        String tooltip = null;

        if (c instanceof JLabel) {
            text = ((JLabel) c).getText();
            tooltip = ((JLabel) c).getToolTipText();
        } else if (c instanceof AbstractButton) {
            text = ((AbstractButton) c).getText();
            tooltip = ((AbstractButton) c).getToolTipText();
        } else if (c instanceof JComponent) {
            tooltip = ((JComponent) c).getToolTipText();
        }

        if (text != null && text.toLowerCase().contains(query))
            return true;
        if (tooltip != null && tooltip.toLowerCase().contains(query))
            return true;

        return false;
    }

    // --- Corrected Component Factories ---

    protected JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    protected JCheckBox createStyledCheckBox(String text) {
        JCheckBox chk = new JCheckBox(text);
        chk.setFont(INPUT_FONT);
        chk.setForeground(TEXT_PRIMARY);
        chk.setBackground(BG_COLOR);
        chk.setFocusPainted(false);
        return chk;
    }

    protected JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(INPUT_FONT);
        return tf;
    }

    /**
     * Creates a text field with placeholder hint text.
     * The placeholder appears in gray and disappears when the field gains focus.
     */
    protected JTextField createPlaceholderTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(INPUT_FONT);
        tf.setForeground(new Color(160, 160, 160));
        tf.setText(placeholder);

        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(TEXT_PRIMARY);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (tf.getText().isEmpty()) {
                    tf.setForeground(new Color(160, 160, 160));
                    tf.setText(placeholder);
                }
            }
        });

        return tf;
    }

    protected JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        return btn;
    }

    protected JSpinner createStyledSpinner() {
        JSpinner spinner = new JSpinner();
        spinner.setFont(INPUT_FONT);
        return spinner;
    }

    protected <T> JComboBox<T> createStyledComboBox(T[] items) {
        JComboBox<T> cmb = new JComboBox<>(items);
        cmb.setFont(INPUT_FONT);
        return cmb;
    }

    // --- Info Area System ---
    protected JTextArea infoArea;

    // Field for the wrapper panel to update its size
    protected JPanel infoWrapper;

    protected JPanel createInfoPanel(java.util.Collection<String> helpTexts) {
        infoWrapper = new JPanel(new BorderLayout());
        infoWrapper.setBackground(INFO_BG);
        // Modern Callout: Left Accent Border instead of Top
        infoWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, INFO_BORDER), // Left Accent
                BorderFactory.createEmptyBorder(12, 16, 12, 12))); // Padding

        // Icona Info (Top-aligned)
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        iconPanel.setOpaque(false);
        iconPanel.setBorder(new EmptyBorder(2, 0, 0, 12)); // Spacing between icon and text
        iconPanel.add(new JLabel(new it.ipedmanager.ui.config.VectorIcon("info", 20, ACCENT_COLOR)));
        infoWrapper.add(iconPanel, BorderLayout.WEST);

        // Content Panel (Title + Text)
        JPanel contentInfo = new JPanel(new BorderLayout(0, 4));
        contentInfo.setOpaque(false);

        JLabel lblTitle = new JLabel(BundleManager.getString("panel.base.info.title"));
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 10)); // Small Caps style
        lblTitle.setForeground(ACCENT_COLOR);
        contentInfo.add(lblTitle, BorderLayout.NORTH);

        infoArea = new JTextArea(BundleManager.getString("panel.base.info.default"));
        infoArea.setOpaque(false);
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoArea.setForeground(new Color(51, 65, 85)); // Slate-700
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);

        contentInfo.add(infoArea, BorderLayout.CENTER);

        infoWrapper.add(contentInfo, BorderLayout.CENTER);

        // Initial sizing for default text
        updateInfoPanelHeight(BundleManager.getString("panel.base.info.default"));

        return infoWrapper;
    }

    // Deprecated override for compatibility
    protected JPanel createInfoPanel() {
        return createInfoPanel(null);
    }

    private void updateInfoPanelHeight(String text) {
        if (infoWrapper == null || infoArea == null)
            return;

        // Calculate required height based on content
        FontMetrics fm = infoArea.getFontMetrics(infoArea.getFont());
        // Fallback if not displayable yet
        if (fm == null) {
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(1, 1,
                    java.awt.image.BufferedImage.TYPE_INT_ARGB);
            fm = img.createGraphics().getFontMetrics(infoArea.getFont());
        }

        int availableWidth = 600; // Estimate
        if (infoWrapper.getWidth() > 0) {
            // Precise width: Panel Width - Padding (16+12) - Icon (approx 40)
            availableWidth = infoWrapper.getWidth() - 28 - 40;
            if (availableWidth < 100)
                availableWidth = 100; // Safety
        }

        int lineHeight = fm.getHeight();
        int textWidth = fm.stringWidth(text);
        int lines = (int) Math.ceil((double) textWidth / availableWidth);
        if (lines < 1)
            lines = 1;

        // Header (30) + Padding (24) + Text
        int newHeight = 30 + 24 + (lines * lineHeight);
        // Cap minimal height to avoid flickering for 1 line
        newHeight = Math.max(85, newHeight);

        infoWrapper.setPreferredSize(new Dimension(0, newHeight));
        infoWrapper.revalidate();

        // If part of a window, validate the root
        Container parent = infoWrapper.getParent();
        while (parent != null) {
            if (parent instanceof JComponent) {
                ((JComponent) parent).revalidate();
            }
            parent = parent.getParent();
        }
    }

    protected void addHoverHelp(JComponent comp, String helpText) {
        comp.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (infoArea != null) {
                    infoArea.setText(helpText);
                    updateInfoPanelHeight(helpText); // Dynamic Resize
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                // Optional: Reset?
            }
        });
    }

    // Deprecated static version kept for compatibility if needed, but better to
    // replace usage
    protected JComponent createStyledInfoArea(String text) {
        JPanel p = createInfoPanel();
        if (infoArea != null) {
            infoArea.setText(text);
            updateInfoPanelHeight(text);
        }
        return p;
    }

    // --- Layout Helpers (MODERN SECTIONS) ---

    protected JPanel createSectionPanel(String title) {
        // Modern Section: No TitledBorder. Label + Line separator.
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);

        panel.setBorder(new AbstractBorder() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw Title
                g2.setFont(HEADER_FONT);
                g2.setColor(ACCENT_COLOR);
                g2.drawString(title.toUpperCase(), x + 5, y + 20);

                // Draw Separator Line
                g2.setColor(BORDER_COLOR);
                int textWidth = g2.getFontMetrics().stringWidth(title.toUpperCase());
                // Line after text
                g2.drawLine(x + 5 + textWidth + 10, y + 15, x + width - 10, y + 15);

                g2.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(30, 0, 10, 0); // Reserve space for header
            }
        });

        return panel;
    }

    protected GridBagConstraints createGbc(int x, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    protected void addField(JPanel panel, String labelText, JComponent comp, int row) {
        GridBagConstraints gbc = createGbc(0, row);
        gbc.weightx = 0.0;
        panel.add(createStyledLabel(labelText), gbc);

        gbc = createGbc(1, row);
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;
        panel.add(comp, gbc);
    }

    protected void addFileField(JPanel panel, String labelText, JTextField tf, boolean dirOnly, int row) {
        GridBagConstraints gbc = createGbc(0, row);
        gbc.weightx = 0.0;
        panel.add(createStyledLabel(labelText), gbc);

        gbc = createGbc(1, row);
        gbc.weightx = 1.0;
        panel.add(tf, gbc);

        gbc = createGbc(2, row);
        gbc.weightx = 0.0;
        JButton btn = createStyledButton("...");
        btn.addActionListener(e -> browseFile(tf, dirOnly));
        panel.add(btn, gbc);
    }

    protected void addFullWidthComponent(JPanel panel, JComponent comp, int row) {
        GridBagConstraints gbc = createGbc(0, row);
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        panel.add(comp, gbc);
    }

    protected void addCompactRow(JPanel panel, int row, Component... components) {
        int col = 0;
        for (Component comp : components) {
            GridBagConstraints gbc = createGbc(col, row);
            if (comp instanceof JLabel) {
                gbc.weightx = 0.0;
                gbc.fill = GridBagConstraints.NONE;
            } else {
                gbc.weightx = 1.0;
                gbc.fill = GridBagConstraints.HORIZONTAL;
            }
            panel.add(comp, gbc);
            col++;
        }
    }

    protected void addGridCheckboxes(JPanel panel, int row, int cols, JCheckBox... checks) {
        JPanel grid = new JPanel(new GridLayout(0, cols, 5, 0));
        grid.setBackground(BG_COLOR);
        for (JCheckBox chk : checks) {
            grid.add(chk);
        }
        GridBagConstraints gbc = createGbc(0, row);
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        panel.add(grid, gbc);
    }
}
