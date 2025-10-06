package vn.edu.lianac.DownloadItem;

import vn.edu.lianac.DownloadState.DownloadState;

public class DownloadItem {
    private long downloadId;
    private String url;
    private String paperName;
    private String fileSize; // e.g., "1.2 MB"
    private int progressPercentage; // 0 to 100
    private DownloadState state;

    public DownloadItem(String url, String paperName, DownloadState state) {
        this.url = url;
        this.paperName = paperName;
        this.state = state;
        this.fileSize = "";
        this.progressPercentage = 0;
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
}
