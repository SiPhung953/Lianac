package vn.edu.lianac.Download.DownloadItem;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import vn.edu.lianac.Download.DownloadState.DownloadState;

@Entity(tableName = "downloads")
public class DownloadItem {
    @PrimaryKey
    @NonNull
    private String url;

    private long downloadId;
    private String paperName;
    private String fileSize; // e.g., "1.2 MB"
    private int progressPercentage; // 0 to 100
    private DownloadState state;
    private String filePath; // To store the local URI of the downloaded file

    // Constructor for Room
    public DownloadItem(@NonNull String url, String paperName, DownloadState state) {
        this.url = url;
        this.paperName = paperName;
        this.state = state;
        this.fileSize = "";
        this.progressPercentage = 0;
        this.filePath = null; // Initialize as null
    }

    // --- Getters ---
    @NonNull
    public String getUrl() {
        return url;
    }

    public long getDownloadId() {
        return downloadId;
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
    public void setUrl(@NonNull String url) {
        this.url = url;
    }

    public void setDownloadId(long downloadId) {
        this.downloadId = downloadId;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
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
