package it.ipedmanager.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.sun.management.OperatingSystemMXBean;

/**
 * Servizio per la lettura delle metriche hardware e di sistema in tempo reale,
 * sia a livello globale del PC che a livello dell'intero albero di processi di IPED (parent + worker child processes).
 */
public class SystemHardwareService {

    private static SystemHardwareService instance;
    private final OperatingSystemMXBean osBean;
    private final int availableProcessors;
    private final String osName;
    private final String osArch;

    private volatile long currentIpedPid = -1;
    private long lastIpedCpuNanos = -1;
    private long lastIpedSampleTimeMs = -1;

    public static class DiskMetric {
        private final String rootPath;
        private final String label;
        private final long totalBytes;
        private final long freeBytes;
        private final long usableBytes;
        private final boolean isRamDisk;

        public DiskMetric(String rootPath, String label, long totalBytes, long freeBytes, long usableBytes, boolean isRamDisk) {
            this.rootPath = rootPath;
            this.label = label;
            this.totalBytes = totalBytes;
            this.freeBytes = freeBytes;
            this.usableBytes = usableBytes;
            this.isRamDisk = isRamDisk;
        }

        public String getRootPath() { return rootPath; }
        public String getLabel() { return label; }
        public long getTotalBytes() { return totalBytes; }
        public long getFreeBytes() { return freeBytes; }
        public long getUsableBytes() { return usableBytes; }
        public long getUsedBytes() { return Math.max(0, totalBytes - freeBytes); }
        public double getUsedPercent() {
            if (totalBytes <= 0) return 0.0;
            return (getUsedBytes() * 100.0) / totalBytes;
        }
        public double getTotalGB() { return totalBytes / (1024.0 * 1024 * 1024); }
        public double getFreeGB() { return freeBytes / (1024.0 * 1024 * 1024); }
        public double getUsedGB() { return getUsedBytes() / (1024.0 * 1024 * 1024); }
        public boolean isRamDisk() { return isRamDisk; }
    }

    public static class SystemSnapshot {
        public double systemCpuLoad = 0.0;       // 0.0 - 100.0% (Totale PC)
        public double ipedCpuLoad = 0.0;         // 0.0 - 100.0% (Consumo esclusivo di IPED e suoi processi worker)
        public double processCpuLoad = 0.0;      // 0.0 - 100.0%
        
        public long totalPhysicalMemory = 0;     // bytes (Totale PC)
        public long freePhysicalMemory = 0;      // bytes (Libera PC)
        public long usedPhysicalMemory = 0;      // bytes (Usata PC)
        public double memoryUsedPercent = 0.0;   // 0.0 - 100.0%
        
        public long ipedRamUsedBytes = 0;        // bytes (RAM fisica usata dall'albero processi IPED)
        
        public long jvmTotalMemory = 0;          // bytes
        public long jvmFreeMemory = 0;           // bytes
        public long jvmMaxMemory = 0;            // bytes
        public long jvmUsedMemory = 0;           // bytes
        
        public int activeCores = 0;
        public DiskMetric outputDisk;
        public DiskMetric tempDisk;
        public List<DiskMetric> allDisks = new ArrayList<>();
    }

    private SystemHardwareService() {
        OperatingSystemMXBean bean = null;
        try {
            java.lang.management.OperatingSystemMXBean baseBean = ManagementFactory.getOperatingSystemMXBean();
            if (baseBean instanceof OperatingSystemMXBean) {
                bean = (OperatingSystemMXBean) baseBean;
            }
        } catch (Exception ignored) {
        }
        this.osBean = bean;
        this.availableProcessors = Runtime.getRuntime().availableProcessors();
        this.osName = System.getProperty("os.name", "Windows");
        this.osArch = System.getProperty("os.arch", "x64");
    }

    public static synchronized SystemHardwareService getInstance() {
        if (instance == null) {
            instance = new SystemHardwareService();
        }
        return instance;
    }

    public int getAvailableProcessors() {
        return availableProcessors;
    }

    public String getOsName() {
        return osName;
    }

    public String getOsArch() {
        return osArch;
    }

    public void setIpedPid(long pid) {
        this.currentIpedPid = pid;
        this.lastIpedCpuNanos = -1;
        this.lastIpedSampleTimeMs = -1;
    }

    public long getIpedPid() {
        return currentIpedPid;
    }

