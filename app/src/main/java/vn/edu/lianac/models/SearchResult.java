package vn.edu.lianac.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a paginated search result from the arXiv API
 * Contains articles and OpenSearch pagination metadata
 */
public class SearchResult {

    private final List<Article> articles;
    private final int totalResults;    // Total number of results available
    private final int startIndex;      // Starting index of this page (0-based)
    private final int itemsPerPage;    // Number of items in this page

    public SearchResult(List<Article> articles, int totalResults, int startIndex, int itemsPerPage) {
        this.articles = articles != null ? articles : new ArrayList<>();
        this.totalResults = Math.max(0, totalResults);
        this.startIndex = Math.max(0, startIndex);
        this.itemsPerPage = Math.max(0, itemsPerPage);
    }

    // --- Getters ---

    public List<Article> getArticles() {
        return articles;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    // --- Pagination Helpers ---

    /**
     * Check if there are more results available after this page
     */
    public boolean hasMoreResults() {
        return startIndex + articles.size() < totalResults;
    }

    /**
     * Get the start index for the next page
     * Returns -1 if there is no next page
     */
    public int getNextPageStartIndex() {
        if (!hasMoreResults()) {
            return -1;
        }
        return startIndex + itemsPerPage;
    }

    /**
     * Get the start index for the previous page
     * Returns -1 if there is no previous page
     */
    public int getPreviousPageStartIndex() {
        if (startIndex == 0) {
            return -1;
        }
        return Math.max(0, startIndex - itemsPerPage);
    }

    /**
     * Check if this is the first page
     */
    public boolean isFirstPage() {
        return startIndex == 0;
    }

    /**
     * Check if this is the last page
     */
    public boolean isLastPage() {
        return !hasMoreResults();
    }

    /**
     * Get current page number (1-based for display)
     */
    public int getCurrentPageNumber() {
        if (itemsPerPage == 0) {
            return 1;
        }
        return (startIndex / itemsPerPage) + 1;
    }

    /**
     * Get total number of pages
     */
    public int getTotalPages() {
        if (itemsPerPage == 0 || totalResults == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalResults / itemsPerPage);
    }

    /**
     * Get the range of results shown (e.g., "1-25 of 1000")
     */
    public String getResultRangeString() {
        if (articles.isEmpty()) {
            return "0 results";
        }

        int endIndex = Math.min(startIndex + articles.size(), totalResults);

        if (totalResults == 1) {
            return "1 result";
        }

        return String.format("%d-%d of %d", startIndex + 1, endIndex, totalResults);
    }

    /**
     * Check if results are empty
     */
    public boolean isEmpty() {
        return articles.isEmpty();
    }

    /**
     * Get the actual number of articles in this result
     */
    public int size() {
        return articles.size();
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "articles=" + articles.size() +
                ", totalResults=" + totalResults +
                ", startIndex=" + startIndex +
                ", itemsPerPage=" + itemsPerPage +
                ", page=" + getCurrentPageNumber() + "/" + getTotalPages() +
                '}';
    }
}