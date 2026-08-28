package it.ipedmanager.ui;

import it.ipedmanager.Main;
import it.ipedmanager.model.Evidence;
import it.ipedmanager.service.IpedExecutor;
import it.ipedmanager.service.IpedExecutor.ProcessingOptions;

import it.ipedmanager.config.ConfigManager;
import it.ipedmanager.config.PropertiesConfigFile;
import it.ipedmanager.utils.BundleManager;

import it.ipedmanager.ui.config.VectorIcon;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Finestra principale di IPEDManager - Design Professionale
 * 
 * @author William Tritapepe
 * @version 2.0.0
 */
public class MainFrame extends JFrame {

    private EvidenceTableModel tableModel;
    @SuppressWarnings("unused")
    private JLabel infoLabel;
    @SuppressWarnings("unused")
    private JProgressBar globalProgress;
    private JLabel destinationLabel;
    @SuppressWarnings("unused")
    private JLabel zipDestinationLabel;
    private JLabel ipedPathLabel;
    private JLabel ipedVersionHeaderLabel;

    // Checkbox opzioni elaborazione
    private JCheckBox chkContinue, chkRestart, chkAppend, chkNoGui, chkNoLog;
    private JCheckBox chkPortable, chkAddOwner, chkNoPstAttachs, chkDownloadInternet;

    // Combo e campi
    private JComboBox<String> profileCombo;
    private JComboBox<String> languageCombo;
    private JTextField splashField;
    private JTextField extraParamsField;

    // Pulsanti
    private JButton processButton;
    private JButton previewButton;
    private JButton openReportButton;
    private JButton removeAllButton; // Smart Button field
    private JButton addFile; // Animation field
    private JButton addFolder; // Animation field

    // Servizi
    private IpedExecutor ipedExecutor;
    private String destinationPath = "";
    @SuppressWarnings("unused")
    private String zipDestinationPath = "";
    private File lastUsedDir; // Memory field

    // Constants
    // Constants
    private static final Color HEADER_BG = new Color(30, 58, 138); // Navy Blue (Coherent)
    private static final Color PANEL_BG = new Color(248, 250, 252); // Slate 50 (Professional Light)
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(225, 229, 235);
    private static final Color TEXT_PRIMARY = new Color(40, 45, 55);
    private static final Color TEXT_SECONDARY = new Color(110, 115, 125);

    private static final Color PRIMARY_BLUE = new Color(30, 58, 138); // Navy Blue
    private static final Color PRIMARY_BLUE_BORDER = new Color(30, 58, 138); // Navy Blue
    private static final Color SUCCESS_GREEN = new Color(16, 185, 129);
    private static final Color DANGER_RED = new Color(239, 68, 68);
    @SuppressWarnings("unused")
    private static final Color MENU_TEXT_COLOR = Color.WHITE;
    @SuppressWarnings("unused")
    private static final Color TABLE_HEADER_BG = new Color(241, 245, 249); // Slate 100

    private static final String[] SUPPORTED_EXTENSIONS = { "e01", "001", "dd", "raw", "bin", "vmdk", "iso", "ufdr",
            "zip", "rar", "7z", "tar", "gz" };

    private JTable evidenceTable;

