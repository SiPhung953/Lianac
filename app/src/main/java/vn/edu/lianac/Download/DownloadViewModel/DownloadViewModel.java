package vn.edu.lianac.Download.DownloadViewModel;

import android.app.Application;
import android.app.DownloadManager;
import android.net.Uri;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import vn.edu.lianac.Download.DownloadItem.DownloadItem;
import vn.edu.lianac.Download.DownloadState.DownloadState;

public class DownloadViewModel extends AndroidViewModel {

    private final MutableLiveData<List<DownloadItem>> _downloadList = new MutableLiveData<>(new ArrayList<>());
    public final LiveData<List<DownloadItem>> downloadList = _downloadList;

    private final MutableLiveData<DownloadItem> _startDownloadEvent = new MutableLiveData<>();
    public final LiveData<DownloadItem> startDownloadEvent = _startDownloadEvent;

    private final DownloadManager downloadManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();


    public DownloadViewModel(Application application) {
        super(application);
        _downloadList.setValue(new ArrayList<>());
        downloadManager = application.getSystemService(DownloadManager.class);
    }

    public void fetchTitleAndAddDownload(String pdfUrl) {
        executor.execute(() -> {
            String paperName = "New PDF"; // Default name
            try {
                // 1. Convert PDF URL to Abstract URL
                String abstractUrlString = pdfUrl.replace("/pdf/", "/abs/");

                // 2. Fetch HTML content
                URL url = new URL(abstractUrlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                StringBuilder content = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        content.append(line);
                    }
                }

                // 3. Parse title from HTML using regex
                Pattern pattern = Pattern.compile("<meta name=\"citation_title\" content=\"(.*?)\" />");
                Matcher matcher = pattern.matcher(content.toString());
                if (matcher.find()) {
                    paperName = matcher.group(1);
                }
                connection.disconnect();

            } catch (Exception e) {
                e.printStackTrace(); // Keep default name on error
            }

            // 4. Add the new item to the list
            DownloadItem newItem = new DownloadItem(pdfUrl, paperName, DownloadState.NOT_DOWNLOADED);
            addDownloadItem(newItem);
        });
    }

    public void handleDownloadAction(DownloadItem item) {
        switch (item.getState()) {
            case NOT_DOWNLOADED:
            case FAILED:
            case CANCELLED: // Add CANCELLED state to allow retrying
                item.setState(DownloadState.QUEUED);
                item.setProgressPercentage(0);
                updateItemInList(item);
                _startDownloadEvent.setValue(item);
                break;
            case DOWNLOADING:
            case QUEUED: // Also allow cancellation from QUEUED state
                if (item.getDownloadId() != 0) {
                    downloadManager.remove(item.getDownloadId());
                }
                item.setState(DownloadState.CANCELLED);
                updateItemInList(item); // Update the item instead of removing it
                break;
            case COMPLETED:
                // The delete button is now separate, so this action does nothing.
                break;
            default:
                break;
        }
    }

    public void onDownloadStarted() {
        _startDownloadEvent.setValue(null);
    }

    public void updateDownloadProgress(long downloadId, int progress, int status, String url, String filePath) {
        List<DownloadItem> currentList = _downloadList.getValue();
        if (currentList == null || url == null) return;

        DownloadItem itemToUpdate = findItemByUrl(currentList, url);

        if (itemToUpdate != null) {
            // Do not update items that have been cancelled by the user
            if (itemToUpdate.getState() == DownloadState.CANCELLED) {
                return;
            }
            
            // Associate downloadId if it's the first update for this item
            if (itemToUpdate.getDownloadId() == 0) {
                itemToUpdate.setDownloadId(downloadId);
            }

            DownloadState newState = itemToUpdate.getState();
            switch (status) {
                case DownloadManager.STATUS_RUNNING:
                    newState = DownloadState.DOWNLOADING;
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    newState = DownloadState.COMPLETED;
                    progress = 100;
                    if (filePath != null) {
                        itemToUpdate.setFilePath(filePath);
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
            itemToUpdate.setProgressPercentage(progress);
            itemToUpdate.setState(newState);
            updateItemInList(itemToUpdate);
        }
    }

    private void updateItemInList(DownloadItem itemToUpdate) {
        List<DownloadItem> currentList = _downloadList.getValue();
        if (currentList != null) {
            ArrayList<DownloadItem> newList = new ArrayList<>(currentList);
            int index = -1;
            for (int i = 0; i < newList.size(); i++) {
                if (Objects.equals(newList.get(i).getUrl(), itemToUpdate.getUrl())) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                newList.set(index, itemToUpdate);
                _downloadList.postValue(newList);
            }
        }
    }

    public void deleteDownload(DownloadItem item) {
        // If the download is in progress, cancel it first.
        if (item.getDownloadId() != 0 && (item.getState() == DownloadState.DOWNLOADING || item.getState() == DownloadState.QUEUED)) {
            downloadManager.remove(item.getDownloadId());
        }

        // If the download is completed and we have a file path, delete the file.
        if (item.getState() == DownloadState.COMPLETED && item.getFilePath() != null) {
            try {
                Uri fileUri = Uri.parse(item.getFilePath());
                File file = new File(fileUri.getPath());
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                // Log or handle the exception if file deletion fails
                e.printStackTrace();
            }
        }

        removeItemFromList(item);
    }

    private void removeItemFromList(DownloadItem itemToRemove) {
        List<DownloadItem> currentList = _downloadList.getValue();
        if (currentList != null) {
            ArrayList<DownloadItem> newList = new ArrayList<>(currentList);
            newList.removeIf(item -> Objects.equals(item.getUrl(), itemToRemove.getUrl()));
            _downloadList.postValue(newList);
        }
    }

    private DownloadItem findItemByUrl(List<DownloadItem> list, String url) {
        for (DownloadItem item : list) {
            if (url.equals(item.getUrl())) {
                return item;
            }
        }
        return null;
    }

    public void addDownloadItem(DownloadItem newItem) {
        List<DownloadItem> currentList = _downloadList.getValue();
        if (currentList != null) {
            ArrayList<DownloadItem> newList = new ArrayList<>(currentList);
            newList.add(newItem);
            _downloadList.postValue(newList);
        }
    }
}
