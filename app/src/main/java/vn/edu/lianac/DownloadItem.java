package vn.edu.lianac;

// Corrected import path
import vn.edu.lianac.DownloadState.DownloadState;

public class DownloadItem {
    private String paperName;
    private String fileSize; // e.g., "1.2 MB"
    private int progressPercentage; // 0 to 100
    private DownloadState state;

    public DownloadItem(String paperName, String fileSize, DownloadState state) {
        this.paperName = paperName;
        this.fileSize = fileSize;
        this.state = state;
        this.progressPercentage = 0;
    }

    // --- Getters ---
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

    // --- Setters (used by ViewModel to update state and progress) ---
    public void setProgressPercentage(int progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public void setState(DownloadState state) {
        this.state = state;
        // Optionally reset progress if state changes to QUEUED/FAILED
        if (state == DownloadState.QUEUED || state == DownloadState.FAILED) {
            this.progressPercentage = (state == DownloadState.QUEUED) ? 0 : this.progressPercentage;
        }
    }
}