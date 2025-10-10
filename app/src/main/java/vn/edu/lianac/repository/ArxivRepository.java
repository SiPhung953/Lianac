package vn.edu.lianac.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import vn.edu.lianac.models.Article;
import vn.edu.lianac.models.SearchResult;
import vn.edu.lianac.network.ArxivAPIService;
import vn.edu.lianac.utils.QueryBuilder;
import vn.edu.lianac.utils.QueryOptions;

/**
 * arXiv-specific implementation of PaperRepository.
 * Handles communication with arXiv.org API using Atom feed format.
 */
public class ArxivRepository implements PaperRepository {
    private static final String TAG = "ArxivRepository";

    private final ArxivAPIService apiService;
    private final Handler mainHandler;
    private static ArxivRepository instance;

    private QueryOptions currentQuery;
    private List<Article> currentResults;
    private SearchResult lastSearchResult;
    private boolean isLoading;

    private ArxivRepository() {
        this.apiService = ArxivAPIService.getInstance();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.currentResults = new ArrayList<>();
        this.isLoading = false;
    }

    /**
     * Get singleton instance.
     * Note: For dependency injection, consider using a factory or DI framework instead.
     */
    public static synchronized ArxivRepository getInstance() {
        if (instance == null) {
            instance = new ArxivRepository();
        }
        return instance;
    }

    // --- PaperRepository Implementation ---

    @Override
    public void search(QueryOptions options, final SearchCallback callback) {
        if (options == null || callback == null) {
            Log.e(TAG, "Options or callback is null");
            return;
        }

        isLoading = true;
        currentQuery = options;

        String queryUrl = new QueryBuilder(options).build();
        Log.d(TAG, "Searching: " + queryUrl);
        Log.d(TAG, "Start index: " + options.getStart() + ", Max results: " + options.getMaxResults());

        apiService.fetchArticlesWithMetadata(queryUrl, new ArxivAPIService.ArxivSearchResultListener() {
            @Override
            public void onSuccess(SearchResult result) {
                isLoading = false;
                currentResults = new ArrayList<>(result.getArticles());
                lastSearchResult = result;

                Log.d(TAG, "Got " + result.getArticles().size() + " articles (total: " + result.getTotalResults() + ")");

                // Check for empty results when we expect data
                if (result.getArticles().isEmpty() && result.getTotalResults() > 0) {
                    Log.w(TAG, "Empty result set received despite totalResults > 0");

                    // If this is not the first page and we got empty results,
                    // we've likely hit an API limitation
                    if (options.getStart() > 0) {
                        Log.w(TAG, "API returned empty results beyond start=" + options.getStart() + " - this may be an API limitation");
                    }
                }

                postToMain(() -> callback.onSuccess(result));
            }

            @Override
            public void onError(Exception e) {
                isLoading = false;
                Log.e(TAG, "Search error", e);
                postToMain(() -> callback.onError(e));
            }
        });
    }

    @Override
    public void loadNextPage(final SearchCallback callback) {
        if (currentQuery == null || isLoading) {
            Log.d(TAG, "Cannot load next page: no query or loading");
            return;
        }

        // Enhanced check: don't proceed if we just got empty results
        if (!hasMore() || (lastSearchResult != null && lastSearchResult.getArticles().isEmpty())) {
            Log.d(TAG, "No more pages available or last result was empty");
            return;
        }

        QueryOptions nextPage = currentQuery.nextPage();
        Log.d(TAG, "Loading next page from start: " + nextPage.getStart());
        search(nextPage, callback);
    }

    @Override
    public void loadPreviousPage(final SearchCallback callback) {
        if (currentQuery == null || isLoading || currentQuery.getStart() == 0) {
            return;
        }

        QueryOptions prevPage = currentQuery.previousPage();
        search(prevPage, callback);
    }

    @Override
    public void goToFirstPage(final SearchCallback callback) {
        if (currentQuery == null) {
            return;
        }
        QueryOptions firstPage = currentQuery.toBuilder().start(0).build();
        search(firstPage, callback);
    }

    @Override
    public void goToLastPage(final SearchCallback callback) {
        if (currentQuery == null || lastSearchResult == null) {
            return;
        }

        // Calculate the start index for the last page
        int totalResults = lastSearchResult.getTotalResults();
        int maxResults = currentQuery.getMaxResults();

        if (totalResults == 0 || maxResults == 0) {
            return;
        }

        // Last page start = total pages - 1 (zero-indexed) * items per page
        int totalPages = (int) Math.ceil((double) totalResults / maxResults);
        int lastPageStart = (totalPages - 1) * maxResults;

        QueryOptions lastPage = currentQuery.toBuilder().start(lastPageStart).build();
        search(lastPage, callback);
    }

