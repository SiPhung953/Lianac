package vn.edu.lianac.DownloadState;

public enum DownloadState {
    NOT_DOWNLOADED, // Not yet downloaded
    QUEUED,     // Waiting to start (Grey Progress)
    DOWNLOADING, // Actively progressing (Black Progress)
    COMPLETED,  // Download finished successfully (Green Progress)
    FAILED,     // Download terminated with an error (Red Progress)
    CANCELLED   // Removed by user or finished cancellation
}