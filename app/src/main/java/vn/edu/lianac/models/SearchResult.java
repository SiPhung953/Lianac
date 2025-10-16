package vn.edu.lianac.models;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {
    private final List<Article> articles;
    private final int totalResults;
    private final int startIndex;
    private final int itemsPerPage;

    public SearchResult(List<Article> articles, int totalResults, int startIndex, int itemsPerPage) {
        this.articles = articles != null ? articles : new ArrayList<>();
        this.totalResults = Math.max(0, totalResults);
        this.startIndex = Math.max(0, startIndex);
        this.itemsPerPage = Math.max(0, itemsPerPage);
    }

    public List<Article> getArticles() { return articles; }
    public int getTotalResults() { return totalResults; }
    public int getStartIndex() { return startIndex; }
    public int getItemsPerPage() { return itemsPerPage; }

    public boolean hasMoreResults() {
        return startIndex + articles.size() < totalResults;
    }

    public boolean isFirstPage() {
        return startIndex == 0;
    }

    public int getCurrentPageNumber() {
        return itemsPerPage == 0 ? 1 : (startIndex / itemsPerPage) + 1;
    }

    public int getTotalPages() {
        if (itemsPerPage == 0 || totalResults == 0) return 0;
        return (int) Math.ceil((double) totalResults / itemsPerPage);
    }

    public String getResultRangeString() {
        if (articles.isEmpty()) return "0 results";
        int end = Math.min(startIndex + articles.size(), totalResults);
        return String.format("%d-%d of %d", startIndex + 1, end, totalResults);
    }

    @Override
    public String toString() {
        return String.format("SearchResult{articles=%d, total=%d, page=%d/%d}",
                articles.size(), totalResults, getCurrentPageNumber(), getTotalPages());
    }
}