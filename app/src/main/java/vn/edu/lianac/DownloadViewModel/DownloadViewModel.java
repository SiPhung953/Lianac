package vn.edu.lianac.DownloadViewModel;

import android.app.Application;
import android.app.DownloadManager;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import vn.edu.lianac.DownloadItem.DownloadItem;
import vn.edu.lianac.DownloadState.DownloadState;

public class DownloadViewModel extends AndroidViewModel {

    private final MutableLiveData<List<DownloadItem>> _downloadList = new MutableLiveData<>(new ArrayList<>());
    public final LiveData<List<DownloadItem>> downloadList = _downloadList;

    private final MutableLiveData<DownloadItem> _startDownloadEvent = new MutableLiveData<>();
    public final LiveData<DownloadItem> startDownloadEvent = _startDownloadEvent;

    private final DownloadManager downloadManager;

    public DownloadViewModel(Application application) {
        super(application);
        _downloadList.setValue(new ArrayList<>());
        downloadManager = application.getSystemService(DownloadManager.class);
    }

    public void addNewDownload(String url) {
        DownloadItem newItem = new DownloadItem(url, "New PDF", DownloadState.NOT_DOWNLOADED);
        List<DownloadItem> currentList = _downloadList.getValue();
        if (currentList != null) {
            ArrayList<DownloadItem> newList = new ArrayList<>(currentList);
            newList.add(newItem);
            _downloadList.setValue(newList);
        }
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

    public void updateDownloadProgress(long downloadId, int progress, int status, String url) {
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
