package it.ipedmanager.ui;

import it.ipedmanager.ui.components.PerformanceDashboardPanel;
import it.ipedmanager.ui.config.VectorIcon;
import it.ipedmanager.utils.BundleManager;

import javax.swing.*;
import java.awt.*;

/**
 * Finestra autonoma per il monitoraggio in tempo reale delle prestazioni hardware del PC,
 * con TitleBar integrata immersiva (FlatLaf).
 */
public class SystemPerformanceDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private static final Color HEADER_NAVY = new Color(30, 58, 138);

    private final PerformanceDashboardPanel dashboard;

    public SystemPerformanceDialog(Window owner) {
        super(owner, BundleManager.getString("mainframe.menu.hardwareMonitor") + " - IPED Manager", ModalityType.MODELESS);
        setSize(920, 280);
        setResizable(true);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // FlatLaf TitleBar Integration
        getRootPane().putClientProperty("JRootPane.titleBarBackground", HEADER_NAVY);
        getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);
        getRootPane().putClientProperty("JRootPane.titleBarButtonIconColor", Color.WHITE);
        getRootPane().putClientProperty("JRootPane.titleBarButtonHoverBackground", new Color(60, 100, 170));
        getRootPane().putClientProperty("JRootPane.titleBarButtonPressedBackground", new Color(80, 120, 190));
        getRootPane().putClientProperty("JRootPane.titleBarShowIcon", false);
        getRootPane().putClientProperty("JRootPane.titleBarShowTitle", false);
        getRootPane().putClientProperty("JRootPane.menuBarEmbedded", true);

        // Header integrato in JMenuBar
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(HEADER_NAVY);
        menuBar.setOpaque(true);
        menuBar.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));

        JPanel titleBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleBox.setOpaque(false);

        JLabel iconLabel = new JLabel(new VectorIcon("activity", 18, Color.WHITE));
        JLabel title = new JLabel("Cruscotto Prestazioni Hardware in Tempo Reale");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(Color.WHITE);

        titleBox.add(iconLabel);
        titleBox.add(title);
        
        // Forza l'espansione per spingere i bottoni della finestra a destra
        titleBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        
        menuBar.add(titleBox);
        setJMenuBar(menuBar);

        // Dashboard
        dashboard = new PerformanceDashboardPanel();
        dashboard.setPaths(null, null, false);
        dashboard.startMonitoring();

        add(dashboard, BorderLayout.CENTER);

        // Cleanup on close
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                dashboard.stopMonitoring();
                dispose();
            }
        });
    }

    public static void showDialog(Window owner) {
        SystemPerformanceDialog dlg = new SystemPerformanceDialog(owner);
        dlg.setVisible(true);
    }
}
