package vn.edu.lianac;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.lianac.DownloadAdapter.DownloadAdapter;
import vn.edu.lianac.DownloadItem.DownloadItem;
import vn.edu.lianac.DownloadItemDecoration.DownloadItemDecoration;
import vn.edu.lianac.DownloadState.DownloadState;
import vn.edu.lianac.DownloadViewModel.DownloadViewModel;

import java.util.ArrayList;

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
                String url = resultData.getString(DownloadService.EXTRA_URL);
                if (mViewModel != null && url != null) {
                    mViewModel.updateDownloadProgress(downloadId, progress, status, url);
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

        mViewModel = new ViewModelProvider(this).get(DownloadViewModel.class);
        progressReceiver = new DownloadResultReceiver(new Handler(Looper.getMainLooper()));


        mRecyclerView = view.findViewById(R.id.recycler_view_downloads);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAdapter = new DownloadAdapter(new ArrayList<>(), this);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addItemDecoration(
                new DownloadItemDecoration(getContext(), R.drawable.divider_line)
        );

        mViewModel.downloadList.observe(getViewLifecycleOwner(), downloadItems -> {
            mAdapter.updateList(downloadItems);
        });

        // For testing purposes, remove later
        etPdfUrl = view.findViewById(R.id.et_pdf_url);
        btnDownload = view.findViewById(R.id.btn_download);

        btnDownload.setOnClickListener(v -> {
            String url = etPdfUrl.getText().toString();
            if (!url.isEmpty()) {
                DownloadItem newItem = new DownloadItem(url, "New PDF", DownloadState.NOT_DOWNLOADED);
                mViewModel.addDownloadItem(newItem);
                etPdfUrl.setText("");
                startDownload(newItem);
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
        getActivity().startService(intent);
    }

    @Override
    public void onActionButtonClick(DownloadItem item, DownloadState currentState) {
        if (currentState == DownloadState.NOT_DOWNLOADED || currentState == DownloadState.FAILED || currentState == DownloadState.CANCELLED) {
            startDownload(item);
        } else {
            mViewModel.handleDownloadAction(item);
        }
    }

    @Override
    public void onDeleteButtonClick(DownloadItem item) {
        mViewModel.deleteDownload(item);
    }
}
