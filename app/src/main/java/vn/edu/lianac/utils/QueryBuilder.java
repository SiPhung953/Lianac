package vn.edu.lianac.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import vn.edu.lianac.models.QueryOptions;
import vn.edu.lianac.models.SearchRow;

/**
 * Query builder for arXiv API supporting both basic and advanced searches.
 * <p>
 * Basic search: single search term + field
 * Example: all:quantum or ti:"machine learning"
 * <p>
 * Advanced search: multiple search rows with boolean operators
 * Example: ti:quantum AND au:einstein OR abs:relativity
 * <p>
 * Filter-only search: categories and/or date ranges without search terms
 * Example: cat:cs.AI AND submittedDate:[20200101 TO 20231231]
 */
public class QueryBuilder {
    private static final String BASE_URL = "https://export.arxiv.org/api/query";
    private final QueryOptions options;

    public QueryBuilder(QueryOptions options) {
        this.options = options;
    }

    /**
     * Static convenience method
     */
    public static String buildUrl(QueryOptions options) {
        return new QueryBuilder(options).build();
    }

    public String build() {
        String searchQuery = buildSearchQuery();
        String encoded = urlEncode(searchQuery);

        return String.format("%s?search_query=%s&start=%d&max_results=%d&sortBy=%s&sortOrder=%s",
                BASE_URL, encoded,
                options.getStart(),
                Math.min(options.getMaxResults(), 2000),
                options.getSortBy(),
                options.getSortOrder());
    }

    private String buildSearchQuery() {
        List<String> queryParts = new ArrayList<>();

        // Build the main search query (basic term + advanced rows combined)
        String mainSearchQuery = buildCombinedSearchQuery();
        if (mainSearchQuery != null && !mainSearchQuery.trim().isEmpty()) {
            queryParts.add(mainSearchQuery);
        }

        // Add category filter
        if (options.hasCategoryFilter()) {
            String categoryQuery = buildCategoryQuery();
            if (categoryQuery != null && !categoryQuery.trim().isEmpty()) {
                queryParts.add(categoryQuery);
            }
        }

        // Add date filter
        if (options.hasDateFilter()) {
            String dateQuery = buildDateQuery();
            if (dateQuery != null && !dateQuery.trim().isEmpty()) {
                queryParts.add(dateQuery);
            }
        }

        // If nothing was built, return wildcard
        if (queryParts.isEmpty()) {
            return "all:*";
        }

        // Join all parts with AND
        return String.join(" AND ", queryParts);
    }

    /**
     * Combine basic search term with advanced search rows
     */
    private String buildCombinedSearchQuery() {
        List<String> parts = new ArrayList<>();

        // Add basic search term first (if exists)
        if (options.hasSearchTerm()) {
            String basicQuery = buildBasicSearchQuery();
            if (basicQuery != null && !basicQuery.trim().isEmpty()) {
                parts.add(basicQuery);
            }
        }

        // Add advanced search rows
        if (options.hasRows() && options.getRows() != null && !options.getRows().isEmpty()) {
            String advancedQuery = buildAdvancedSearchQuery();
            if (advancedQuery != null && !advancedQuery.trim().isEmpty()) {
                // If we have a basic term, connect with appropriate operator
                if (!parts.isEmpty()) {
                    List<SearchRow> rows = options.getRows();
                    if (rows != null && !rows.isEmpty()) {
                        String firstOperator = rows.get(0).getOperator();
                        if (firstOperator != null && !firstOperator.trim().isEmpty()) {
                            if ("NOT".equalsIgnoreCase(firstOperator)) {
                                parts.add("AND NOT");
                            } else {
                                parts.add(firstOperator.toUpperCase());
                            }
                        } else {
                            parts.add("AND");
                        }
                    }
                }
                parts.add(advancedQuery);
            }
        }

        // Return null if nothing was built (let buildSearchQuery handle it)
        if (parts.isEmpty()) {
            return null;
        }

        return String.join(" ", parts);
    }


    /**
     * Build basic search query: single search term + field
     * Example: all:quantum or ti:"machine learning"
     */
    private String buildBasicSearchQuery() {
        String term = options.getSearchTerm();
        String field = options.getSearchField();

        if (term == null || term.trim().isEmpty()) {
            return null;
        }

        term = term.trim();

        // Quote multi-word searches
        if (term.contains(" ") && !term.startsWith("\"")) {
            term = "\"" + term + "\"";
        }

        String fieldCode = mapFieldCode(field);

        if ("all".equals(fieldCode)) {
            return "all:" + term;  // Explicitly add "all:" prefix for consistency
        }

        return fieldCode + ":" + term;
    }

