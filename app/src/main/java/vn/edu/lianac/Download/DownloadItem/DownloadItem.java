package vn.edu.lianac.Download.DownloadItem;

import vn.edu.lianac.Download.DownloadState.DownloadState;

public class DownloadItem {
    private long downloadId;
    private String url;
    private String paperName;
    private String fileSize; // e.g., "1.2 MB"
    private int progressPercentage; // 0 to 100
    private DownloadState state;
    private String filePath; // To store the local URI of the downloaded file

    public DownloadItem(String url, String paperName, DownloadState state) {
        this.url = url;
        this.paperName = paperName;
        this.state = state;
        this.fileSize = "";
        this.progressPercentage = 0;
        this.filePath = null; // Initialize as null
    }

    // --- Getters ---
    public long getDownloadId() {
        return downloadId;
    }

    public String getUrl() {
        return url;
    }

    public String getPaperName() {
        return paperName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public int getProgressPercentage() {
        return progressPercentage;
    }

    public DownloadState getState() {
        return state;
    }

    public String getFilePath() {
        return filePath;
    }

    // --- Setters ---
    public void setDownloadId(long downloadId) {
        this.downloadId = downloadId;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public void setProgressPercentage(int progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public void setState(DownloadState state) {
        this.state = state;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
