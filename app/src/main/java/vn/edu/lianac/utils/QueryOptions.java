package vn.edu.lianac.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import vn.edu.lianac.models.SearchRow;

public class QueryOptions {
    // Basic search
    final String searchTerm;
    final String searchField; // Field for the basic search (e.g., "ti", "au", "all")

    // Pagination
    final int start;
    final int maxResults;

    // Sorting
    final String sortBy;
    final String sortOrder;

    // Advanced search fields (remains for complex, multi-field searches)
    final String title;
    final String author;
    final String category;
    final String abstractTerm;

    // Multi-row advanced search
    final List<SearchRow> rows;
    final String defaultOperator;

    // Category filtering
    final List<String> categories;

    // Date filtering
    final String dateFrom;
    final String dateTo;

    private QueryOptions(Builder builder) {
        this.searchTerm = builder.searchTerm;
        this.searchField = builder.searchField;
        this.start = builder.start;
        this.maxResults = builder.maxResults;
        this.sortBy = builder.sortBy;
        this.sortOrder = builder.sortOrder;
        this.title = builder.title;
        this.author = builder.author;
        this.category = builder.category;
        this.abstractTerm = builder.abstractTerm;
        this.rows = builder.rows != null ? Collections.unmodifiableList(new ArrayList<>(builder.rows)) : Collections.emptyList();
        this.defaultOperator = builder.defaultOperator;
        this.categories = builder.categories != null ? Collections.unmodifiableList(new ArrayList<>(builder.categories)) : Collections.emptyList();
        this.dateFrom = builder.dateFrom;
        this.dateTo = builder.dateTo;
    }

    // --- URL Building ---

    /**
     * Build the complete arXiv API URL for this query
     */
    public String buildUrl() {
        return new QueryBuilder(this).build();
    }

    // --- Getters ---
    public String getSearchTerm() { return searchTerm; }
    public String getSearchField() { return searchField; }
    public int getStart() { return start; }
    public int getMaxResults() { return maxResults; }
    public String getSortBy() { return sortBy; }
    public String getSortOrder() { return sortOrder; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public String getAbstractTerm() { return abstractTerm; }
    public List<SearchRow> getRows() { return rows; }
    public String getDefaultOperator() { return defaultOperator; }
    public List<String> getCategories() { return categories; }
    public String getDateFrom() { return dateFrom; }
    public String getDateTo() { return dateTo; }

    // --- Query Type Detection ---
    public boolean isBasicSearch() {
        // A basic search has a single search term and a target field.
        return isNonEmpty(searchTerm) && !hasAdvancedSearch() && !hasMultiRowSearch();
    }

    public boolean hasAdvancedSearch() {
        return isNonEmpty(title) || isNonEmpty(author) ||
                isNonEmpty(category) || isNonEmpty(abstractTerm);
    }

    public boolean hasMultiRowSearch() {
        return rows != null && !rows.isEmpty() &&
                rows.stream().anyMatch(SearchRow::isValid);
    }

    public boolean hasCategoryFilter() {
        return categories != null && !categories.isEmpty();
    }

    // THIS IS THE MISSING METHOD
    public boolean hasDateFilter() {
        return isNonEmpty(dateFrom) || isNonEmpty(dateTo);
    }

    public boolean hasSearchTerm() {
        return searchTerm != null && !searchTerm.trim().isEmpty();
    }

    /**
     * An empty query now means there is no valid search term.
     * The minimum length is 1 character for searchTerm.
     */
    public boolean isEmpty() {
        return (searchTerm == null || searchTerm.trim().isEmpty()) &&
                !hasAdvancedSearch() &&
                !hasMultiRowSearch() &&
                !hasCategoryFilter() &&
                !hasDateFilter();
    }

    // --- Validation ---
    public boolean isValid() {
        // A query is invalid if it's empty.
        if (isEmpty()) return false;

        // Pagination must be valid
        if (start < 0 || maxResults < 1) return false;  // Removed the maxResults > 2000 check

        // Sort parameters must be valid
        if (!isValidSortBy(sortBy)) return false;
        if (!isValidSortOrder(sortOrder)) return false;

        return true;
    }

    private boolean isValidSortBy(String sortBy) {
        return sortBy != null &&
                (sortBy.equals("relevance") ||
                        sortBy.equals("lastUpdatedDate") ||
                        sortBy.equals("submittedDate"));
    }

    private boolean isValidSortOrder(String sortOrder) {
        return sortOrder != null &&
                (sortOrder.equals("ascending") || sortOrder.equals("descending"));
    }

    // --- Copy with modifications ---
    public Builder toBuilder() {
        return new Builder()
                .searchTerm(this.searchTerm)
                .searchField(this.searchField)
                .start(this.start)
                .maxResults(this.maxResults)
                .sortBy(this.sortBy)
                .sortOrder(this.sortOrder)
                .title(this.title)
                .author(this.author)
                .category(this.category)
                .abstractTerm(this.abstractTerm)
                .rows(new ArrayList<>(this.rows))
                .defaultOperator(this.defaultOperator)
                .categories(new ArrayList<>(this.categories))
                .dateFrom(this.dateFrom)
                .dateTo(this.dateTo);
    }

    public QueryOptions withStart(int newStart) { return toBuilder().start(newStart).build(); }
    public QueryOptions nextPage() { return toBuilder().start(start + maxResults).build(); }
    public QueryOptions previousPage() { return toBuilder().start(Math.max(0, start - maxResults)).build(); }
    public QueryOptions withMaxResults(int newMaxResults) { return toBuilder().maxResults(newMaxResults).build(); }
    public QueryOptions withSort(String sortBy, String sortOrder) { return toBuilder().sortBy(sortBy).sortOrder(sortOrder).build(); }

    // --- Helpers ---
    private boolean isNonEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "QueryOptions{" +
                "searchTerm='" + searchTerm + '\'' +
                ", searchField='" + searchField + '\'' +
                ", start=" + start +
                ", maxResults=" + maxResults +
                ", sortBy='" + sortBy + '\'' +
                ", hasAdvanced=" + hasAdvancedSearch() +
                ", categories=" + (categories != null ? categories.size() : 0) +
                ", hasDateFilter=" + hasDateFilter() +
                '}';
    }

