package vn.edu.lianac;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import vn.edu.lianac.bookmark.BookmarkItem;
import vn.edu.lianac.bookmark.BookmarkManager;
import vn.edu.lianac.ui.WebViewFragment;

public class DetailFragment extends Fragment {

    private ImageView downloadIcon, bookmarkIcon, menuIcon;
    private BookmarkManager bookmarkManager;
    private TextView readButton;
    private TextView textSubject, textSubclass;
    private TextView articleId, paperTitle, paperAuthor, paperPublished, paperUpdated;
    private TextView paperCategories, paperDoi, paperSummary;
    private LinearLayout breadcrumbBar;

    // Article data from arguments
    private String articleIdValue;
    private String articleTitle;
    private String articleSummary;
    private String articleAuthors;
    private String articlePublished;
    private String articleUpdated;
    private String articleCategories;
    private String pdfUrl;
    private String absUrl;
    private String doi;
    private String primaryCategory;
    private String primaryCategoryName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        // Initialize views
        initViews(view);

        // Initialize BookmarkManager
        bookmarkManager = new BookmarkManager(requireContext());

        // Get data from arguments
        loadArgumentsData();

        // Populate UI with article data
        populateArticleData();

        // Setup click listeners
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        downloadIcon = view.findViewById(R.id.download_icon);
        bookmarkIcon = view.findViewById(R.id.bookmark_icon);
        menuIcon = view.findViewById(R.id.menu_icon);
        readButton = view.findViewById(R.id.read_button);
        textSubject = view.findViewById(R.id.text_subject);
        textSubclass = view.findViewById(R.id.text_subclass);
        breadcrumbBar = view.findViewById(R.id.breadcrumb_bar);

