package vn.edu.lianac.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import vn.edu.lianac.R;
import vn.edu.lianac.models.Article;

import java.util.List;

/**
 * RecyclerView adapter for displaying a list of Article objects.
 * Uses ListAdapter for efficient list updates.
 */
public class ArticleAdapter extends ListAdapter<Article, ArticleAdapter.ArticleViewHolder> {

    /**
     * Constructor uses a DiffUtil callback to efficiently update the list.
     */
    public ArticleAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout from item_article.xml
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_article, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        // Get the article at the current position and bind its data to the ViewHolder
        Article article = getItem(position);
        if (article != null) {
            holder.bind(article);
        }
    }

    /**
     * ViewHolder class that holds references to the views for a single article item.
     */
    public static class ArticleViewHolder extends RecyclerView.ViewHolder {
        // Declare views from item_article.xml
        private final TextView articleId;
        private final TextView articleTitle;
        private final TextView articleAuthors;
        private final TextView articlePublished;
        // The categories container is optional for now, but can be populated if needed.

        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find views by their ID
            articleId = itemView.findViewById(R.id.articleId);
            articleTitle = itemView.findViewById(R.id.articleTitle);
            articleAuthors = itemView.findViewById(R.id.articleAuthors);
            articlePublished = itemView.findViewById(R.id.articlePublished);
        }

        /**
         * Binds an Article object's data to the views.
         * @param article The article to display.
         */
        public void bind(Article article) {
            // Use the model's getters and convenience methods to populate the UI
            articleId.setText(article.getId());
            articleTitle.setText(article.getTitle());
            articleAuthors.setText(article.getFormattedAuthors()); // Using the convenience method
            articlePublished.setText(article.getPublishedDateFormatted()); // Using the formatted date
        }
    }

    /**
     * DiffUtil.ItemCallback implementation for calculating the difference between two lists.
     * This allows ListAdapter to perform efficient updates (e.g., animations for added/removed items).
     */
    private static final DiffUtil.ItemCallback<Article> DIFF_CALLBACK = new DiffUtil.ItemCallback<Article>() {
        @Override
        public boolean areItemsTheSame(@NonNull Article oldItem, @NonNull Article newItem) {
            // Articles are the same if their unique IDs match.
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Article oldItem, @NonNull Article newItem) {
            // Check if the content has changed.
            // For simplicity, we can compare titles and authors. For full accuracy,
            // you might compare hashes or all relevant fields.
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getAuthors().equals(newItem.getAuthors()) &&
                    oldItem.getPublishedDateRaw().equals(newItem.getPublishedDateRaw());
        }
    };
}