    /**
     * Build advanced search query from multiple rows with boolean operators
     * Example: ti:quantum AND au:einstein OR abs:relativity
     */
    private String buildAdvancedSearchQuery() {
        List<SearchRow> rows = options.getRows();
        if (rows == null || rows.isEmpty()) {
            return null;
        }

        List<String> rowQueries = new ArrayList<>();
        int addedCount = 0;

        for (SearchRow row : rows) {
            String value = row.getValue();
            if (value == null || value.trim().isEmpty()) {
                continue; // Skip empty rows
            }

            value = value.trim();

            // Quote multi-word searches
            if (value.contains(" ") && !value.startsWith("\"")) {
                value = "\"" + value + "\"";
            }

            String fieldCode = mapFieldCode(row.getField());
            String fieldQuery = fieldCode + ":" + value;

            // Add boolean operator BEFORE this term (except for the first non-empty term)
            if (addedCount > 0) {
                String operator = row.getOperator();
                if (operator != null && !operator.trim().isEmpty()) {
                    if ("NOT".equalsIgnoreCase(operator)) {
                        rowQueries.add("AND NOT");
                    } else if ("NOT".equalsIgnoreCase(operator)) {
                        rowQueries.add("AND NOT");
                    } else {
                        rowQueries.add(operator.toUpperCase());
                    }
                } else {
                    rowQueries.add("AND"); // Default operator
                }
            }

            rowQueries.add(fieldQuery);
            addedCount++;
        }

        if (rowQueries.isEmpty()) {
            return null;
        }

        return String.join(" ", rowQueries);
    }

    /**
     * Build category filter query
     * Example: cat:cs.AI or cat:(cs.AI OR cs.LG)
     */
    private String buildCategoryQuery() {
        List<String> cats = options.getCategories();
        if (cats == null || cats.isEmpty()) {
            return null;
        }

        if (cats.size() == 1) {
            return "cat:" + cats.get(0);
        }

        // Multiple categories with OR
        return "cat:(" + String.join(" OR ", cats) + ")";
    }

    /**
     * Build date range filter query
     * Example: submittedDate:[20200101 TO 20231231]
     */
    private String buildDateQuery() {
        String from = formatDate(options.getDateFrom());
        String to = formatDate(options.getDateTo());

        // Validate that at least one date was successfully formatted
        if ((from == null || from.isEmpty()) && (to == null || to.isEmpty())) {
            return null;
        }

        if (from != null && !from.isEmpty() && to != null && !to.isEmpty()) {
            return "submittedDate:[" + from + " TO " + to + "]";
        }
        if (from != null && !from.isEmpty()) {
            return "submittedDate:[" + from + " TO *]";
        }
        if (to != null && !to.isEmpty()) {
            return "submittedDate:[* TO " + to + "]";
        }

        return null;
    }

    /**
     * Format date from YYYY-MM-DD to YYYYMMDD
     */
    private String formatDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return null;
        }

        // Remove all non-digits
        String cleaned = date.replaceAll("[^0-9]", "");

        // Validate we have at least 8 digits (YYYYMMDD)
        if (cleaned.length() < 8) {
            return null; // Invalid date format
        }

        return cleaned.substring(0, 8);
    }

    /**
     * Map field names to arXiv API codes
     */
    private String mapFieldCode(String field) {
        if (field == null) return "all";

        switch (field.toLowerCase()) {
            case "title":
                return "ti";
            case "author":
                return "au";
            case "abstract":
                return "abs";
            case "comment":
                return "co";
            case "journal":
            case "journal reference":
                return "jr";
            case "category":
                return "cat";
            case "report number":
                return "rn";
            case "id":
                return "id";
            case "all":
            default:
                return "all";
        }
    }

    /**
     * URL encode the query string while preserving special characters arXiv accepts
     */
    private String urlEncode(String str) {
        try {
            String encoded = URLEncoder.encode(str, StandardCharsets.UTF_8.name());
            // Preserve special characters that arXiv accepts
            return encoded
                    .replace("%28", "(")
                    .replace("%29", ")")
                    .replace("%5B", "[")
                    .replace("%5D", "]")
                    .replace("%2A", "*")
                    .replace("%3A", ":")
                    .replace("%22", "\"");  // Preserve quotes
        } catch (Exception e) {
            return str;
        }
    }
}