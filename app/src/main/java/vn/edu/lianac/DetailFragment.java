package vn.edu.lianac;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import vn.edu.lianac.Download.DownloadItem.DownloadItem;
import vn.edu.lianac.Download.DownloadService;
import vn.edu.lianac.Download.DownloadState.DownloadState;
import vn.edu.lianac.Download.DownloadViewModel.DownloadViewModel;
import vn.edu.lianac.bookmark.BookmarkItem;
import vn.edu.lianac.bookmark.BookmarkManager;
import vn.edu.lianac.models.Article;
import vn.edu.lianac.ui.SubjectDetailFragment;
import vn.edu.lianac.utils.CategoryProvider;

public class DetailFragment extends Fragment {
    private static final String TAG = "DetailFragment";

    // Views
    private ImageView downloadIcon, bookmarkIcon, menuIcon;
    private TextView downloadProgress;
    private TextView readButton, textSubject, textSubclass;
    private TextView paperTitle, paperAuthor, articleIdText;
    private TextView paperSubmitted, paperSummaryText, paperDoi;
    private LinearLayout breadcrumbBar, categoriesContainer;
    private LinearLayout downloadContainer;

    // Data
    private Article article;

    // Managers
    private BookmarkManager bookmarkManager;
    private CategoryProvider categoryProvider;
    private DownloadViewModel downloadViewModel;

    // Download state tracking
    private DownloadItem currentDownloadItem;
    private DownloadResultReceiver progressReceiver;

    private class DownloadResultReceiver extends ResultReceiver {
        public DownloadResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == DownloadService.UPDATE_CODE && resultData != null) {
                long downloadId = resultData.getLong(DownloadService.EXTRA_DOWNLOAD_ID, -1);
                int progress = resultData.getInt(DownloadService.EXTRA_PROGRESS, 0);
                int status = resultData.getInt("status", -1);
                String filePath = resultData.getString(DownloadService.EXTRA_FILE_PATH);
                String url = resultData.getString(DownloadService.EXTRA_URL);
                if (downloadViewModel != null && url != null) {
                    downloadViewModel.updateDownloadProgress(downloadId, progress, status, url, filePath);
                }
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the article from arguments
        if (getArguments() != null) {
            article = getArguments().getParcelable("article");
            if (article == null) {
                Log.e(TAG, "No article provided in arguments");
            } else {
                Log.d(TAG, "Received article: " + article.getId());
            }
        }

        // Initialize progress receiver
        progressReceiver = new DownloadResultReceiver(new Handler(Looper.getMainLooper()));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initManagers();

        // Check if article exists
        if (article == null) {
            Toast.makeText(getContext(), "Error loading article", Toast.LENGTH_SHORT).show();
            return;
        }

        populateArticleData();
        setupClickListeners();
        observeDownloadState();
    }

    private void initViews(View view) {
        // Icons
        downloadIcon = view.findViewById(R.id.download_icon);
        bookmarkIcon = view.findViewById(R.id.bookmark_icon);
        downloadProgress = view.findViewById(R.id.download_progress);
        downloadContainer = view.findViewById(R.id.download_container);

        // Buttons
        readButton = view.findViewById(R.id.read_button);

        // Breadcrumb
        textSubject = view.findViewById(R.id.text_subject);
        textSubclass = view.findViewById(R.id.text_subclass);
        breadcrumbBar = view.findViewById(R.id.breadcrumb_bar);

        // Article info
        articleIdText = view.findViewById(R.id.article_id);
        paperTitle = view.findViewById(R.id.paper_title);
        paperAuthor = view.findViewById(R.id.paper_author);
        paperSubmitted = view.findViewById(R.id.paper_submitted);
        paperSummaryText = view.findViewById(R.id.paper_summary_text);
        paperDoi = view.findViewById(R.id.paper_doi);

        // Categories container
        categoriesContainer = view.findViewById(R.id.categoriesContainer);
    }

    private void initManagers() {
        bookmarkManager = new BookmarkManager(requireContext());
        categoryProvider = CategoryProvider.getInstance(requireContext());
        downloadViewModel = new ViewModelProvider(requireActivity()).get(DownloadViewModel.class);
    }

