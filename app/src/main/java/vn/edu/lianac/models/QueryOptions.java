package vn.edu.lianac.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Query options for arXiv API searches.
 * Supports both basic and advanced search modes.
 */
public class QueryOptions {
    private final String searchTerm;
    private final String searchField;
    private final List<SearchRow> rows;  // For advanced search
    private final int start;
    private final int maxResults;
    private final String sortBy;
    private final String sortOrder;
    private final List<String> categories;
    private final boolean includeCrossLists;
    private final String dateFrom;
    private final String dateTo;
    private boolean isCategoryBrowse;

    // ==================== GETTERS ====================

    public String getSearchTerm() {
        return searchTerm;
    }

    public String getSearchField() {
        return searchField;
    }

    public List<SearchRow> getRows() {
        return rows;
    }

    public int getStart() {
        return start;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public String getSortBy() {
        return sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public List<String> getCategories() {
        return categories;
    }

    public boolean isIncludeCrossLists() {
        return includeCrossLists;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public String getDateTo() {
        return dateTo;
    }

    // ==================== QUERY HELPERS ====================

    /**
     * Check if this query has a basic search term
     */
    public boolean hasSearchTerm() {
        return searchTerm != null && !searchTerm.trim().isEmpty();
    }

    /**
     * Check if this query has advanced search rows
     */
    public boolean hasRows() {
        return rows != null && !rows.isEmpty();
    }

    /**
     * Check if date filter is applied
     */
    public boolean hasDateFilter() {
        return (dateFrom != null && !dateFrom.isEmpty()) ||
                (dateTo != null && !dateTo.isEmpty());
    }

    /**
     * Check if category filter is applied
     */
    public boolean hasCategoryFilter() {
        return categories != null && !categories.isEmpty();
    }

    /**
     * Check if this is a broad search (wildcards, very short terms, etc.)
     */
    public boolean isBroadSearch() {
        // Has advanced rows - check if any are specific
        if (hasRows()) {
            boolean hasSpecificTerm = false;
            for (SearchRow row : rows) {
                String value = row.getValue();
                if (value != null && value.trim().length() > 2 && !value.contains("*")) {
                    hasSpecificTerm = true;
                    break;
                }
            }
            return !hasSpecificTerm;
        }

        // Basic search - check term
        if (!hasSearchTerm()) return true;

        String term = searchTerm.trim();

        // Very short or wildcard searches are broad
        if (term.length() <= 2 || term.contains("*") || term.equalsIgnoreCase("all")) {
            return true;
        }

        // Date-only searches without specific terms
        return hasDateFilter() && term.equalsIgnoreCase("all");
    }

    private QueryOptions(Builder builder) {
        this.searchTerm = builder.searchTerm;
        this.searchField = builder.searchField;
        this.rows = builder.rows;
        this.start = builder.start;
        this.maxResults = builder.maxResults;
        this.sortBy = builder.sortBy;
        this.sortOrder = builder.sortOrder;
        this.categories = builder.categories;
        this.includeCrossLists = builder.includeCrossLists;
        this.dateFrom = builder.dateFrom;
        this.dateTo = builder.dateTo;
        this.isCategoryBrowse = builder.isCategoryBrowse; // ADD THIS LINE
    }

    // ==================== NAVIGATION HELPERS ====================

    /**
     * Create query for next page
     */
    public QueryOptions nextPage() {
        return toBuilder().start(start + maxResults).build();
    }

    /**
     * Create query for previous page
     */
    public QueryOptions previousPage() {
        return toBuilder().start(Math.max(0, start - maxResults)).build();
    }

    public boolean isCategoryBrowse() {
        return isCategoryBrowse;
    }

    /**
     * Create a builder from this query
     */
    public Builder toBuilder() {
        return new Builder()
                .searchTerm(searchTerm)
                .searchField(searchField)
                .rows(rows)
                .start(start)
                .maxResults(maxResults)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .categories(categories)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .includeCrossLists(includeCrossLists)
                .categoryBrowse(isCategoryBrowse); // ADD THIS LINE
    }

    public Builder categoryBrowse(boolean isCategoryBrowse) {
        this.isCategoryBrowse = isCategoryBrowse;
        return this.toBuilder();
    }

    @Override
    public String toString() {
        if (hasRows()) {
            return String.format("QueryOptions{rows=%d, start=%d, max=%d}",
                    rows.size(), start, maxResults);
        } else {
            return String.format("QueryOptions{term='%s', field='%s', start=%d, max=%d}",
                    searchTerm, searchField, start, maxResults);
        }
    }

    // ==================== BUILDER ====================

    public static class Builder {
        private boolean isCategoryBrowse = false;
        private String searchTerm;
        private String searchField = "all";
        private List<SearchRow> rows = new ArrayList<>();
        private int start = 0;
        private int maxResults = 10;
        private String sortBy = "submittedDate";
        private String sortOrder = "descending";
        private List<String> categories = new ArrayList<>();
        private boolean includeCrossLists = false;
        private String dateFrom;
        private String dateTo;

        public Builder searchTerm(String term) {
            this.searchTerm = term;
            return this;
        }

        public Builder searchField(String field) {
            this.searchField = (field != null && !field.isEmpty()) ? field : "all";
            return this;
        }

        public Builder rows(List<SearchRow> rows) {
            this.rows = (rows != null) ? new ArrayList<>(rows) : new ArrayList<>();
            return this;
        }

        public Builder start(int start) {
            this.start = Math.max(0, start);
            return this;
        }

        public Builder maxResults(int max) {
            this.maxResults = Math.max(1, Math.min(max, 2000));
            return this;
        }

        public Builder sortBy(String sortBy) {
            this.sortBy = sortBy;
            return this;
        }

        public Builder sortOrder(String order) {
            this.sortOrder = order;
            return this;
        }

        public Builder categories(List<String> cats) {
            this.categories = (cats != null) ? new ArrayList<>(cats) : new ArrayList<>();
            return this;
        }

        public Builder includeCrossLists(boolean include) {
            this.includeCrossLists = include;
            return this;
        }

        public Builder dateFrom(String date) {
            this.dateFrom = date;
            return this;
        }

        public Builder dateTo(String date) {
            this.dateTo = date;
            return this;
        }

        public QueryOptions build() {
            return new QueryOptions(this);
        }

        public Builder categoryBrowse(boolean isCategoryBrowse) {
            this.isCategoryBrowse = isCategoryBrowse;
            return this;
        }
    }
}