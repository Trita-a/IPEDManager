package it.ipedmanager.ui.config;

import it.ipedmanager.config.ConfigManager;
import it.ipedmanager.config.PropertiesConfigFile;
import it.ipedmanager.ui.components.OverlayPanel;
import it.ipedmanager.ui.components.ToggleSwitch;
import it.ipedmanager.ui.config.panels.*;

import javax.swing.*;
import it.ipedmanager.utils.BundleManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dialog PROFESSIONALE per la gestione delle configurazioni IPED.
 * Design chiaro, moderno e intuitivo.
 * 
 * @author William Tritapepe
 */
public class ConfigManagerDialog extends JDialog {

    private ConfigManager configManager;
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JButton selectedButton;

    // Colori tema CHIARO
    private static final Color SIDEBAR_BG = new Color(241, 245, 249); // Slate 100 (Slightly darker than content)
    private static final Color SIDEBAR_BORDER = new Color(226, 232, 240);
    private static final Color SIDEBAR_HOVER = new Color(226, 232, 240); // Slate 200 (Visible hover)
    private static final Color SIDEBAR_SELECTED = new Color(30, 58, 138); // Dark Blue (Navy)
    private static final Color CONTENT_BG = new Color(255, 255, 255);
    private static final Color HEADER_BG = new Color(30, 58, 138); // Navy Blue (Match MainFrame)
    private static final Color ACCENT = new Color(30, 58, 138); // Dark Blue (Navy)
    private static final Color TEXT_PRIMARY = new Color(30, 41, 59);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);

    // Pannelli
    private LocalConfigPanel localConfigPanel;
    private IPEDConfigPanel ipedConfigPanel;
    private HTMLReportConfigPanel htmlReportPanel;
    private OCRConfigPanel ocrConfigPanel;
    private AudioConfigPanel audioConfigPanel;
    private MediaConfigPanel mediaConfigPanel; // Unisce Video + Image
    private FaceRecognitionConfigPanel faceConfigPanel;
    private PhotoDNAConfigPanel photoDNAConfigPanel;
    private ElasticConfigPanel elasticConfigPanel;
    private MinIOConfigPanel minIOConfigPanel;
    private ExportConfigPanel exportConfigPanel;

    // Menu items - Simplified Icons (Letters/Symbols that are safe)
    // Structure: { ID, ICON, TITLE, SUBTITLE, CATEGORY, CONFIG_KEY (optional) }
    private static final String[][] MENU_ITEMS = {
            { "ambiente", "home", "sidebar.ambiente", "sidebar.subtitle.ambiente", "sidebar.category.principale",
                    null },
            { "elaborazione", "gears", "sidebar.elaborazione", "sidebar.subtitle.elaborazione",
                    "sidebar.category.principale", null },
            { "report", "report", "sidebar.report", "sidebar.subtitle.report", "sidebar.category.principale",
                    "enableHTMLReport" },
            { "---", "", "", "", "", null },
            { "ocr", "ocr", "sidebar.ocr", "sidebar.subtitle.ocr", "sidebar.category.analisi", "enableOCR" },
            { "audio", "audio", "sidebar.audio", "sidebar.subtitle.audio", "sidebar.category.analisi",
                    "enableAudioTranscription" },
            { "media", "media", "sidebar.media", "sidebar.subtitle.media", "sidebar.category.analisi",
                    "virtual:media" },
            { "---", "", "", "", "", null },
            { "volti", "face", "sidebar.volti", "sidebar.subtitle.volti", "sidebar.category.avanzato",
                    "enableFaceRecognition" },
            { "photodna", "photodna", "sidebar.photodna", "sidebar.subtitle.photodna", "sidebar.category.avanzato",
                    "enablePhotoDNA" },
            { "elastic", "elastic", "sidebar.elastic", "sidebar.subtitle.elastic", "sidebar.category.avanzato",
                    "enableIndexToElasticSearch" },
            { "minio", "minio", "sidebar.minio", "sidebar.subtitle.minio", "sidebar.category.avanzato", "enableMinIO" },
            { "export", "export", "sidebar.export", "sidebar.subtitle.export", "sidebar.category.avanzato",
                    "enableAutomaticExportFiles" }
    };

    // Map to hold overlay panels for enabling/disabling
    private Map<String, OverlayPanel> panelOverlays = new HashMap<>();
    // Map to hold panel instances for search
    private Map<String, BaseConfigPanel> panelsMap = new HashMap<>();

    public ConfigManagerDialog(Frame parent) {
        super(parent, "", true); // Empty title to avoid center text
        this.configManager = ConfigManager.getInstance();

        initItems();
        initComponents();
        loadAllConfigs();

        setSize(1050, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(CONTENT_BG);

        // FlatLaf Title Bar Integration
        getRootPane().putClientProperty("JRootPane.titleBarBackground", HEADER_BG);
        getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);
        getRootPane().putClientProperty("JRootPane.titleBarButtons", "close"); // Keep only close
        getRootPane().putClientProperty("JRootPane.titleBarShowTitle", false); // Hide center title
        // FlatLaf embeds this at the left of the title bar
        getRootPane().putClientProperty("JRootPane.menuBarEmbedded", true);

        // Use JMenuBar as the embedded component (FlatLaf integrates it into title bar)
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(HEADER_BG);
        menuBar.setOpaque(true);
        menuBar.setBorder(BorderFactory.createEmptyBorder(6, 5, 2, 0)); // More top padding to push down

        // Add only logo + title (clean layout)
        menuBar.add(createTitleBarContent());

        setJMenuBar(menuBar);

        // Removed: add(createHeader(), BorderLayout.NORTH);

        // Main = Sidebar + Content
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(CONTENT_BG);

        // Sidebar con scroll
        mainPanel.add(createSidebar(), BorderLayout.WEST);

        // Content
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(CONTENT_BG);
        // Reduced outer margin for density (was 20)
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        addPanels();

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // Footer
        add(createFooter(), BorderLayout.SOUTH);

        selectMenuItem("ambiente");
    }

    // Returns a compact panel with logo and title for embedded title bar
    private JPanel createTitleBarContent() {
        JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        content.setOpaque(false);

        // Title
        JLabel title = new JLabel(BundleManager.getString("header.title"));
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(Color.WHITE);
        content.add(title);

        return content;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setPreferredSize(new Dimension(0, 60)); // Reduced height (was 65)
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 20, 10, 20))); // Reduced padding (was 12)

        // Left: Logo + Title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftPanel.setOpaque(false);

        // Logo circle with VectorIcon (properly centered)
        JPanel logoContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillOval(0, 0, 40, 40);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoContainer.setOpaque(false);
        logoContainer.setPreferredSize(new Dimension(40, 40));
        logoContainer.setLayout(new GridBagLayout()); // True center
        JLabel logoIcon = new JLabel(new VectorIcon("gears", 20, ACCENT));
        logoContainer.add(logoIcon);
        leftPanel.add(logoContainer);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel(BundleManager.getString("header.title"));
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Indexador e Processador de Evidencias Digitais");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(200, 200, 200)); // Light Gray on Navy

        titlePanel.add(title);
        titlePanel.add(subtitle);
        leftPanel.add(titlePanel);

        header.add(leftPanel, BorderLayout.WEST);

        // Right: Stats
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
        rightPanel.setOpaque(false);

        rightPanel.add(createStatBadge("13", BundleManager.getString("header.stats.files")));
        // Removed "100+" badge as per user request

        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createStatBadge(String number, String label) {
        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Semi-transparent white on Navy for subtle contrast
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(255, 255, 255, 80)); // Light border
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        badge.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(70, 35));

        JLabel numLabel = new JLabel(number);
        numLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        numLabel.setForeground(Color.WHITE); // White number on Navy

        JLabel textLabel = new JLabel(label);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        textLabel.setForeground(new Color(200, 200, 200)); // Light gray text

        badge.add(numLabel);
        badge.add(textLabel);

        return badge;
    }

    // Sidebar structure
    private static class SidebarItem {
        String id;
        String icon;
        String title;
        String subtitle;
        String category;
        String configKey; // Key in IPEDConfig.txt associated with this panel

        SidebarItem(String id, String icon, String title, String subtitle, String category, String configKey) {
            this.id = id;
            this.icon = icon;
            this.title = title;
            this.subtitle = subtitle;
            this.category = category;
            this.configKey = configKey;
        }
    }

    private List<SidebarItem> allItems = new ArrayList<>();
    private JTextField searchField;
    private JPanel itemsContainer;

    private void initItems() {
        for (String[] item : MENU_ITEMS) {
            if (!item[0].equals("---")) {
                allItems.add(new SidebarItem(item[0], item[1], item[2], item[3], item[4], item[5]));
            }
        }
    }

    private JPanel createSidebar() {
        JPanel sidebarWrapper = new JPanel(new BorderLayout());
        sidebarWrapper.setBackground(SIDEBAR_BG);
        sidebarWrapper.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        // Search Bar
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(SIDEBAR_BG);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", BundleManager.getString("sidebar.search"));
        searchField.setPreferredSize(new Dimension(0, 30));

        // Listener
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterItems();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterItems();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterItems();
            }
        });

        searchPanel.add(searchField, BorderLayout.CENTER);
        sidebarWrapper.add(searchPanel, BorderLayout.NORTH);

        // Items Container
        itemsContainer = new JPanel();
        itemsContainer.setLayout(new BoxLayout(itemsContainer, BoxLayout.Y_AXIS));
        itemsContainer.setBackground(SIDEBAR_BG);
        itemsContainer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Popola
        renderSidebarItems(allItems);

        JScrollPane scrollPane = new JScrollPane(itemsContainer);
        // scrollPane.setPreferredSize(new Dimension(240, 0)); // Remove fixed width to
        // allow Layout to manage
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        sidebarWrapper.add(scrollPane, BorderLayout.CENTER);
        sidebarWrapper.setPreferredSize(new Dimension(280, 0)); // Increased width for switches

        sidebarPanel = itemsContainer;

        return sidebarWrapper;
    }

    private void filterItems() {
        String text = searchField.getText().toLowerCase().trim();
        List<SidebarItem> filtered = new ArrayList<>();

        for (SidebarItem item : allItems) {
            boolean metadataMatch = BundleManager.getString(item.title).toLowerCase().contains(text) ||
                    BundleManager.getString(item.subtitle).toLowerCase().contains(text) ||
                    BundleManager.getString(item.category).toLowerCase().contains(text);

            boolean contentMatch = false;
            // Always filter content so that if user opens the panel, it is filtered
            BaseConfigPanel panel = panelsMap.get(item.id);
            if (panel != null) {
                contentMatch = panel.performFilter(text);
            }

            // Show sidebar item if metadata matches OR content matches
            if (metadataMatch || contentMatch) {
                filtered.add(item);
            }
        }
        renderSidebarItems(filtered);
    }

    private void renderSidebarItems(List<SidebarItem> items) {
        itemsContainer.removeAll();
        String currentCategory = "";

        for (SidebarItem item : items) {
            if (!item.category.equals(currentCategory)) {
                currentCategory = item.category;
                itemsContainer.add(Box.createVerticalStrut(5));
                JLabel catLabel = new JLabel(BundleManager.getString(currentCategory));
                catLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
                catLabel.setForeground(TEXT_SECONDARY);
                catLabel.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 0));
                catLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                itemsContainer.add(catLabel);
                itemsContainer.add(Box.createVerticalStrut(2));
            }

            JButton btn = createSidebarItem(item);
            itemsContainer.add(btn);
            itemsContainer.add(Box.createVerticalStrut(1));
        }

        itemsContainer.revalidate();
        itemsContainer.repaint();
    }

    // Helper to update visual state of a button (colors)
    private void updateButtonState(JButton btn, boolean selected) {
        String id = (String) btn.getClientProperty("actionId");
        SidebarItem item = allItems.stream().filter(i -> i.id.equals(id)).findFirst().orElse(null);

        boolean enabled = true;
        if (item != null && item.configKey != null) {
            if ("virtual:media".equals(item.configKey)) {
                boolean img = configManager.getIPEDConfig().getBoolean("enableImageThumbs", false);
                boolean vid = configManager.getIPEDConfig().getBoolean("enableVideoThumbs", false);
                enabled = img || vid;
            } else {
                enabled = configManager.getIPEDConfig().getBoolean(item.configKey, false);
            }
        }

        Color iconColor;
        Color textColor;
        Color switchOnColor;
        Color switchOffColor;
        Color knobColor;

        if (selected) {
            iconColor = Color.WHITE;
            textColor = Color.WHITE;
            // High contrast for switch on blue background
            switchOnColor = Color.WHITE;
            switchOffColor = new Color(255, 255, 255, 100);
            knobColor = ACCENT; // Blue knob
        } else {
            iconColor = enabled ? ACCENT : TEXT_SECONDARY;
            textColor = enabled ? TEXT_PRIMARY : TEXT_SECONDARY;
            // Standard colors
            switchOnColor = ACCENT;
            switchOffColor = new Color(203, 213, 225);
            knobColor = Color.WHITE;
        }

        for (Component c : btn.getComponents()) {
            if (c instanceof JPanel) {
                // Check inner containers
                for (Component inner : ((JPanel) c).getComponents()) {
                    if (inner instanceof JLabel) {
                        JLabel lbl = (JLabel) inner;
                        if (lbl.getIcon() instanceof VectorIcon) {
                            ((VectorIcon) lbl.getIcon()).setColor(iconColor);
                            lbl.repaint();
                        } else {
                            lbl.setForeground(textColor);
                        }
                    } else if (inner instanceof ToggleSwitch) {
                        ((ToggleSwitch) inner).setColors(switchOnColor, switchOffColor, knobColor);
                    } else if (inner instanceof JPanel) { // For the texts panel
                        for (Component textComp : ((JPanel) inner).getComponents()) {
                            if (textComp instanceof JLabel) {
                                ((JLabel) textComp).setForeground(textColor);
                            }
                        }
                    }
                }
            }
        }
        btn.repaint();
    }

    private JButton createSidebarItem(SidebarItem item) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                boolean selected = (this == selectedButton);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Sfondo
                if (selected) {
                    g2.setColor(SIDEBAR_SELECTED);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } else if (getModel().isRollover()) {
                    g2.setColor(SIDEBAR_HOVER);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setLayout(new BorderLayout(10, 0));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); // Slightly taller for switches
        btn.setPreferredSize(new Dimension(200, 40));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 8)); // Adjusted padding

        // Left Container (Icon + Title) - Use GridBagLayout for true vertical centering
        JPanel leftContainer = new JPanel(new GridBagLayout());
        leftContainer.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 10); // Gap after icon

        JLabel iconLabel = new JLabel(new VectorIcon(item.icon, 20, ACCENT));
        leftContainer.add(iconLabel, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        // Texts
        JPanel texts = new JPanel(new GridLayout(2, 1));
        texts.setOpaque(false);
        JLabel titleLabel = new JLabel(BundleManager.getString(item.title));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subLabel = new JLabel(BundleManager.getString(item.subtitle));
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10)); // Smaller font for subtitle
        subLabel.setForeground(TEXT_SECONDARY);

        texts.add(titleLabel);
        texts.add(subLabel);
        leftContainer.add(texts, gbc);

        btn.add(leftContainer, BorderLayout.WEST);

        // Right Container (Switch if applicable)
        if (item.configKey != null) {
            ToggleSwitch toggle = new ToggleSwitch();
            // Load initial state
            boolean enabled;
            if ("virtual:media".equals(item.configKey)) {
                boolean img = configManager.getIPEDConfig().getBoolean("enableImageThumbs", false);
                boolean vid = configManager.getIPEDConfig().getBoolean("enableVideoThumbs", false);
                enabled = img || vid; // ON if at least one is enabled
            } else {
                enabled = configManager.getIPEDConfig().getBoolean(item.configKey, false);
            }
            toggle.setSelected(enabled);

            toggle.addActionListener(selected -> {
                if ("virtual:media".equals(item.configKey)) {
                    // Update both flags
                    configManager.getIPEDConfig().setBoolean("enableImageThumbs", selected);
                    configManager.getIPEDConfig().setBoolean("enableVideoThumbs", selected);
                    // Also refresh panel UI if instance exists
                    if (mediaConfigPanel != null) {
                        mediaConfigPanel.loadConfig(); // Reloads these new values into UI
                    }
                } else {
                    configManager.getIPEDConfig().setBoolean(item.configKey, selected);
                }

                // VALIDATION: Export must have at least one category
                if (selected && "enableAutomaticExportFiles".equals(item.configKey)) {
                    if (exportConfigPanel != null && !exportConfigPanel.hasSelectedCategories()) {
                        JOptionPane.showMessageDialog(ConfigManagerDialog.this,
                                "Attenzione: Hai attivato l'Export ma non hai selezionato nessuna categoria!\n" +
                                        "Verrà selezionata automaticamente 'Documenti' per evitare un export vuoto.",
                                "Export Vuoto", JOptionPane.WARNING_MESSAGE);
                        exportConfigPanel.selectDefaultCategory();
                    }
                }

                // Enable/Disable panel usage via Overlay
                OverlayPanel overlay = panelOverlays.get(item.id);
                if (overlay != null) {
                    overlay.setContentEnabled(selected);
                }
                // Update text/icon color immediately
                updateButtonState(btn, btn == selectedButton);
            });

            // Prevent button click when clicking switch
            // Wrap in panel to align right
            JPanel switchContainer = new JPanel(new GridBagLayout()); // Vertically centered
            switchContainer.setOpaque(false);
            switchContainer.add(toggle);

            btn.add(switchContainer, BorderLayout.EAST);
        }

        btn.addActionListener(e -> selectMenuItem(item.id));
        btn.putClientProperty("actionId", item.id);

        return btn;
    }

    private void selectMenuItem(String id) {
        for (Component c : sidebarPanel.getComponents()) {
            if (c instanceof JButton) {
                JButton btn = (JButton) c;
                boolean isTarget = id.equals(btn.getClientProperty("actionId"));
                if (isTarget) {
                    selectedButton = btn;
                }
                updateButtonState(btn, isTarget);
            }
        }
        sidebarPanel.repaint();
        cardLayout.show(contentPanel, id);

        if ("elaborazione".equals(id) && ipedConfigPanel != null) {
            saveToMemory();
            ipedConfigPanel.refreshDependencies();
        }

        // Refresh Media panel when selecting it so it syncs with switch state
        if ("media".equals(id) && mediaConfigPanel != null) {
            mediaConfigPanel.loadConfig(); // Refresh checkboxes
        }
    }

    private void saveToMemory() {
        try {
            if (exportConfigPanel != null)
                exportConfigPanel.saveConfig();
            if (localConfigPanel != null)
                localConfigPanel.saveConfig();
            if (elasticConfigPanel != null)
                elasticConfigPanel.saveConfig();
            if (minIOConfigPanel != null)
                minIOConfigPanel.saveConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void restoreDefaults() {
        // Path to clean default installation (packaged with app)
        // This 'defaults' folder is deployed by the build script
        java.nio.file.Path defaultRoot = configManager.getIpedPath().resolve("defaults");
        java.nio.file.Path defaultConf = defaultRoot.resolve("conf");

        java.nio.file.Path targetRoot = configManager.getIpedPath(); // Active root
        java.nio.file.Path targetConf = configManager.getConfPath(); // Active conf

        if (targetRoot == null || targetConf == null) {
            JOptionPane.showMessageDialog(this, BundleManager.getString("dialog.error.configPathNotFound"), "Errore",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!java.nio.file.Files.exists(defaultRoot)) {
            JOptionPane.showMessageDialog(this,
                    BundleManager.getString("dialog.warning.defaultFolderNotFound") + "\n" +
                            BundleManager.getString("dialog.warning.checkInstallation"),
                    BundleManager.getString("dialog.warning.configMissing"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(this,
                BundleManager.getString("dialog.confirm.restoreDefaults") + "\n" +
                        BundleManager.getString("dialog.confirm.configsIn") + ": " + defaultRoot.getFileName() + "\n" +
                        BundleManager.getString("dialog.confirm.operationCannotBeUndone"),
                BundleManager.getString("dialog.confirm.restoreTitle"), JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            try {
                // 1. Restore Root Configs (LocalConfig.txt, IPEDConfig.txt)
                String[] rootFiles = { "LocalConfig.txt", "IPEDConfig.txt" };
                for (String fileName : rootFiles) {
                    java.nio.file.Path src = defaultRoot.resolve(fileName);
                    java.nio.file.Path dst = targetRoot.resolve(fileName);
                    if (java.nio.file.Files.exists(src)) {
                        java.nio.file.Files.copy(src, dst, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                }

                // 2. Restore 'conf' directory recursively
                if (java.nio.file.Files.exists(defaultConf)) {
                    java.nio.file.Files.walk(defaultConf).forEach(source -> {
                        try {
                            java.nio.file.Path destination = targetConf.resolve(defaultConf.relativize(source));
                            if (java.nio.file.Files.isDirectory(source)) {
                                if (!java.nio.file.Files.exists(destination))
                                    java.nio.file.Files.createDirectory(destination);
                            } else {
                                java.nio.file.Files.copy(source, destination,
                                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

                // Force reload of everything
                configManager.reload();
                loadAllConfigs();

                // Close the dialog as requested
                dispose();

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        BundleManager.getString("dialog.error.restoreFailed") + ": " + e.getMessage(),
                        BundleManager.getString("dialog.error.title"),
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addPanels() {
        localConfigPanel = new LocalConfigPanel();
        contentPanel.add(wrapContent(BundleManager.getString("panel.local.title"),
                BundleManager.getString("panel.local.description"),
                localConfigPanel), "ambiente");

        ipedConfigPanel = new IPEDConfigPanel();
        contentPanel.add(
                wrapContent(BundleManager.getString("panel.iped.title"),
                        BundleManager.getString("panel.iped.description"), ipedConfigPanel),
                "elaborazione");

        // --- Wrapped Panels below ---

        htmlReportPanel = new HTMLReportConfigPanel();
        contentPanel.add(
                createWrappedPanel(BundleManager.getString("panel.htmlReport.title"),
                        BundleManager.getString("panel.htmlReport.description"), htmlReportPanel,
                        "enableHTMLReport", "report"),
                "report");

        ocrConfigPanel = new OCRConfigPanel();
        contentPanel.add(
                createWrappedPanel(BundleManager.getString("panel.ocr.title"),
                        BundleManager.getString("panel.ocr.description"), ocrConfigPanel,
                        "enableOCR", "ocr"),
                "ocr");

        audioConfigPanel = new AudioConfigPanel();
        contentPanel.add(
                createWrappedPanel(BundleManager.getString("panel.audio.title"),
                        BundleManager.getString("panel.audio.description"), audioConfigPanel,
                        "enableAudioTranscription", "audio"),
                "audio");

        // Media uses special key
        mediaConfigPanel = new MediaConfigPanel();
        contentPanel.add(
                createWrappedPanel(BundleManager.getString("panel.media.title"),
                        BundleManager.getString("panel.media.description"), mediaConfigPanel,
                        "virtual:media", "media"),
                "media");

        faceConfigPanel = new FaceRecognitionConfigPanel();
        contentPanel.add(
                createWrappedPanel(BundleManager.getString("panel.face.title"),
                        BundleManager.getString("panel.face.description"),
                        faceConfigPanel, "enableFaceRecognition", "volti"),
                "volti");

        photoDNAConfigPanel = new PhotoDNAConfigPanel();
        contentPanel.add(
                createWrappedPanel(BundleManager.getString("panel.photodna.title"),
                        BundleManager.getString("panel.photodna.description"), photoDNAConfigPanel,
                        "enablePhotoDNA", "photodna"),
                "photodna");

        elasticConfigPanel = new ElasticConfigPanel();
        contentPanel.add(
                createWrappedPanel(BundleManager.getString("panel.elastic.title"),
                        BundleManager.getString("panel.elastic.description"), elasticConfigPanel,
                        "enableIndexToElasticSearch", "elastic"),
                "elastic");

        minIOConfigPanel = new MinIOConfigPanel();
        contentPanel.add(
                createWrappedPanel(BundleManager.getString("panel.minio.title"),
                        BundleManager.getString("panel.minio.description"), minIOConfigPanel,
                        "enableMinIO", "minio"),
                "minio");

        exportConfigPanel = new ExportConfigPanel();
        contentPanel.add(
                createWrappedPanel(BundleManager.getString("panel.export.title"),
                        BundleManager.getString("panel.export.description"),
                        exportConfigPanel,
                        "enableAutomaticExportFiles", "export"),
                "export");

        // Populate search map
        panelsMap.put("ambiente", localConfigPanel);
        panelsMap.put("elaborazione", ipedConfigPanel);
        panelsMap.put("report", htmlReportPanel);
        panelsMap.put("ocr", ocrConfigPanel);
        panelsMap.put("audio", audioConfigPanel);
        panelsMap.put("media", mediaConfigPanel);
        panelsMap.put("volti", faceConfigPanel);
        panelsMap.put("photodna", photoDNAConfigPanel);
        panelsMap.put("elastic", elasticConfigPanel);
        panelsMap.put("minio", minIOConfigPanel);
        panelsMap.put("export", exportConfigPanel);
    }

    private JPanel wrapContent(String title, String description, JPanel panel) {
        // Reduced gap between header and card from 12 to 6
        JPanel wrapper = new JPanel(new BorderLayout(0, 6));
        wrapper.setBackground(CONTENT_BG);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        // Reduced bottom padding from 10 to 4
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(TEXT_SECONDARY);

        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setOpaque(false);
        headerText.add(titleLabel);
        headerText.add(Box.createVerticalStrut(2)); // Reduced strut
        headerText.add(descLabel);

        headerPanel.add(headerText, BorderLayout.CENTER);
        wrapper.add(headerPanel, BorderLayout.NORTH);

        // Content card
        JPanel contentCard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); // Reduced radius 12->8
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        contentCard.setOpaque(false);
        // Reduced inner padding from 15 to 8 for maximum density
        contentCard.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        panel.setBackground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        contentCard.add(scroll, BorderLayout.CENTER);
        wrapper.add(contentCard, BorderLayout.CENTER);

        return wrapper;
    }

    // Helper to create wrapped panel with Overlay and register it
    private JPanel createWrappedPanel(String title, String desc, JPanel content, String configKey, String id) {
        JPanel normalWrapper = wrapContent(title, desc, content);

        // Wrap the whole thing (including header? No, just the content part)
        // Let's wrap the INNER content card.

        // To do this cleanly with wrapContent reuse:
        // We need to inspect the container tree or simply reimplement wrapContent logic
        // here for control.
        // Reimplementing for clarity:

        JPanel wrapper = new JPanel(new BorderLayout(0, 6));
        wrapper.setBackground(CONTENT_BG);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(TEXT_SECONDARY);

        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setOpaque(false);
        headerText.add(titleLabel);
        headerText.add(Box.createVerticalStrut(2));
        headerText.add(descLabel);

        headerPanel.add(headerText, BorderLayout.CENTER);
        wrapper.add(headerPanel, BorderLayout.NORTH);

        // Content card
        JPanel contentCard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        contentCard.setOpaque(false);
        contentCard.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8)); // padding for inner content

        content.setBackground(Color.WHITE);

        JComponent contentToOverlay;
        if (content instanceof ExportConfigPanel) {
            // Export panel manages its own scrolling (SplitPane with internal ScrollPanes)
            // Wrapping it in another ScrollPane breaks mouse wheel scrolling
            contentToOverlay = content;
        } else {
            JScrollPane scroll = new JScrollPane(content);
            scroll.setBorder(null);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            contentToOverlay = scroll;
        }

        // Overlay around the ScrollPane (or content directly)
        OverlayPanel overlay = new OverlayPanel(contentToOverlay);
        contentCard.add(overlay, BorderLayout.CENTER);

        wrapper.add(contentCard, BorderLayout.CENTER);

        // Register for later use
        panelOverlays.put(id, overlay);

        // Initial State
        boolean enabled;
        if ("virtual:media".equals(configKey)) {
            boolean img = configManager.getIPEDConfig().getBoolean("enableImageThumbs", false);
            boolean vid = configManager.getIPEDConfig().getBoolean("enableVideoThumbs", false);
            enabled = img || vid;
        } else {
            enabled = configManager.getIPEDConfig().getBoolean(configKey, false);
        }
        overlay.setContentEnabled(enabled);

        return wrapper;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE); // White footer for contrast with Navy button
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 20, 10, 20))); // Reduced vertical padding

        JLabel infoLabel = new JLabel(BundleManager.getString("footer.info"));
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoLabel.setForeground(TEXT_SECONDARY);
        footer.add(infoLabel, BorderLayout.WEST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);

        // JButton btnReset = createButton(BundleManager.getString("footer.reload"),
        // false);
        // btnReset.addActionListener(e -> loadAllConfigs());

        JButton btnDefault = createButton(BundleManager.getString("footer.default"), false);
        btnDefault.addActionListener(e -> restoreDefaults());

        JButton btnCancel = createButton(BundleManager.getString("footer.cancel"), false);
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = createButton(BundleManager.getString("footer.save"), true);
        btnSave.addActionListener(e -> save());

        buttons.add(btnDefault);
        buttons.add(btnCancel);
        buttons.add(btnSave);
        footer.add(buttons, BorderLayout.EAST);

        return footer;
    }

    private JButton createButton(String text, boolean primary) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg = primary
                        ? (getModel().isPressed() ? ACCENT.darker()
                                : getModel().isRollover() ? new Color(40, 68, 148) : ACCENT) // Slightly lighter navy on
                                                                                             // hover
                        : (getModel().isPressed() ? new Color(226, 232, 240)
                                : getModel().isRollover() ? new Color(241, 245, 249) : Color.WHITE);

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                if (!primary) {
                    g2.setColor(BORDER_COLOR);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(primary ? Color.WHITE : TEXT_PRIMARY);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(primary ? 130 : 90, 36));

        return btn;
    }

    private void loadAllConfigs() {
        try {
            configManager.reload();

            setConfigSafe(localConfigPanel, configManager.getLocalConfig());
            setConfigSafe(ipedConfigPanel, configManager.getIPEDConfig());
            // Hash algorithms config (integrated in Elaborazione panel)
            if (ipedConfigPanel != null) {
                ipedConfigPanel.setHashConfig(configManager.getConfigFile(ConfigManager.HASH_CONFIG));
            }
            setConfigSafe(htmlReportPanel, configManager.getHTMLReportConfig());
            setConfigSafe(ocrConfigPanel, configManager.getConfigFile(ConfigManager.OCR_CONFIG));
            setConfigSafe(audioConfigPanel, configManager.getConfigFile(ConfigManager.AUDIO_CONFIG));
            // Media panel uses both image and video configs + IPED config for flags
            if (mediaConfigPanel != null) {
                mediaConfigPanel.setImageConfig(configManager.getConfigFile(ConfigManager.IMAGE_CONFIG));
                mediaConfigPanel.setVideoConfig(configManager.getConfigFile(ConfigManager.VIDEO_CONFIG));
                mediaConfigPanel.setIPEDConfig(configManager.getIPEDConfig());
            }
            setConfigSafe(faceConfigPanel, configManager.getConfigFile(ConfigManager.FACE_CONFIG));
            setConfigSafe(photoDNAConfigPanel, configManager.getConfigFile(ConfigManager.PHOTODNA_CONFIG));
            setConfigSafe(elasticConfigPanel, configManager.getConfigFile(ConfigManager.ELASTIC_CONFIG));
            setConfigSafe(minIOConfigPanel, configManager.getConfigFile(ConfigManager.MINIO_CONFIG));
            exportConfigPanel.setConfigManager(configManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setConfigSafe(Object panel, PropertiesConfigFile config) {
        if (config == null)
            return;
        try {
            if (panel instanceof LocalConfigPanel)
                ((LocalConfigPanel) panel).setConfig(config);
            else if (panel instanceof IPEDConfigPanel)
                ((IPEDConfigPanel) panel).setConfig(config);
            else if (panel instanceof HTMLReportConfigPanel)
                ((HTMLReportConfigPanel) panel).setConfig(config);
            else if (panel instanceof OCRConfigPanel)
                ((OCRConfigPanel) panel).setConfig(config);
            else if (panel instanceof AudioConfigPanel)
                ((AudioConfigPanel) panel).setConfig(config);
            else if (panel instanceof FaceRecognitionConfigPanel)
                ((FaceRecognitionConfigPanel) panel).setConfig(config);
            else if (panel instanceof PhotoDNAConfigPanel)
                ((PhotoDNAConfigPanel) panel).setConfig(config);
            else if (panel instanceof ElasticConfigPanel)
                ((ElasticConfigPanel) panel).setConfig(config);
            else if (panel instanceof MinIOConfigPanel)
                ((MinIOConfigPanel) panel).setConfig(config);
        } catch (Exception e) {
            System.err.println("Config error: " + e.getMessage());
        }
    }

    private void save() {
        try {
            localConfigPanel.saveConfig();
            ipedConfigPanel.saveConfig();
            htmlReportPanel.saveConfig();
            ocrConfigPanel.saveConfig();
            audioConfigPanel.saveConfig();
            mediaConfigPanel.saveConfig();
            faceConfigPanel.saveConfig();
            photoDNAConfigPanel.saveConfig();
            elasticConfigPanel.saveConfig();
            minIOConfigPanel.saveConfig();
            exportConfigPanel.saveConfig();

            if (configManager.saveAll()) {
                JOptionPane.showMessageDialog(this,
                        "✅ Configurazione salvata!\n\n11 file aggiornati.",
                        "Salvataggio Completato", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "❌ Errore: " + e.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void showDialog(Frame parent) {
        try {
            new ConfigManagerDialog(parent).setVisible(true);
        } catch (Throwable t) {
            t.printStackTrace();
            StringBuilder msg = new StringBuilder(t.toString());
            for (StackTraceElement e : t.getStackTrace()) {
                if (msg.length() < 1000) {
                    msg.append("\n  at ").append(e.toString());
                }
            }
            JOptionPane.showMessageDialog(parent,
                    "Errore durante l'apertura del menu:\n" + msg.toString(),
                    "Errore Runtime",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
