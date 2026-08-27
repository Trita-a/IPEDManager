package it.ipedmanager.service;

import it.ipedmanager.Main;
import java.util.prefs.Preferences;

public class RamDiskService {
    private static RamDiskService instance;
    private final Preferences prefs;
    private String lastAssignedLetter = null;

    private static final String PREF_ENGINE = "ramdisk.engine";
    private static final String PREF_OSF_PATH = "ramdisk.osf.path";
    private static final String PREF_ARSENAL_PATH = "ramdisk.arsenal.path";
    private static final String PREF_SIZE = "ramdisk.size";
    private static final String PREF_LETTER = "ramdisk.letter";
    private static final String PREF_AUTO = "ramdisk.auto";

    public static final String ENGINE_OSF = "OSFMount";
    public static final String ENGINE_ARSENAL = "Arsenal";

    private RamDiskService() {
        prefs = Preferences.userNodeForPackage(Main.class);
    }

    public static RamDiskService getInstance() {
        if (instance == null) {
            instance = new RamDiskService();
        }
        return instance;
    }

    public String getEngine() { return prefs.get(PREF_ENGINE, ENGINE_OSF); }
    public void setEngine(String engine) { prefs.put(PREF_ENGINE, engine); }

    public String getOsfPath() { return prefs.get(PREF_OSF_PATH, "C:\\Program Files\\OSFMount\\osfmount.com"); }
    public void setOsfPath(String path) { prefs.put(PREF_OSF_PATH, path); }

    public String getArsenalPath() { return prefs.get(PREF_ARSENAL_PATH, "C:\\Arsenal\\aim_cli.exe"); }
    public void setArsenalPath(String path) { prefs.put(PREF_ARSENAL_PATH, path); }

    public int getSizeGb() { return prefs.getInt(PREF_SIZE, 32); }
    public void setSizeGb(int size) { prefs.putInt(PREF_SIZE, size); }

    public String getDriveLetter() { return prefs.get(PREF_LETTER, "X:"); }
    public void setDriveLetter(String letter) { prefs.put(PREF_LETTER, letter); }

    public boolean isAutoMode() { return prefs.getBoolean(PREF_AUTO, false); }
    public void setAutoMode(boolean auto) { prefs.putBoolean(PREF_AUTO, auto); }

    public String getLastAssignedLetter() { return lastAssignedLetter != null ? lastAssignedLetter : getDriveLetter(); }

    public boolean mount() {
        String engine = getEngine();
        try {
            ProcessBuilder pb;
            if (ENGINE_OSF.equals(engine)) {
                pb = new ProcessBuilder(
                    getOsfPath(), "-a", "-t", "vm", "-m", getDriveLetter(), 
                    "-o", "format:NTFS:\"RAM Disk\"", "-s", getSizeGb() + "G"
                );
            } else {
                // Arsenal
                pb = new ProcessBuilder(
                    getArsenalPath(), "--ramdisk", "--disksize=" + getSizeGb() + "GB"
                );
            }
            pb.redirectErrorStream(true);
            Process p = pb.start();
            p.waitFor();
            return p.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean unmount() {
        String engine = getEngine();
        if (ENGINE_ARSENAL.equals(engine)) {
            System.err.println("Unmount for Arsenal not easily supported via CLI without device ID.");
            return false;
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(
                getOsfPath(), "-D", "-m", getDriveLetter()
            );
            pb.redirectErrorStream(true);
            Process p = pb.start();
            p.waitFor();
            return p.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
