package vn.edu.lianac;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Imports are now self-referencing or standard Android
import vn.edu.lianac.R;
import vn.edu.lianac.DownloadItem;
import vn.edu.lianac.DownloadState;

import java.util.List;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.DownloadViewHolder> {

    private List<DownloadItem> mDownloads;
    private final DownloadInteractionListener mListener;

    public interface DownloadInteractionListener {
        void onActionButtonClick(DownloadItem item, DownloadState currentState);
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

        holder.tvTitle.setText(item.getResearchName());
        holder.tvFileSize.setText(item.getFileSize());
        holder.progressBar.setProgress(item.getProgressPercentage());
        holder.tvPercentage.setText(item.getProgressPercentage() + "%");

        // 1. Progress Bar Color & Visibility Logic
        int progressColor;
        int actionIconId;

        switch (item.getState()) {
            case QUEUED:
                progressColor = Color.GRAY;
                actionIconId = android.R.drawable.ic_menu_close_clear_cancel; // Cancel icon
                break;
            case DOWNLOADING:
                progressColor = Color.BLACK;
                actionIconId = android.R.drawable.ic_menu_close_clear_cancel; // Cancel icon
                break;
            case FAILED:
                progressColor = Color.RED;
                actionIconId = android.R.drawable.ic_menu_rotate; // Retry icon
                break;
            case COMPLETED:
                progressColor = Color.GREEN;
                actionIconId = android.R.drawable.ic_menu_delete; // Remove (Recycle bin) icon
                break;
            case CANCELLED:
                progressColor = Color.RED;
                actionIconId = android.R.drawable.ic_menu_rotate;
                break;
            default:
                progressColor = Color.GRAY;
                actionIconId = android.R.drawable.ic_menu_close_clear_cancel;
        }

        // Apply color tint
        holder.progressBar.getProgressDrawable().setColorFilter(
                progressColor, android.graphics.PorterDuff.Mode.SRC_IN);

        // 2. Action Button Icon
        holder.btnAction.setImageResource(actionIconId);

        // 3. Action Button Click Listener
        holder.btnAction.setOnClickListener(v ->
                mListener.onActionButtonClick(item, item.getState())
        );
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

        public DownloadViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tv_research_name);
            tvFileSize = view.findViewById(R.id.tv_file_size);
            progressBar = view.findViewById(R.id.progress_bar_download);
            tvPercentage = view.findViewById(R.id.tv_download_percentage);
            btnAction = view.findViewById(R.id.btn_action);
        }
    }
}