package vn.edu.lianac.Download.db;

import android.app.Application;
import android.app.DownloadManager;
import androidx.lifecycle.LiveData;

import java.io.File;
import java.util.List;
import vn.edu.lianac.Download.DownloadItem.DownloadItem;
import vn.edu.lianac.Download.DownloadState.DownloadState;

public class DownloadRepository {
    private final DownloadDao mDownloadDao;
    private final LiveData<List<DownloadItem>> mAllDownloads;

    public DownloadRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mDownloadDao = db.downloadDao();
        mAllDownloads = mDownloadDao.getAllDownloads();
    }

    public LiveData<List<DownloadItem>> getAllDownloads() {
        return mAllDownloads;
    }

    public void insert(DownloadItem download) {
        AppDatabase.databaseWriteExecutor.execute(() -> mDownloadDao.insert(download));
    }

    public void update(DownloadItem download) {
        AppDatabase.databaseWriteExecutor.execute(() -> mDownloadDao.update(download));
    }

    public void delete(DownloadItem download) {
        if (download == null /*|| download.getUrl() == null*/ /*Possibly redundant OR*/) {
            return;
        }
        final String url = download.getUrl();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            DownloadItem freshItem = mDownloadDao.findByUrl(url);

            // Use the fresh item to check the state and file path
            if (freshItem == null) {
                android.util.Log.e("DownloadRepository", "freshItem is NULL for url: " + url);
                return; // Nothing to do lmao
            }

            android.util.Log.d("DownloadRepository", "Delete called for: " + freshItem.getPaperName());
            android.util.Log.d("DownloadRepository", "State: " + freshItem.getState());
            android.util.Log.d("DownloadRepository", "FilePath: " + freshItem.getFilePath());

            if (freshItem.getState() == DownloadState.COMPLETED && freshItem.getFilePath() != null && !freshItem.getFilePath().isEmpty()) {

                // URI to file path converter
                File file = new File(freshItem.getFilePath());
                String filePath = freshItem.getFilePath();

                if (filePath.startsWith("file://")) {
                    try {
                        android.net.Uri uri = android.net.Uri.parse(filePath);
                        file = new File(uri.getPath());
                    } catch (Exception e) {
                        android.util.Log.e("DownloadRepository", "Error parsing file path", e);
                        return;
                    }
                } else {
                    file = new File(filePath);
                }

                android.util.Log.d("DownloadRepository", "File exists: " + file.exists());
                android.util.Log.d("DownloadRepository", "File path: " + file.getAbsolutePath());
                android.util.Log.d("DownloadRepository", "File can write: " + file.canWrite());

                if (file.exists()) {
                    boolean deleted = file.delete();
                    // Deletes the file from device storage
                    // Added boolean condition for logging/toasts
                    if (deleted) {
                        android.util.Log.d("DownloadRepository", "File deleted successfully");
                    } else {
                        android.util.Log.d("DownloadRepository", "File deletion failed");
                    }
                } else {
                    android.util.Log.d("DownloadRepository", "File does not exist");
                }
            } else {
                android.util.Log.d("DownloadRepository", "File deletion skipped");
            }

            // Delete from database using the fresh item
            mDownloadDao.delete(freshItem);

            // Better version using Primary key "url"
            // mDownloadDao.deleteByUrl(url);

            android.util.Log.d("DownloadRepository", "Database record deleted");
        });
    }


    public void updateDownloadProgress(long downloadId, int progress, int status, String url, String filePath) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            DownloadItem item = mDownloadDao.findByUrl(url);
            if (item == null) return;

            if (item.getDownloadId() == 0) {
                item.setDownloadId(downloadId);
            }

            int finalProgress = progress;
            DownloadState newState = item.getState();
            switch (status) {
                case DownloadManager.STATUS_RUNNING:
                    newState = DownloadState.DOWNLOADING;
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    newState = DownloadState.COMPLETED;
                    finalProgress = 100;
                    if (filePath != null) {
                        item.setFilePath(filePath);
                    }
                    break;
                case DownloadManager.STATUS_FAILED:
                    newState = DownloadState.FAILED;
                    break;
                case DownloadManager.STATUS_PAUSED:
                case DownloadManager.STATUS_PENDING:
                    newState = DownloadState.QUEUED;
                    break;
            }

            item.setState(newState);
            item.setProgressPercentage(finalProgress);
            mDownloadDao.update(item);
        });
    }

}