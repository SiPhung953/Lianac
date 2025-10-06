package vn.edu.lianac;

import android.os.Bundle;
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

// Corrected imports (all classes are now in the same package)
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_download, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(DownloadViewModel.class);

        mRecyclerView = view.findViewById(R.id.recycler_view_downloads);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAdapter = new DownloadAdapter(new ArrayList<>(), this);
        mRecyclerView.setAdapter(mAdapter);

        // Add the Custom Item Decoration for the dividers
        mRecyclerView.addItemDecoration(
                new DownloadItemDecoration(getContext(), R.drawable.divider_line)
        );

        // Observe the LiveData list from the ViewModel
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
            }
        });
        // End of testing section
    }

    @Override
    public void onActionButtonClick(DownloadItem item, DownloadState currentState) {
        mViewModel.handleDownloadAction(item, currentState);
    }
}