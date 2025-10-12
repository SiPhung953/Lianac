package vn.edu.lianac.Download;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

import vn.edu.lianac.Download.DownloadAdapter.DownloadAdapter;
import vn.edu.lianac.Download.DownloadItem.DownloadItem;
import vn.edu.lianac.Download.DownloadItemDecoration.DownloadItemDecoration;
import vn.edu.lianac.Download.DownloadState.DownloadState;
import vn.edu.lianac.Download.DownloadViewModel.DownloadViewModel;
import vn.edu.lianac.R;

public class DownloadFragment extends Fragment implements DownloadAdapter.DownloadInteractionListener {

    private DownloadViewModel mViewModel;
    private DownloadAdapter mAdapter;
    private RecyclerView mRecyclerView;

    // For testing purposes, remove later
    private EditText etPdfUrl;
    private Button btnDownload;
    // End of testing section

    private DownloadResultReceiver progressReceiver;

    private class DownloadResultReceiver extends ResultReceiver {
        public DownloadResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == DownloadService.UPDATE_CODE && resultData != null) {
                long downloadId = resultData.getLong(DownloadService.EXTRA_DOWNLOAD_ID, -1);
                int progress = resultData.getInt(DownloadService.EXTRA_PROGRESS, 0);
                int status = resultData.getInt("status", -1);
                String filePath = resultData.getString(DownloadService.EXTRA_FILE_PATH);
                String url = resultData.getString(DownloadService.EXTRA_URL);
                if (mViewModel != null && url != null) {
                    mViewModel.updateDownloadProgress(downloadId, progress, status, url, filePath);
                }
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_download, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(requireActivity()).get(DownloadViewModel.class);
        progressReceiver = new DownloadResultReceiver(new Handler(Looper.getMainLooper()));

        mRecyclerView = view.findViewById(R.id.recycler_view_downloads);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // The adapter is initialized with an empty list. Data will come from LiveData.
        mAdapter = new DownloadAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addItemDecoration(
                new DownloadItemDecoration(getContext(), R.drawable.divider_line)
        );

        // Observe the download list from the ViewModel
        mViewModel.downloadList.observe(getViewLifecycleOwner(), downloadItems -> {
            if (downloadItems != null) {
                // Use the new submitList method to efficiently update the adapter
                mAdapter.submitList(downloadItems);
            }
        });

        // Observe the start download event from the ViewModel
        mViewModel.startDownloadEvent.observe(getViewLifecycleOwner(), item -> {
            if (item != null) {
                startDownload(item);
                mViewModel.onDownloadStarted(); // Reset the event
            }
        });

        // For testing purposes, remove later
        etPdfUrl = view.findViewById(R.id.et_pdf_url);
        btnDownload = view.findViewById(R.id.btn_download);

        btnDownload.setOnClickListener(v -> {
            String url = etPdfUrl.getText().toString();
            if (!url.isEmpty()) {
                mViewModel.fetchTitleAndAddDownload(url);
                etPdfUrl.setText("");
            }
        });
        // End of testing section
    }

    private void startDownload(DownloadItem item) {
        Intent intent = new Intent(getActivity(), DownloadService.class);
        intent.setAction(DownloadService.ACTION_DOWNLOAD);
        intent.putExtra(DownloadService.EXTRA_URL, item.getUrl());
        intent.putExtra(DownloadService.EXTRA_FILE_NAME, item.getPaperName());
        intent.putExtra(DownloadService.EXTRA_RECEIVER, progressReceiver);
        if (getActivity() != null) {
            getActivity().startService(intent);
        }
    }

    @Override
    public void onActionButtonClick(DownloadItem item) {
        mViewModel.handleDownloadAction(item);
    }

    @Override
    public void onDeleteButtonClick(DownloadItem item) {
        mViewModel.deleteDownload(item);
    }

    @Override
    public void onItemClick(DownloadItem item) {
        if (item.getState() == DownloadState.COMPLETED && item.getFilePath() != null) {
            File file = new File(item.getFilePath());
            if (file.exists()) {
                Uri fileUri = FileProvider.getUriForFile(requireContext(),
                        requireContext().getPackageName() + ".provider", file);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(fileUri, "application/pdf");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getContext(), "No PDF viewer found", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
