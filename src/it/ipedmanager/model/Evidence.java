package it.ipedmanager.model;

import java.io.File;

/**
 * Modello per un'evidenza da elaborare con IPED.
 */
public class Evidence {

    private File file;
    private String dname;
    private String additionalCommands;
    private String password;
    private String timezone;
    private long size;
    private String format;
    private boolean isDirectory;

    public Evidence(File file) {
        this.file = file;
        this.dname = file.getName();
        this.additionalCommands = "";
        this.password = "";
        this.timezone = "";
        this.isDirectory = file.isDirectory();
        this.size = calculateSize(file);
        this.format = isDirectory ? "FOLDER" : getFormat(file.getName());
    }

    public Evidence(File file, String dname) {
        this(file);
        this.dname = dname != null ? dname : "";
    }

    /**
     * Calculate size - for files returns file size, for directories calculates
     * total recursively
     */
    private long calculateSize(File f) {
        if (f.isFile()) {
            return f.length();
        } else if (f.isDirectory()) {
            return calculateFolderSize(f);
        }
        return 0;
    }

    private long calculateFolderSize(File folder) {
        long totalSize = 0;
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    totalSize += f.length();
                } else if (f.isDirectory()) {
                    totalSize += calculateFolderSize(f);
                }
            }
        }
        return totalSize;
    }

    private String getFormat(String name) {
        int i = name.lastIndexOf('.');
        if (i > 0) {
            return name.substring(i + 1).toUpperCase();
        }
        return "UNKNOWN";
    }

    public String getSizeReadable() {
        if (size <= 0)
            return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new java.text.DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " "
                + units[digitGroups];
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    // Getters
    public File getFile() {
        return file;
    }

    public String getFilePath() {
        return file.getAbsolutePath();
    }

    public String getFileName() {
        return file.getName();
    }

    public String getDname() {
        return dname;
    }

    public String getAdditionalCommands() {
        return additionalCommands;
    }

    public String getPassword() {
        return password;
    }

    public String getTimezone() {
        return timezone;
    }

    public long getSize() {
        return size;
    }

    public String getFormat() {
        return format;
    }

    // Setters
    public void setFile(File file) {
        this.file = file;
    }

    public void setDname(String dname) {
        this.dname = dname;
    }

    public void setAdditionalCommands(String cmd) {
        this.additionalCommands = cmd;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    @Override
    public String toString() {
        return file.getName();
    }
}
