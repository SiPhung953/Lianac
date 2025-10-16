package vn.edu.lianac.Download.DownloadViewModel;

import android.app.Application;
import android.app.DownloadManager;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import vn.edu.lianac.Download.DownloadItem.DownloadItem;
import vn.edu.lianac.Download.DownloadState.DownloadState;
import vn.edu.lianac.Download.db.DownloadRepository;

public class DownloadViewModel extends AndroidViewModel {

    public final DownloadRepository mRepository;
    public final LiveData<List<DownloadItem>> downloadList;

    private final MutableLiveData<DownloadItem> _startDownloadEvent = new MutableLiveData<>();
    public final LiveData<DownloadItem> startDownloadEvent = _startDownloadEvent;

    private final DownloadManager downloadManager;
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();

    public DownloadViewModel(Application application) {
        super(application);
        mRepository = new DownloadRepository(application);
        downloadList = mRepository.getAllDownloads();
        downloadManager = application.getSystemService(DownloadManager.class);
    }

    public void fetchTitleAndAddDownload(String pdfUrl) {
        networkExecutor.execute(() -> {
            String paperName = "New PDF";
            try {
                String abstractUrlString = pdfUrl.replace("/pdf/", "/abs/");
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
                connection.disconnect();

                Pattern pattern = Pattern.compile("<meta name=\"citation_title\" content=\"(.*?)\" />");
                Matcher matcher = pattern.matcher(content.toString());
                if (matcher.find() && matcher.group(1) != null) {
                    paperName = matcher.group(1);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            DownloadItem newItem = new DownloadItem(pdfUrl, paperName, DownloadState.NOT_DOWNLOADED);
            mRepository.insert(newItem);
        });
    }

    public void handleDownloadAction(DownloadItem item) {
        switch (item.getState()) {
            case NOT_DOWNLOADED:
            case FAILED:
                item.setState(DownloadState.QUEUED);
                item.setProgressPercentage(0);
                mRepository.update(item);
                _startDownloadEvent.setValue(item);
                break;
            case DOWNLOADING:
            case QUEUED:
                if (item.getDownloadId() != 0) {
                    downloadManager.remove(item.getDownloadId());
                }
                item.setState(DownloadState.FAILED);
                mRepository.update(item);
                break;
            case COMPLETED:
            case REMOVED:
                break;
        }
    }

    public void onDownloadStarted() {
        _startDownloadEvent.setValue(null);
    }

    public void updateDownloadProgress(long downloadId, int progress, int status, String url, String filePath) {
        mRepository.updateDownloadProgress(downloadId, progress, status, url, filePath);
    }

    public void deleteDownload(DownloadItem item) {
        if (item.getDownloadId() != 0 && (item.getState() == DownloadState.DOWNLOADING || item.getState() == DownloadState.QUEUED)) {
            downloadManager.remove(item.getDownloadId());
        }
        mRepository.delete(item);
    }
}
