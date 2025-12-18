package it.ipedmanager.ui;

import it.ipedmanager.config.ConfigManager;
import it.ipedmanager.ui.config.VectorIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Dialog per la gestione dei profili IPED.
 * Permette di salvare, eliminare e rinominare profili.
 */
public class ProfileManagerDialog extends JDialog {

    private static final Color HEADER_BG = new Color(30, 58, 138); // Navy Blue (same as MainFrame)
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);

    private JList<String> profileList;
    private DefaultListModel<String> listModel;
    private Path profilesDir;
    private Runnable onProfilesChanged;

    public ProfileManagerDialog(Frame owner, Path profilesDir, Runnable onProfilesChanged) {
        super(owner, "Gestione Profili", true);
        this.profilesDir = profilesDir;
        this.onProfilesChanged = onProfilesChanged;

        setSize(500, 400);
        setLocationRelativeTo(owner);
        initComponents();
        loadProfiles();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);
        setTitle(""); // Hide default title in center

        // FlatLaf Title Bar Integration (same as ConfigManagerDialog)
        getRootPane().putClientProperty("JRootPane.titleBarBackground", HEADER_BG);
        getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);
        getRootPane().putClientProperty("JRootPane.titleBarShowTitle", false);

        // Embed logo+title in title bar
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(HEADER_BG);
        menuBar.setOpaque(true);
        menuBar.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        JLabel titleLabel = new JLabel("Gestione Profili");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);

        menuBar.add(titleLabel);

        getRootPane().putClientProperty("JRootPane.menuBarEmbedded", true);
        setJMenuBar(menuBar);

        // Profile List
        listModel = new DefaultListModel<>();
        profileList = new JList<>(listModel);
        profileList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        profileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        profileList.setCellRenderer(new ProfileCellRenderer());

        JScrollPane scroll = new JScrollPane(profileList);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scroll, BorderLayout.CENTER);

        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(5, 10, 10, 10));

        JButton btnSave = createButton("Salva Corrente", "plus");
        JButton btnRename = createButton("Rinomina", "pencil");
        JButton btnDelete = createButton("Elimina", "trash");
        JButton btnClose = createButton("Chiudi", null);

        btnSave.addActionListener(e -> saveCurrentProfile());
        btnRename.addActionListener(e -> renameProfile());
        btnDelete.addActionListener(e -> deleteProfile());
        btnClose.addActionListener(e -> dispose());

        btnPanel.add(btnSave);
        btnPanel.add(btnRename);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClose);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private JButton createButton(String text, String iconName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        if (iconName != null) {
            btn.setIcon(new VectorIcon(iconName, 14, TEXT_PRIMARY));
        }
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadProfiles() {
        listModel.clear();
        if (Files.isDirectory(profilesDir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(profilesDir)) {
                List<String> names = new ArrayList<>();
                for (Path entry : stream) {
                    if (Files.isDirectory(entry)) {
                        names.add(entry.getFileName().toString());
                    }
                }
                names.sort(String::compareToIgnoreCase);
                for (String name : names) {
                    listModel.addElement(name);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveCurrentProfile() {
        String name = DialogHelper.showInput(this, "Salva Profilo", "Nome del nuovo profilo:");

        if (name == null || name.trim().isEmpty())
            return;
        name = name.trim().replaceAll("[\\\\/:*?\"<>|]", "_"); // Sanitize

        Path newProfileDir = profilesDir.resolve(name);
        if (Files.exists(newProfileDir)) {
            boolean confirm = DialogHelper.showConfirm(this, "Conferma",
                    "Il profilo '" + name + "' esiste già. Sovrascrivere?", "alert");
            if (!confirm)
                return;
        }

        try {
            ConfigManager cm = ConfigManager.getInstance();
            Path confPath = cm.getConfPath();
            Path ipedConfig = cm.getIpedPath().resolve("IPEDConfig.txt");

            // Create profile directory
            Files.createDirectories(newProfileDir);

            // Copy IPEDConfig.txt
            if (Files.exists(ipedConfig)) {
                Files.copy(ipedConfig, newProfileDir.resolve("IPEDConfig.txt"),
                        StandardCopyOption.REPLACE_EXISTING);
            }

            // Copy conf/ directory
            Path newConfDir = newProfileDir.resolve("conf");
            Files.createDirectories(newConfDir);
            Files.walk(confPath).forEach(source -> {
                try {
                    Path dest = newConfDir.resolve(confPath.relativize(source));
                    if (Files.isDirectory(source)) {
                        Files.createDirectories(dest);
                    } else {
                        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            loadProfiles();
            if (onProfilesChanged != null)
                onProfilesChanged.run();
            DialogHelper.showSuccess(this, "Successo", "Profilo '" + name + "' salvato!");

        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.showError(this, "Errore", "Errore: " + e.getMessage());
        }
    }

    private void renameProfile() {
        String selected = profileList.getSelectedValue();
        if (selected == null) {
            DialogHelper.showWarning(this, "Attenzione", "Seleziona un profilo da rinominare.");
            return;
        }

        String newName = DialogHelper.showInput(this, "Rinomina Profilo",
                "Nuovo nome per '" + selected + "':", selected);

        if (newName == null || newName.trim().isEmpty() || newName.equals(selected))
            return;
        newName = newName.trim().replaceAll("[\\\\/:*?\"<>|]", "_");

        boolean confirm = DialogHelper.showConfirm(this, "Conferma Rinomina",
                "Rinominare '" + selected + "' in '" + newName + "'?");
        if (!confirm)
            return;

        try {
            Path oldPath = profilesDir.resolve(selected);
            Path newPath = profilesDir.resolve(newName);
            Files.move(oldPath, newPath);
            loadProfiles();
            if (onProfilesChanged != null)
                onProfilesChanged.run();
        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.showError(this, "Errore", "Errore: " + e.getMessage());
        }
    }

    private void deleteProfile() {
        String selected = profileList.getSelectedValue();
        if (selected == null) {
            DialogHelper.showWarning(this, "Attenzione", "Seleziona un profilo da eliminare.");
            return;
        }

        boolean confirm = DialogHelper.showConfirm(this, "Conferma Eliminazione",
                "Sei sicuro di voler eliminare il profilo '" + selected + "'?\n" +
                        "Questa operazione non può essere annullata.",
                "alert");

        if (!confirm)
            return;

        try {
            Path profilePath = profilesDir.resolve(selected);
            // Delete directory recursively
            Files.walk(profilePath)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            loadProfiles();
            if (onProfilesChanged != null)
                onProfilesChanged.run();
        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.showError(this, "Errore", "Errore: " + e.getMessage());
        }
    }

    // Custom cell renderer for list items
    private static class ProfileCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            label.setIcon(new VectorIcon("folder", 16, isSelected ? Color.WHITE : TEXT_PRIMARY));
            label.setBorder(new EmptyBorder(8, 10, 8, 10));
            return label;
        }
    }

    public static void showDialog(Frame owner, Path profilesDir, Runnable onProfilesChanged) {
        new ProfileManagerDialog(owner, profilesDir, onProfilesChanged).setVisible(true);
    }
}