    public MainFrame() {
        super("IPEDManager v" + Main.VERSION);

        // Set App Icon (used for Taskbar and Task Switcher)
        try {
            // Load from classpath (inside JAR) or file system
            java.net.URL iconUrl = getClass().getResource("/icons/ipedmanager_logo.png");
            if (iconUrl != null) {
                setIconImage(new ImageIcon(iconUrl).getImage());
            } else {
                // Fallback if running from IDE
                setIconImage(new ImageIcon("resources/icons/ipedmanager_logo.png").getImage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // --- FlatLaf TitleBar Integration for Main Window ---
        Color navy = new Color(30, 58, 138);
        getRootPane().putClientProperty("JRootPane.titleBarBackground", navy);
        getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);
        getRootPane().putClientProperty("JRootPane.titleBarButtonIconColor", Color.WHITE);
        getRootPane().putClientProperty("JRootPane.titleBarButtonHoverBackground", new Color(60, 100, 170));
        getRootPane().putClientProperty("JRootPane.titleBarButtonPressedBackground", new Color(80, 120, 190));

        this.ipedExecutor = new IpedExecutor();

        // Initialize ConfigManager if IPED is found
        if (ipedExecutor.isIpedConfigured()) {
            ConfigManager.getInstance().initialize(ipedExecutor.getIpedJarPath());
        }

        // Initialize i18n
        String locale = "it-IT";
        try {
            if (ConfigManager.getInstance().isValid()) {
                PropertiesConfigFile localConf = ConfigManager.getInstance().getLocalConfig();
                if (localConf != null) {
                    locale = localConf.get("managerLocale", "it"); // Separated from IPED 'locale'
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        BundleManager.loadFromConfig(locale != null ? locale : "it-IT");

        initComponents();
        createMenuBar();

        // Main Layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(PANEL_BG);

        // Header moved to MenuBar ("Integrated Look")

        JPanel content = new JPanel(new BorderLayout(0, 15));
        content.setBackground(PANEL_BG);
        content.setBorder(new EmptyBorder(20, 25, 20, 25));

        content.add(createConfigRow(), BorderLayout.NORTH);
        content.add(createEvidencePanel(), BorderLayout.CENTER);
        content.add(createOptionsRow(), BorderLayout.SOUTH);

        mainPanel.add(content, BorderLayout.CENTER);

        setContentPane(mainPanel);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setupListeners();
        setupDragDrop(); // Setup DnD after UI is fully built (so getParent() works)
        updateButtonStates();
        applyAnimations();

        // Applica zoom predefinito (+1)

        // --- FlatLaf Integration for Seamless Header ---
        // Force the Window Title Bar to Navy Blue
        getRootPane().putClientProperty("JRootPane.titleBarBackground", HEADER_BG);
        getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);
        // Make window control buttons (minimize, maximize, close) white
        getRootPane().putClientProperty("JRootPane.titleBarButtonIconColor", Color.WHITE);
        getRootPane().putClientProperty("JRootPane.titleBarButtonHoverBackground", new Color(60, 100, 170));
        getRootPane().putClientProperty("JRootPane.titleBarButtonPressedBackground", new Color(80, 120, 190));
        // Unified look (merge title bar with content area)
        getRootPane().putClientProperty("JRootPane.titleBarShowIcon", false);
    }

    private void initComponents() {
        tableModel = new EvidenceTableModel();
        evidenceTable = new JTable(tableModel);

        destinationLabel = new JLabel(BundleManager.getString("mainframe.noFolderSelected"));
        zipDestinationLabel = new JLabel(BundleManager.getString("mainframe.destinationDefault"));
        ipedPathLabel = new JLabel();
        updateIpedPathLabelText();

        // Init fields
        splashField = new JTextField();
        extraParamsField = new JTextField();

        // Init Buttons
        processButton = createPrimaryButton(BundleManager.getString("mainframe.startAnalysis"), "play");
        previewButton = createSmallButton(BundleManager.getString("mainframe.previewCommand"), "terminal");
        openReportButton = createActionHighlightButton(BundleManager.getString("mainframe.outputFolder"),
                "folder-open");
        openReportButton.setEnabled(false); // Initially disabled
    }

    // ==================== MENU BAR ====================

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(HEADER_BG);
        menuBar.setOpaque(true);
        menuBar.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15)); // Good padding

        // 1. LEFT: Branding & Version
        JLabel brandLabel = new JLabel("IPEDManager");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        brandLabel.setForeground(Color.WHITE);
        menuBar.add(brandLabel);

        menuBar.add(Box.createHorizontalStrut(8));

        JLabel verLabel = new JLabel("v" + Main.VERSION);
        verLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        verLabel.setForeground(new Color(200, 200, 200));
        menuBar.add(verLabel);

        menuBar.add(Box.createHorizontalStrut(15));

        // 2. LEFT: Hamburger Menu (Next to version)
        JButton btnMenu = createIconButton("menu", BundleManager.getString("mainframe.tooltip.menu"), null);
        btnMenu.addActionListener(e -> showHamburgerMenu(btnMenu));
        menuBar.add(btnMenu);

        // 3. SPACER
        menuBar.add(Box.createHorizontalGlue());

        // 4. RIGHT: Settings Icon
        JButton btnSettings = createIconButton("gears", BundleManager.getString("mainframe.tooltip.settings"),
                e -> openAdvancedConfig());
        // Add rotation animation to existing button
        addSpinAnimation(btnSettings);
        menuBar.add(btnSettings);

        setJMenuBar(menuBar);
    }

    // Helper for Header Icons
    private JButton createIconButton(String iconName, String tooltip, java.awt.event.ActionListener action) {
        JButton btn = new JButton();
        // Fallback or use VectorIcon
        btn.setIcon(new VectorIcon(iconName, 18, Color.WHITE));
        btn.setToolTipText(tooltip);
        if (action != null)
            btn.addActionListener(action);
        btn.setFocusable(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setContentAreaFilled(true);
                btn.setBackground(new Color(255, 255, 255, 30)); // Subtle white tint
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setContentAreaFilled(false);
            }
        });
        return btn;
    }

    // Helper for Spin Animation
    private void addSpinAnimation(JButton btn) {
        if (!(btn.getIcon() instanceof VectorIcon))
            return;
        VectorIcon icon = (VectorIcon) btn.getIcon();

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            private javax.swing.Timer timer;
            private double angle = 0;

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (timer == null) {
                    timer = new javax.swing.Timer(20, evt -> {
                        angle += 5;
                        if (angle >= 360)
                            angle -= 360;
                        icon.setRotation(angle);
                        btn.repaint();
                    });
                }
                timer.start();
                // Ensure bg effect from createIconButton is preserved or re-applied if needed
                btn.setContentAreaFilled(true);
                btn.setBackground(new Color(255, 255, 255, 30));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (timer != null) {
                    timer.stop();
                    angle = 0;
                    icon.setRotation(0);
                    btn.repaint();
                }
                btn.setContentAreaFilled(false);
            }
        });
    }

    // Hamburger Popup Logic
    private void showHamburgerMenu(Component invoker) {
        JPopupMenu popup = new JPopupMenu();

        // New Case (First Item)
        popup.add(createPopupItem(BundleManager.getString("mainframe.menu.newCase"), "plus", e -> newCase()));
        popup.addSeparator();

        // View Section
        popup.add(createPopupItem(BundleManager.getString("mainframe.zoomIn"), "plus", e -> changeZoom(0.1f)));
        popup.add(createPopupItem(BundleManager.getString("mainframe.zoomOut"), "minus", e -> changeZoom(-0.1f)));
        popup.add(createPopupItem(BundleManager.getString("mainframe.resetZoom"), "refresh", e -> resetZoom()));
        popup.addSeparator();

        // Config Files Section
        JMenu fileMenu = new JMenu(BundleManager.getString("mainframe.menu.manualConfig"));
        fileMenu.setIcon(new VectorIcon("home", 16, TEXT_PRIMARY));
        fileMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        fileMenu.add(createPopupItem("LocalConfig.txt", null, e -> openConfigFile("LocalConfig.txt")));
        fileMenu.add(createPopupItem("IPEDConfig.txt", null, e -> openConfigFile("IPEDConfig.txt")));
        fileMenu.add(createPopupItem("HTMLReportConfig.txt", null, e -> openConfigFile("conf/HTMLReportConfig.txt")));
        popup.add(fileMenu);

        // Profile Manager
        popup.add(createPopupItem(BundleManager.getString("mainframe.menu.profiles"), "folder",
                e -> openProfileManager()));
        // Live PC Performance Monitor
        popup.add(createPopupItem(BundleManager.getString("mainframe.menu.hardwareMonitor"), "activity",
                e -> SystemPerformanceDialog.showDialog(this)));
        popup.addSeparator();

        // Help Section
        popup.add(createPopupItem(BundleManager.getString("mainframe.menu.userGuide"), "info", e -> showHelp()));
        popup.add(createPopupItem(BundleManager.getString("mainframe.menu.paramGuide"), "report",
                e -> showParametersGuide()));
        popup.add(createPopupItem("GitHub", "github", e -> {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/Trita-a"));
            } catch (Exception ex) {
            }
        }));
        popup.addSeparator();

        popup.add(createPopupItem(BundleManager.getString("mainframe.menu.about"), "info", e -> showAbout()));
        popup.add(createPopupItem(BundleManager.getString("mainframe.menu.exit"), "export", e -> System.exit(0)));

        popup.show(invoker, 0, invoker.getHeight() + 5);
    }

    private JMenuItem createPopupItem(String text, String iconName, java.awt.event.ActionListener action) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (iconName != null)
            item.setIcon(new VectorIcon(iconName, 16, TEXT_PRIMARY));
        if (action != null)
            item.addActionListener(action);
        return item;
    }

    private void newCase() {
        boolean confirm = DialogHelper.showConfirm(this,
                BundleManager.getString("mainframe.dialog.newProject.title"),
                BundleManager.getString("mainframe.dialog.newProject.message"));
        if (confirm) {
            tableModel.clearAll();
            destinationPath = "";
            destinationLabel.setText(BundleManager.getString("mainframe.noFolderSelected"));
            destinationLabel.setForeground(TEXT_SECONDARY);
            updateButtonStates();
        }
    }

    // ==================== CONFIG ROW ====================

    private JPanel createConfigRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        ipedVersionHeaderLabel = createHeaderLabel(BundleManager.getString("mainframe.card.ipedVersion"), "chip");
        row.add(createCard(ipedVersionHeaderLabel, createIpedContent(),
                BundleManager.getString("mainframe.tooltip.ipedVersion")));
        updateIpedPathLabelText(); // Trigger an update now that the label is ready
        row.add(createCard(BundleManager.getString("mainframe.card.profile"), "user", createProfileContent(),
                BundleManager.getString("mainframe.tooltip.profile")));
        row.add(createCard(BundleManager.getString("mainframe.card.language"), "globe", createLanguageContent(),
                BundleManager.getString("mainframe.tooltip.language")));

        return row;
    }

    private JPanel createIpedContent() {
        JPanel p = new JPanel(new BorderLayout(5, 0));
        p.setOpaque(false);

        ipedPathLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ipedPathLabel.setForeground(TEXT_PRIMARY);

        JButton btn = createSmallButton(" " + BundleManager.getString("mainframe.button.change"));
        btn.addActionListener(e -> selectIpedJar());

        p.add(ipedPathLabel, BorderLayout.CENTER);
        p.add(btn, BorderLayout.EAST);
        return p;
    }

    private JPanel createProfileContent() {
        JPanel p = new JPanel(new BorderLayout(5, 0));
        p.setOpaque(false);

        profileCombo = new JComboBox<>();
        profileCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        reloadProfiles();

        // Listen for profile selection changes
        profileCombo.addItemListener(e -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                String selectedProfile = (String) profileCombo.getSelectedItem();
                onProfileSelected(selectedProfile);
            }
        });

        JButton btnManage = createSmallButton("", "folder");
        btnManage.setToolTipText(BundleManager.getString("mainframe.menu.profiles"));
        btnManage.addActionListener(e -> openProfileManager());

        p.add(profileCombo, BorderLayout.CENTER);
        p.add(btnManage, BorderLayout.EAST);
        return p;
    }

    /**
     * Called when a profile is selected from the dropdown.
     * Loads profile's config settings (except for "Personalizzato").
     * Settings are written to the conf/ files so IPED uses them directly.
     */
    private void onProfileSelected(String profileName) {
        // Ensure IPED is configured before loading profile
        if (!ipedExecutor.isIpedConfigured()) {
            return;
        }

        // Initialize ConfigManager if not already done
        it.ipedmanager.config.ConfigManager cm = it.ipedmanager.config.ConfigManager.getInstance();
        cm.initialize(ipedExecutor.getIpedJarPath());
        
        // Imposta il profilo attivo (se null o "Personalizzato", userà i file base)
        cm.setActiveProfile(profileName);
    }

    private void reloadProfiles() {
        String current = (String) profileCombo.getSelectedItem();
        profileCombo.removeAllItems();
        profileCombo.addItem(BundleManager.getString("mainframe.profile.custom"));

        // Dynamically load profiles from profiles/ directory
        if (ipedExecutor.isIpedConfigured()) {
            java.nio.file.Path profilesDir = java.nio.file.Paths.get(ipedExecutor.getIpedJarPath())
                    .getParent().resolve("profiles");
            if (java.nio.file.Files.isDirectory(profilesDir)) {
                try (java.nio.file.DirectoryStream<java.nio.file.Path> stream = java.nio.file.Files
                        .newDirectoryStream(profilesDir)) {
                    java.util.List<String> names = new java.util.ArrayList<>();
                    for (java.nio.file.Path entry : stream) {
                        if (java.nio.file.Files.isDirectory(entry)) {
                            names.add(entry.getFileName().toString());
                        }
                    }
                    java.util.Collections.sort(names, String::compareToIgnoreCase);
                    for (String name : names) {
                        profileCombo.addItem(name);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // Restore selection if possible
        if (current != null) {
            profileCombo.setSelectedItem(current);
        }
        if (profileCombo.getSelectedIndex() < 0) {
            profileCombo.setSelectedIndex(0);
        }
    }

    private void openProfileManager() {
        if (!ipedExecutor.isIpedConfigured()) {
            DialogHelper.showWarning(this,
                    BundleManager.getString("dialog.warning.title"),
                    BundleManager.getString("mainframe.dialog.warning.configureIped"));
            return;
        }
        java.nio.file.Path profilesDir = java.nio.file.Paths.get(ipedExecutor.getIpedJarPath())
                .getParent().resolve("profiles");
        ProfileManagerDialog.showDialog(this, profilesDir, this::reloadProfiles);
    }

    private JPanel createLanguageContent() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        languageCombo = new JComboBox<>(new String[] { "Italiano", "English", "Português", "Español" });
        languageCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Select current language
        String currentLang = BundleManager.getLocale().getLanguage();
        // Simple mapping
        switch (currentLang) {
            case "en":
                languageCombo.setSelectedItem("English");
                break;
            case "pt":
                languageCombo.setSelectedItem("Português");
                break;
            case "es":
                languageCombo.setSelectedItem("Español");
                break;
            default:
                languageCombo.setSelectedItem("Italiano");
                break;
        }

        languageCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selected = (String) e.getItem();
                String code = "it";
                if ("English".equals(selected))
                    code = "en";
                else if ("Português".equals(selected))
                    code = "pt";
                else if ("Español".equals(selected))
                    code = "es";

                // Save to LocalConfig as managerLocale
                if (ConfigManager.getInstance().isValid()) {
                    PropertiesConfigFile localConf = ConfigManager.getInstance().getLocalConfig();
                    if (localConf != null) {
                        localConf.set("managerLocale", code);
                        ConfigManager.getInstance().saveAll();
                        DialogHelper.showInfo(this,
                                BundleManager.getString("panel.local.language"),
                                BundleManager.getString("footer.info"));
                    }
                }
            }
        });

        p.add(languageCombo, BorderLayout.CENTER);

        return p;
    }

    @SuppressWarnings("unused")
    private JPanel createCard(String title, String icon, JComponent content) {
        return createCard(title, icon, content, null);
    }

    private JPanel createCard(JLabel headerLabel, JComponent content, String helpText) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(5, 10, 5, 10))); 

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)), 
                new EmptyBorder(0, 0, 4, 0))); 

        headerPanel.add(headerLabel, BorderLayout.WEST);

        if (helpText != null) {
            String titleStr = headerLabel.getText() != null ? headerLabel.getText().replaceAll("<[^>]*>", "").trim() : "";
            JButton helpBtn = createHelpButton(titleStr, helpText);
            headerPanel.add(helpBtn, BorderLayout.EAST);
        }

        card.add(headerPanel, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    private JPanel createCard(String title, String icon, JComponent content, String helpText) {
        return createCard(createHeaderLabel(title, icon), content, helpText);
    }

    private JButton createHelpButton(String title, String message) {
        JButton btn = new JButton(new VectorIcon("help", 16, TEXT_SECONDARY));
        btn.setBorder(null);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Aiuto");
        btn.addActionListener(e -> {
            // Create custom dialog with proper title color
            javax.swing.JDialog helpDialog = new javax.swing.JDialog(this, title, true);
            helpDialog.setDefaultCloseOperation(javax.swing.JDialog.DISPOSE_ON_CLOSE);
            // Force FlatLaf to use dark title bar text (black on light background)
            helpDialog.getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.BLACK);
            helpDialog.getRootPane().putClientProperty("JRootPane.titleBarBackground", Color.WHITE);

            JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
            contentPanel.setBackground(Color.WHITE);
            contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

            // Icon on left
            JLabel iconLabel = new JLabel(new VectorIcon("help", 40, PRIMARY_BLUE));
            iconLabel.setVerticalAlignment(SwingConstants.TOP);
            contentPanel.add(iconLabel, BorderLayout.WEST);

            // Text area
            JTextArea area = new JTextArea(message);
            area.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setEditable(false);
            area.setOpaque(false);
            area.setForeground(new Color(50, 50, 50));

            JScrollPane scroll = new JScrollPane(area);
            scroll.setBorder(null);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.setPreferredSize(new Dimension(320, 120));
            contentPanel.add(scroll, BorderLayout.CENTER);

            // OK Button (aligned right)
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            buttonPanel.setOpaque(false);
            JButton okBtn = new JButton("OK");
            okBtn.setPreferredSize(new Dimension(80, 30));
            okBtn.setBackground(PRIMARY_BLUE);
            okBtn.setForeground(Color.WHITE);
            okBtn.setFocusPainted(false);
            okBtn.addActionListener(ev -> helpDialog.dispose());
            buttonPanel.add(okBtn);
            contentPanel.add(buttonPanel, BorderLayout.SOUTH);

            helpDialog.setContentPane(contentPanel);
            helpDialog.pack();
            helpDialog.setLocationRelativeTo(this);
            helpDialog.setVisible(true);
        });
        return btn;
    }

    // ==================== OPTIONS ROW ====================

    private JPanel createOptionsRow() {
        JPanel container = new JPanel(new BorderLayout(0, 8)); // Reduced gap between cards and bottom area
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(0, 0, 0, 0));

        // === 1. TOP ROW: 3 Cards (Modalità, Output, Avanzate) ===
        JPanel cardsRow = new JPanel(new GridLayout(1, 3, 10, 0)); // Reduced horizontal gap
        cardsRow.setOpaque(false);
        // Removed fixed preferred size to let it shrink-wrap

        // Card 1: Modalità
        chkContinue = cb(BundleManager.getString("mainframe.check.continue"),
                BundleManager.getString("mainframe.check.continue.desc"));
        chkRestart = cb(BundleManager.getString("mainframe.check.restart"),
                BundleManager.getString("mainframe.check.restart.desc"));
        chkAppend = cb(BundleManager.getString("mainframe.check.append"),
                BundleManager.getString("mainframe.check.append.desc"));

        // Logic: Mutual Exclusion (Radio Button behavior)
        chkContinue.addActionListener(e -> {
            if (chkContinue.isSelected()) {
                chkRestart.setSelected(false);
                chkAppend.setSelected(false);
            }
        });
        chkRestart.addActionListener(e -> {
            if (chkRestart.isSelected()) {
                chkContinue.setSelected(false);
                chkContinue.setEnabled(false);
                chkAppend.setSelected(false);
                chkAppend.setEnabled(false);
            } else {
                chkContinue.setEnabled(true);
                chkAppend.setEnabled(true);
            }
        });
        chkAppend.addActionListener(e -> {
            if (chkAppend.isSelected()) {
                chkContinue.setSelected(false);
                chkRestart.setSelected(false);
            }
        });

        // Card 1: Modalità
        cardsRow.add(createCard(BundleManager.getString("mainframe.card.mode"), "settings",
                createOptionGroup(new JCheckBox[] {
                        chkContinue, chkRestart, chkAppend
                }), BundleManager.getString("mainframe.card.help.mode")));

        // Card 2: Output
        cardsRow.add(createCard(BundleManager.getString("mainframe.card.output"), "folder",
                createOptionGroup(new JCheckBox[] {
                        chkPortable = cb(BundleManager.getString("mainframe.check.portable"),
                                BundleManager.getString("mainframe.check.portable.desc")),
                        chkNoGui = cb(BundleManager.getString("mainframe.check.nogui"),
                                BundleManager.getString("mainframe.check.nogui.desc")),
                        chkNoLog = cb(BundleManager.getString("mainframe.check.nolog"),
                                BundleManager.getString("mainframe.check.nolog.desc"))
                }), BundleManager.getString("mainframe.card.help.output")));

        // Card 3: Avanzate
        cardsRow.add(createCard(BundleManager.getString("mainframe.card.advanced"), "sliders",
                createOptionGroup(new JCheckBox[] {
                        chkAddOwner = cb(BundleManager.getString("mainframe.check.addowner"),
                                BundleManager.getString("mainframe.check.addowner.desc")),
                        chkNoPstAttachs = cb(BundleManager.getString("mainframe.check.nopstattachs"),
                                BundleManager.getString("mainframe.check.nopstattachs.desc")),
                        chkDownloadInternet = cb(BundleManager.getString("mainframe.check.download"),
                                BundleManager.getString("mainframe.check.download.desc"))
                }), BundleManager.getString("mainframe.card.help.advanced")));

        container.add(cardsRow, BorderLayout.CENTER);

        // === 2. BOTTOM AREA: Actions and Extra Params ===
        JPanel bottomArea = new JPanel(new BorderLayout(0, 10)); // Gap between params and buttons
        bottomArea.setOpaque(false);

        // Extra Params Row
        JPanel paramsRow = new JPanel(new BorderLayout(15, 0));
        paramsRow.setOpaque(false);
        paramsRow.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel fieldsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        fieldsPanel.setOpaque(false);

        JLabel extLabel = new JLabel(BundleManager.getString("mainframe.label.extraparams"));
        extLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        extLabel.setForeground(TEXT_SECONDARY);

        extraParamsField.setColumns(30);
        extraParamsField.setToolTipText(BundleManager.getString("mainframe.tooltip.extraparams"));

        fieldsPanel.add(extLabel);
        fieldsPanel.add(Box.createHorizontalStrut(10));
        fieldsPanel.add(extraParamsField);

        paramsRow.add(fieldsPanel, BorderLayout.CENTER); // Center to fill width if needed, or WEST

        // Action Buttons Row (Destination + Buttons)
        JPanel actionsRow = new JPanel(new BorderLayout());
        actionsRow.setOpaque(false);
        actionsRow.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));
        actionsRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(15, 0, 0, 0))); // Top padding

        // Left: Destination
        JPanel destPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        destPanel.setOpaque(false);
        destinationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        destinationLabel.setForeground(TEXT_SECONDARY);
        JButton btnDest = createSmallButton(BundleManager.getString("mainframe.button.chooseFolder"), "folder");
        btnDest.addActionListener(e -> selectDestination());
        destPanel.add(destinationLabel);
        destPanel.add(btnDest);

        // Right: Main Buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);

        openReportButton.setPreferredSize(new Dimension(160, 38));
        previewButton.setPreferredSize(new Dimension(160, 38));
        processButton.setPreferredSize(new Dimension(200, 42));
        processButton.setFont(new Font("Segoe UI", Font.BOLD, 13));

        previewButton.addActionListener(e -> showCommandPreview());
        openReportButton.addActionListener(e -> openReport());
        processButton.addActionListener(e -> processEvidences());

        buttonsPanel.add(openReportButton);
        buttonsPanel.add(previewButton);
        buttonsPanel.add(processButton);

        actionsRow.add(destPanel, BorderLayout.WEST);
        actionsRow.add(buttonsPanel, BorderLayout.EAST);

        bottomArea.add(paramsRow, BorderLayout.NORTH);
        bottomArea.add(actionsRow, BorderLayout.SOUTH);

        container.add(bottomArea, BorderLayout.SOUTH);

        return container;
    }

    private JPanel createOptionGroup(JCheckBox[] checkboxes) {
        // Checkboxes in vertical list
        JPanel checkPanel = new JPanel();
        checkPanel.setLayout(new BoxLayout(checkPanel, BoxLayout.Y_AXIS));
        checkPanel.setOpaque(false);

        for (JCheckBox cb : checkboxes) {
            cb.setAlignmentX(Component.LEFT_ALIGNMENT);
            checkPanel.add(cb);
            checkPanel.add(Box.createVerticalStrut(2));
        }
        return checkPanel;
    }

    private JCheckBox cb(String text, String tooltip) {
        JCheckBox c = new JCheckBox(text);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        c.setOpaque(false);
        c.setToolTipText(tooltip);
        c.setFocusPainted(false);
        return c;
    }

    // ==================== EVIDENCE TABLE ====================

    private JPanel createEvidencePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(12, 14, 12, 14)));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel(BundleManager.getString("mainframe.panel.evidence.title"));
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(TEXT_PRIMARY);
        titlePanel.add(title);

        titlePanel.add(createHelpButton(BundleManager.getString("mainframe.help.evidence.title"),
                BundleManager.getString("mainframe.help.evidence.message")));

        header.add(titlePanel, BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);

        // 1. Add File - Minimal Style (Icon Only "file-plus")
        addFile = createSmallButton("", "file-plus"); // Icon includes plus
        addFile.setPreferredSize(new Dimension(45, 30));
        addFile.setBorderPainted(false); // Remove border "togli il bordo"
        addFile.setContentAreaFilled(false); // Clean look, but we want hover..
        addFile.setOpaque(true); // Needed for background color change
        addFile.setBorder(null); // Explicitly remove border
        // Use vector icon with consistent Primary Blue
        addFile.setIcon(new VectorIcon("file-plus", 20, PRIMARY_BLUE));
        addFile.setToolTipText(null);
        addFile.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                addFile.setBackground(new Color(240, 249, 255));
            }

            public void mouseExited(MouseEvent e) {
                addFile.setBackground(Color.WHITE);
            }
        });
        addFile.addActionListener(e -> addFiles());

        // 2. Add Folder - Minimal Style (Icon Only "folder-plus")
        addFolder = createSmallButton("", "folder-plus");
        addFolder.setPreferredSize(new Dimension(45, 30));
        addFolder.setBorderPainted(false);
        addFolder.setContentAreaFilled(false);
        addFolder.setOpaque(true);
        addFolder.setBorder(null);
        addFolder.setIcon(new VectorIcon("folder-plus", 20, PRIMARY_BLUE));
        addFolder.setToolTipText(null);
        addFolder.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                addFolder.setBackground(new Color(240, 249, 255));
            }

            public void mouseExited(MouseEvent e) {
                addFolder.setBackground(Color.WHITE);
            }
        });
        addFolder.addActionListener(e -> addDirectory());

        // 3. Remove All - Icon Only ("solo il cestino")
        removeAllButton = createSmallButton("", "trash");
        removeAllButton.setPreferredSize(new Dimension(40, 30)); // Compact square-ish
        removeAllButton.setBorderPainted(false);
        removeAllButton.setContentAreaFilled(false);
        removeAllButton.setOpaque(true);
        removeAllButton.setBorder(null);
        removeAllButton.setForeground(DANGER_RED);
        removeAllButton.setIcon(new VectorIcon("trash", 18, DANGER_RED));
        removeAllButton.setToolTipText(null); // No tooltip
        removeAllButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (removeAllButton.isEnabled()) {
                    // Subtle red tint on hover, icon STAYS RED
                    removeAllButton.setBackground(new Color(255, 235, 235));
                }
            }

            public void mouseExited(MouseEvent e) {
                removeAllButton.setBackground(Color.WHITE);
            }
        });
        removeAllButton.addActionListener(e -> {
            if (tableModel.getRowCount() > 0) {
                boolean confirm = DialogHelper.showConfirm(this,
                        BundleManager.getString("mainframe.dialog.confirmRemoveAll.title"),
                        BundleManager.getString("mainframe.dialog.confirmRemoveAll.message"), "alert");
                if (confirm) {
                    tableModel.clearAll();
                    updateButtonStates();
                }
            }
        });

        btns.add(addFile);
        btns.add(addFolder);
        btns.add(removeAllButton);
        header.add(btns, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);

        // === PROFESSIONAL COMPACT TABLE STYLING WITH SPACIOUS PADDING ===
        evidenceTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        evidenceTable.setSelectionBackground(new Color(220, 235, 252)); // Soft blue selection
        evidenceTable.setSelectionForeground(Color.BLACK);
        evidenceTable.setRowHeight(28); // Generous row height with breathing room
        evidenceTable.setShowGrid(false); // Global OFF
        evidenceTable.setShowVerticalLines(false); // Explicitly OFF
        evidenceTable.setShowHorizontalLines(true); // Horizontal ON
        evidenceTable.setGridColor(new Color(241, 245, 249)); // Slate-100 subtle line
        evidenceTable.setFillsViewportHeight(true);
        evidenceTable.setIntercellSpacing(new Dimension(0, 1));

        // Optimized column widths for 6-column layout with icon
        evidenceTable.getColumnModel().getColumn(0).setPreferredWidth(34); // Icon type
        evidenceTable.getColumnModel().getColumn(0).setMaxWidth(38);
        evidenceTable.getColumnModel().getColumn(0).setMinWidth(30);
        evidenceTable.getColumnModel().getColumn(1).setPreferredWidth(160); // Name
        evidenceTable.getColumnModel().getColumn(1).setMinWidth(100);
        evidenceTable.getColumnModel().getColumn(2).setPreferredWidth(300); // Path
        evidenceTable.getColumnModel().getColumn(2).setMinWidth(120);
        evidenceTable.getColumnModel().getColumn(3).setPreferredWidth(75); // Size
        evidenceTable.getColumnModel().getColumn(3).setMaxWidth(95);
        evidenceTable.getColumnModel().getColumn(3).setMinWidth(60);
        evidenceTable.getColumnModel().getColumn(4).setPreferredWidth(85); // Password
        evidenceTable.getColumnModel().getColumn(4).setMaxWidth(105);
        evidenceTable.getColumnModel().getColumn(4).setMinWidth(70);
        evidenceTable.getColumnModel().getColumn(5).setPreferredWidth(36); // Delete
        evidenceTable.getColumnModel().getColumn(5).setMaxWidth(40);
        evidenceTable.getColumnModel().getColumn(5).setMinWidth(32);

        // Professional cell renderer with icons and generous padding
        DefaultTableCellRenderer professionalRenderer = new DefaultTableCellRenderer() {
            private final Color EVEN_ROW = Color.WHITE;
            private final Color ODD_ROW = new Color(248, 250, 252); // Slate-50 zebra
            private final Color SELECTED_BG = new Color(220, 235, 252);
            private final Color DELETE_RED = new Color(220, 53, 69);
            private final Color FOLDER_COLOR = new Color(245, 158, 11); // Amber 500
            private final Color FILE_COLOR = new Color(100, 116, 139); // Slate 500

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Reset icon
                setIcon(null);
                setFont(new Font("Segoe UI", Font.PLAIN, 12));

                // Background and foreground
                if (isSelected) {
                    setBackground(SELECTED_BG);
                    setForeground(Color.BLACK);
                } else {
                    setBackground(row % 2 == 0 ? EVEN_ROW : ODD_ROW);
                    setForeground(TEXT_PRIMARY);
                }

                // Column-specific styling and padding
                switch (column) {
                    case 0: // Type Icon (Folder/File)
                        setHorizontalAlignment(JLabel.CENTER);
                        setBorder(new EmptyBorder(0, 8, 0, 2));
                        setText("");
                        if ("D".equals(value)) {
                            setIcon(new VectorIcon("folder", 18, FOLDER_COLOR));
                            setToolTipText(BundleManager.getString("mainframe.table.tooltip.folder"));
                        } else {
                            setIcon(new VectorIcon("file", 18, FILE_COLOR));
                            setToolTipText(BundleManager.getString("mainframe.table.tooltip.file"));
                        }
                        break;
                    case 1: // Name - bold, left aligned with left margin
                        setFont(new Font("Segoe UI", Font.BOLD, 12));
                        setHorizontalAlignment(JLabel.LEFT);
                        setBorder(new EmptyBorder(0, 6, 0, 8));
                        if (value != null) {
                            String name = value.toString();
                            setToolTipText(name.length() > 20 ? name : null);
                        }
                        break;
                    case 2: // Path - left aligned with padding and dynamic width
                        setHorizontalAlignment(JLabel.LEFT);
                        setBorder(new EmptyBorder(0, 8, 0, 8));
                        if (value instanceof String) {
                            String path = (String) value;
                            int colWidth = table.getColumnModel().getColumn(column).getWidth() - 20;
                            FontMetrics fm = getFontMetrics(getFont());
                            if (colWidth > 60 && fm != null && fm.stringWidth(path) > colWidth) {
                                setText(smartShortenPathForWidth(path, fm, colWidth));
                            } else {
                                setText(path);
                            }
                            setToolTipText("<html><b>" + BundleManager.getString("mainframe.table.tooltip.path")
                                    + "</b> " + path + "</html>");
                        }
                        break;
                    case 3: // Size - right aligned with right margin
                        setHorizontalAlignment(JLabel.RIGHT);
                        setBorder(new EmptyBorder(0, 8, 0, 10));
                        setFont(new Font("Consolas", Font.PLAIN, 11));
                        setForeground(isSelected ? Color.BLACK : new Color(100, 116, 139));
                        setToolTipText(null);
                        break;
                    case 4: // Password - centered
                        setHorizontalAlignment(JLabel.CENTER);
                        setBorder(new EmptyBorder(0, 6, 0, 6));
                        if (value != null && !value.toString().isEmpty()) {
                            setText("••••");
                            setToolTipText(BundleManager.getString("mainframe.table.tooltip.passwordSet"));
                        } else {
                            setText("-");
                            setForeground(new Color(180, 180, 180));
                            setToolTipText(BundleManager.getString("mainframe.table.tooltip.noPassword"));
                        }
                        break;
                    case 5: // Delete button
                        setHorizontalAlignment(JLabel.CENTER);
                        setBorder(new EmptyBorder(0, 4, 0, 6));
                        setIcon(new VectorIcon("trash", 16, DELETE_RED));
                        setText("");
                        setToolTipText(null);
                        break;
                    default:
                        setHorizontalAlignment(JLabel.LEFT);
                        setBorder(new EmptyBorder(0, 8, 0, 8));
                        setToolTipText(null);
                }

                return this;
            }
        };
        evidenceTable.setDefaultRenderer(Object.class, professionalRenderer);

        // Professional table header matching column content alignment and padding
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(new Font("Segoe UI", Font.BOLD, 11));
                setBackground(new Color(233, 236, 239)); // Gray (#E9ECEF)
                setForeground(new Color(33, 37, 41)); // Dark text

                switch (column) {
                    case 0: // Icon column
                        setHorizontalAlignment(JLabel.CENTER);
                        setBorder(new EmptyBorder(0, 0, 0, 0));
                        break;
                    case 1: // Name - Left aligned with matching padding
                        setHorizontalAlignment(JLabel.LEFT);
                        setBorder(new EmptyBorder(0, 6, 0, 8));
                        break;
                    case 2: // Path - Left aligned with matching padding
                        setHorizontalAlignment(JLabel.LEFT);
                        setBorder(new EmptyBorder(0, 8, 0, 8));
                        break;
                    case 3: // Size - Right aligned with matching padding
                        setHorizontalAlignment(JLabel.RIGHT);
                        setBorder(new EmptyBorder(0, 8, 0, 10));
                        break;
                    case 4: // Password - Centered
                        setHorizontalAlignment(JLabel.CENTER);
                        setBorder(new EmptyBorder(0, 6, 0, 6));
                        break;
                    case 5: // Delete button
                        setHorizontalAlignment(JLabel.CENTER);
                        setBorder(new EmptyBorder(0, 0, 0, 0));
                        break;
                    default:
                        setHorizontalAlignment(JLabel.LEFT);
                        setBorder(new EmptyBorder(0, 8, 0, 8));
                }
                return this;
            }
        };
        evidenceTable.getTableHeader().setDefaultRenderer(headerRenderer);
        evidenceTable.getTableHeader().setPreferredSize(new Dimension(0, 28)); // Matching 28px height
        evidenceTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));
        evidenceTable.getTableHeader().setReorderingAllowed(false);

        // Delete column click handler
        setupDeleteColumn();

        // Scroll pane with clean border
        JScrollPane scroll = new JScrollPane(evidenceTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scroll, BorderLayout.CENTER);

        // Footer hint - more compact
        JLabel hint = new JLabel(BundleManager.getString("mainframe.table.hint"));
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        hint.setForeground(new Color(150, 150, 150));
        hint.setBorder(new EmptyBorder(4, 0, 0, 0));
        panel.add(hint, BorderLayout.SOUTH);

        return panel;
    }

    // ==================== HEADER ====================

    @SuppressWarnings("unused")
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setPreferredSize(new Dimension(0, 55));
        header.setBorder(new EmptyBorder(0, 25, 0, 25));

        // Titolo
        JLabel title = new JLabel("IPEDManager");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel version = new JLabel("  v" + Main.VERSION);
        version.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        version.setForeground(new Color(160, 170, 180));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 15));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(version);
        header.add(titlePanel, BorderLayout.WEST);

        // Link e autore
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 17));
        infoPanel.setOpaque(false);

        JLabel github = createLink("GitHub", "https://github.com/Trita-a");
        infoPanel.add(github);

        JLabel author = new JLabel(Main.AUTHOR);
        author.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        author.setForeground(new Color(140, 150, 160));
        infoPanel.add(author);

        header.add(infoPanel, BorderLayout.EAST);

        return header;
    }

    private JLabel createLink(String text, String url) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(130, 180, 255));
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            public void mouseEntered(MouseEvent e) {
                label.setText("<html><u>" + text + "</u></html>");
            }

            public void mouseExited(MouseEvent e) {
                label.setText(text);
            }
        });
        return label;
    }

    private void setupDeleteColumn() {
        if (evidenceTable.getColumnCount() > 5) {
            TableColumn col = evidenceTable.getColumnModel().getColumn(5); // Delete is column 5
            col.setMaxWidth(28);
            col.setMinWidth(24);
            col.setPreferredWidth(24);

            // Custom renderer for delete button
            col.setCellRenderer((table, value, sel, foc, row, column) -> {
                JLabel lbl = new JLabel("", JLabel.CENTER);
                lbl.setIcon(new VectorIcon("trash", 16, new Color(220, 53, 69)));
                lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                lbl.setOpaque(true);
                lbl.setBackground(
                        sel ? new Color(220, 235, 252) : (row % 2 == 0 ? Color.WHITE : new Color(250, 251, 253)));
                // No tooltip
                return lbl;
            });

            // Mouse click handler for delete action
            evidenceTable.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int clickedCol = evidenceTable.columnAtPoint(e.getPoint());
                    int row = evidenceTable.rowAtPoint(e.getPoint());
                    if (clickedCol == 5 && row >= 0) { // Column 5 = Delete
                        tableModel.removeEvidence(row);
                        updateButtonStates();
                    }
                }
            });
        }
    }

    // ==================== LISTENERS & HELPERS ====================

    private void setupListeners() {
        chkContinue.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                chkRestart.setSelected(false);
                chkRestart.setEnabled(false);
            } else if (!chkAppend.isSelected()) {
                chkRestart.setEnabled(true);
            }
        });

        chkRestart.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                chkContinue.setSelected(false);
                chkContinue.setEnabled(false);
                chkAppend.setSelected(false);
                chkAppend.setEnabled(false);
            } else {
                chkContinue.setEnabled(true);
                chkAppend.setEnabled(true);
            }
        });

        chkAppend.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                chkRestart.setSelected(false);
                chkRestart.setEnabled(false);
            } else if (!chkContinue.isSelected()) {
                chkRestart.setEnabled(true);
            }
        });
    }

    private void updateButtonStates() {
        boolean hasFiles = !tableModel.isEmpty();
        boolean hasDest = !destinationPath.isEmpty();
        boolean canProcess = hasFiles && hasDest && ipedExecutor.isIpedConfigured();

        processButton.setEnabled(canProcess);
        previewButton.setEnabled(hasFiles);

        if (canProcess) {
            processButton.setBackground(SUCCESS_GREEN);
            processButton.setForeground(Color.WHITE);
            // Icona bianca quando attivo
            if (processButton.getIcon() instanceof VectorIcon) {
                ((VectorIcon) processButton.getIcon()).setColor(Color.WHITE);
            }
        } else {
            processButton.setBackground(new Color(180, 180, 180));
            processButton.setForeground(Color.WHITE);
            // Icona nera (TEXT_PRIMARY) quando disabilitato
            if (processButton.getIcon() instanceof VectorIcon) {
                ((VectorIcon) processButton.getIcon()).setColor(TEXT_PRIMARY);
            }
        }

        if (removeAllButton != null) {
            removeAllButton.setEnabled(hasFiles);
            if (!hasFiles) {
                // Bug fix: Reset aesthetic state if disabled (icon might be stuck on white from
                // hover)
                removeAllButton.setBackground(Color.WHITE);
                if (removeAllButton.getIcon() instanceof VectorIcon) {
                    ((VectorIcon) removeAllButton.getIcon()).setColor(DANGER_RED);
                }
            }
        }

        if (hasDest) {
            File rep = new File(destinationPath, "IPED-SearchApp.exe");
            openReportButton.setEnabled(rep.exists());
        }
    }

    private void openConfigFile(String filename) {
        if (!ipedExecutor.isIpedConfigured()) {
            DialogHelper.showWarning(this,
                    BundleManager.getString("dialog.warning.title"),
                    BundleManager.getString("mainframe.dialog.warning.configureIped"));
            return;
        }

        File ipedDir = new File(ipedExecutor.getIpedJarPath()).getParentFile();
        File configFile = new File(ipedDir, filename);

        if (!configFile.exists()) {
            DialogHelper.showError(this,
                    BundleManager.getString("dialog.error.title"),
                    BundleManager.getString("mainframe.error.fileNotFound", configFile.getAbsolutePath()));
            return;
        }

        try {
            Desktop.getDesktop().open(configFile);
        } catch (Exception e) {
            DialogHelper.showError(this,
                    BundleManager.getString("dialog.error.title"),
                    BundleManager.getString("mainframe.error.openFile", e.getMessage()));
        }
    }

    private void openAdvancedConfig() {
        if (!ipedExecutor.isIpedConfigured()) {
            DialogHelper.showWarning(this,
                    BundleManager.getString("dialog.warning.title"),
                    BundleManager.getString("mainframe.dialog.warning.configureIped"));
            return;
        }

        // Inizializza il ConfigManager con il percorso corrente
        it.ipedmanager.config.ConfigManager.getInstance().initialize(ipedExecutor.getIpedJarPath());

        // Apri il dialog
        it.ipedmanager.ui.config.ConfigManagerDialog.showDialog(this);
    }

    private void showHelp() {
        String help = BundleManager.getString("mainframe.help.content", Main.VERSION);
        DialogHelper.showInfo(this, BundleManager.getString("mainframe.help.title"), help, "help");
    }

    private void showParametersGuide() {
        String params = BundleManager.getString("mainframe.guide.parameters.content");
        DialogHelper.showInfo(this, BundleManager.getString("mainframe.menu.paramGuide"), params, "terminal");
    }

    private void showAbout() {
        String about = BundleManager.getString("mainframe.about.content", Main.VERSION, Main.AUTHOR);
        DialogHelper.showInfo(this, BundleManager.getString("mainframe.about.title"), about, "info");
    }

    private void addFiles() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(BundleManager.getString("mainframe.dialog.selectFile.title"));
        fc.setMultiSelectionEnabled(true);
        if (lastUsedDir != null)
            fc.setCurrentDirectory(lastUsedDir);
        fc.setFileFilter(new FileNameExtensionFilter(BundleManager.getString("mainframe.dialog.filter.forensicImages"),
                SUPPORTED_EXTENSIONS));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            lastUsedDir = fc.getCurrentDirectory();
            for (File f : fc.getSelectedFiles())
                tableModel.addEvidence(new Evidence(f));
            updateButtonStates();
        }
    }

    private void addDirectory() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(BundleManager.getString("mainframe.button.chooseFolder"));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (lastUsedDir != null)
            fc.setCurrentDirectory(lastUsedDir);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            lastUsedDir = fc.getCurrentDirectory();
            tableModel.addEvidence(new Evidence(fc.getSelectedFile()));
            updateButtonStates();
        }
    }

    private void selectDestination() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(BundleManager.getString("mainframe.dialog.destination.title"));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            destinationPath = fc.getSelectedFile().getAbsolutePath();
            destinationLabel.setText(smartShortenPath(destinationPath, 50));
            destinationLabel.setForeground(TEXT_PRIMARY);
            zipDestinationPath = destinationPath;
            updateButtonStates();
        }
    }

    private void selectIpedJar() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(BundleManager.getString("mainframe.dialog.iped.title"));
        fc.setFileFilter(new FileNameExtensionFilter("IPED JAR", "jar"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            ipedExecutor.setIpedJarPath(fc.getSelectedFile().getAbsolutePath());
            updateIpedPathLabelText();
            ipedPathLabel.setForeground(TEXT_PRIMARY);
            updateButtonStates();
        }
    }

    private ProcessingOptions getOpts() {
        ProcessingOptions o = new ProcessingOptions();
        o.continueProcessing = chkContinue.isSelected();
        o.restart = chkRestart.isSelected();
        o.append = chkAppend.isSelected();
        o.nogui = chkNoGui.isSelected();
        o.nolog = chkNoLog.isSelected();
        o.portable = chkPortable.isSelected();
        o.addOwner = chkAddOwner.isSelected();
        o.noPstAttachs = chkNoPstAttachs.isSelected();
        o.downloadInternetData = chkDownloadInternet.isSelected();
        o.splashMessage = splashField.getText();
        o.extraParams = extraParamsField.getText();

        // Fix: Read RAM from LocalConfig
        try {
            it.ipedmanager.config.ConfigManager cm = it.ipedmanager.config.ConfigManager.getInstance();
            if (!cm.isValid() && ipedExecutor.isIpedConfigured()) {
                cm.initialize(ipedExecutor.getIpedJarPath());
            }
            if (cm.isValid()) {
                it.ipedmanager.config.PropertiesConfigFile lc = cm.getLocalConfig();
                if (lc != null) {
                    o.maxMemoryGB = lc.getInt("maxMemoryGB", 8); // Default 8GB
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading memory config: " + e.getMessage());
            o.maxMemoryGB = 8; // Fallback
        }

        return o;
    }

    private void showCommandPreview() {
        List<String> cmd = ipedExecutor.buildCommand(
                tableModel.getEvidences(),
                destinationPath.isEmpty() ? "[DESTINAZIONE]" : destinationPath,
                (String) profileCombo.getSelectedItem(),
                getOpts());

        String s = ipedExecutor.getCommandString(cmd);

        JTextArea area = new JTextArea(s, 10, 60);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Consolas", Font.PLAIN, 11));

        JButton copy = new JButton(BundleManager.getString("mainframe.button.copyClipboard"));
        copy.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(s), null);
            copy.setText(BundleManager.getString("mainframe.button.copied"));
        });

        JPanel p = new JPanel(new BorderLayout(5, 8));
        p.add(new JScrollPane(area), BorderLayout.CENTER);
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bp.add(copy);
        p.add(bp, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, p, BundleManager.getString("mainframe.dialog.commandPreview.title"),
                JOptionPane.PLAIN_MESSAGE);
    }

    private void processEvidences() {
        if (tableModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, BundleManager.getString("mainframe.dialog.warning.noEvidence"),
                    BundleManager.getString("dialog.warning.title"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (destinationPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, BundleManager.getString("mainframe.dialog.warning.noDestination"),
                    BundleManager.getString("dialog.warning.title"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // VALIDAZIONE CONFIGURAZIONE
        List<String> configErrors = it.ipedmanager.utils.ConfigValidator.getInstance().validateConfiguration();
        if (!configErrors.isEmpty()) {
            String message = BundleManager.getString("mainframe.dialog.error.configValidation",
                    String.join("\n\n", configErrors));
            JOptionPane.showMessageDialog(this, message, BundleManager.getString("dialog.error.title"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // VERIFICA E PROMEMORIA RISORSE RAM PRE-AVVIO
        if (!checkPreflightResourceConfirmation()) {
            return;
        }

        // Open Monitor Dialog
        ExecutionMonitorDialog monitor = new ExecutionMonitorDialog(this);
        monitor.setLocationRelativeTo(this);

        ProcessingOptions opts = getOpts();
        it.ipedmanager.config.PropertiesConfigFile initLc = it.ipedmanager.config.ConfigManager.getInstance().getConfigFile(it.ipedmanager.config.ConfigManager.LOCAL_CONFIG);
        String initTemp = initLc != null ? initLc.getString("indexTemp") : null;
        monitor.setPaths(destinationPath, initTemp, false, opts.maxMemoryGB);

        monitor.setOnStopCallback(() -> {
            monitor.appendLog("\n[SISTEMA] Richiesta di interruzione inviata. Attendere...");
            ipedExecutor.abortExecution();
        });


        new Thread(() -> {
            try {
                SwingUtilities.invokeLater(() -> monitor.setVisible(true));

                            // GESTIONE RAM DISK AUTOMATICO
                            it.ipedmanager.service.RamDiskService rds = it.ipedmanager.service.RamDiskService.getInstance();
                            final boolean[] ramDiskMounted = {false};
                            final String[] originalTemp = {null};
                            final it.ipedmanager.config.PropertiesConfigFile[] localConfig = {null};

                            if (rds.isAutoMode()) {
                                monitor.appendLog("\n[SISTEMA] Creazione RAM Disk in corso (" + rds.getEngine() + ")...");
                                if (rds.mount()) {
                                    String letter = rds.getLastAssignedLetter();
                                    if (letter != null) {
                                        ramDiskMounted[0] = true;
                                        monitor.appendLog("\n[SISTEMA] RAM Disk montato su " + letter);
                                        
                                        localConfig[0] = it.ipedmanager.config.ConfigManager.getInstance().getConfigFile(it.ipedmanager.config.ConfigManager.LOCAL_CONFIG);
                                        originalTemp[0] = localConfig[0].getString("indexTemp");
                                        String ramTemp = letter + "\\Temp";
                                        localConfig[0].setString("indexTemp", ramTemp);
                                        localConfig[0].save();
                                        monitor.setPaths(destinationPath, ramTemp, true, opts.maxMemoryGB);
                                        monitor.appendLog("\n[SISTEMA] Temp di IPED impostata su: " + ramTemp + "\n");
                                    } else {
                                        monitor.appendLog("\n[SISTEMA] Impossibile determinare la lettera del RAM Disk. Verranno usate le impostazioni di default.\n");
                                    }
                                } else {
                                    monitor.appendLog("\n[SISTEMA] ERRORE durante la creazione del RAM Disk. L'analisi procederà con il disco fisico.\n");
                                }
                            }

                            List<String> cmd = ipedExecutor.buildCommand(
                                    tableModel.getEvidences(), destinationPath,
                                    (String) profileCombo.getSelectedItem(), opts);

                monitor.appendLog("[SISTEMA] Versione IPED in uso: " + ipedExecutor.getIpedVersion() + "\n");
                monitor.appendLog(
                        BundleManager.getString("dialog.monitor.command") + ": " + String.join(" ", cmd) + "\n");
                monitor.appendLog("---------------------------------------------------\n");

                int exitCode = ipedExecutor.execute(cmd, line -> monitor.appendLog(line));
                boolean success = (exitCode == 0) && !ipedExecutor.isAborted();

                SwingUtilities.invokeLater(() -> {
                    monitor.setFinished(success);

                    // SMONTAGGIO RAM DISK AUTOMATICO
                    if (rds.isAutoMode() && ramDiskMounted[0]) {
                        monitor.appendLog("\n[SISTEMA] Analisi terminata. Smontaggio RAM Disk in corso...");
                        if (rds.unmount()) {
                            monitor.appendLog("\n[SISTEMA] RAM Disk smontato con successo.");
                        } else {
                            monitor.appendLog("\n[SISTEMA] ERRORE: Impossibile smontare il RAM Disk automaticamente.");
                        }
                        if (localConfig[0] != null && originalTemp[0] != null) {
                            localConfig[0].setString("indexTemp", originalTemp[0]);
                            localConfig[0].save();
                        }
                    }

                    updateButtonStates();
                });
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    monitor.appendLog(
                            "\n" + BundleManager.getString("mainframe.dialog.error.critical") + " " + e.getMessage());
                    monitor.setFinished(false);
                    JOptionPane.showMessageDialog(this,
                            BundleManager.getString("dialog.error.title") + ": " + e.getMessage(),
                            BundleManager.getString("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    private void openReport() {
        try {
            File f = new File(destinationPath, "IPED-SearchApp.exe");
            if (f.exists())
                Desktop.getDesktop().open(f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Mostra un dialogo di riepilogo pre-volo che indica la RAM assegnata ad IPED
     * e permette all'utente di modificarla al volo prima di avviare.
     */
    private boolean checkPreflightResourceConfirmation() {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        boolean showReminder = prefs.getBoolean("showPreflightRamReminder", true);
        if (!showReminder) {
            return true;
        }

        while (true) {
            ProcessingOptions opts = getOpts();
            int ramGB = opts.maxMemoryGB;

            it.ipedmanager.service.SystemHardwareService.SystemSnapshot snap = 
                    it.ipedmanager.service.SystemHardwareService.getInstance().captureSnapshot(null, null, false);
            double totalSysGB = snap.totalPhysicalMemory / (1024.0 * 1024 * 1024);
            java.text.DecimalFormat df1 = new java.text.DecimalFormat("#0.0");
            String totalSysStr = totalSysGB > 0 ? (df1.format(totalSysGB) + " GB") : "--";

            String profile = (String) profileCombo.getSelectedItem();
            if (profile == null || profile.isEmpty()) profile = "Default";

            JPanel panel = new JPanel(new BorderLayout(0, 14));
            panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

            String htmlText = "<html><body style='font-family:Segoe UI, sans-serif; font-size:11px;'>"
                    + "<div style='font-size:13px; font-weight:bold; color:#1E3A8A; margin-bottom:8px;'>"
                    + "Riepilogo Risorse & Avvio Elaborazione"
                    + "</div>"
                    + "<table cellpadding='4' cellspacing='0' style='font-size:11px;'>"
                    + "<tr><td><b>Memoria RAM Assegnata a IPED (-Xmx):</b></td><td><b style='color:#059669; font-size:12px;'>" + ramGB + " GB</b> <span style='color:#64748B;'>(su " + totalSysStr + " Totali PC)</span></td></tr>"
                    + "<tr><td><b>Profilo Forense Selezionato:</b></td><td><b>" + profile + "</b></td></tr>"
                    + "<tr><td><b>Numero Evidenze:</b></td><td>" + tableModel.getRowCount() + " elemento/i</td></tr>"
                    + "<tr><td><b>Cartella Destinazione:</b></td><td><span style='color:#475569;'>" + (destinationPath.length() > 42 ? destinationPath.substring(0, 39) + "..." : destinationPath) + "</span></td></tr>"
                    + "</table>"
                    + "<div style='margin-top:10px; color:#64748B; font-size:10px;'>"
                    + "<i>Per modificare la quantità di RAM assegnata a IPED, clicca su 'Modifica Impostazioni'.</i>"
                    + "</div></body></html>";

            JLabel infoLabel = new JLabel(htmlText);
            infoLabel.setIcon(new it.ipedmanager.ui.config.VectorIcon("ram", 32, new Color(30, 58, 138)));
            infoLabel.setIconTextGap(14);
            panel.add(infoLabel, BorderLayout.CENTER);

            JCheckBox chkDontShow = new JCheckBox("Non mostrare più questo riepilogo prima di avviare");
            chkDontShow.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            chkDontShow.setForeground(new Color(100, 116, 139));
            panel.add(chkDontShow, BorderLayout.SOUTH);

            Object[] options = new Object[]{"Avvia Elaborazione", "Modifica Impostazioni...", "Annulla"};
            int choice = JOptionPane.showOptionDialog(
                    this,
                    panel,
                    "Verifica Risorse IPED",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (chkDontShow.isSelected()) {
                prefs.putBoolean("showPreflightRamReminder", false);
            }

            if (choice == 0) {
                return true;
            } else if (choice == 1) {
                it.ipedmanager.ui.config.ConfigManagerDialog.showDialog(this);
            } else {
                return false;
            }
        }
    }

    // ==================== UTILS ====================

    private JButton createSmallButton(String text, String iconName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        if (iconName != null) {
            btn.setIcon(new VectorIcon(iconName, 18, TEXT_PRIMARY));
        }
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBackground(CARD_BG);
        btn.setForeground(TEXT_PRIMARY);

        // Preserve FlatLaf rounded border but set color
        btn.putClientProperty("Component.borderColor", BORDER_COLOR);
        btn.putClientProperty("Component.borderWidth", 1);

        return btn;
    }

    private JButton createSmallButton(String text) {
        return createSmallButton(text, null);
    }

    private JButton createPrimaryButton(String text, String iconName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        Color contentColor = Color.WHITE; // Use White for text

        if (iconName != null) {
            // Default icon black (TEXT_PRIMARY), will be toggled
            btn.setIcon(new VectorIcon(iconName, 18, TEXT_PRIMARY));
        }
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBackground(PRIMARY_BLUE);
        btn.setForeground(contentColor);

        // Preserve FlatLaf rounded border but set color
        btn.putClientProperty("Component.borderColor", PRIMARY_BLUE_BORDER);
        btn.putClientProperty("Component.borderWidth", 1);

        return btn;
    }

    private JButton createActionHighlightButton(String text, String iconName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        Color solidBg = new Color(80, 140, 230);

        if (iconName != null) {
            // Icona sempre nera
            btn.setIcon(new VectorIcon(iconName, 18, TEXT_PRIMARY));
        }
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBackground(solidBg);
        btn.setForeground(Color.WHITE);
        return btn;
    }

    private String smartShortenPath(String path, int max) {
        if (path == null)
            return "";
        if (path.length() <= max)
            return path;

        // Keep drive (C:\) or start
        int startLen = 3;
        // Keep end (filename)
        int endLen = max - startLen - 3; // 3 for "..."

        if (endLen < 5)
            endLen = 5; // Minimum end

        return path.substring(0, startLen) + "..." + path.substring(path.length() - endLen);
    }

    private String smartShortenPathForWidth(String path, FontMetrics fm, int targetWidth) {
        if (path == null || fm == null || fm.stringWidth(path) <= targetWidth) {
            return path != null ? path : "";
        }
        int len = path.length();
        int minEnd = 6;
        int maxEnd = len - 4;
        for (int endLen = maxEnd; endLen >= minEnd; endLen--) {
            String candidate = path.substring(0, 3) + "..." + path.substring(len - endLen);
            if (fm.stringWidth(candidate) <= targetWidth) {
                return candidate;
            }
        }
        return path.substring(0, 3) + "..." + path.substring(Math.max(3, len - minEnd));
    }

    private void setupDragDrop() {
        DropTargetAdapter adapter = new DropTargetAdapter() {
            public void drop(DropTargetDropEvent e) {
                try {
                    e.acceptDrop(DnDConstants.ACTION_COPY);
                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>) e.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);

                    for (File f : files) {
                        if (isValid(f))
                            tableModel.addEvidence(new Evidence(f));
                    }
                    updateButtonStates();
                    e.dropComplete(true);
                } catch (Exception ex) {
                    System.err.println("DnD Error: " + ex.getMessage());
                    ex.printStackTrace();
                    e.dropComplete(false);
                    JOptionPane.showMessageDialog(MainFrame.this, "Errore Drag & Drop: " + ex.getMessage(), "Errore",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

            public void dragEnter(DropTargetDragEvent e) {
                // Visual feedback only on table
                evidenceTable.setBorder(BorderFactory.createLineBorder(PRIMARY_BLUE, 2));
            }

            public void dragExit(DropTargetEvent e) {
                evidenceTable.setBorder(null);
            }
        };

        // Attach to table
        new DropTarget(evidenceTable, adapter);

        // Try attach to viewport (parent) to catch drops on empty area
        if (evidenceTable.getParent() != null) {
            new DropTarget(evidenceTable.getParent(), adapter);
        }
    }

    private boolean isValid(File f) {
        // Accept files AND directories (folders)
        // Directories will be processed recursively by IPED
        return f.isFile() || f.isDirectory();
    }

    private JLabel createHeaderLabel(String text, String iconName) {
        JLabel lbl = new JLabel(" " + text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(TEXT_SECONDARY);
        if (iconName != null) {
            lbl.setIcon(new VectorIcon(iconName, 20, TEXT_SECONDARY));
        }
        return lbl;
    }

    // ==================== ZOOM / SCALING ====================

    private float currentZoom = 1.1f; // Default zoom +1

    private void changeZoom(float delta) {
        setZoom(currentZoom + delta);
    }

    private void resetZoom() {
        setZoom(1.0f);
    }

    private void setZoom(float scale) {
        if (scale < 0.5f)
            scale = 0.5f;
        if (scale > 2.0f)
            scale = 2.0f;

        this.currentZoom = scale;
        applyScaling(this, scale);
        SwingUtilities.updateComponentTreeUI(this);
    }

    private void applyScaling(Container container, float scale) {
        for (Component c : container.getComponents()) {
            if (c instanceof JComponent) {
                JComponent jc = (JComponent) c;
                // Store original font
                Font original = (Font) jc.getClientProperty("originalFont");
                if (original == null) {
                    original = jc.getFont();
                    if (original != null) {
                        jc.putClientProperty("originalFont", original);
                    }
                }

                if (original != null) {
                    int newSize = Math.round(original.getSize() * scale);
                    // Derive new font with scaled size
                    jc.setFont(original.deriveFont((float) newSize));
                }
            }

            if (c instanceof Container) {
                applyScaling((Container) c, scale);
            }
        }
    }

    // ==================== ANIMATIONS ====================

    private enum AnimationType {
        ROTATE, SHAKE, PULSE
    }

    private void attachAnimation(JButton btn, AnimationType type) {
        btn.addMouseListener(new IconAnimator(btn, type));
    }

    private class IconAnimator extends MouseAdapter {
        private final JButton button;
        private final AnimationType type;
        private javax.swing.Timer timer;
        private double frame = 0;
        private boolean increasing = true;

        public IconAnimator(JButton button, AnimationType type) {
            this.button = button;
            this.type = type;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (!button.isEnabled())
                return;
            startTimer();
        }

        @Override
        public void mouseExited(MouseEvent e) {
            stopTimer();
        }

        private void startTimer() {
            if (timer != null && timer.isRunning())
                return;

            frame = 0;
            increasing = true;

            timer = new javax.swing.Timer(20, evt -> {
                if (!button.isEnabled()) {
                    stopTimer();
                    return;
                }

                Icon icon = button.getIcon();
                if (!(icon instanceof VectorIcon))
                    return;
                VectorIcon vIcon = (VectorIcon) icon;

                switch (type) {
                    case ROTATE:
                        frame = (frame + 5) % 360;
                        vIcon.setRotation(frame);
                        break;
                    case SHAKE:
                        // Shake -10 to +10
                        if (increasing) {
                            frame += 2;
                            if (frame >= 10)
                                increasing = false;
                        } else {
                            frame -= 2;
                            if (frame <= -10)
                                increasing = true;
                        }
                        vIcon.setRotation(frame);
                        break;
                    case PULSE:
                        // Scale 1.0 to 1.2
                        if (increasing) {
                            frame += 0.02;
                            if (frame >= 0.2)
                                increasing = false;
                        } else {
                            frame -= 0.02;
                            if (frame <= 0)
                                increasing = true;
                        }
                        vIcon.setScale(1.0 + frame);
                        break;
                }
                button.repaint();
            });
            timer.start();
        }

        private void stopTimer() {
            if (timer != null) {
                timer.stop();
                timer = null;
            }
            // Reset state
            Icon icon = button.getIcon();
            if (icon instanceof VectorIcon) {
                VectorIcon vIcon = (VectorIcon) icon;
                vIcon.setRotation(0);
                vIcon.setScale(1.0);
            }
            button.repaint();
        }
    }

    private void applyAnimations() {
        attachAnimation(addFile, AnimationType.PULSE);
        attachAnimation(addFolder, AnimationType.PULSE);
        attachAnimation(removeAllButton, AnimationType.SHAKE);
        attachAnimation(previewButton, AnimationType.PULSE);
        attachAnimation(openReportButton, AnimationType.PULSE);
        attachAnimation(processButton, AnimationType.PULSE);
    }

    private void updateIpedPathLabelText() {
        if (ipedExecutor.isIpedConfigured()) {
            String ver = ipedExecutor.getIpedVersion();
            String path = smartShortenPath(ipedExecutor.getIpedJarPath(), 35);
            ipedPathLabel.setText(path);
            
            if (ipedVersionHeaderLabel != null) {
                if (!"Sconosciuta".equals(ver)) {
                    ipedVersionHeaderLabel.setText("<html> " + BundleManager.getString("mainframe.card.ipedVersion") + " &nbsp;<b style='color:#1e3a8a;'>v" + ver + "</b></html>");
                } else {
                    ipedVersionHeaderLabel.setText(" " + BundleManager.getString("mainframe.card.ipedVersion"));
                }
            }
        } else {
            ipedPathLabel.setText("Non configurato");
            if (ipedVersionHeaderLabel != null) {
                ipedVersionHeaderLabel.setText(" " + BundleManager.getString("mainframe.card.ipedVersion"));
            }
        }
    }
}
