package vn.edu.lianac.Download;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;

public class DownloadService extends IntentService {

    public static final String ACTION_DOWNLOAD = "vn.edu.lianac.action.DOWNLOAD";
    public static final String EXTRA_URL = "vn.edu.lianac.extra.URL";
    public static final String EXTRA_FILE_NAME = "vn.edu.lianac.extra.FILE_NAME";
    public static final String EXTRA_RECEIVER = "vn.edu.lianac.extra.RECEIVER";

    public static final int UPDATE_CODE = 8344;
    public static final String EXTRA_DOWNLOAD_ID = "vn.edu.lianac.extra.DOWNLOAD_ID";
    public static final String EXTRA_PROGRESS = "vn.edu.lianac.extra.PROGRESS";
    public static final String EXTRA_FILE_PATH = "vn.edu.lianac.extra.FILE_PATH";


    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && ACTION_DOWNLOAD.equals(intent.getAction())) {
            final String url = intent.getStringExtra(EXTRA_URL);
            final String fileName = intent.getStringExtra(EXTRA_FILE_NAME);
            final ResultReceiver receiver = intent.getParcelableExtra(EXTRA_RECEIVER);
            handleActionDownload(url, fileName, receiver);
        }
    }

    private void handleActionDownload(String url, String fileName, ResultReceiver receiver) {
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setTitle(fileName)
                .setDescription("Downloading PDF")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName + ".pdf");

        long downloadId = downloadManager.enqueue(request);
        sendProgressUpdate(receiver, downloadId, 0, DownloadManager.STATUS_PENDING, url, null);

        // Start monitoring thread
        monitorDownload(downloadManager, downloadId, receiver, url);
    }

    private void monitorDownload(DownloadManager downloadManager, long downloadId, ResultReceiver receiver, String url) {
        boolean downloading = true;
        String localFilePath = null;

        while (downloading) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            Cursor cursor = downloadManager.query(query);
            if (cursor.moveToFirst()) {
                int statusColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int bytesDownloadedColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                int bytesTotalColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);

                int status = (statusColumnIndex != -1) ? cursor.getInt(statusColumnIndex) : -1;
                int bytesDownloaded = (bytesDownloadedColumnIndex != -1) ? cursor.getInt(bytesDownloadedColumnIndex) : 0;
                int bytesTotal = (bytesTotalColumnIndex != -1) ? cursor.getInt(bytesTotalColumnIndex) : 0;

                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    int localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                    if (localUriIndex != -1) {
                        localFilePath = cursor.getString(localUriIndex);
                    }
                    downloading = false;
                } else if (status == DownloadManager.STATUS_FAILED) {
                    downloading = false;
                }

                int progress = (bytesTotal > 0) ? (int) ((bytesDownloaded * 100L) / bytesTotal) : 0;
                sendProgressUpdate(receiver, downloadId, progress, status, url, localFilePath);

            } else {
                 sendProgressUpdate(receiver, downloadId, 0, DownloadManager.STATUS_FAILED, url, null);
                 downloading = false;
            }
            cursor.close();

            if (!downloading) {
                 return;
            }

            try {
                Thread.sleep(1000); // Update every second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                downloading = false;
            }
        }
    }

    private void sendProgressUpdate(ResultReceiver receiver, long downloadId, int progress, int status, String url, String filePath) {
        if (receiver != null) {
            Bundle resultData = new Bundle();
            resultData.putLong(EXTRA_DOWNLOAD_ID, downloadId);
            resultData.putInt(EXTRA_PROGRESS, progress);
            resultData.putInt("status", status);
            resultData.putString(EXTRA_URL, url);

            resultData.putString(DownloadService.EXTRA_URL, url);
            receiver.send(UPDATE_CODE, resultData);

            if (filePath != null) {
                resultData.putString(EXTRA_FILE_PATH, filePath);
            }
            receiver.send(UPDATE_CODE, resultData);
        }
    }
}
