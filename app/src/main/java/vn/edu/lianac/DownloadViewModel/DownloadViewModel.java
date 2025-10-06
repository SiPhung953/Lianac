package vn.edu.lianac.DownloadViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.os.Handler;
import android.os.Looper;

// Corrected import paths
import vn.edu.lianac.DownloadItem;
import vn.edu.lianac.DownloadState.DownloadState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadViewModel extends ViewModel {

    private final MutableLiveData<List<DownloadItem>> _downloadList = new MutableLiveData<>();
    public LiveData<List<DownloadItem>> downloadList = _downloadList;

    private final ExecutorService downloadExecutor = Executors.newFixedThreadPool(2);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public DownloadViewModel() {
        // Initial data setup (mocking the entries from the PDF)
        List<DownloadItem> initialList = new ArrayList<>();
        initialList.add(new DownloadItem("Explainable AI-Enhanced Supervisory Control for High-Precision Spacecraft Formation", "2.1 MB", DownloadState.QUEUED));
        initialList.add(new DownloadItem("Bayesian Anomaly Detection for Ia Cosmology: Automating SALT3 Data Curation", "1.5 MB", DownloadState.COMPLETED));
        initialList.add(new DownloadItem("Random paper #3", "0.8 MB", DownloadState.QUEUED));
        initialList.add(new DownloadItem("Random paper #4", "3.0 MB", DownloadState.FAILED));

        // Start one download automatically for demonstration
        DownloadItem startingItem = new DownloadItem("Starting item", "1.1 MB", DownloadState.QUEUED);
        initialList.add(startingItem);
        _downloadList.setValue(initialList);
        startDownload(startingItem);
    }

    public void handleDownloadAction(DownloadItem item, DownloadState currentState) {
        if (currentState == DownloadState.COMPLETED) {
            removeDownload(item);
        } else if (currentState == DownloadState.FAILED || currentState == DownloadState.QUEUED) {
            startDownload(item);
        } else if (currentState == DownloadState.DOWNLOADING) {
            cancelDownload(item);
        }
    }

    private void removeDownload(DownloadItem item) {
        List<DownloadItem> currentList = _downloadList.getValue();
        if (currentList != null) {
            currentList.remove(item);
            _downloadList.setValue(new ArrayList<>(currentList));
        }
    }

    private void startDownload(DownloadItem item) {
        item.setState(DownloadState.QUEUED);
        item.setProgressPercentage(0);
        updateItemInList(item);

        downloadExecutor.execute(() -> {
            item.setState(DownloadState.DOWNLOADING);
            updateItemInList(item);

            // Simulation loop
            for (int progress = 1; progress <= 100; progress += 5) {
                if (item.getState() != DownloadState.DOWNLOADING) {
                    return; // Stop if cancelled
                }

                int finalProgress = progress;
                // Update progress on the main thread
                mainHandler.post(() -> {
                    item.setProgressPercentage(finalProgress);
                    updateItemInList(item);
                });

                try {
                    Thread.sleep(200); // Simulate network delay
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    // 5% chance of failure during the download process
                    if (Math.random() < 0.05 && progress < 90) {
                        item.setState(DownloadState.FAILED);
                    } else {
                        // If interrupted (e.g., during cancellation)
                        item.setState(DownloadState.CANCELLED);
                    }
                    updateItemInList(item);
                    return;
                }
            }

            // After loop, set to COMPLETED
            mainHandler.post(() -> {
                item.setState(DownloadState.COMPLETED);
                item.setProgressPercentage(100);
                updateItemInList(item);
            });
        });
    }

    private void cancelDownload(DownloadItem item) {
        item.setState(DownloadState.CANCELLED);
        removeDownload(item); // Remove the item immediately upon cancellation
    }

    private void updateItemInList(DownloadItem itemToUpdate) {
        List<DownloadItem> currentList = _downloadList.getValue();
        if (currentList != null) {
            // Find and update the item
            int index = currentList.indexOf(itemToUpdate);
            if (index != -1) {
                currentList.set(index, itemToUpdate);
                _downloadList.setValue(new ArrayList<>(currentList));
            }
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        downloadExecutor.shutdownNow();
    }
}