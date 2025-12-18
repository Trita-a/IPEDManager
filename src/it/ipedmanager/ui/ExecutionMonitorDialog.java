package it.ipedmanager.ui;

import it.ipedmanager.utils.BundleManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;

/**
 * Dialog per monitorare l'esecuzione di IPED in tempo reale.
 */
public class ExecutionMonitorDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private JTextArea logArea;
    private JProgressBar progressBar;
    private JButton closeButton;
    private boolean finished = false;

    public ExecutionMonitorDialog(Window owner) {
        super(owner, BundleManager.getString("dialog.monitor.title"), ModalityType.APPLICATION_MODAL);
        setSize(900, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // Prevent closing while running

        initComponents();
    }

    private void initComponents() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 41, 59)); // Navy
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel(BundleManager.getString("dialog.monitor.header"));
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        title.setIconTextGap(12);
        header.add(title, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);

        // Log Area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(250, 250, 250));
        logArea.setMargin(new Insets(10, 10, 10, 10));

        // Auto-scroll
        DefaultCaret caret = (DefaultCaret) logArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(15, 20, 15, 20));

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setString(BundleManager.getString("dialog.monitor.progress.working"));
        progressBar.setStringPainted(true);

        closeButton = new JButton(BundleManager.getString("dialog.monitor.button.close"));
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeButton.setEnabled(false);
        closeButton.addActionListener(e -> dispose());

        footer.add(progressBar, BorderLayout.CENTER);
        footer.add(closeButton, BorderLayout.EAST);

        add(footer, BorderLayout.SOUTH);
    }

    public void appendLog(String text) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(text + "\n");
        });
    }

    public void setFinished(boolean success) {
        SwingUtilities.invokeLater(() -> {
            finished = true;
            progressBar.setIndeterminate(false);
            progressBar.setValue(100);
            progressBar.setString(success ? BundleManager.getString("dialog.monitor.status.success")
                    : BundleManager.getString("dialog.monitor.status.error"));
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            closeButton.setEnabled(true);

            // Re-enable close button logic
            closeButton.requestFocus();
        });
    }
}
