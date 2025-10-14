package vn.edu.lianac.ui;

import android.text.TextUtils;
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
 * Added a click event to open a WebViewFragment.
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

        // 🔹 Handle clicks on each item
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
        TextView tvTitle, tvAuthors, tvSubjects;

        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            // Correct the IDs to match the item_article.xml file
            tvTitle = itemView.findViewById(R.id.articleTitle);
            tvAuthors = itemView.findViewById(R.id.articleAuthors);
            // Temporarily using the articleId TextView to display Categories
            tvSubjects = itemView.findViewById(R.id.articleId);
        }

        public void bind(Article article) {
            tvTitle.setText(article.getTitle());
            // Join the list of authors into a single string
            tvAuthors.setText(TextUtils.join(", ", article.getAuthors()));
            tvSubjects.setText(TextUtils.join(", ", article.getCategories()));
        }
    }
}