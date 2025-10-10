package vn.edu.lianac.Download.DownloadAdapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.lianac.R;
import vn.edu.lianac.Download.DownloadItem.DownloadItem;
import vn.edu.lianac.Download.DownloadState.DownloadState;

import java.util.List;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.DownloadViewHolder> {

    private List<DownloadItem> mDownloads;
    private final DownloadInteractionListener mListener;

    public interface DownloadInteractionListener {
        void onActionButtonClick(DownloadItem item, DownloadState currentState);
        void onDeleteButtonClick(DownloadItem item);
    }

    public DownloadAdapter(List<DownloadItem> downloads, DownloadInteractionListener listener) {
        mDownloads = downloads;
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
        DownloadItem item = mDownloads.get(position);

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
                actionIconId = R.drawable.cancel_download_fill;
                deleteButtonVisible = false;
                break;
            case FAILED:
            case CANCELLED:
                progressColor = Color.RED;
                actionIconId = R.drawable.retry_download_fill;
                deleteButtonVisible = true;
                break;
            case COMPLETED:
                progressColor = Color.GREEN;
                actionIconId = R.drawable.downloaddone_download_fill;
                deleteButtonVisible = true;
                break;
            default: // NOT_DOWNLOADED
                progressColor = Color.GRAY;
                actionIconId = R.drawable.downloadstart_download_fill;
                deleteButtonVisible = false;
        }

        holder.progressBar.getProgressDrawable().setColorFilter(progressColor, android.graphics.PorterDuff.Mode.SRC_IN);
        holder.btnAction.setImageResource(actionIconId);
        holder.btnDelete.setVisibility(deleteButtonVisible ? View.VISIBLE : View.GONE);

        holder.btnAction.setOnClickListener(v -> mListener.onActionButtonClick(item, item.getState()));
        holder.btnDelete.setOnClickListener(v -> mListener.onDeleteButtonClick(item));
    }

    @Override
    public int getItemCount() {
        return mDownloads.size();
    }

    public void updateList(List<DownloadItem> newDownloads) {
        mDownloads = newDownloads;
        notifyDataSetChanged();
    }

    static class DownloadViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvFileSize;
        final ProgressBar progressBar;
        final TextView tvPercentage;
        final ImageButton btnAction;
        final ImageButton btnDelete;

        public DownloadViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tv_research_name);
            tvFileSize = view.findViewById(R.id.tv_file_size);
            progressBar = view.findViewById(R.id.progress_bar_download);
            tvPercentage = view.findViewById(R.id.tv_download_percentage);
            btnAction = view.findViewById(R.id.btn_action);
            btnDelete = view.findViewById(R.id.btn_delete);
        }
    }
}
