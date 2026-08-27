package it.ipedmanager.service;

import it.ipedmanager.model.Evidence;
import it.ipedmanager.Main;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

/**
 * Servizio per eseguire IPED con le evidenze configurate.
 */
public class IpedExecutor {

    private String ipedJarPath;
    private String jrePath;
    private static final String PREF_IPED_PATH = "ipedPath";
    private Process currentProcess;
    private boolean wasAborted = false;


    public IpedExecutor() {
        detectIpedPaths();
    }

    /**
     * Rileva automaticamente i path di IPED e JRE.
     * Cerca prima nel percorso salvato nelle preferenze, poi nella cartella
     * corrente.
     */
    private void detectIpedPaths() {
        File currentDir = new File(System.getProperty("user.dir"));

        // 1. Prima prova a cercare nella cartella corrente
        File ipedJar = new File(currentDir, "iped.jar");
        File jreDir = new File(currentDir, "jre");

        if (ipedJar.exists()) {
            this.ipedJarPath = ipedJar.getAbsolutePath();
        }

        if (jreDir.exists() && jreDir.isDirectory()) {
            File javaExe = new File(jreDir, "bin/java.exe");
            if (javaExe.exists()) {
                this.jrePath = javaExe.getAbsolutePath();
            }
        }

        // 2. Se non trovato, cerca nel percorso IPED salvato nelle preferenze
        if (this.ipedJarPath == null || this.jrePath == null) {
            Preferences prefs = Preferences.userNodeForPackage(Main.class);
            String savedIpedPath = prefs.get(PREF_IPED_PATH, null);

            if (savedIpedPath != null) {
                File savedDir = new File(savedIpedPath);

                // Carica iped.jar dal percorso salvato
                if (this.ipedJarPath == null) {
                    File savedJar = new File(savedDir, "iped.jar");
                    if (savedJar.exists()) {
                        this.ipedJarPath = savedJar.getAbsolutePath();
                    }
                }

                // Carica JRE dal percorso salvato
                if (this.jrePath == null || this.jrePath.equals("java")) {
                    File savedJre = new File(savedDir, "jre/bin/java.exe");
                    if (savedJre.exists()) {
                        this.jrePath = savedJre.getAbsolutePath();
                    }
                }
            }
        }

        // 3. Fallback a java di sistema
        if (this.jrePath == null) {
            this.jrePath = "java";
        }
    }

    public String getIpedVersion() {
        if (ipedJarPath == null || ipedJarPath.isEmpty()) {
            return "Sconosciuta";
        }
        try {
            File releaseNotes = new File(new File(ipedJarPath).getParentFile(), "ReleaseNotes.txt");
            if (releaseNotes.exists()) {
                try (BufferedReader reader = new BufferedReader(new java.io.FileReader(releaseNotes))) {
                    String firstLine = reader.readLine();
                    if (firstLine != null && firstLine.contains("IPED-")) {
                        int idx = firstLine.indexOf("IPED-");
                        return firstLine.substring(idx + 5).trim();
                    }
                }
            }
        } catch (Exception e) {
            // Ignora eventuali eccezioni e ritorna Sconosciuta
        }
        return "Sconosciuta";
    }

    public void setIpedJarPath(String path) {
        this.ipedJarPath = path;
    }

    public String getIpedJarPath() {
        return ipedJarPath;
    }

    public boolean isIpedConfigured() {
        return ipedJarPath != null && new File(ipedJarPath).exists();
    }