        articleId = view.findViewById(R.id.article_id);
        paperTitle = view.findViewById(R.id.paper_title);
        paperAuthor = view.findViewById(R.id.paper_author);
        paperPublished = view.findViewById(R.id.paper_published);
        paperUpdated = view.findViewById(R.id.paper_updated);
        paperCategories = view.findViewById(R.id.paper_categories);
        paperDoi = view.findViewById(R.id.paper_doi);
        paperSummary = view.findViewById(R.id.paper_summary);
    }

    private void loadArgumentsData() {
        Bundle args = getArguments();
        if (args != null) {
            articleIdValue = args.getString("article_id", "");
            articleTitle = args.getString("article_title", "");
            articleSummary = args.getString("article_summary", "");
            articleAuthors = args.getString("article_authors", "");
            articlePublished = args.getString("article_published", "");
            articleUpdated = args.getString("article_updated", "");
            articleCategories = args.getString("article_categories", "");
            pdfUrl = args.getString("pdf_url", "");
            absUrl = args.getString("abs_url", "");
            doi = args.getString("doi", "");
            primaryCategory = args.getString("primary_category", "");
            primaryCategoryName = args.getString("primary_category_name", "");
        }
    }

    private void populateArticleData() {
        // Article ID
        if (articleIdValue != null && !articleIdValue.isEmpty()) {
            articleId.setText("arXiv:" + articleIdValue);
            articleId.setVisibility(View.VISIBLE);
        } else {
            articleId.setVisibility(View.GONE);
        }

        // Title
        if (articleTitle != null && !articleTitle.isEmpty()) {
            paperTitle.setText(articleTitle);
        } else {
            paperTitle.setText("No title available");
        }

        // Authors
        if (articleAuthors != null && !articleAuthors.isEmpty()) {
            paperAuthor.setText("Authors: " + articleAuthors);
            paperAuthor.setVisibility(View.VISIBLE);
        } else {
            paperAuthor.setVisibility(View.GONE);
        }

        // Published date
        if (articlePublished != null && !articlePublished.isEmpty()) {
            paperPublished.setText("Published: " + articlePublished);
            paperPublished.setVisibility(View.VISIBLE);
        } else {
            paperPublished.setVisibility(View.GONE);
        }

        // Updated date (only show if different from published)
        if (articleUpdated != null && !articleUpdated.isEmpty()
                && !articleUpdated.equals(articlePublished)) {
            paperUpdated.setText("Updated: " + articleUpdated);
            paperUpdated.setVisibility(View.VISIBLE);
        } else {
            paperUpdated.setVisibility(View.GONE);
        }

        // Categories
        if (articleCategories != null && !articleCategories.isEmpty()) {
            paperCategories.setText("Categories: " + articleCategories);
            paperCategories.setVisibility(View.VISIBLE);
        } else {
            paperCategories.setVisibility(View.GONE);
        }

        // DOI (clickable)
        if (doi != null && !doi.isEmpty()) {
            paperDoi.setText("DOI: " + doi);
            paperDoi.setVisibility(View.VISIBLE);
        } else {
            paperDoi.setVisibility(View.GONE);
        }

        // Summary/Abstract
        if (articleSummary != null && !articleSummary.isEmpty()) {
            paperSummary.setText(articleSummary);
            paperSummary.setVisibility(View.VISIBLE);
        } else {
            paperSummary.setText("No abstract available");
        }

        // Breadcrumb (if available)
        if (primaryCategoryName != null && !primaryCategoryName.isEmpty()) {
            textSubject.setText(primaryCategoryName);
            if (primaryCategory != null && !primaryCategory.isEmpty()) {
                textSubclass.setText(primaryCategory);
            }
            breadcrumbBar.setVisibility(View.VISIBLE);
        } else {
            breadcrumbBar.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        // MENU
        if (menuIcon != null) {
            menuIcon.setOnClickListener(v ->
                    Toast.makeText(getContext(), R.string.menu_clicked, Toast.LENGTH_SHORT).show()
            );
        }

        // BOOKMARK
        updateBookmarkIcon();
        if (bookmarkIcon != null) {
            bookmarkIcon.setOnClickListener(v -> {
                toggleBookmark();
                updateBookmarkIcon();
            });
        }

        // DOWNLOAD
        if (downloadIcon != null) {
            downloadIcon.setOnClickListener(v -> {
                if (pdfUrl == null || pdfUrl.isEmpty()) {
                    Toast.makeText(getContext(), R.string.no_pdf_link_available, Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(getContext(), R.string.loading, Toast.LENGTH_SHORT).show();
                downloadIcon.setEnabled(false);

                new Handler().postDelayed(() -> {
                    boolean success = Math.random() > 0.3;
                    if (success) {
                        downloadIcon.setImageResource(R.drawable.check);
                        Toast.makeText(getContext(), R.string.done, Toast.LENGTH_SHORT).show();
                    } else {
                        downloadIcon.setImageResource(R.drawable.cancel);
                        Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                    }
                    downloadIcon.setEnabled(true);
                }, 2000);
            });
        }

        // READ BUTTON - Opens PDF in WebViewFragment
        if (readButton != null) {
            readButton.setOnClickListener(v -> {
                String urlToOpen = pdfUrl;

                // If no PDF URL, try abstract URL
                if (urlToOpen == null || urlToOpen.isEmpty()) {
                    urlToOpen = absUrl;
                }

                // Still no URL? Show error
                if (urlToOpen == null || urlToOpen.isEmpty()) {
                    Toast.makeText(getContext(), R.string.no_pdf_link_available, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Ensure URL ends with .pdf for PDF URLs
                if (urlToOpen.contains("arxiv.org/pdf") && !urlToOpen.endsWith(".pdf")) {
                    urlToOpen = urlToOpen + ".pdf";
                }

                Log.d("DetailFragment", "Opening PDF URL: " + urlToOpen);

                // Open WebViewFragment
                if (getActivity() instanceof MainActivity) {
                    WebViewFragment webViewFragment = WebViewFragment.newInstance(urlToOpen);
                    ((MainActivity) getActivity()).replaceFragment(webViewFragment);
                }
            });
        }

        // DOI Click - Open in browser
        if (paperDoi != null) {
            paperDoi.setOnClickListener(v -> {
                if (doi != null && !doi.isEmpty()) {
                    String doiUrl = "https://doi.org/" + doi;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(doiUrl));
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Unable to open DOI link", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // Breadcrumb navigation
        if (textSubject != null) {
            textSubject.setOnClickListener(v -> openSubjectFragment(primaryCategoryName));
        }

        if (textSubclass != null) {
            textSubclass.setOnClickListener(v -> openSubclassFragment(primaryCategoryName, primaryCategory));
        }
    }

    private void toggleBookmark() {
        if (articleIdValue == null || articleIdValue.isEmpty()) {
            Toast.makeText(requireContext(), "Cannot bookmark: Invalid article", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bookmarkManager.isBookmarked(articleIdValue)) {
            bookmarkManager.removeBookmark(articleIdValue);
            Toast.makeText(requireContext(), R.string.bookmark_removed, Toast.LENGTH_SHORT).show();
        } else {
            BookmarkItem item = new BookmarkItem(
                    articleIdValue,
                    articleTitle != null ? articleTitle : "Untitled",
                    System.currentTimeMillis()
            );
            bookmarkManager.addBookmark(item);
            Toast.makeText(requireContext(), R.string.bookmarked, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBookmarkIcon() {
        if (bookmarkManager != null && articleIdValue != null && bookmarkManager.isBookmarked(articleIdValue)) {
            bookmarkIcon.setImageResource(R.drawable.bookmark_done);
        } else if (bookmarkIcon != null) {
            bookmarkIcon.setImageResource(R.drawable.bookmark);
        }
    }

    // Open SubjectFragment
    private void openSubjectFragment(String subjectName) {
        if (getActivity() instanceof MainActivity) {
            Fragment fragment = new SubjectFragment();
            Bundle args = new Bundle();
            args.putString("subject_name", subjectName);
            fragment.setArguments(args);
            ((MainActivity) getActivity()).replaceFragment(fragment);
        }
    }

    // Open SubclassFragment
    private void openSubclassFragment(String subjectName, String subclassName) {
        if (getActivity() instanceof MainActivity) {
            Fragment fragment = new SubclassFragment();
            Bundle args = new Bundle();
            args.putString("subject_name", subjectName);
            args.putString("subclass_name", subclassName);
            fragment.setArguments(args);
            ((MainActivity) getActivity()).replaceFragment(fragment);
        }
    }
}