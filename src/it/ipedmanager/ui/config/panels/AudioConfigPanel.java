package it.ipedmanager.ui.config.panels;

import it.ipedmanager.config.PropertiesConfigFile;
import it.ipedmanager.utils.BundleManager;

import javax.swing.*;
import java.awt.*;

/**
 * Pannello per configurazione Audio Transcription (AudioTranscriptConfig.txt)
 * Layout compatto per opzioni motore e whisper.
 */
public class AudioConfigPanel extends BaseConfigPanel {

    private PropertiesConfigFile config;

    private JComboBox<String> cmbImplementation;
    private JComboBox<String> cmbLanguage;
    private JCheckBox chkSkipKnown;
    private JSpinner spnMinTimeout;
    private JSpinner spnTimeoutPerSec;
    private JSpinner spnMinWordScore;

    // Whisper options
    private JComboBox<String> cmbWhisperModel;
    private JComboBox<String> cmbDevice;
    private JComboBox<String> cmbPrecision;
    private JSpinner spnBatchSize;

    private static final String[] IMPLEMENTATIONS = {
            "vosk", "wav2vec2", "whisper", "remote", "microsoft", "google"
    };

    private static final String[] WHISPER_MODELS = {
            "tiny", "base", "small", "medium", "large", "large-v3"
    };

    private static final String[] DEVICES = { "cpu", "gpu" };
    private static final String[] PRECISIONS = { "float32", "float16", "int8" };
    private static final String[] LANGUAGES = { "auto", "it", "en", "pt", "de", "fr", "es" };

    public AudioConfigPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_COLOR);

        // === IMPLEMENTAZIONE ===
        JPanel implPanel = createSectionPanel(BundleManager.getString("panel.audio.section.engine"));

        cmbImplementation = createStyledComboBox(IMPLEMENTATIONS);
        cmbLanguage = createStyledComboBox(LANGUAGES);

        // Compact Row: Motore + Lingua
        addCompactRow(implPanel, 0,
                createStyledLabel(BundleManager.getString("panel.audio.label.engine")), cmbImplementation,
                createStyledLabel(BundleManager.getString("panel.audio.label.language")), cmbLanguage);

        chkSkipKnown = createStyledCheckBox(BundleManager.getString("panel.audio.check.skipKnown"));
        addFullWidthComponent(implPanel, chkSkipKnown, 1);

        content.add(implPanel);
        content.add(Box.createVerticalStrut(15));

        // === TIMEOUT & VOSK ===
        // Uniamo Timeout e Vosk in una sezione "Parametri Base" per risparmiare spazio
        JPanel basePanel = createSectionPanel(BundleManager.getString("panel.audio.section.base"));

        spnMinTimeout = createStyledSpinner();
        spnMinTimeout.setModel(new SpinnerNumberModel(180, 30, 600, 10));

        spnTimeoutPerSec = createStyledSpinner();
        spnTimeoutPerSec.setModel(new SpinnerNumberModel(3, 1, 30, 1));

        addCompactRow(basePanel, 0,
                createStyledLabel(BundleManager.getString("panel.audio.label.minTime")), spnMinTimeout,
                createStyledLabel(BundleManager.getString("panel.audio.label.timePerSec")), spnTimeoutPerSec);

        spnMinWordScore = createStyledSpinner();
        spnMinWordScore.setModel(new SpinnerNumberModel(0.5, 0.0, 1.0, 0.1));

        // Vosk score on a separate compact row (maybe could share, but safer here)
        // Using a Panel for the single field to align nicely or just use addField
        addField(basePanel, BundleManager.getString("panel.audio.label.voskScore"), spnMinWordScore, 1);

        content.add(basePanel);
        content.add(Box.createVerticalStrut(15));

        // === WHISPER ===
        JPanel whisperPanel = createSectionPanel(BundleManager.getString("panel.audio.section.whisper"));

        cmbWhisperModel = createStyledComboBox(WHISPER_MODELS);
        cmbDevice = createStyledComboBox(DEVICES);

        // Row 1: Model + Device
        addCompactRow(whisperPanel, 0,
                createStyledLabel(BundleManager.getString("panel.audio.label.model")), cmbWhisperModel,
                createStyledLabel(BundleManager.getString("panel.audio.label.device")), cmbDevice);

        cmbPrecision = createStyledComboBox(PRECISIONS);
        spnBatchSize = createStyledSpinner();
        spnBatchSize.setModel(new SpinnerNumberModel(1, 1, 64, 1));

        // Row 2: Precision + Batch
        addCompactRow(whisperPanel, 1,
                createStyledLabel(BundleManager.getString("panel.audio.label.precision")), cmbPrecision,
                createStyledLabel(BundleManager.getString("panel.audio.label.batch")), spnBatchSize);

        content.add(whisperPanel);
        content.add(Box.createVerticalStrut(15));

        // Info
        JComponent infoText = createStyledInfoArea(
                BundleManager.getString("panel.audio.info"));
        // Add to SOUTH to fix sizing and layout issues
        add(infoText, BorderLayout.SOUTH);

        content.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setConfig(PropertiesConfigFile config) {
        this.config = config;
        loadConfig();
    }

    @Override
    public void loadConfig() {
        if (config == null || !config.isLoaded())
            return;

        String impl = config.get("implementation", "vosk");
        cmbImplementation.setSelectedItem(impl);

        cmbLanguage.setSelectedItem(config.get("language", "it"));
        chkSkipKnown.setSelected(config.getBoolean("skipKnownFiles", true));
        spnMinTimeout.setValue(config.getInt("minTimeout", 180));
        spnTimeoutPerSec.setValue(config.getInt("timeoutPerSec", 3));
        try {
            spnMinWordScore.setValue(Double.parseDouble(config.get("minWordScore", "0.5")));
        } catch (NumberFormatException e) {
            spnMinWordScore.setValue(0.5);
        }

        cmbWhisperModel.setSelectedItem(config.get("whisperModel", "medium"));
        cmbDevice.setSelectedItem(config.get("device", "cpu"));
        cmbPrecision.setSelectedItem(config.get("precision", "int8"));
        spnBatchSize.setValue(config.getInt("batchSize", 1));
    }

    @Override
    public void saveConfig() {
        if (config == null)
            return;

        config.set("implementation", (String) cmbImplementation.getSelectedItem());
        config.set("language", (String) cmbLanguage.getSelectedItem());
        config.setBoolean("skipKnownFiles", chkSkipKnown.isSelected());
        config.setInt("minTimeout", (Integer) spnMinTimeout.getValue());
        config.setInt("timeoutPerSec", (Integer) spnTimeoutPerSec.getValue());
        config.set("minWordScore", String.valueOf(spnMinWordScore.getValue()));

        config.set("whisperModel", (String) cmbWhisperModel.getSelectedItem());
        config.set("device", (String) cmbDevice.getSelectedItem());
        config.set("precision", (String) cmbPrecision.getSelectedItem());
        config.setInt("batchSize", (Integer) spnBatchSize.getValue());
    }
}
