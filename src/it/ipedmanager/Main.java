package it.ipedmanager;

import com.formdev.flatlaf.FlatLightLaf;
import it.ipedmanager.ui.MainFrame;
import javax.swing.*;
import java.awt.Color;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;

/**
 * IPEDManager - GUI per elaborazione evidenze con IPED
 * 
 * @author William Tritapepe
 * @version 2.0.0
 */
public class Main {

    public static final String VERSION = "1.0.0"; // Initial Release
    public static final String AUTHOR = "William Tritapepe";
    private static final String PREF_IPED_PATH = "ipedPath";

    // Messaggi multilingue (IT / EN)
    private static final boolean IS_ITALIAN = Locale.getDefault().getLanguage().equals("it");

    // Titoli
    private static final String TITLE_JRE_CONFIG = IS_ITALIAN
            ? "IPEDManager - Configurazione JRE"
            : "IPEDManager - JRE Configuration";
    private static final String TITLE_ERROR = IS_ITALIAN ? "Errore" : "Error";

    // Messaggi dialogo principale
    private static final String MSG_NOT_IN_IPED = IS_ITALIAN
            ? "<html><div style='width: 400px; font-size: 11px;'>" +
                    "<b style='font-size: 12px;'>IPEDManager non è stato avviato dalla cartella IPED</b><br><br>" +
                    "Per una corretta visualizzazione dell'interfaccia, è necessario indicare " +
                    "la cartella IPED per utilizzare la JRE inclusa (Java 11).<br><br>" +
                    "<i>In alternativa, puoi copiare questo file EXE direttamente nella cartella IPED.</i><br><br>" +
                    "<b>Vuoi selezionare la cartella IPED ora?</b></div></html>"
            : "<html><div style='width: 400px; font-size: 11px;'>" +
                    "<b style='font-size: 12px;'>IPEDManager was not launched from the IPED folder</b><br><br>" +
                    "For correct interface display, you need to specify the IPED folder " +
                    "to use the included JRE (Java 11).<br><br>" +
                    "<i>Alternatively, you can copy this EXE file directly into the IPED folder.</i><br><br>" +
                    "<b>Do you want to select the IPED folder now?</b></div></html>";

    // File chooser
    private static final String CHOOSER_TITLE = IS_ITALIAN
            ? "Seleziona la cartella IPED (es. iped-4.2.2)"
            : "Select the IPED folder (e.g. iped-4.2.2)";

    // Errore JRE non trovata
    private static final String MSG_JRE_NOT_FOUND = IS_ITALIAN
            ? "<html>La cartella selezionata non contiene una JRE valida.<br>" +
                    "Assicurati di selezionare la cartella IPED principale<br>" +
                    "(es. <b>iped-4.2.2</b>) che contiene la sottocartella <b>jre</b>.</html>"
            : "<html>The selected folder does not contain a valid JRE.<br>" +
                    "Make sure to select the main IPED folder<br>" +
                    "(e.g. <b>iped-4.2.2</b>) that contains the <b>jre</b> subfolder.</html>";

    // Errore riavvio
    private static final String MSG_RESTART_ERROR = IS_ITALIAN
            ? "Errore durante il riavvio: "
            : "Error during restart: ";

    public static void main(String[] args) {
        // Controlla se dobbiamo riavviarci con la JRE corretta
        if (shouldRestartWithCorrectJre()) {
            return; // L'app si riavvierà con la JRE corretta
        }

        // Configura Look and Feel moderno - IntelliJ theme
        try {
            JFrame.setDefaultLookAndFeelDecorated(true);
            UIManager.put("Button.arc", 10); // Standard rounded
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 8);

            // Global Accent Color (Navy Blue)
            Color navy = new Color(30, 58, 138);
            UIManager.put("Component.accentColor", navy);
            UIManager.put("Component.focusColor", navy);
            UIManager.put("CheckBox.icon.selectedBackground", navy);
            UIManager.put("CheckBox.icon.selectedBorderColor", navy);
            UIManager.put("CheckBox.icon.focusedBackground", navy);
            UIManager.put("CheckBox.icon.hoverBorderColor", navy);
            UIManager.put("CheckBox.icon.checkmarkColor", Color.WHITE);

            // Immersive Header (Navy Blue TitleBar & MenuBar)
            UIManager.put("TitlePane.background", navy);
            UIManager.put("TitlePane.foreground", Color.WHITE);
            UIManager.put("TitlePane.unifiedBackground", true);
            UIManager.put("MenuBar.background", navy);
            UIManager.put("MenuBar.foreground", Color.WHITE);
            UIManager.put("MenuBar.bordered", false); // Seamless look

            FlatLightLaf.setup();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Avvia UI nel thread EDT
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }

