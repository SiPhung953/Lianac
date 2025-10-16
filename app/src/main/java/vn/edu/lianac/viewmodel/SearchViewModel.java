package vn.edu.lianac.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import vn.edu.lianac.models.Article;
import vn.edu.lianac.models.QueryOptions;
import vn.edu.lianac.models.SearchResult;
import vn.edu.lianac.network.ArxivAPIService;
import vn.edu.lianac.utils.QueryBuilder;

/**
 * ViewModel for arXiv article search with integrated repository logic.
 * Simplified architecture: combines ViewModel and Repository responsibilities.
 */
public class SearchViewModel extends ViewModel {
    private static final String TAG = "SearchViewModel";
    private static final int MAX_BROAD_SEARCH_PAGE = 8;
    private static final int MAX_SPECIFIC_SEARCH_PAGE = 100;

    private final ArxivAPIService apiService;

    // UI State
    private final MutableLiveData<List<Article>> articles = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Pagination State
    private final MutableLiveData<Integer> totalResults = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> currentPage = new MutableLiveData<>(1);
    private final MutableLiveData<Integer> totalPages = new MutableLiveData<>(0);
    private final MutableLiveData<String> resultRangeText = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> hasNextPage = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> hasPreviousPage = new MutableLiveData<>(false);

    // Current query tracking - NOW OBSERVABLE!
    private final MutableLiveData<QueryOptions> currentQuery = new MutableLiveData<>();
    private SearchResult lastSearchResult;
    private boolean hasLoadedInitialData = false;

    public SearchViewModel() {
        this.apiService = ArxivAPIService.getInstance();
    }

    // ============= PUBLIC API =============

