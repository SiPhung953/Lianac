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
        if (download == null || download.getUrl() == null) {
            return; // Nothing to do
        }
        final String url = download.getUrl();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            DownloadItem freshItem = mDownloadDao.findByUrl(url);

            // Use the fresh item to check the state and file path
            if (freshItem != null && freshItem.getState() == DownloadState.COMPLETED && freshItem.getFilePath() != null && !freshItem.getFilePath().isEmpty()) {
                File file = new File(freshItem.getFilePath());
                if (file.exists()) {
                    file.delete();
                    // Deletes the file from device storage
                }
            }

            // Delete from database using the fresh item
            mDownloadDao.delete(freshItem);

            // Better version using Primary key "url"
            // mDownloadDao.deleteByUrl(url);
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