    /**
     * Controlla se stiamo usando Java 8 (che ha problemi di DPI) e se non siamo
     * nella cartella IPED. Se sì, chiede all'utente la cartella IPED e riavvia
     * l'applicazione con la JRE di IPED.
     * 
     * @return true se l'app deve riavviarsi, false se può continuare normalmente
     */
    private static boolean shouldRestartWithCorrectJre() {
        String javaVersion = System.getProperty("java.version");

        // Se stiamo già usando Java 11+, tutto ok
        if (!javaVersion.startsWith("1.8")) {
            return false;
        }

        // Verifica se esiste una JRE locale (siamo nella cartella IPED)
        File currentDir = new File(System.getProperty("user.dir"));
        File localJre = new File(currentDir, "jre/bin/java.exe");
        if (localJre.exists()) {
            return false; // Siamo nella cartella IPED, tutto ok
        }

        // Stiamo usando Java 8 e non siamo nella cartella IPED
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // Prova a recuperare il percorso salvato
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        String savedPath = prefs.get(PREF_IPED_PATH, null);

        File ipedDir = null;

        // Verifica se il percorso salvato è ancora valido
        if (savedPath != null) {
            File savedDir = new File(savedPath);
            File savedJre = new File(savedDir, "jre/bin/java.exe");
            if (savedJre.exists()) {
                ipedDir = savedDir;
            }
        }

        // Se non abbiamo un percorso valido, chiedi all'utente
        if (ipedDir == null) {
            int choice = JOptionPane.showConfirmDialog(null,
                    MSG_NOT_IN_IPED,
                    TITLE_JRE_CONFIG,
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (choice != JOptionPane.YES_OPTION) {
                return false;
            }

            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle(CHOOSER_TITLE);
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                ipedDir = fc.getSelectedFile();

                // Verifica che contenga la JRE
                File selectedJre = new File(ipedDir, "jre/bin/java.exe");
                if (!selectedJre.exists()) {
                    JOptionPane.showMessageDialog(null,
                            MSG_JRE_NOT_FOUND,
                            TITLE_ERROR,
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                // Salva il percorso per le prossime volte
                prefs.put(PREF_IPED_PATH, ipedDir.getAbsolutePath());
            } else {
                return false;
            }
        }

        // Riavvia con la JRE di IPED
        try {
            restartWithJre(ipedDir);
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    MSG_RESTART_ERROR + e.getMessage(),
                    TITLE_ERROR,
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Riavvia l'applicazione usando la JRE della cartella IPED specificata.
     */
    private static void restartWithJre(File ipedDir) throws Exception {
        File javaExe = new File(ipedDir, "jre/bin/javaw.exe");

        // Trova il JAR o la classe corrente
        String jarPath = Main.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI().getPath();

        // Su Windows, rimuovi lo slash iniziale se presente
        if (jarPath.startsWith("/") && jarPath.contains(":")) {
            jarPath = jarPath.substring(1);
        }

        List<String> cmd = new ArrayList<>();
        cmd.add(javaExe.getAbsolutePath());
        cmd.add("-jar");
        cmd.add(jarPath);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(new File(jarPath).getParentFile());

        // Imposta variabili d'ambiente
        pb.environment().put("IPED_HOME", ipedDir.getAbsolutePath());

        pb.start();

        // Termina questa istanza
        System.exit(0);
    }
}