    public void search(QueryOptions options) {
        if (options == null) {
            errorMessage.postValue("Invalid search options");
            return;
        }

        // Validate wildcard searches (only if search term exists)
        String searchTerm = options.getSearchTerm();
        if (searchTerm != null && !searchTerm.trim().isEmpty() && searchTerm.trim().startsWith("*")) {
            errorMessage.postValue("Searches cannot start with a wildcard (*)");
            return;
        }

        currentQuery.postValue(options);
        isLoading.postValue(true);
        errorMessage.postValue(null);

        String queryUrl = new QueryBuilder(options).build();

        apiService.fetchArticlesWithMetadata(queryUrl, new ArxivAPIService.ArxivSearchResultListener() {
            @Override
            public void onSuccess(SearchResult result) {
                lastSearchResult = result;
                updateUIState(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                handleError(e);
                isLoading.postValue(false);
            }
        });
    }

    public LiveData<QueryOptions> getCurrentQuery() {
        return currentQuery;
    }

    public void loadInitialData() {
        if (hasLoadedInitialData || hasResults()) {
            return;
        }

        hasLoadedInitialData = true;
        QueryOptions defaultQuery = new QueryOptions.Builder()
                .searchTerm("all")
                .searchField("all")
                .start(0)
                .maxResults(10)
                .sortBy("lastUpdatedDate")
                .sortOrder("descending")
                .build();

        search(defaultQuery);
    }

    /**
     * Search articles by category with specific sort order
     */
    public void searchByCategory(String categoryId, String sortBy) {
        List<String> categoryList = new ArrayList<>();
        categoryList.add(categoryId);

        QueryOptions query = new QueryOptions.Builder()
                .categories(categoryList)
                .sortBy(sortBy)
                .sortOrder("descending")
                .maxResults(25)
                .start(0)
                .build();

        search(query);
    }

    /**
     * Reset to default all:all query
     */
    public void resetToDefault() {
        QueryOptions defaultQuery = new QueryOptions.Builder()
                .searchTerm("all")
                .searchField("all")
                .sortBy("submittedDate")
                .sortOrder("descending")
                .maxResults(10)
                .start(0)
                .build();

        search(defaultQuery);
    }

    public void nextPage() {
        QueryOptions current = currentQuery.getValue();
        if (!Boolean.TRUE.equals(hasNextPage.getValue()) || current == null) {
            return;
        }

        // Check page limit for broad searches
        Integer page = currentPage.getValue();
        if (page != null && page >= getMaxReliablePage()) {
            errorMessage.postValue("Cannot navigate beyond page " + getMaxReliablePage() +
                    " for broad searches. Try a more specific search term.");
            return;
        }

        // Check if last result was empty
        if (lastSearchResult != null && lastSearchResult.getArticles().isEmpty()) {
            errorMessage.postValue("No more results available");
            return;
        }

        search(current.nextPage());
    }

    public void previousPage() {
        QueryOptions current = currentQuery.getValue();
        if (!Boolean.TRUE.equals(hasPreviousPage.getValue()) || current == null) {
            return;
        }
        search(current.previousPage());
    }

    public void goToFirstPage() {
        QueryOptions current = currentQuery.getValue();
        if (current == null || Boolean.FALSE.equals(hasPreviousPage.getValue())) {
            return;
        }
        search(current.toBuilder().start(0).build());
    }

    public void goToLastPage() {
        QueryOptions current = currentQuery.getValue();
        if (current == null || lastSearchResult == null || Boolean.FALSE.equals(hasNextPage.getValue())) {
            return;
        }

        int totalResults = lastSearchResult.getTotalResults();
        int maxResults = current.getMaxResults();
        int lastPageStart = ((totalResults - 1) / maxResults) * maxResults;

        search(current.toBuilder().start(lastPageStart).build());
    }

    public void updatePageSize(int pageSize) {
        if (pageSize < 1 || pageSize > 2000) {
            errorMessage.postValue("Page size must be between 1 and 2000");
            return;
        }
        QueryOptions current = currentQuery.getValue();
        if (current == null) {
            return;
        }

        search(current.toBuilder()
                .start(0)
                .maxResults(pageSize)
                .build());
    }

    public void updateSort(String sortBy, String sortOrder) {
        QueryOptions current = currentQuery.getValue();
        if (current == null) {
            return;
        }

        search(current.toBuilder()
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .start(0)
                .build());
    }

    public void refresh() {
        QueryOptions current = currentQuery.getValue();
        if (current != null) {
            search(current.toBuilder().start(0).build());
        }
    }

    public void updateQueryOptions(QueryOptions options) {
        currentQuery.postValue(options);
    }

    public void clearResults() {
        currentQuery.postValue(null);
        lastSearchResult = null;
        articles.postValue(new ArrayList<>());
        totalResults.postValue(0);
        currentPage.postValue(1);
        totalPages.postValue(0);
        resultRangeText.postValue("");
        hasNextPage.postValue(false);
        hasPreviousPage.postValue(false);
        errorMessage.postValue(null);
    }

    public void clearError() {
        errorMessage.postValue(null);
    }

    // ============= LIVEDATA GETTERS =============

    public LiveData<List<Article>> getArticles() {
        return articles;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Integer> getTotalResults() {
        return totalResults;
    }

    public LiveData<Integer> getCurrentPage() {
        return currentPage;
    }

    public LiveData<Integer> getTotalPages() {
        return totalPages;
    }

    public LiveData<String> getResultRangeText() {
        return resultRangeText;
    }

    public LiveData<Boolean> getHasNextPage() {
        return hasNextPage;
    }

    public LiveData<Boolean> getHasPreviousPage() {
        return hasPreviousPage;
    }

    // ============= HELPER METHODS =============

    private void updateUIState(SearchResult result) {
        if (result == null) {
            return;
        }

        // Warn about empty results
        if (result.getArticles().isEmpty() && result.getTotalResults() > 0) {
            errorMessage.postValue("API returned no results. Query may be too broad for deep pagination.");
        }

        articles.postValue(result.getArticles());
        totalResults.postValue(result.getTotalResults());
        currentPage.postValue(result.getCurrentPageNumber());
        totalPages.postValue(result.getTotalPages());
        resultRangeText.postValue(result.getResultRangeString());
        hasNextPage.postValue(result.hasMoreResults());
        hasPreviousPage.postValue(!result.isFirstPage());
    }

    private void handleError(Exception e) {

        String message = "Failed to load articles";

        if (e instanceof ArxivAPIService.NetworkException) {
            message = "Network error. Please check your connection.";
        } else if (e instanceof ArxivAPIService.HttpException) {
            ArxivAPIService.HttpException httpEx = (ArxivAPIService.HttpException) e;
            if (httpEx.getCode() == 503) {
                message = "Service temporarily unavailable. Please try again later.";
            } else if (httpEx.getCode() >= 500) {
                message = "Server error. Please try again later.";
            } else if (httpEx.getCode() == 429) {
                message = "Too many requests. Please wait and try again.";
            } else {
                message = "Failed to load articles (Error " + httpEx.getCode() + ")";
            }
        } else if (e instanceof ArxivAPIService.ParseException) {
            message = "Failed to parse results. Please try again.";
        } else if (e.getMessage() != null && !e.getMessage().isEmpty()) {
            message = e.getMessage();
        }

        errorMessage.postValue(message);
    }

    private boolean hasResults() {
        List<Article> current = articles.getValue();
        return current != null && !current.isEmpty();
    }

    public int getMaxReliablePage() {
        return isBroadSearch() ? MAX_BROAD_SEARCH_PAGE : MAX_SPECIFIC_SEARCH_PAGE;
    }

    public boolean isBroadSearch() {
        QueryOptions current = currentQuery.getValue();
        if (current == null) return false;

        String term = current.getSearchTerm();

        // Filter-only searches (no term) are considered specific if they have category/date filters
        if (term == null || term.trim().isEmpty()) {
            return !current.hasCategoryFilter() && !current.hasDateFilter();
        }

        // Wildcard searches
        if (term.contains("all:*") || term.equals("*")) return true;

        // Very short searches
        if (term.replaceAll("\\s+", "").length() <= 2) return true;

        // Generic terms
        if (term.matches("(?i).*(\\ball\\b|\\ba\\b).*")) return true;

        // Date-only searches (deprecated now that we support filter-only)
        if (current.hasDateFilter() && term.equalsIgnoreCase("all")) {
            return true;
        }

        return false;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        apiService.cancelAll();
    }
}