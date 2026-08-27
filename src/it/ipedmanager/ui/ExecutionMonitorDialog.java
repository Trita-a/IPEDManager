package it.ipedmanager.ui;

import it.ipedmanager.ui.components.PerformanceDashboardPanel;
import it.ipedmanager.ui.config.VectorIcon;
import it.ipedmanager.utils.BundleManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Dialog per monitorare l'esecuzione di IPED in tempo reale.
 * Con design ultra-pulito, TitleBar FlatLaf integrata, Console IDE Dark e pulsanti di azione avanzati (es. Apri Cartella Output, Esporta Log).
 */
public class ExecutionMonitorDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    // Palette colori coerente con FlatLaf Light & Navy
    private static final Color HEADER_NAVY = new Color(30, 58, 138);  // Navy Blue (#1E3A8A)
    private static final Color HEADER_PILL_BG = new Color(23, 37, 84); // Deep Navy (#172554)
    private static final Color PANEL_BG = new Color(248, 250, 252);     // Slate 50 (#F8FAFC)
    private static final Color BORDER_COLOR = new Color(226, 232, 240); // Slate 200 (#E2E8F0)
    
    // Console IDE Dark Palette
    private static final Color CONSOLE_HEADER_BG = new Color(30, 41, 59); // Slate 800 (#1E293B)
    private static final Color CONSOLE_BG = new Color(15, 23, 42);        // Slate 900 (#0F172A)
    private static final Color CONSOLE_TEXT = new Color(241, 245, 249);   // Slate 100
    private static final Color CONSOLE_BADGE_BG = new Color(51, 65, 85);  // Slate 700
    private static final Color TEXT_PRIMARY = new Color(30, 41, 59);

    private static final Color STATUS_GREEN = new Color(16, 185, 129); // Emerald 500
    private static final Color STATUS_RED = new Color(239, 68, 68);    // Red 500

    private JTextArea logArea;
    private JLabel consoleBadge;
    private JProgressBar progressBar;
    private JLabel footerStatusLabel;
    private JButton closeButton;
    private JButton openOutputButton;
    private JButton stopButton;
    private JButton toggleDashboardButton;
    
    // Indicatori live nell'header superiore
    private JLabel statusBadgeLabel;
    private JLabel headerTimeLabel;
    private JLabel headerLogLabel;

    private PerformanceDashboardPanel dashboardPanel;
    private JPanel dashboardWrapper;

    private String targetOutputPath;
    @SuppressWarnings("unused")
    private boolean finished = false;
    private Runnable onStopCallback;

    public ExecutionMonitorDialog(Window owner) {
        super(owner, "", ModalityType.APPLICATION_MODAL);
        setSize(1060, 750);
        setMinimumSize(new Dimension(880, 560));
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        // --- FlatLaf TitleBar Integration ---
        getRootPane().putClientProperty("JRootPane.titleBarBackground", HEADER_NAVY);
        getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);
        getRootPane().putClientProperty("JRootPane.titleBarButtonIconColor", Color.WHITE);
        getRootPane().putClientProperty("JRootPane.titleBarButtonHoverBackground", new Color(60, 100, 170));
        getRootPane().putClientProperty("JRootPane.titleBarButtonPressedBackground", new Color(80, 120, 190));
        getRootPane().putClientProperty("JRootPane.titleBarShowIcon", false);
        getRootPane().putClientProperty("JRootPane.titleBarShowTitle", false);
        getRootPane().putClientProperty("JRootPane.titleBarButtons", "close");
        getRootPane().putClientProperty("JRootPane.menuBarEmbedded", true);

        initComponents();
    }

    private void initComponents() {
        // =========================================================================================
        // 1. HEADER SUPERIORE BILANCIATO SU JMENUBAR (Stile MainFrame)
        // =========================================================================================
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(HEADER_NAVY);
        menuBar.setOpaque(true);
        menuBar.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        // --- SINISTRA: Icona + Titolo + Badge Stato ---
        JLabel iconLabel = new JLabel(new VectorIcon("activity", 18, Color.WHITE));
        iconLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel(BundleManager.getString("dialog.monitor.header"));
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(Color.WHITE);
        title.setAlignmentY(Component.CENTER_ALIGNMENT);

        statusBadgeLabel = new JLabel("IN ANALISI");
        statusBadgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusBadgeLabel.setForeground(Color.WHITE);
        statusBadgeLabel.setOpaque(true);
        statusBadgeLabel.setBackground(STATUS_GREEN);
        statusBadgeLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        statusBadgeLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(52, 211, 153), 1, true),
                new EmptyBorder(3, 10, 3, 10)
        ));

        // --- CENTRO: Pillole Live (Tempo + Log) ---
        JPanel timePill = createHeaderPill("report", "Tempo: 00:00:00");
        timePill.setAlignmentY(Component.CENTER_ALIGNMENT);
        headerTimeLabel = (JLabel) timePill.getComponent(1);

        JPanel logPill = createHeaderPill("terminal", "Log: 0 righe");
        logPill.setAlignmentY(Component.CENTER_ALIGNMENT);
        headerLogLabel = (JLabel) logPill.getComponent(1);

        // --- DESTRA: Tasto Toggle Dashboard ---
        toggleDashboardButton = new JButton("Nascondi Hardware", new VectorIcon("gauge", 13, Color.WHITE));
        toggleDashboardButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        toggleDashboardButton.setForeground(Color.WHITE);
        toggleDashboardButton.setBackground(new Color(37, 99, 235)); // Accent Blue
        toggleDashboardButton.setFocusPainted(false);
        toggleDashboardButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleDashboardButton.setAlignmentY(Component.CENTER_ALIGNMENT);
        toggleDashboardButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(96, 165, 250), 1, true),
                new EmptyBorder(4, 12, 4, 12)
        ));
        toggleDashboardButton.addActionListener(e -> {
            boolean isVisible = dashboardWrapper.isVisible();
            dashboardWrapper.setVisible(!isVisible);
            toggleDashboardButton.setText(isVisible ? "Mostra Hardware" : "Nascondi Hardware");
            revalidate();
            repaint();
        });

        // Montaggio ordinato sulla MenuBar
        menuBar.add(iconLabel);
        menuBar.add(Box.createHorizontalStrut(8));
        menuBar.add(title);
        menuBar.add(Box.createHorizontalStrut(10));
        menuBar.add(statusBadgeLabel);

        menuBar.add(Box.createHorizontalGlue()); // Molla elastica sinistra

        menuBar.add(timePill);
        menuBar.add(Box.createHorizontalStrut(10));
        menuBar.add(logPill);

        menuBar.add(Box.createHorizontalGlue()); // Molla elastica destra

        menuBar.add(toggleDashboardButton);
        menuBar.add(Box.createHorizontalStrut(14)); // Spazio prima della X di chiusura

        setJMenuBar(menuBar);

        // ============================================================
        // 2. CORPO CENTRALE: CRUSCOTTO HARDWARE + CONSOLE LOG IDE DARK
        // ============================================================
        JPanel centerPanel = new JPanel(new BorderLayout(0, 0));
        centerPanel.setBackground(PANEL_BG);

        // Dashboard Prestazioni
        dashboardPanel = new PerformanceDashboardPanel();
        dashboardPanel.setMetricsListener((timeText, logText, cpu, ram) -> {
            headerTimeLabel.setText("Tempo: " + timeText);
            headerLogLabel.setText("Log: " + logText);
        });

        dashboardWrapper = new JPanel(new BorderLayout());
        dashboardWrapper.setOpaque(false);
        dashboardWrapper.add(dashboardPanel, BorderLayout.CENTER);
        centerPanel.add(dashboardWrapper, BorderLayout.NORTH);

        // Container Log
        JPanel logContainer = new JPanel(new BorderLayout());
        logContainer.setOpaque(false);
        logContainer.setBorder(new EmptyBorder(4, 14, 10, 14));

        // Toolbar Integrata Stile IDE (Slate 800)
        JPanel logToolbar = new JPanel(new BorderLayout());
        logToolbar.setBackground(CONSOLE_HEADER_BG);
        logToolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1, true),
                new EmptyBorder(5, 12, 5, 12)
        ));

        // Sinistra Toolbar: Icona + Titolo + Badge Righe
        JPanel logTitleBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        logTitleBox.setOpaque(false);
        
        JLabel logIcon = new JLabel(new VectorIcon("terminal", 13, new Color(56, 189, 248))); // Sky Accent
        JLabel logTitle = new JLabel("Console Output & Registro Analisi");
        logTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logTitle.setForeground(Color.WHITE);

        consoleBadge = new JLabel("0 righe");
        consoleBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        consoleBadge.setForeground(new Color(203, 213, 225));
        consoleBadge.setOpaque(true);
        consoleBadge.setBackground(CONSOLE_BADGE_BG);
        consoleBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(71, 85, 105), 1, true),
                new EmptyBorder(1, 6, 1, 6)
        ));

        logTitleBox.add(logIcon);
        logTitleBox.add(logTitle);
        logTitleBox.add(consoleBadge);

        // Destra Toolbar: Azioni Rapide (Copia, Salva, Pulisci)
        JPanel logActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        logActions.setOpaque(false);

        JButton copyBtn = createDarkMiniButton("Copia Log", "report");
        copyBtn.setToolTipText("Copia tutti i log negli appunti");
        copyBtn.addActionListener(e -> {
            String text = logArea.getText();
            if (text != null && !text.isEmpty()) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
                JOptionPane.showMessageDialog(this, "Log copiato negli appunti!", "Copia Riuscita", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton saveBtn = createDarkMiniButton("Salva su File...", "archive");
        saveBtn.setToolTipText("Esporta il registro in un file .txt");
        saveBtn.addActionListener(e -> exportLogToFile());

        JButton clearBtn = createDarkMiniButton("Pulisci", "trash");
        clearBtn.setToolTipText("Svuota la schermata dei log");
        clearBtn.addActionListener(e -> {
            logArea.setText("");
            consoleBadge.setText("0 righe");
        });

        logActions.add(copyBtn);
        logActions.add(saveBtn);
        logActions.add(clearBtn);

        logToolbar.add(logTitleBox, BorderLayout.WEST);
        logToolbar.add(logActions, BorderLayout.EAST);
        logContainer.add(logToolbar, BorderLayout.NORTH);

        // Area di Testo Terminale Monospace Dark
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(CONSOLE_BG);
        logArea.setForeground(CONSOLE_TEXT);
        logArea.setCaretColor(new Color(56, 189, 248));
        logArea.setMargin(new Insets(10, 14, 10, 14));

        DefaultCaret caret = (DefaultCaret) logArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
                BorderFactory.createEmptyBorder()
        ));
        logContainer.add(scroll, BorderLayout.CENTER);

        centerPanel.add(logContainer, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // ==========================================
        // 3. FOOTER PULITO, MODERNO ED ELEGANTE
        // ==========================================
        JPanel footer = new JPanel(new BorderLayout(15, 0));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(10, 18, 10, 18)
        ));

        // Sinistra Footer: Progress Bar Sottile + Etichetta Descrittiva
        JPanel footerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        footerLeft.setOpaque(false);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(false);
        progressBar.putClientProperty("ProgressBar.arc", 8);
        progressBar.setPreferredSize(new Dimension(160, 14));

        footerStatusLabel = new JLabel(BundleManager.getString("dialog.monitor.progress.working"));
        footerStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        footerStatusLabel.setForeground(TEXT_PRIMARY);

        footerLeft.add(progressBar);
        footerLeft.add(footerStatusLabel);

        // Destra Footer: Azioni (Apri Cartella Output, Interrompi, Chiudi)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);

        openOutputButton = new JButton("Apri Cartella Output", new VectorIcon("folder-open", 13, Color.WHITE));
        openOutputButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        openOutputButton.setForeground(Color.WHITE);
        openOutputButton.setBackground(new Color(16, 185, 129)); // Emerald Accent
        openOutputButton.setPreferredSize(new Dimension(175, 32));
        openOutputButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        openOutputButton.setFocusPainted(false);
        openOutputButton.setVisible(false); // Visibile solo al termine
        openOutputButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(5, 150, 105), 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        openOutputButton.addActionListener(e -> openOutputFolder());

        stopButton = new JButton("Interrompi", new VectorIcon("x", 13, new Color(220, 38, 38)));
        stopButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        stopButton.setForeground(new Color(220, 38, 38));
        stopButton.setBackground(new Color(254, 242, 242));
        stopButton.setPreferredSize(new Dimension(115, 32));
        stopButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        stopButton.setFocusPainted(false);
        stopButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(252, 165, 165), 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        stopButton.addActionListener(e -> {
            int res = JOptionPane.showConfirmDialog(this,
                    "Sei sicuro di voler interrompere l'analisi in corso?\nL'operazione non può essere annullata.",
                    "Conferma Interruzione", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (res == JOptionPane.YES_OPTION) {
                stopButton.setEnabled(false);
                statusBadgeLabel.setText("INTERROTTO");
                statusBadgeLabel.setBackground(STATUS_RED);
                footerStatusLabel.setText("Elaborazione interrotta dall'utente.");
                footerStatusLabel.setForeground(STATUS_RED);
                dashboardPanel.setExecutionFinished(false, true);
                if (onStopCallback != null) {
                    onStopCallback.run();
                }
            }
        });

        closeButton = new JButton(BundleManager.getString("dialog.monitor.button.close"));
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeButton.setEnabled(false);
        closeButton.setPreferredSize(new Dimension(100, 32));
        closeButton.addActionListener(e -> {
            dashboardPanel.stopMonitoring();
            dispose();
        });

        buttonPanel.add(openOutputButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(closeButton);

        footer.add(footerLeft, BorderLayout.WEST);
        footer.add(buttonPanel, BorderLayout.EAST);

        add(footer, BorderLayout.SOUTH);

        // Avvia il monitoraggio live
        dashboardPanel.startMonitoring();
    }

    private JPanel createHeaderPill(String iconName, String initialText) {
        JPanel pill = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pill.setOpaque(true);
        pill.setBackground(HEADER_PILL_BG);
        pill.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 80, 160), 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));

        JLabel icon = new JLabel(new VectorIcon(iconName, 12, new Color(147, 197, 253)));
        JLabel label = new JLabel(initialText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(Color.WHITE);

        pill.add(icon);
        pill.add(label);
        return pill;
    }

    private JButton createDarkMiniButton(String text, String iconName) {
        JButton btn = new JButton(text, new VectorIcon(iconName, 11, new Color(203, 213, 225)));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setBackground(new Color(51, 65, 85)); // Slate 700
        btn.setForeground(new Color(241, 245, 249));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(71, 85, 105), 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));
        return btn;
    }

    private void exportLogToFile() {
        String content = logArea.getText();
        if (content == null || content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nessun registro da esportare.", "Avviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Esporta Registro Log IPED");
        chooser.setSelectedFile(new File("IPED_Execution_Log.txt"));
        chooser.setFileFilter(new FileNameExtensionFilter("File di testo (*.txt)", "txt"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File dest = chooser.getSelectedFile();
            if (!dest.getName().toLowerCase().endsWith(".txt")) {
                dest = new File(dest.getAbsolutePath() + ".txt");
            }
            try (FileWriter writer = new FileWriter(dest)) {
                writer.write(content);
                JOptionPane.showMessageDialog(this, "Registro salvato con successo in:\n" + dest.getAbsolutePath(), "Esportazione Riuscita", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Errore durante il salvataggio del file: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openOutputFolder() {
        if (targetOutputPath != null && !targetOutputPath.trim().isEmpty()) {
            File outDir = new File(targetOutputPath);
            if (outDir.exists()) {
                try {
                    Desktop.getDesktop().open(outDir);
                    return;
                } catch (Exception ignored) {
                }
            }
        }
        JOptionPane.showMessageDialog(this, "Percorso cartella di output non trovato o non ancora disponibile.", "Cartella Non Trovata", JOptionPane.WARNING_MESSAGE);
    }

    public void setPaths(String outputPath, String tempPath, boolean isTempRamDisk, int maxJvmGB) {
        this.targetOutputPath = outputPath;
        dashboardPanel.setPaths(outputPath, tempPath, isTempRamDisk, maxJvmGB);
    }

    public void setPaths(String outputPath, String tempPath, boolean isTempRamDisk) {
        setPaths(outputPath, tempPath, isTempRamDisk, 0);
    }

    public void setOnStopCallback(Runnable callback) {
        this.onStopCallback = callback;
    }

    public void appendLog(String text) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(text + "\n");
            int lines = 1;
            for (int i = 0; i < text.length(); i++) {
                if (text.charAt(i) == '\n') lines++;
            }
            dashboardPanel.incrementLogLines(lines);
            if (consoleBadge != null) {
                consoleBadge.setText(logArea.getLineCount() + " righe");
            }
        });
    }

    public void setFinished(boolean success) {
        SwingUtilities.invokeLater(() -> {
            finished = true;
            statusBadgeLabel.setText(success ? "COMPLETATO" : "ERRORE");
            statusBadgeLabel.setBackground(success ? STATUS_GREEN : STATUS_RED);

            dashboardPanel.setExecutionFinished(success, false);
            
            progressBar.setIndeterminate(false);
            progressBar.setValue(100);
            progressBar.setForeground(success ? STATUS_GREEN : STATUS_RED);

            footerStatusLabel.setText(success ? "Elaborazione completata con successo!" : "Elaborazione terminata con errori.");
            footerStatusLabel.setForeground(success ? new Color(5, 150, 105) : STATUS_RED);

            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            closeButton.setEnabled(true);
            closeButton.setBackground(HEADER_NAVY);
            closeButton.setForeground(Color.WHITE);

            if (stopButton != null) {
                stopButton.setVisible(false); // Rimuove il tasto interrompi a fine processo
            }

            if (success && targetOutputPath != null && new File(targetOutputPath).exists()) {
                openOutputButton.setVisible(true); // Mostra il tasto rapido Apri Output
            }

            closeButton.requestFocus();
        });
    }
}
