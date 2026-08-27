package it.ipedmanager.ui.components;

import it.ipedmanager.service.SystemHardwareService;
import it.ipedmanager.service.SystemHardwareService.DiskMetric;
import it.ipedmanager.service.SystemHardwareService.SystemSnapshot;
import it.ipedmanager.ui.config.VectorIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Dashboard in tempo reale per monitorare le prestazioni del PC (CPU, RAM, Dischi, Attività),
 * con design pulito, moderno, card arrotondate e perfettamente integrato nello stile FlatLaf di IPEDManager.
 */
public class PerformanceDashboardPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // Palette colori coerente con FlatLaf Light & Navy
    private static final Color PANEL_BG = new Color(248, 250, 252);     // Slate 50 (#F8FAFC)
    private static final Color CARD_BG = Color.WHITE;
    private static final Color CARD_BORDER = new Color(226, 232, 240); // Slate 200 (#E2E8F0)
    private static final Color CARD_HEADER_LINE = new Color(241, 245, 249); // Slate 100
    private static final Color TEXT_PRIMARY = new Color(30, 41, 59);    // Slate 800
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139); // Slate 500
    private static final Color PRIMARY_NAVY = new Color(30, 58, 138);   // IPED Navy Blue

    private static final Color COLOR_GREEN = new Color(16, 185, 129);   // Emerald 500
    private static final Color COLOR_AMBER = new Color(245, 158, 11);   // Amber 500
    private static final Color COLOR_RED = new Color(239, 68, 68);      // Red 500
    private static final Color COLOR_BLUE = new Color(2, 132, 199);     // Sky 600

    private final DecimalFormat df1 = new DecimalFormat("0.0");
    private final DecimalFormat df0 = new DecimalFormat("#,##0");

    // Componenti Card 1: CPU
    private JLabel cpuValueLabel;
    private JLabel cpuDetailsLabel;
    private JProgressBar cpuBar;
    private double peakCpu = 0.0;
    private double peakIpedCpu = 0.0;
    private long peakIpedRamBytes = 0;

    // Componenti Card 2: RAM
    private JLabel ramValueLabel;
    private JLabel ramDetailsLabel;
    private JProgressBar ramBar;

    // Componenti Card 3: Disco Output
    private JLabel outputDiskValueLabel;
    private JLabel outputDiskDetailsLabel;
    private JProgressBar outputDiskBar;

    // Componenti Card 4: Disco Temp / RAM Disk
    private JLabel tempDiskTitleLabel;
    private JLabel tempDiskValueLabel;
    private JLabel tempDiskDetailsLabel;
    private JProgressBar tempDiskBar;
    private JLabel ramDiskBadge;

    // Listener per aggiornamenti esterni (per la barra del titolo centrale)
    public interface MetricsListener {
        void onMetricsUpdated(String timeText, String logThroughputText, double cpuPercent, double ramPercent);
    }
    private MetricsListener metricsListener;

    // Dati di runtime
    private String targetOutputPath;
    private String targetTempPath;
    private boolean isTempRamDisk;
    private long startTimeMs = 0;
    private long endTimeMs = 0;
    private boolean isRunning = false;
    private final AtomicInteger totalLogLines = new AtomicInteger(0);
    private int lastLogLineCount = 0;
    private long lastRateCheckMs = 0;

    private Timer refreshTimer;

    public PerformanceDashboardPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(PANEL_BG);
        setBorder(new EmptyBorder(8, 14, 8, 14));

        initComponents();
    }

    public void setMetricsListener(MetricsListener listener) {
        this.metricsListener = listener;
    }

    private void initComponents() {
        // Grid delle 4 Card simmetriche
        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, 12, 0));
        cardsGrid.setOpaque(false);

        cardsGrid.add(createCpuCard());
        cardsGrid.add(createRamCard());
        cardsGrid.add(createOutputDiskCard());
        cardsGrid.add(createTempDiskCard());

        add(cardsGrid, BorderLayout.CENTER);
    }

    private JPanel createCpuCard() {
        JPanel card = createCardContainer();

        JPanel header = createCardHeader("CPU SISTEMA", "chip", COLOR_BLUE);
        cpuValueLabel = new JLabel("0.0 %");
        cpuValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        cpuValueLabel.setForeground(TEXT_PRIMARY);

        cpuBar = createStyledProgressBar();

        cpuDetailsLabel = new JLabel(SystemHardwareService.getInstance().getAvailableProcessors() + " Core • IPED: 0%");
        cpuDetailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cpuDetailsLabel.setForeground(TEXT_SECONDARY);

        card.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(3, 1, 0, 4));
        content.setOpaque(false);
        content.add(cpuValueLabel);
        content.add(cpuBar);
        content.add(cpuDetailsLabel);
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    private JPanel createRamCard() {
        JPanel card = createCardContainer();

        JPanel header = createCardHeader("MEMORIA RAM", "sliders", COLOR_GREEN);
        ramValueLabel = new JLabel("0.0 / 0.0 GB");
        ramValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        ramValueLabel.setForeground(TEXT_PRIMARY);

        ramBar = createStyledProgressBar();

        ramDetailsLabel = new JLabel("Libera: -- GB • 0% usata");
        ramDetailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        ramDetailsLabel.setForeground(TEXT_SECONDARY);

        card.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(3, 1, 0, 4));
        content.setOpaque(false);
        content.add(ramValueLabel);
        content.add(ramBar);
        content.add(ramDetailsLabel);
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    private JPanel createOutputDiskCard() {
        JPanel card = createCardContainer();

        JPanel header = createCardHeader("DISCO OUTPUT", "archive", COLOR_AMBER);
        outputDiskValueLabel = new JLabel("-- GB liberi");
        outputDiskValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        outputDiskValueLabel.setForeground(TEXT_PRIMARY);

        outputDiskBar = createStyledProgressBar();

        outputDiskDetailsLabel = new JLabel("Spazio: -- / -- GB");
        outputDiskDetailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        outputDiskDetailsLabel.setForeground(TEXT_SECONDARY);

        card.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(3, 1, 0, 4));
        content.setOpaque(false);
        content.add(outputDiskValueLabel);
        content.add(outputDiskBar);
        content.add(outputDiskDetailsLabel);
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    private JPanel createTempDiskCard() {
        JPanel card = createCardContainer();

        JPanel headerPanel = new JPanel(new BorderLayout(5, 0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_HEADER_LINE),
                new EmptyBorder(0, 0, 6, 0)
        ));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        titlePanel.setOpaque(false);

        JLabel iconLabel = new JLabel(new VectorIcon("harddrive", 13, COLOR_BLUE));
        tempDiskTitleLabel = new JLabel("CARTELLA TEMP");
        tempDiskTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tempDiskTitleLabel.setForeground(PRIMARY_NAVY);

        titlePanel.add(iconLabel);
        titlePanel.add(tempDiskTitleLabel);
        
        ramDiskBadge = new JLabel(" ⚡ ATTIVA ");
        ramDiskBadge.setFont(new Font("Segoe UI", Font.BOLD, 9));
        ramDiskBadge.setForeground(Color.WHITE);
        ramDiskBadge.setBackground(new Color(16, 185, 129));
        ramDiskBadge.setOpaque(true);
        ramDiskBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(5, 150, 105), 1, true),
                new EmptyBorder(1, 6, 1, 6)
        ));
        ramDiskBadge.setVisible(false);

        headerPanel.add(titlePanel, BorderLayout.CENTER);
        headerPanel.add(ramDiskBadge, BorderLayout.EAST);

        tempDiskValueLabel = new JLabel("-- GB liberi");
        tempDiskValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        tempDiskValueLabel.setForeground(TEXT_PRIMARY);

        tempDiskBar = createStyledProgressBar();

        tempDiskDetailsLabel = new JLabel("Spazio: -- / -- GB");
        tempDiskDetailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tempDiskDetailsLabel.setForeground(TEXT_SECONDARY);

        card.add(headerPanel, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(3, 1, 0, 4));
        content.setOpaque(false);
        content.add(tempDiskValueLabel);
        content.add(tempDiskBar);
        content.add(tempDiskDetailsLabel);
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    private JPanel createCardContainer() {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        return card;
    }

    private JPanel createCardHeader(String title, String iconName, Color accentColor) {
        JPanel p = new JPanel(new BorderLayout(4, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_HEADER_LINE),
                new EmptyBorder(0, 0, 6, 0)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setOpaque(false);

        JLabel iconLabel = new JLabel(new VectorIcon(iconName, 13, accentColor));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(PRIMARY_NAVY);

        left.add(iconLabel);
        left.add(titleLabel);

        p.add(left, BorderLayout.WEST);
        return p;
    }

    private JProgressBar createStyledProgressBar() {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(0);
        bar.setStringPainted(false);
        bar.setPreferredSize(new Dimension(80, 8));
        bar.setBackground(new Color(241, 245, 249));
        bar.setForeground(COLOR_GREEN);
        bar.putClientProperty("ProgressBar.arc", 8);
        bar.setBorder(BorderFactory.createEmptyBorder());
        return bar;
    }

    private Color getThresholdColor(double percent) {
        if (percent >= 85.0) return COLOR_RED;
        if (percent >= 70.0) return COLOR_AMBER;
        return COLOR_GREEN;
    }

    public boolean isRunning() {
        return isRunning;
    }

    private int maxJvmGB = 0;

    public void setPaths(String outputPath, String tempPath, boolean isTempRamDisk, int maxJvmGB) {
        this.targetOutputPath = outputPath;
        this.targetTempPath = tempPath;
        this.isTempRamDisk = isTempRamDisk;
        this.maxJvmGB = maxJvmGB;
        if (ramDiskBadge != null) {
            ramDiskBadge.setVisible(isTempRamDisk);
        }
        if (tempDiskTitleLabel != null) {
            tempDiskTitleLabel.setText(isTempRamDisk ? "RAM DISK TEMP" : "CARTELLA TEMP");
        }
    }

    public void setPaths(String outputPath, String tempPath, boolean isTempRamDisk) {
        setPaths(outputPath, tempPath, isTempRamDisk, 0);
    }

    public void incrementLogLines(int count) {
        totalLogLines.addAndGet(count);
    }

    public void startMonitoring() {
        this.isRunning = true;
        this.startTimeMs = System.currentTimeMillis();
        this.endTimeMs = 0;
        this.lastRateCheckMs = System.currentTimeMillis();
        this.totalLogLines.set(0);
        this.lastLogLineCount = 0;
        this.peakCpu = 0.0;
        this.peakIpedCpu = 0.0;
        this.peakIpedRamBytes = 0;

        if (refreshTimer != null && refreshTimer.isRunning()) {
            refreshTimer.stop();
        }

        refreshTimer = new Timer(1000, e -> updateMetrics());
        refreshTimer.start();
        updateMetrics();
    }

    public void setExecutionFinished(boolean success, boolean aborted) {
        this.isRunning = false;
        this.endTimeMs = System.currentTimeMillis();
        updateMetrics();
    }

    public void stopMonitoring() {
        this.isRunning = false;
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }

    private void updateMetrics() {
        SystemSnapshot snap = SystemHardwareService.getInstance().captureSnapshot(targetOutputPath, targetTempPath, isTempRamDisk);

        // 1. CPU
        double sysCpu = snap.systemCpuLoad;
        double ipedCpu = snap.ipedCpuLoad;
        if (sysCpu > peakCpu) peakCpu = sysCpu;
        if (ipedCpu > peakIpedCpu) peakIpedCpu = ipedCpu;
        
        cpuValueLabel.setText(df1.format(sysCpu) + " %");
        cpuBar.setValue((int) Math.round(sysCpu));
        cpuBar.setForeground(getThresholdColor(sysCpu));
        
        String ipedCpuStr;
        if (isRunning) {
            ipedCpuStr = (ipedCpu > 0.0) ? ("IPED: " + df1.format(ipedCpu) + "% • ") : "IPED: 0.0% • ";
        } else {
            ipedCpuStr = (peakIpedCpu > 0.0) ? ("IPED Max: " + df1.format(peakIpedCpu) + "% • ") : "";
        }
        cpuDetailsLabel.setText(ipedCpuStr + snap.activeCores + " Core • Max: " + df1.format(peakCpu) + "%");

        // 2. RAM
        double usedGB = snap.usedPhysicalMemory / (1024.0 * 1024 * 1024);
        double totalGB = snap.totalPhysicalMemory / (1024.0 * 1024 * 1024);
        double freeGB = snap.freePhysicalMemory / (1024.0 * 1024 * 1024);
        double ramPerc = snap.memoryUsedPercent;

        ramValueLabel.setText(df1.format(usedGB) + " / " + df1.format(totalGB) + " GB");
        ramBar.setValue((int) Math.round(ramPerc));
        ramBar.setForeground(getThresholdColor(ramPerc));
        
        if (snap.ipedRamUsedBytes > peakIpedRamBytes) {
            peakIpedRamBytes = snap.ipedRamUsedBytes;
        }
        
        String ipedRamFormatted;
        long ramToDisplay = isRunning ? snap.ipedRamUsedBytes : peakIpedRamBytes;
        String prefix = isRunning ? "IPED: " : "IPED Max: ";
        
        if (ramToDisplay >= 1024L * 1024L * 1024L) {
            double gb = ramToDisplay / (1024.0 * 1024 * 1024);
            ipedRamFormatted = prefix + df1.format(gb) + " GB • ";
        } else if (ramToDisplay > 0) {
            long mb = ramToDisplay / (1024 * 1024);
            ipedRamFormatted = prefix + mb + " MB • ";
        } else {
            ipedRamFormatted = isRunning ? "IPED: attesa... • " : "";
        }
        
        int jvmLimit = maxJvmGB > 0 ? maxJvmGB : (int) Math.round(snap.jvmMaxMemory / (1024.0 * 1024 * 1024));
        String jvmStr = jvmLimit > 0 ? (" • Max: " + jvmLimit + " GB") : "";
        ramDetailsLabel.setText(ipedRamFormatted + "Libera: " + df1.format(freeGB) + " GB" + jvmStr);

        // 3. Disco Output
        DiskMetric outD = snap.outputDisk;
        if (outD == null || outD.getTotalBytes() <= 0) {
            String dir = targetOutputPath != null ? targetOutputPath : System.getProperty("user.dir", "C:\\");
            File f = new File(dir);
            outD = new DiskMetric(f.getAbsolutePath().substring(0, Math.min(3, f.getAbsolutePath().length())), "Output", f.getTotalSpace(), f.getFreeSpace(), f.getUsableSpace(), false);
        }
        if (outD.getTotalBytes() > 0) {
            outputDiskValueLabel.setText(df1.format(outD.getFreeGB()) + " GB liberi");
            outputDiskBar.setValue((int) Math.round(outD.getUsedPercent()));
            outputDiskBar.setForeground(getThresholdColor(outD.getUsedPercent()));
            outputDiskDetailsLabel.setText(outD.getRootPath() + " (" + df1.format(outD.getUsedPercent()) + "% di " + df1.format(outD.getTotalGB()) + " GB)");
        }

        // 4. Disco Temp / RAM Disk
        DiskMetric tempD = snap.tempDisk;
        if (tempD == null || tempD.getTotalBytes() <= 0) {
            String dir = targetTempPath != null ? targetTempPath : System.getProperty("java.io.tmpdir", "C:\\");
            File f = new File(dir);
            tempD = new DiskMetric(f.getAbsolutePath().substring(0, Math.min(3, f.getAbsolutePath().length())), "Temp", f.getTotalSpace(), f.getFreeSpace(), f.getUsableSpace(), isTempRamDisk);
        }
        if (tempD.getTotalBytes() > 0) {
            tempDiskValueLabel.setText(df1.format(tempD.getFreeGB()) + " GB liberi");
            tempDiskBar.setValue((int) Math.round(tempD.getUsedPercent()));
            tempDiskBar.setForeground(getThresholdColor(tempD.getUsedPercent()));
            String subtitle = isTempRamDisk 
                    ? ("RAM Disk (" + tempD.getRootPath() + ") • " + df1.format(tempD.getUsedPercent()) + "% occupato") 
                    : ("Disco Fisico (" + tempD.getRootPath() + ") • " + df1.format(tempD.getUsedPercent()) + "% occupato");
            tempDiskDetailsLabel.setText(subtitle);
        }

        // 5. Calcolo Timer e Throughput
        String timeStr = "00:00:00";
        if (startTimeMs > 0) {
            long refTime = isRunning ? System.currentTimeMillis() : (endTimeMs > 0 ? endTimeMs : System.currentTimeMillis());
            long elapsedSec = (refTime - startTimeMs) / 1000;
            long h = elapsedSec / 3600;
            long m = (elapsedSec % 3600) / 60;
            long s = elapsedSec % 60;
            timeStr = String.format("%02d:%02d:%02d", h, m, s);
        }

        int currentLines = totalLogLines.get();
        String logStr;
        if (isRunning) {
            long now = System.currentTimeMillis();
            double timeDeltaSec = (now - lastRateCheckMs) / 1000.0;
            int linesDiff = currentLines - lastLogLineCount;
            double rate = timeDeltaSec > 0 ? (linesDiff / timeDeltaSec) : 0;
            logStr = df0.format(currentLines) + " righe (" + df1.format(rate) + " l/s)";
            lastLogLineCount = currentLines;
            lastRateCheckMs = now;
        } else {
            logStr = df0.format(currentLines) + " righe";
        }

        // Notifica il container (ExecutionMonitorDialog) per aggiornare la barra in alto
        if (metricsListener != null) {
            metricsListener.onMetricsUpdated(timeStr, logStr, sysCpu, ramPerc);
        }
    }
}