    /**
     * Cattura un'istantanea completa delle metriche hardware correnti.
     */
    public SystemSnapshot captureSnapshot(String outputPath, String tempPath, boolean isTempRamDisk) {
        SystemSnapshot snap = new SystemSnapshot();
        snap.activeCores = availableProcessors;

        // CPU & Physical RAM globale del PC
        if (osBean != null) {
            try {
                double sysCpu = osBean.getSystemCpuLoad();
                snap.systemCpuLoad = Math.max(0.0, Math.min(100.0, sysCpu * 100.0));

                double procCpu = osBean.getProcessCpuLoad();
                snap.processCpuLoad = Math.max(0.0, Math.min(100.0, procCpu * 100.0));

                snap.totalPhysicalMemory = osBean.getTotalPhysicalMemorySize();
                snap.freePhysicalMemory = osBean.getFreePhysicalMemorySize();
                snap.usedPhysicalMemory = Math.max(0, snap.totalPhysicalMemory - snap.freePhysicalMemory);
                if (snap.totalPhysicalMemory > 0) {
                    snap.memoryUsedPercent = (snap.usedPhysicalMemory * 100.0) / snap.totalPhysicalMemory;
                }
            } catch (Exception ignored) {
            }
        }

        // Metriche specifiche dell'intero albero processi IPED (Wrapper launcher + Worker process)
        if (currentIpedPid > 0) {
            snap.ipedCpuLoad = sampleIpedCpu(currentIpedPid);
            snap.ipedRamUsedBytes = sampleIpedRam(currentIpedPid);
        }

        // JVM Memory locale
        Runtime rt = Runtime.getRuntime();
        snap.jvmTotalMemory = rt.totalMemory();
        snap.jvmFreeMemory = rt.freeMemory();
        snap.jvmMaxMemory = rt.maxMemory();
        snap.jvmUsedMemory = snap.jvmTotalMemory - snap.jvmFreeMemory;

        // Dischi
        if (outputPath != null && !outputPath.trim().isEmpty()) {
            snap.outputDisk = inspectDisk(outputPath, "Destinazione Report", false);
        }

        if (tempPath != null && !tempPath.trim().isEmpty()) {
            snap.tempDisk = inspectDisk(tempPath, isTempRamDisk ? "RAM Disk Temp" : "Temp", isTempRamDisk);
        } else {
            String defaultTmp = System.getProperty("java.io.tmpdir", "C:\\Temp");
            snap.tempDisk = inspectDisk(defaultTmp, "Temp di Sistema", false);
        }

        File[] roots = File.listRoots();
        if (roots != null) {
            for (File root : roots) {
                if (root.exists()) {
                    long total = root.getTotalSpace();
                    long free = root.getFreeSpace();
                    long usable = root.getUsableSpace();
                    if (total > 0) {
                        snap.allDisks.add(new DiskMetric(root.getAbsolutePath(), root.getAbsolutePath(), total, free, usable, false));
                    }
                }
            }
        }

        return snap;
    }

    private List<Long> collectTreePids(long rootPid) {
        List<Long> pids = new ArrayList<>();
        if (rootPid <= 0) return pids;
        pids.add(rootPid);
        try {
            Class<?> phClass = Class.forName("java.lang.ProcessHandle");
            Method ofMethod = phClass.getMethod("of", long.class);
            Method descendantsMethod = phClass.getMethod("descendants");
            Method pidMethod = phClass.getMethod("pid");

            Object optPh = ofMethod.invoke(null, rootPid);
            Method isPresentMethod = optPh.getClass().getMethod("isPresent");
            if ((Boolean) isPresentMethod.invoke(optPh)) {
                Method getMethod = optPh.getClass().getMethod("get");
                Object ph = getMethod.invoke(optPh);
                Stream<?> stream = (Stream<?>) descendantsMethod.invoke(ph);
                List<?> allHandles = stream.collect(Collectors.toList());
                for (Object childPh : allHandles) {
                    pids.add((Long) pidMethod.invoke(childPh));
                }
            }
        } catch (Exception ignored) {
        }
        return pids;
    }

