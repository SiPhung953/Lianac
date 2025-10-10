package vn.edu.lianac.repository;

import java.util.Comparator;
import java.util.List;

import vn.edu.lianac.models.Article;
import vn.edu.lianac.models.SearchResult;
import vn.edu.lianac.utils.QueryOptions;

/**
 * Generic repository interface for academic paper sources.
 * Defines the contract for searching, paginating, and managing paper data.
 *
 * Implementations might include: arXiv, PubMed, IEEE Xplore, Google Scholar, etc.
 */
public interface PaperRepository {

    // --- Core Search Operations ---

    /**
     * Perform a search with given query options
     * @param options Query parameters (terms, filters, pagination, sorting)
     * @param callback Success/error callback with results
     */
    void search(QueryOptions options, SearchCallback callback);

    /**
     * Reload current search from the beginning
     */
    void refresh(SearchCallback callback);

    // --- Pagination Operations ---

    /**
     * Load the next page of current search results
     */
    void loadNextPage(SearchCallback callback);

    /**
     * Load the previous page of current search results
     */
    void loadPreviousPage(SearchCallback callback);

    /**
     * Jump to the first page of current search results
     */
    void goToFirstPage(SearchCallback callback);

    /**
     * Jump to the last page of current search results
     */
    void goToLastPage(SearchCallback callback);

    /**
     * Change the number of results per page and restart from first page
     */
    void changePageSize(int newPageSize, SearchCallback callback);

    // --- Data Access ---

    /**
     * Get the current list of articles (from last successful search)
     */
    List<Article> getCurrentResults();

    /**
     * Check if there are more results available to load
     */
    boolean hasMore();

    /**
     * Check if currently loading data
     */
    boolean isLoading();

    // --- Filtering and Sorting (Client-side operations on cached results) ---

    /**
     * Filter current results using a custom filter
     * @param filter Predicate to test each article
     * @return Filtered copy of results (does not modify cached results)
     */
    List<Article> filterResults(ArticleFilter filter);

    /**
     * Sort current results using a custom comparator
     * @param comparator Comparator for article ordering
     * @return Sorted copy of results (does not modify cached results)
     */
    List<Article> sortResults(Comparator<Article> comparator);

    // --- Lifecycle Management ---

    /**
     * Clear all cached results and reset state
     */
    void clearResults();

    /**
     * Cancel any pending network requests
     */
    void cancelRequests();

    // --- Callback Interface ---

    interface SearchCallback {
        void onSuccess(SearchResult result);
        void onError(Exception e);
    }

    // --- Filter Interface ---

    interface ArticleFilter {
        boolean matches(Article article);
    }
}