package vn.edu.lianac.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import vn.edu.lianac.models.Article;
import vn.edu.lianac.R;

import java.util.List;

/**
 * Adapter to display a list of articles from arXiv.
 * Now includes summary, date, and categories in the list view.
 */
public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {

    private List<Article> articleList;

    // Interface for the click event
    public interface OnArticleClickListener {
        void onArticleClick(Article article);
    }

    private OnArticleClickListener listener;

    public void setOnArticleClickListener(OnArticleClickListener listener) {
        this.listener = listener;
    }

    public void setArticles(List<Article> articles) {
        this.articleList = articles;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_article, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        Article article = articleList.get(position);
        holder.bind(article);

        // Handle clicks on each item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onArticleClick(article);
            }
        });
    }

    @Override
    public int getItemCount() {
        return articleList != null ? articleList.size() : 0;
    }

    public static class ArticleViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvTitle, tvAuthors, tvSummary, tvPublished, tvCategories;

        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.articleId);
            tvTitle = itemView.findViewById(R.id.articleTitle);
            tvAuthors = itemView.findViewById(R.id.articleAuthors);
            tvSummary = itemView.findViewById(R.id.articleSummary);
            tvPublished = itemView.findViewById(R.id.articlePublished);
            tvCategories = itemView.findViewById(R.id.articleCategories);
        }

        public void bind(Article article) {
            // Article ID
            tvId.setText(article.getId() != null ? article.getId() : "");

            // Title
            tvTitle.setText(article.getTitle() != null ? article.getTitle() : "No title");

            // Authors
            String authorsText = article.getFormattedAuthors();
            if (authorsText != null && !authorsText.isEmpty()) {
                tvAuthors.setText(authorsText);
                tvAuthors.setVisibility(View.VISIBLE);
            } else {
                tvAuthors.setVisibility(View.GONE);
            }

            // Summary (shortened)
            String summary = article.getShortenedSummary();
            if (summary != null && !summary.isEmpty()) {
                tvSummary.setText(summary);
                tvSummary.setVisibility(View.VISIBLE);
            } else {
                tvSummary.setVisibility(View.GONE);
            }

            // Published date
            String publishedDate = article.getPublishedDateFormatted();
            if (publishedDate != null && !publishedDate.isEmpty()) {
                tvPublished.setText("Published: " + publishedDate);
                tvPublished.setVisibility(View.VISIBLE);
            } else {
                tvPublished.setVisibility(View.GONE);
            }

            // Categories
            String categories = article.getCategoriesString();
            if (categories != null && !categories.isEmpty()) {
                tvCategories.setText("Categories: " + categories);
                tvCategories.setVisibility(View.VISIBLE);
            } else {
                tvCategories.setVisibility(View.GONE);
            }
        }
    }
}