    // equals() and hashCode() remain the same, but should be updated if you need deep equality checks on the new field.

    // --- Builder ---
    public static class Builder {
        private String searchTerm = "all:a";
        private String searchField = "all"; // Defaults to "all" but can be overridden
        private int start = 0;
        private int maxResults = 25;
        private String sortBy = "lastUpdatedDate";
        private String sortOrder = "descending";
        private String title;
        private String author;
        private String category;
        private String abstractTerm;
        private List<SearchRow> rows = new ArrayList<>();
        private String defaultOperator = "AND";
        private List<String> categories = new ArrayList<>();
        private String dateFrom;
        private String dateTo;

        public Builder searchTerm(String searchTerm) {
            this.searchTerm = searchTerm;
            return this;
        }

        public Builder searchField(String field) {
            // Ensure a valid field is passed, default to "all" if null/empty
            this.searchField = (field != null && !field.trim().isEmpty()) ? field : "all";
            return this;
        }

        public Builder start(int start) { this.start = Math.max(0, start); return this; }
        public Builder maxResults(int maxResults) { this.maxResults = Math.max(1, maxResults); return this; }
        public Builder sortBy(String sortBy) { this.sortBy = sortBy; return this; }
        public Builder sortOrder(String sortOrder) { this.sortOrder = sortOrder; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder author(String author) { this.author = author; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder abstractTerm(String abs) { this.abstractTerm = abs; return this; }
        public Builder rows(List<SearchRow> rows) { this.rows = rows != null ? new ArrayList<>(rows) : new ArrayList<>(); return this; }
        public Builder addRow(SearchRow row) { if (row != null && row.isValid()) { this.rows.add(row); } return this; }
        public Builder clearRows() { this.rows.clear(); return this; }
        public Builder defaultOperator(String operator) { this.defaultOperator = operator; return this; }
        public Builder categories(List<String> categories) { this.categories = categories != null ? new ArrayList<>(categories) : new ArrayList<>(); return this; }
        public Builder addCategory(String category) { if (category != null && !category.trim().isEmpty()) { this.categories.add(category); } return this; }
        public Builder clearCategories() { this.categories.clear(); return this; }
        public Builder dateFrom(String dateFrom) { this.dateFrom = dateFrom; return this; }
        public Builder dateTo(String dateTo) { this.dateTo = dateTo; return this; }
        public Builder clearDateFilter() { this.dateFrom = null; this.dateTo = null; return this; }

        public QueryOptions build() {
            return new QueryOptions(this);
        }
    }
}
