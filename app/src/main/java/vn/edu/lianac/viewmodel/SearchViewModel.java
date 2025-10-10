package vn.edu.lianac.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import vn.edu.lianac.models.Article;
import vn.edu.lianac.models.SearchResult;
import vn.edu.lianac.network.ArxivAPIService;
import vn.edu.lianac.repository.ArxivRepository;
import vn.edu.lianac.repository.PaperRepository;
import vn.edu.lianac.utils.QueryOptions;

/**
 * ViewModel for managing article search and listing state.
 * Now works with any PaperRepository implementation (arXiv, PubMed, etc.)
 * Handles communication between UI and Repository layer.
 */
public class SearchViewModel extends ViewModel {

    private final PaperRepository repository;

    // LiveData for UI state
    private final MutableLiveData<List<Article>> articles = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);

    // Pagination state
    private final MutableLiveData<Integer> totalResults = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> currentPage = new MutableLiveData<>(1);
    private final MutableLiveData<Integer> totalPages = new MutableLiveData<>(0);
    private final MutableLiveData<String> resultRangeText = new MutableLiveData<>("");

    // Navigation state
    private final MutableLiveData<Boolean> hasNextPage = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> hasPreviousPage = new MutableLiveData<>(false);

    // Track current query for sorting
    public QueryOptions currentQuery;

    private boolean hasLoadedInitialData = false;


    /**
     * Default constructor - uses arXiv repository
     * For production apps, prefer constructor injection with a DI framework
     */
    public SearchViewModel() {
        this(ArxivRepository.getInstance());
    }

    /**
     * Constructor with repository injection (for testing or multiple sources)
     * @param repository Any implementation of PaperRepository
     */
    public SearchViewModel(PaperRepository repository) {
        this.repository = repository;
    }

    // --- Public API ---

    /**
     * Perform a search with given query options
     */
    public void search(QueryOptions options) {
        if (options == null) {
            errorMessage.setValue("Invalid search options");
            return;
        }

        // Check for wildcard searches at the start
        String searchTerm = options.getSearchTerm();
        if (searchTerm != null && searchTerm.trim().startsWith("*")) {
            errorMessage.setValue("Searches cannot start with a wildcard (*). Please add text before the wildcard.");
            return;
        }

        // Store current query for sorting operations
        currentQuery = options;

        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.search(options, new PaperRepository.SearchCallback() {
            @Override
            public void onSuccess(SearchResult result) {
                updateState(result);
                isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                handleError(e);
                isLoading.setValue(false);
            }
        });
    }

    public void loadInitialData() {
        // Only load default data on the very first launch
        if (!hasLoadedInitialData && (getArticles().getValue() == null || getArticles().getValue().isEmpty())) {
            hasLoadedInitialData = true;

            // Search for recent papers without hardcoded date filters
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
    }

    /**
     * Load next page of results
     */
    public void loadNextPage() {
        if (!Boolean.TRUE.equals(hasNextPage.getValue())) {
            return;
        }

        // Prevent navigation beyond reliable pages for broad searches
        Integer currentPage = getCurrentPage().getValue();
        if (currentPage != null && currentPage >= getMaxReliablePage()) {
            errorMessage.setValue("Cannot navigate beyond page " + getMaxReliablePage() +
                    " for broad searches. Try a more specific search term.");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.loadNextPage(new PaperRepository.SearchCallback() {
            @Override
            public void onSuccess(SearchResult result) {
                // Check if we got empty results
                if (result.getArticles().isEmpty() && result.getTotalResults() > 0) {
                    errorMessage.setValue("API returned no results. This query may be too broad for deep pagination.");
                    isLoading.setValue(false);
                    return;
                }
                updateState(result);
                isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                handleError(e);
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Calls loadNextPage
     */
    public void nextPage() {
        loadNextPage();
    }

    /**
     * Load previous page of results
     */
    public void loadPreviousPage() {
        if (!Boolean.TRUE.equals(hasPreviousPage.getValue())) {
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.loadPreviousPage(new PaperRepository.SearchCallback() {
            @Override
            public void onSuccess(SearchResult result) {
                updateState(result);
                isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                handleError(e);
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Calls loadPreviousPage
     */
    public void previousPage() {
        loadPreviousPage();
    }


    public void goToFirstPage() {
        if (isFirstPage()) {
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.goToFirstPage(new PaperRepository.SearchCallback() {
            @Override
            public void onSuccess(SearchResult result) {
                updateState(result);
                isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                handleError(e);
                isLoading.setValue(false);
            }
        });
    }

    public void goToLastPage() {
        if (isLastPage()) {
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.goToLastPage(new PaperRepository.SearchCallback() {
            @Override
            public void onSuccess(SearchResult result) {
                updateState(result);
                isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                handleError(e);
                isLoading.setValue(false);
            }
        });
    }

    public void changePageSize(int newPageSize) {
        if (newPageSize < 1 || newPageSize > 2000) {
            errorMessage.setValue("Invalid page size");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.changePageSize(newPageSize, new PaperRepository.SearchCallback() {
            @Override
            public void onSuccess(SearchResult result) {
                updateState(result);
                isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                handleError(e);
                isLoading.setValue(false);
            }
        });
    }

    public void updatePageSize(int pageSize) {
        changePageSize(pageSize);
    }

    /**
     * Refresh current results (reload from start)
     */
    public void refresh() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.refresh(new PaperRepository.SearchCallback() {
            @Override
            public void onSuccess(SearchResult result) {
                updateState(result);
                isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                handleError(e);
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Set sort order and refresh search
     */
    public void setSortOrder(String sortBy, String sortOrder) {
        if (currentQuery != null) {
            QueryOptions newOptions = currentQuery.toBuilder()
                    .sortBy(sortBy)
                    .sortOrder(sortOrder)
                    .start(0) // Reset to first page when changing sort
                    .build();
            search(newOptions);
        }
    }

    public void updateSort(String sortBy, String sortOrder) {
        setSortOrder(sortBy, sortOrder);
    }

    /**
     * Refresh search with current query
     */
    public void refreshSearch() {
        if (currentQuery != null) {
            search(currentQuery);
        }
    }

    /**
     * Clear all results and reset state
     */
    public void clearResults() {
        repository.clearResults();
        currentQuery = null;
        articles.setValue(new ArrayList<>());
        totalResults.setValue(0);
        currentPage.setValue(1);
        totalPages.setValue(0);
        resultRangeText.setValue("");
        hasNextPage.setValue(false);
        hasPreviousPage.setValue(false);
        errorMessage.setValue(null);
    }

    /**
     * Clear error message
     */
    public void clearError() {
        errorMessage.setValue(null);
    }

    // --- LiveData Getters ---

    public LiveData<List<Article>> getArticles() {
        return articles;
    }

    public LiveData<List<Article>> getSearchResults() {
        return getArticles();
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getError() {
        return getErrorMessage();
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

    public QueryOptions getCurrentQuery() {
        return currentQuery;
    }

    public int getMaxReliablePage() {
        return isBroadSearch() ? 8 : 100; // Broad searches limited to 8 pages, specific searches to 100
    }

    // --- Helper Methods ---

    /**
     * Update all state from a SearchResult
     */
    private void updateState(SearchResult result) {
        if (result == null) {
            Log.w("SearchViewModel", "updateState called with null result");
            return;
        }

        if (result.getArticles() == null || result.getArticles().isEmpty()) {
            Log.w("SearchViewModel", "updateState called with empty articles. " +
                    "Total results: " + result.getTotalResults() +
                    ", Current page: " + result.getCurrentPageNumber());
        }

        // Update article list
        articles.setValue(result.getArticles());

        // Update pagination metadata
        totalResults.setValue(result.getTotalResults());
        currentPage.setValue(result.getCurrentPageNumber());
        totalPages.setValue(result.getTotalPages());
        resultRangeText.setValue(result.getResultRangeString());

        // Update navigation state
        hasNextPage.setValue(result.hasMoreResults());
        hasPreviousPage.setValue(!result.isFirstPage());
    }

    /**
     * Handle errors and set appropriate error message
     */
    private void handleError(Exception e) {
        if (e == null) {
            errorMessage.setValue("Unknown error occurred");
            return;
        }

        Log.e("SearchViewModel", "Error loading articles", e);

        String message = e.getMessage();

        // Provide user-friendly error messages
        if (e instanceof ArxivAPIService.NetworkException) {
            message = "Network error. Please check your connection.";
        } else if (e instanceof ArxivAPIService.HttpException) {
            ArxivAPIService.HttpException httpEx = (ArxivAPIService.HttpException) e;
            if (httpEx.getCode() == 503) {
                message = "Service is temporarily unavailable. Please try again later.";
            } else if (httpEx.getCode() >= 500) {
                message = "Server error. Please try again later.";
            } else if (httpEx.getCode() == 429) {
                message = "Too many requests. Please wait and try again.";
            } else {
                message = "Failed to load articles (Error " + httpEx.getCode() + ")";
            }
        } else if (e instanceof ArxivAPIService.ParseException) {
            message = "Failed to parse results. Please try again.";
        } else if (message == null || message.isEmpty()) {
            message = "Failed to load articles";
        }

        errorMessage.setValue(message);
    }

    /**
     * Check if there are any results
     */
    public boolean hasResults() {
        List<Article> currentArticles = articles.getValue();
        return currentArticles != null && !currentArticles.isEmpty();
    }

    /**
     * Check if currently on first page
     */
    public boolean isFirstPage() {
        Boolean hasPrev = hasPreviousPage.getValue();
        return hasPrev == null || !hasPrev;
    }

    /**
     * Check if currently on last page
     */
    public boolean isLastPage() {
        Boolean hasNext = hasNextPage.getValue();
        return hasNext == null || !hasNext;
    }

    public boolean isBroadSearch() {
        if (currentQuery == null) return false;

        String term = currentQuery.getSearchTerm();
        if (term == null) return false;

        // Check for wildcard searches
        if (term.contains("all:*") || term.equals("*")) return true;

        // Single character searches are very broad
        String cleanTerm = term.replaceAll("\\s+", "");
        if (cleanTerm.length() <= 2) return true;

        // Common words that match everything
        if (term.matches("(?i).*(\\ball\\b|\\ba\\b).*")) return true;

        // Date-only searches without specific terms are broad
        // If we only have date filters and a generic "all" search term, it's broad
        if (currentQuery.hasDateFilter() &&
                (term.equalsIgnoreCase("all") || term.isEmpty() || term.trim().isEmpty())) {
            return true;
        }

        return false;
    }
}