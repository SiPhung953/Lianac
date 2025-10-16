package vn.edu.lianac.Download.DownloadAdapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import vn.edu.lianac.R;
import vn.edu.lianac.Download.DownloadItem.DownloadItem;
import vn.edu.lianac.Download.DownloadState.DownloadState;

public class DownloadAdapter extends ListAdapter<DownloadItem, DownloadAdapter.DownloadViewHolder> {

    private final DownloadInteractionListener mListener;

    public interface DownloadInteractionListener {
        void onActionButtonClick(DownloadItem item);
        void onDeleteButtonClick(DownloadItem item);
        void onItemClick(DownloadItem item);
    }

    public DownloadAdapter(DownloadInteractionListener listener) {
        super(DIFF_CALLBACK);
        mListener = listener;
    }

    @NonNull
    @Override
    public DownloadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_download, parent, false);
        return new DownloadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadViewHolder holder, int position) {
        DownloadItem item = getItem(position);

        holder.tvTitle.setText(item.getPaperName());
        holder.tvFileSize.setText(item.getFileSize());
        holder.progressBar.setProgress(item.getProgressPercentage());
        holder.tvPercentage.setText(item.getProgressPercentage() + "%");

        int progressColor;
        int actionIconId;
        boolean deleteButtonVisible = false;

        switch (item.getState()) {
            case QUEUED:
            case DOWNLOADING:
                progressColor = Color.BLACK;
                actionIconId = R.drawable.ic_cancel;
                break;
            case FAILED:
                progressColor = Color.RED;
                actionIconId = R.drawable.ic_retry;
                deleteButtonVisible = true;
                break;
            case COMPLETED:
                progressColor = Color.GREEN;
                actionIconId = R.drawable.ic_downloaddone;
                deleteButtonVisible = true;
                break;
            default: // AKA NOT_DOWNLOADED
                progressColor = Color.GRAY;
                actionIconId = R.drawable.ic_download;
        }

        holder.progressBar.getProgressDrawable().setColorFilter(progressColor, android.graphics.PorterDuff.Mode.SRC_IN);
        holder.btnAction.setImageResource(actionIconId);
        holder.btnDelete.setVisibility(deleteButtonVisible ? View.VISIBLE : View.GONE);

        holder.btnAction.setOnClickListener(v -> mListener.onActionButtonClick(item));
        holder.btnDelete.setOnClickListener(v -> mListener.onDeleteButtonClick(item));
        holder.itemView.setOnClickListener(v -> mListener.onItemClick(item));
    }

    static class DownloadViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle, tvFileSize, tvPercentage;
        final ProgressBar progressBar;
        final ImageButton btnAction, btnDelete;

        public DownloadViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tv_paper_title);
            tvFileSize = view.findViewById(R.id.tv_file_size);
            progressBar = view.findViewById(R.id.progress_bar_download);
            tvPercentage = view.findViewById(R.id.tv_download_percentage);
            btnAction = view.findViewById(R.id.btn_action);
            btnDelete = view.findViewById(R.id.btn_delete);
        }
    }

    private static final DiffUtil.ItemCallback<DownloadItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<DownloadItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull DownloadItem oldItem, @NonNull DownloadItem newItem) {
            return oldItem.getUrl().equals(newItem.getUrl());
        }

        @Override
        public boolean areContentsTheSame(@NonNull DownloadItem oldItem, @NonNull DownloadItem newItem) {
            return oldItem.equals(newItem);
        }
    };
}
