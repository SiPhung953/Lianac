package vn.edu.lianac.bookmark;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.edu.lianac.R;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ViewHolder> {
    private List<BookmarkItem> bookmarks;
    private OnBookmarkClickListener listener;

    public interface OnBookmarkClickListener {
        void onBookmarkClick(BookmarkItem item);
        void onBookmarkRemove(BookmarkItem item);
    }

    public BookmarkAdapter(List<BookmarkItem> bookmarks, OnBookmarkClickListener listener) {
        this.bookmarks = bookmarks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bookmark, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookmarkItem item = bookmarks.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return bookmarks.size();
    }

    public void updateBookmarks(List<BookmarkItem> newBookmarks) {
        this.bookmarks = newBookmarks;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvTimestamp;
        ImageButton btnRemove;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_bookmark_title);
            tvTimestamp = itemView.findViewById(R.id.tv_bookmark_timestamp);
            btnRemove = itemView.findViewById(R.id.btn_remove_bookmark);
        }

        void bind(BookmarkItem item) {
            tvTitle.setText(item.getTitle());

            // Format timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            tvTimestamp.setText(sdf.format(new Date(item.getTimestamp())));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookmarkClick(item);
                }
            });

            btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookmarkRemove(item);
                }
            });
        }
    }
}