    /**
     * Costruisce il comando IPED completo.
     */
    public List<String> buildCommand(List<Evidence> evidences, String outputPath,
            String profile, ProcessingOptions options) {

        List<String> cmd = new ArrayList<>();

        // Java executable
        cmd.add(jrePath);

        // JAR
        cmd.add("-jar");
        cmd.add(ipedJarPath);

        // Memory options (MUST be after jar, as args to IPED wrapper)
        if (options.maxMemoryGB > 0) {
            cmd.add("-Xmx" + options.maxMemoryGB + "G");
        }

        // Evidenze
        for (Evidence ev : evidences) {
            cmd.add("-d");
            cmd.add(ev.getFilePath());

            if (ev.getDname() != null && !ev.getDname().trim().isEmpty()) {
                cmd.add("-dname");
                cmd.add(ev.getDname().trim());
            }

            if (ev.getPassword() != null && !ev.getPassword().trim().isEmpty()) {
                cmd.add("-p");
                cmd.add(ev.getPassword());
            }

            if (ev.getTimezone() != null && !ev.getTimezone().trim().isEmpty()) {
                cmd.add("-tz");
                cmd.add(ev.getTimezone());
            }

            if (ev.getAdditionalCommands() != null && !ev.getAdditionalCommands().trim().isEmpty()) {
                cmd.add(ev.getAdditionalCommands().trim());
            }
        }

        // Output
        cmd.add("-o");
        cmd.add(outputPath);

        // Profile - skip if "Personalizzato" (uses user's custom conf/) or "default"
        if (profile != null && !profile.isEmpty()
                && !profile.equalsIgnoreCase("default")
                && !profile.equalsIgnoreCase("Personalizzato")) {
            cmd.add("-profile");
            cmd.add(profile);
        }

        // Processing options
        if (options.continueProcessing)
            cmd.add("--continue");
        if (options.restart)
            cmd.add("--restart");
        if (options.append)
            cmd.add("--append");
        if (options.nogui)
            cmd.add("--nogui");
        if (options.nolog)
            cmd.add("--nologfile");
        if (options.portable)
            cmd.add("--portable");
        if (options.addOwner)
            cmd.add("--addowner");
        if (options.noPstAttachs)
            cmd.add("--nopstattachs");
        if (options.downloadInternetData)
            cmd.add("--downloadInternetData");

        // Extra params
        if (options.extraParams != null && !options.extraParams.trim().isEmpty()) {
            cmd.add(options.extraParams.trim());
        }

        // Splash message
        if (options.splashMessage != null && !options.splashMessage.trim().isEmpty()) {
            cmd.add("-splash");
            cmd.add(options.splashMessage.trim());
        }

        return cmd;
    }

    /**
     * Restituisce il comando come stringa leggibile.
     */
    public String getCommandString(List<String> cmd) {
        StringBuilder sb = new StringBuilder();
        for (String arg : cmd) {
            if (arg.contains(" ")) {
                sb.append("\"").append(arg).append("\"");
            } else {
                sb.append(arg);
            }
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    /**
     * Esegue IPED con output in tempo reale.
     */
    public int execute(List<String> command, Consumer<String> outputCallback) throws Exception {
        wasAborted = false;
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        pb.directory(new File(ipedJarPath).getParentFile());

        currentProcess = pb.start();

        long pid = -1;
        try {
            Method pidMethod = Process.class.getMethod("pid");
            pid = (Long) pidMethod.invoke(currentProcess);
        } catch (Exception ignored) {
        }
        SystemHardwareService.getInstance().setIpedPid(pid);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(currentProcess.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (outputCallback != null) {
                    outputCallback.accept(line);
                }
            }
        }

        int exitCode = currentProcess.waitFor();
        currentProcess = null;
        SystemHardwareService.getInstance().setIpedPid(-1);
        return exitCode;
    }

    /**
     * Annulla l'esecuzione del processo corrente in modo sicuro.
     */
    public void abortExecution() {
        wasAborted = true;
        if (currentProcess != null && currentProcess.isAlive()) {
            currentProcess.destroy();
        }
    }

    public boolean isAborted() {
        return wasAborted;
    }


    /**
     * Classe per le opzioni di elaborazione.
     */
    public static class ProcessingOptions {
        public boolean continueProcessing = false;
        public boolean restart = false;
        public boolean append = false;
        public boolean nogui = false;
        public boolean nolog = false;
        public boolean portable = false;
        public boolean addOwner = false;
        public boolean noPstAttachs = false;
        public boolean downloadInternetData = false;
        public int maxMemoryGB = 0;
        public String extraParams = "";
        public String splashMessage = "";
    }
}