    @Override
    public void changePageSize(int newPageSize, final SearchCallback callback) {
        if (currentQuery == null) {
            return;
        }

        if (newPageSize < 1 || newPageSize > 2000) {
            postToMain(() -> callback.onError(
                    new IllegalArgumentException("Page size must be between 1 and 2000")
            ));
            return;
        }

        // Create new query with updated page size, starting from page 1
        QueryOptions newQuery = currentQuery.toBuilder()
                .start(0)
                .maxResults(newPageSize)
                .build();

        search(newQuery, callback);
    }

    @Override
    public void refresh(SearchCallback callback) {
        if (currentQuery == null) {
            return;
        }
        QueryOptions refreshQuery = currentQuery.toBuilder().start(0).build();
        search(refreshQuery, callback);
    }

    @Override
    public List<Article> getCurrentResults() {
        return new ArrayList<>(currentResults);
    }

    @Override
    public boolean hasMore() {
        if (currentQuery == null || lastSearchResult == null) {
            return false;
        }

        int currentStart = currentQuery.getStart();
        int maxResults = currentQuery.getMaxResults();
        int totalResults = lastSearchResult.getTotalResults();

        // If we got empty results but API says there should be more,
        // we hit an API limitation - don't allow further navigation
        if (lastSearchResult.getArticles().isEmpty() && currentStart < totalResults) {
            Log.w(TAG, "Empty results detected but API claims more exist - stopping navigation");
            return false;
        }

        // Normal check for more results
        boolean hasMore = (currentStart + maxResults) < totalResults;
        Log.d(TAG, "Has more results: " + hasMore + " (start: " + currentStart + ", max: " + maxResults + ", total: " + totalResults + ")");
        return hasMore;
    }

    @Override
    public boolean isLoading() {
        return isLoading;
    }

    @Override
    public List<Article> filterResults(ArticleFilter filter) {
        if (filter == null) {
            return new ArrayList<>(currentResults);
        }

        List<Article> filtered = new ArrayList<>();
        for (Article article : currentResults) {
            if (filter.matches(article)) {
                filtered.add(article);
            }
        }
        return filtered;
    }

    @Override
    public List<Article> sortResults(Comparator<Article> comparator) {
        if (comparator == null) {
            return new ArrayList<>(currentResults);
        }

        List<Article> sorted = new ArrayList<>(currentResults);
        java.util.Collections.sort(sorted, comparator);
        return sorted;
    }

    @Override
    public void clearResults() {
        currentResults.clear();
        currentQuery = null;
        lastSearchResult = null;
    }

    @Override
    public void cancelRequests() {
        apiService.cancelAll();
        isLoading = false;
    }

    // --- Helper ---

    private void postToMain(Runnable runnable) {
        mainHandler.post(runnable);
    }

    // --- Static Factory Methods for Common Filters ---
    // These are arXiv-agnostic and could be moved to a FilterFactory class

    public static ArticleFilter byCategoryFilter(final String categoryShortName) {
        return article -> article.hasCategory(categoryShortName);
    }

    public static ArticleFilter byAuthorFilter(final String authorName) {
        return article -> article.containsAuthor(authorName);
    }

    public static ArticleFilter bySearchTermFilter(final String searchTerm) {
        return article -> article.matchesSearchTerm(searchTerm);
    }

    // --- Static Factory Methods for Common Comparators ---
    // These are arXiv-agnostic and could be moved to a ComparatorFactory class

    public static Comparator<Article> byDateComparator(final boolean descending) {
        return (a1, a2) -> {
            String date1 = a1.getPublishedDateRaw();
            String date2 = a2.getPublishedDateRaw();

            if (date1 == null) return 1;
            if (date2 == null) return -1;

            int result = date1.compareTo(date2);
            return descending ? -result : result;
        };
    }

    public static Comparator<Article> byTitleComparator() {
        return (a1, a2) -> {
            String title1 = a1.getTitle();
            String title2 = a2.getTitle();

            if (title1 == null) return 1;
            if (title2 == null) return -1;

            return title1.compareToIgnoreCase(title2);
        };
    }

    public static Comparator<Article> byAuthorComparator() {
        return (a1, a2) -> {
            List<String> authors1 = a1.getAuthors();
            List<String> authors2 = a2.getAuthors();

            if (authors1 == null || authors1.isEmpty()) return 1;
            if (authors2 == null || authors2.isEmpty()) return -1;

            return authors1.get(0).compareToIgnoreCase(authors2.get(0));
        };
    }
}