    private void populateArticleData() {
        // Set arXiv ID
        articleIdText.setText("arXiv:" + article.getId());

        // Set title
        paperTitle.setText(article.getTitle());

        // Set authors
        String authorsText = "Author(s): " + article.getFormattedAuthors();
        paperAuthor.setText(authorsText);

        // Set submitted date
        paperSubmitted.setText(article.getFormattedSubmittedDate());

        // Set summary/abstract
        if (article.getSummary() != null && !article.getSummary().isEmpty()) {
            paperSummaryText.setText(article.getSummary());
        } else {
            paperSummaryText.setText("No abstract available");
        }

        // Set DOI if available
        setupDoi();

        // Setup breadcrumb (Subject > Subclass)
        setupBreadcrumb();

        // Setup category badges
        // setupCategoryBadges();
        // TODO: Discuss whether we need the badge to display

        // Update bookmark icon
        updateBookmarkIcon();
    }

    private void setupDoi() {
        String doi = article.getDoi();
        if (doi != null && !doi.trim().isEmpty()) {
            paperDoi.setText("DOI: " + doi);
            paperDoi.setVisibility(View.VISIBLE);
            paperDoi.setOnClickListener(v -> {
                String doiUrl = "https://doi.org/" + doi;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(doiUrl));
                try {
                    startActivity(browserIntent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Cannot open DOI link", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to open DOI", e);
                }
            });
        } else {
            paperDoi.setVisibility(View.GONE);
        }
    }

    private void setupBreadcrumb() {
        String primaryCategory = article.getPrimaryCategory();

        if (primaryCategory == null || primaryCategory.isEmpty()) {
            breadcrumbBar.setVisibility(View.GONE);
            return;
        }

        String mainCategory = article.getMainCategory();
        String subCategory = article.getSubCategory();

        if (mainCategory.isEmpty()) {
            textSubject.setText(categoryProvider.getCategoryDisplayName(primaryCategory));
            textSubclass.setVisibility(View.GONE);
            View arrowSeparator = getView().findViewById(R.id.arrow_separator);
            if (arrowSeparator != null) {
                arrowSeparator.setVisibility(View.GONE);
            }
        } else {
            textSubject.setText(categoryProvider.getCategoryDisplayName(mainCategory));

            if (!subCategory.isEmpty()) {
                String fullSubcategoryId = mainCategory + "." + subCategory;
                textSubclass.setText(categoryProvider.getCategoryDisplayName(fullSubcategoryId));
                textSubclass.setVisibility(View.VISIBLE);
                View arrowSeparator = getView().findViewById(R.id.arrow_separator);
                if (arrowSeparator != null) {
                    arrowSeparator.setVisibility(View.VISIBLE);
                }
            } else {
                textSubclass.setVisibility(View.GONE);
                View arrowSeparator = getView().findViewById(R.id.arrow_separator);
                if (arrowSeparator != null) {
                    arrowSeparator.setVisibility(View.GONE);
                }
            }
        }
    }

    private void setupCategoryBadges() {
        categoriesContainer.removeAllViews();

        List<String> categories = article.getCategories();
        if (categories == null || categories.isEmpty()) {
            return;
        }

        boolean isFirst = true;
        java.util.Set<String> seenCategories = new java.util.LinkedHashSet<>();

        for (String categoryId : categories) {
            String normalizedCategoryId = normalizeCategoryId(categoryId);
            String displayName = categoryProvider.getCategoryDisplayName(normalizedCategoryId);

            if (displayName.equals(normalizedCategoryId) || !seenCategories.add(normalizedCategoryId)) {
                continue;
            }

            TextView badge = createCategoryBadge(normalizedCategoryId, displayName, isFirst);
            if (badge != null) {
                categoriesContainer.addView(badge);
                isFirst = false;
            }
        }
    }

    private String normalizeCategoryId(String categoryId) {
        if (categoryId == null) return null;
        if ("math.MP".equals(categoryId)) {
            return "math-ph";
        }
        return categoryId;
    }

    private TextView createCategoryBadge(String categoryId, String displayName, boolean isFirst) {
        TextView badge = new TextView(requireContext());
        badge.setText(categoryId);
        badge.setTextSize(11f);

        if (isFirst) {
            badge.setTextColor(getResources().getColor(android.R.color.white));
            badge.setBackgroundResource(R.drawable.category_first_badge_background);
        } else {
            badge.setTextColor(getResources().getColor(android.R.color.black));
            badge.setBackgroundResource(R.drawable.category_normal_badge_background);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int marginPx = dpToPx(4);
        params.setMargins(0, 0, marginPx, 0);
        badge.setLayoutParams(params);

        badge.setClickable(true);
        badge.setFocusable(true);
        badge.setOnClickListener(v -> {
            Toast.makeText(requireContext(), displayName, Toast.LENGTH_SHORT).show();
        });

        return badge;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void setupClickListeners() {
        // Breadcrumb clicks
        textSubject.setOnClickListener(v -> {
            String mainCategory = article.getMainCategory();
            if (!mainCategory.isEmpty()) {
                String displayName = categoryProvider.getCategoryDisplayName(mainCategory);
                SubjectDetailFragment fragment = SubjectDetailFragment.newInstance(
                        mainCategory,
                        displayName,
                        new ArrayList<>()
                );

                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).replaceFragment(fragment);
                }
            }
        });

        textSubclass.setOnClickListener(v -> {
            String subCategory = article.getSubCategory();
            if (!subCategory.isEmpty()) {
                String mainCategory = article.getMainCategory();
                String fullSubcategoryId = mainCategory + "." + subCategory;
                String displayName = categoryProvider.getCategoryDisplayName(fullSubcategoryId);

                // Build breadcrumb path containing the parent (main category)
                ArrayList<SubjectDetailFragment.BreadcrumbItem> breadcrumbPath = new ArrayList<>();
                breadcrumbPath.add(new SubjectDetailFragment.BreadcrumbItem(
                        mainCategory,
                        categoryProvider.getCategoryDisplayName(mainCategory)
                ));

                // Navigate to subcategory with parent in breadcrumb path
                SubjectDetailFragment fragment = SubjectDetailFragment.newInstance(
                        fullSubcategoryId,
                        displayName,
                        breadcrumbPath
                );

                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).replaceFragment(fragment);
                }
            }
        });

        // Bookmark icon
        if (bookmarkIcon != null) {
            bookmarkIcon.setOnClickListener(v -> {
                toggleBookmark();
                updateBookmarkIcon();
            });
        }

        // Download container (handles all download states)
        if (downloadContainer != null) {
            downloadContainer.setOnClickListener(v -> handleDownloadClick());
        }

        // Read button
        if (readButton != null) {
            readButton.setOnClickListener(v -> openPdfInWebView());
        }
    }

    private void observeDownloadState() {
        // Observe download list to update UI based on current download state
        downloadViewModel.downloadList.observe(getViewLifecycleOwner(), downloadItems -> {
            if (downloadItems != null && article.getPdfUrl() != null) {
                // Find if this article is in the download list
                currentDownloadItem = null;
                for (DownloadItem item : downloadItems) {
                    if (item.getUrl().equals(article.getPdfUrl())) {
                        currentDownloadItem = item;
                        break;
                    }
                }

                // Update download button UI based on state
                updateDownloadButtonUI();
            }
        });
    }

    private void updateDownloadButtonUI() {
        if (currentDownloadItem == null) {
            // Not downloaded - show download icon
            downloadIcon.setImageResource(R.drawable.ic_download);
            downloadIcon.setVisibility(View.VISIBLE);
            downloadProgress.setVisibility(View.GONE);
            downloadIcon.setEnabled(true);
        } else {
            DownloadState state = currentDownloadItem.getState();
            int progress = currentDownloadItem.getProgressPercentage();

            switch (state) {
                case NOT_DOWNLOADED:
                    downloadIcon.setImageResource(R.drawable.ic_download);
                    downloadIcon.setVisibility(View.VISIBLE);
                    downloadProgress.setVisibility(View.GONE);
                    downloadIcon.setEnabled(true);
                    break;

                case QUEUED:
                case DOWNLOADING:
                    // For users who want to be able to cancel downloads, they have to go directly to the Download Fragment
                    downloadIcon.setImageResource(R.drawable.ic_download);
                    downloadIcon.setVisibility(View.VISIBLE);
                    downloadProgress.setVisibility(View.VISIBLE);
                    downloadProgress.setText(progress + "%");
                    downloadIcon.setEnabled(true);
                    break;

                case COMPLETED:
                    // Show check mark icon
                    downloadIcon.setImageResource(R.drawable.ic_downloaddone);
                    downloadIcon.setVisibility(View.VISIBLE);
                    downloadProgress.setVisibility(View.GONE);
                    downloadIcon.setEnabled(false);
                    break;

                case FAILED:
                    // Show retry icon, as long as the state of Download Item remain in database, I suppose
                    downloadIcon.setImageResource(R.drawable.ic_retry);
                    downloadIcon.setVisibility(View.VISIBLE);
                    downloadProgress.setVisibility(View.VISIBLE);
                    downloadProgress.setText(R.string.state_failed);
                    downloadIcon.setEnabled(true);
                    break;

                case REMOVED:
                    // Treat as not downloaded
                    downloadIcon.setImageResource(R.drawable.ic_download);
                    downloadIcon.setVisibility(View.VISIBLE);
                    downloadProgress.setVisibility(View.GONE);
                    downloadIcon.setEnabled(true);
                    break;
            }
        }
    }

    private void handleDownloadClick() {
        String pdfUrl = article.getPdfUrl();

        if (pdfUrl == null || pdfUrl.isEmpty()) {
            Toast.makeText(getContext(), R.string.no_pdf_link_available, Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentDownloadItem == null) {
            // Start new download immediately
            startNewDownload(pdfUrl);
        } else {
            DownloadState state = currentDownloadItem.getState();

            switch (state) {
                case NOT_DOWNLOADED:
                case FAILED:
                case REMOVED:
                    // Start/retry download
                    startDownload(currentDownloadItem);
                    break;

                case DOWNLOADING:
                case QUEUED:
                    // Cancel download
                    downloadViewModel.handleDownloadAction(currentDownloadItem);
                    Toast.makeText(getContext(), "Download cancelled", Toast.LENGTH_SHORT).show();
                    break;

                case COMPLETED:
                    // Already downloaded
                    Toast.makeText(getContext(), "Already downloaded, press READ to open", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    private void startNewDownload(String pdfUrl) {
        // Create new download item with article title
        DownloadItem newItem = new DownloadItem(pdfUrl, article.getTitle(), DownloadState.QUEUED);
        newItem.setProgressPercentage(0);

        // Insert into database
        downloadViewModel.mRepository.insert(newItem);

        // Start download immediately after a short delay to let database insert complete
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (currentDownloadItem != null) {
                startDownload(currentDownloadItem);
            }
        }, 200);

        Toast.makeText(getContext(), "Download started", Toast.LENGTH_SHORT).show();
    }

    private void startDownload(DownloadItem item) {
        Intent intent = new Intent(getActivity(), DownloadService.class);
        intent.setAction(DownloadService.ACTION_DOWNLOAD);
        intent.putExtra(DownloadService.EXTRA_URL, item.getUrl());
        intent.putExtra(DownloadService.EXTRA_FILE_NAME, item.getPaperName());
        intent.putExtra(DownloadService.EXTRA_RECEIVER, progressReceiver);

        if (getActivity() != null) {
            getActivity().startService(intent);
        }

        // Update state to DOWNLOADING
        item.setState(DownloadState.DOWNLOADING);
        downloadViewModel.mRepository.update(item);
    }

    private void toggleBookmark() {
        String articleId = article.getId();

        if (bookmarkManager.isBookmarked(articleId)) {
            bookmarkManager.removeBookmark(articleId);
            Toast.makeText(requireContext(), R.string.bookmark_removed, Toast.LENGTH_SHORT).show();
        } else {
            BookmarkItem item = new BookmarkItem(
                    articleId,
                    article.getTitle(),
                    System.currentTimeMillis()
            );
            bookmarkManager.addBookmark(item);
            Toast.makeText(requireContext(), R.string.bookmarked, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBookmarkIcon() {
        if (bookmarkManager != null && bookmarkManager.isBookmarked(article.getId())) {
            bookmarkIcon.setImageResource(R.drawable.ic_bookmark_fill);
        } else if (bookmarkIcon != null) {
            bookmarkIcon.setImageResource(R.drawable.ic_bookmark);
        }
    }

    private void openPdfInWebView() {
        String pdfUrl = article.getPdfUrl();

        if (pdfUrl == null || pdfUrl.isEmpty()) {
            Toast.makeText(getContext(), R.string.no_pdf_link_available, Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigate to PdfViewerFragment
        WebViewerFragment pdfFragment = new WebViewerFragment();
        Bundle args = new Bundle();
        args.putString("pdf_url", pdfUrl);
        args.putString("article_title", article.getTitle());
        pdfFragment.setArguments(args);

        if (getActivity() instanceof MainActivity) {
            try {
                ((MainActivity) getActivity()).replaceFragment(pdfFragment);
            } catch (NoSuchMethodError e) {
                try {
                    ((MainActivity) getActivity()).replaceFragment(pdfFragment);
                } catch (Exception ex) {
                    Log.e(TAG, "Failed to navigate to PDF viewer", ex);
                    openPdfExternal(pdfUrl);
                }
            }
        }
    }

    private void openPdfExternal(String pdfUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(pdfUrl), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), R.string.no_app_found_to_open_pdf, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to open PDF", e);
        }
    }
}