    private double sampleIpedCpu(long pid) {
        if (pid <= 0) return 0.0;
        try {
            Class<?> phClass = Class.forName("java.lang.ProcessHandle");
            Class<?> infoClass = Class.forName("java.lang.ProcessHandle$Info");
            Method ofMethod = phClass.getMethod("of", long.class);
            Method infoMethod = phClass.getMethod("info");
            Method totalCpuMethod = infoClass.getMethod("totalCpuDuration");
            Method descendantsMethod = phClass.getMethod("descendants");

            Object optPh = ofMethod.invoke(null, pid);
            Method isPresentMethod = optPh.getClass().getMethod("isPresent");
            if ((Boolean) isPresentMethod.invoke(optPh)) {
                Method getMethod = optPh.getClass().getMethod("get");
                Object ph = getMethod.invoke(optPh);

                long currentCpuNanos = 0;

                // Parent process CPU
                Object info = infoMethod.invoke(ph);
                Object optDuration = totalCpuMethod.invoke(info);
                if ((Boolean) isPresentMethod.invoke(optDuration)) {
                    Object duration = getMethod.invoke(optDuration);
                    Method toNanosMethod = duration.getClass().getMethod("toNanos");
                    currentCpuNanos += (Long) toNanosMethod.invoke(duration);
                }

                // Child process / Descendants CPU (il processo reale IPED)
                Stream<?> stream = (Stream<?>) descendantsMethod.invoke(ph);
                List<?> allHandles = stream.collect(Collectors.toList());
                for (Object childPh : allHandles) {
                    Object childInfo = infoMethod.invoke(childPh);
                    Object childOptDuration = totalCpuMethod.invoke(childInfo);
                    if ((Boolean) isPresentMethod.invoke(childOptDuration)) {
                        Object childDuration = getMethod.invoke(childOptDuration);
                        Method toNanosMethod = childDuration.getClass().getMethod("toNanos");
                        currentCpuNanos += (Long) toNanosMethod.invoke(childDuration);
                    }
                }

                long now = System.currentTimeMillis();

                if (lastIpedCpuNanos > 0 && lastIpedSampleTimeMs > 0) {
                    long nanosDiff = currentCpuNanos - lastIpedCpuNanos;
                    long timeMsDiff = now - lastIpedSampleTimeMs;
                    if (timeMsDiff > 0 && nanosDiff >= 0) {
                        double timeNanos = timeMsDiff * 1_000_000.0 * Math.max(1, availableProcessors);
                        double cpuPerc = (nanosDiff / timeNanos) * 100.0;
                        lastIpedCpuNanos = currentCpuNanos;
                        lastIpedSampleTimeMs = now;
                        return Math.max(0.0, Math.min(100.0, cpuPerc));
                    }
                }
                lastIpedCpuNanos = currentCpuNanos;
                lastIpedSampleTimeMs = now;
            }
        } catch (Exception ignored) {
        }
        return 0.0;
    }

    private long sampleIpedRam(long pid) {
        if (pid <= 0) return 0;
        long totalRam = 0;
        List<Long> pids = collectTreePids(pid);
        for (long p : pids) {
            totalRam += sampleSinglePidRam(p);
        }
        return totalRam;
    }

    private long sampleSinglePidRam(long pid) {
        if (pid <= 0) return 0;
        try {
            ProcessBuilder pb = new ProcessBuilder("tasklist", "/FI", "PID eq " + pid, "/FO", "CSV", "/NH");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = r.readLine()) != null) {
                    if (line.contains(",")) {
                        String[] parts = line.split("\",\"");
                        if (parts.length >= 5) {
                            String memStr = parts[4].replace("\"", "").replace("K", "").replace("k", "").replace(".", "").replace(",", "").replace("\u00a0", "").trim();
                            return Long.parseLong(memStr) * 1024L;
                        }
                    }
                }
            }
            p.waitFor();
        } catch (Exception ignored) {
        }
        return 0;
    }

    private DiskMetric inspectDisk(String pathStr, String label, boolean isRamDisk) {
        try {
            Path p = Paths.get(pathStr);
            File f = p.toFile();
            while (f != null && !f.exists()) {
                f = f.getParentFile();
            }
            if (f == null) {
                f = new File(pathStr);
            }
            long total = f.getTotalSpace();
            long free = f.getFreeSpace();
            long usable = f.getUsableSpace();
            String rootName = f.getAbsolutePath();
            if (rootName.length() > 3) {
                File root = new File(rootName.substring(0, 3));
                if (root.exists()) {
                    rootName = root.getAbsolutePath();
                }
            }
            return new DiskMetric(rootName, label, total, free, usable, isRamDisk);
        } catch (Exception e) {
            return new DiskMetric(pathStr, label, 0, 0, 0, isRamDisk);
        }
    }
}
