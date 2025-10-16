package vn.edu.lianac.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.lianac.DetailFragment;
import vn.edu.lianac.MainActivity;
import vn.edu.lianac.R;
import vn.edu.lianac.models.Article;
import vn.edu.lianac.utils.CategoryProvider;

/**
 * Article adapter using ListAdapter for efficient updates.
 * Displays article information with clickable category badges.
 */
public class ArticleAdapter extends ListAdapter<Article, ArticleAdapter.ViewHolder> {
    private static final String TAG = "ArticleAdapter";

    public ArticleAdapter() {
        super(DIFF_CALLBACK);
        Log.d(TAG, "ArticleAdapter created");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder called");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_article, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder called for position: " + position);
        holder.bind(getItem(position));
    }

    @Override
    public void submitList(List<Article> list) {
        Log.d(TAG, "submitList called with " + (list != null ? list.size() : "null") + " items");
        // Force a new list instance to trigger diff calculation
        super.submitList(list != null ? new ArrayList<>(list) : null);
    }

    @Override
    public void submitList(List<Article> list, Runnable commitCallback) {
        Log.d(TAG, "submitList (with callback) called with " + (list != null ? list.size() : "null") + " items");
        // Force a new list instance to trigger diff calculation
        super.submitList(list != null ? new ArrayList<>(list) : null, commitCallback);
    }

    @Override
    public void onCurrentListChanged(@NonNull List<Article> previousList, @NonNull List<Article> currentList) {
        super.onCurrentListChanged(previousList, currentList);
        Log.d(TAG, "onCurrentListChanged - previous: " + previousList.size() + ", current: " + currentList.size());
    }

    @Override
    public int getItemCount() {
        int count = super.getItemCount();
        Log.d(TAG, "getItemCount returning: " + count);
        return count;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final Context context;
        private final TextView articleId;
        private final LinearLayout categoriesContainer;
        private final TextView titleText;
        private final TextView authorsText;
        private final TextView submittedText;
        private final TextView announcedText;
        private final TextView classesText;
        private final TextView doiText;
        private final CategoryProvider categoryProvider;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            articleId = itemView.findViewById(R.id.articleId);
            categoriesContainer = itemView.findViewById(R.id.categoriesContainer);
            titleText = itemView.findViewById(R.id.articleTitle);
            authorsText = itemView.findViewById(R.id.articleAuthors);
            submittedText = itemView.findViewById(R.id.articleSubmitted);
            announcedText = itemView.findViewById(R.id.articleAnnounced);
            classesText = itemView.findViewById(R.id.articleClasses);
            doiText = itemView.findViewById(R.id.articleDoi);

            // Initialize CategoryProvider
            categoryProvider = CategoryProvider.getInstance(context);
        }

        void bind(Article article) {
            Log.d(TAG, "Binding article: " + article.getTitle());

            // Set arXiv ID
            articleId.setText(article.getId());
            Log.d(TAG, "Article ID: " + article.getId());

            // Clear and populate categories
            categoriesContainer.removeAllViews();
            List<String> categories = article.getCategories();
            Log.d(TAG, "Categories: " + (categories != null ? categories.size() : "null"));

            if (categories != null && !categories.isEmpty()) {
                boolean isFirst = true;
                List<String> validCategories = new ArrayList<>();
                List<String> acmMscClasses = new ArrayList<>();
                java.util.Set<String> seenCategories = new java.util.LinkedHashSet<>();

                // Separate valid arXiv categories from ACM/MSC codes
                for (String categoryId : categories) {
                    // FIX: Map math.MP to math-ph
                    String normalizedCategoryId = normalizeCategoryId(categoryId);

                    String displayName = categoryProvider.getCategoryDisplayName(normalizedCategoryId);
                    if (displayName.equals(normalizedCategoryId)) {
                        // It's an ACM/MSC code
                        acmMscClasses.add(categoryId);  // Keep original for ACM/MSC display
                    } else {
                        // It's a valid arXiv category - add only if not seen before
                        if (seenCategories.add(normalizedCategoryId)) {
                            validCategories.add(normalizedCategoryId);
                        }
                    }
                }

                // Display valid categories as badges
                for (String categoryId : validCategories) {
                    TextView badge = createCategoryBadge(categoryId, isFirst);
                    if (badge != null) {
                        categoriesContainer.addView(badge);
                        isFirst = false;
                    }
                }

                // Store ACM/MSC classes in the article for display later
                article.setAcmMscClasses(acmMscClasses);
            }

            // Set title
            titleText.setText(article.getTitle());

            // Set authors
            authorsText.setText(article.getFormattedAuthors());

            // Set submitted date
            submittedText.setText(article.getFormattedSubmittedDate());

            // Set announced date (if different from submitted)
            String announcedDate = article.getFormattedAnnouncedDate();
            if (announcedDate != null && !announcedDate.isEmpty()) {
                announcedText.setText(announcedDate);
                announcedText.setVisibility(View.VISIBLE);
            } else {
                announcedText.setVisibility(View.GONE);
            }

            // Set ACM/MSC classes (if any)
            String classesString = article.getAcmMscClassesString();
            if (classesString != null && !classesString.isEmpty()) {
                classesText.setText(classesString);
                classesText.setVisibility(View.VISIBLE);
            } else {
                classesText.setVisibility(View.GONE);
            }

            // FIX: Set DOI with better null/empty checks
            String doi = article.getDoi();
            Log.d(TAG, "DOI for article " + article.getId() + ": " + doi);

            if (doi != null && !doi.trim().isEmpty()) {
                doiText.setText("DOI: " + doi);
                doiText.setVisibility(View.VISIBLE);
                doiText.setOnClickListener(v -> {
                    String doiUrl = "https://doi.org/" + doi;
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(doiUrl));
                    try {
                        context.startActivity(browserIntent);
                    } catch (Exception e) {
                        Toast.makeText(context, "Cannot open DOI link", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to open DOI link", e);
                    }
                });
            } else {
                doiText.setVisibility(View.GONE);
            }

            // Make item clickable to open DetailFragment
            itemView.setOnClickListener(v -> {
                Log.d(TAG, "Item clicked: " + article.getId());
                navigateToDetail(article);
            });

            Log.d(TAG, "Bind complete for: " + article.getId());
        }

        /**
         * Navigate to DetailFragment with the selected article
         */
        private void navigateToDetail(Article article) {
            if (context instanceof FragmentActivity) {
                FragmentActivity activity = (FragmentActivity) context;

                // Check which MainActivity version we're using
                if (activity instanceof MainActivity) {
                    android.os.Bundle args = new android.os.Bundle();
                    args.putParcelable("article", article);

                    DetailFragment fragment = new DetailFragment();
                    fragment.setArguments(args);

                    // Use the appropriate navigation method
                    try {
                        // Try MainActivity_search style first
                        ((MainActivity) activity).loadContentFragment(fragment, true);
                    } catch (NoSuchMethodError e) {
                        // Fallback to MainActivity_detail style
                        try {
                            ((MainActivity) activity).replaceFragment(fragment);
                        } catch (Exception ex) {
                            Log.e(TAG, "Failed to navigate to detail", ex);
                            Toast.makeText(context, "Error opening article details", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }

        /**
         * Normalize category IDs to handle arXiv inconsistencies
         * Example: math.MP should be math-ph
         */
        private String normalizeCategoryId(String categoryId) {
            if (categoryId == null) return null;

            // Handle math.MP -> math-ph mapping
            if ("math.MP".equals(categoryId)) {
                Log.d(TAG, "Normalizing math.MP to math-ph");
                return "math-ph";
            }

            // Add other mappings here if needed in the future

            return categoryId;
        }

        private TextView createCategoryBadge(String categoryId, boolean isFirst) {
            String displayName = categoryProvider.getCategoryDisplayName(categoryId);

            // Skip if it's just the ID echoed back (no resource found)
            // This filters out MSC/ACM codes like "82-10" that aren't in strings.xml
            if (displayName.equals(categoryId)) {
                Log.d(TAG, "Skipping unknown category: " + categoryId);
                return null;  // Don't create badge for unknown categories
            }

            TextView badge = new TextView(context);
            badge.setText(categoryId);  // Display the category CODE (e.g., "cs.AI")
            badge.setTextSize(11f);

            // Only first badge gets the background
            if (isFirst) {
                badge.setTextColor(context.getResources().getColor(android.R.color.white));
                badge.setBackgroundResource(R.drawable.category_first_badge_background);
            }
            else {
                badge.setTextColor(context.getResources().getColor(android.R.color.black));
                badge.setBackgroundResource(R.drawable.category_normal_badge_background);
            }

            // Set margins
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            int marginPx = dpToPx(4);
            params.setMargins(0, 0, marginPx, 0);
            badge.setLayoutParams(params);

            // Make clickable to show full category name
            badge.setClickable(true);
            badge.setFocusable(true);
            badge.setOnClickListener(v -> {
                Toast.makeText(context, displayName, Toast.LENGTH_SHORT).show();
            });

            return badge;
        }

        private int dpToPx(int dp) {
            float density = context.getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }
    }

    private static final DiffUtil.ItemCallback<Article> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull Article old, @NonNull Article newItem) {
                    if (old.getId() == null || newItem.getId() == null) {
                        return false;
                    }
                    return old.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Article old, @NonNull Article newItem) {
                    // Compare title
                    if (!safeEquals(old.getTitle(), newItem.getTitle())) {
                        return false;
                    }

                    // Compare published date
                    if (!safeEquals(old.getPublishedDate(), newItem.getPublishedDate())) {
                        return false;
                    }

                    // Compare DOI
                    if (!safeEquals(old.getDoi(), newItem.getDoi())) {
                        return false;
                    }

                    // Compare categories
                    List<String> oldCats = old.getCategories();
                    List<String> newCats = newItem.getCategories();

                    if (oldCats == null && newCats == null) {
                        return true;
                    }
                    if (oldCats == null || newCats == null) {
                        return false;
                    }
                    if (oldCats.size() != newCats.size()) {
                        return false;
                    }

                    return oldCats.equals(newCats);
                }

                private boolean safeEquals(String str1, String str2) {
                    if (str1 == null && str2 == null) return true;
                    if (str1 == null || str2 == null) return false;
                    return str1.equals(str2);
                }
            };
}