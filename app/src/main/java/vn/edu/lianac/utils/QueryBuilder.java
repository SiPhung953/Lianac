package vn.edu.lianac.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import vn.edu.lianac.models.QueryOptions;
import vn.edu.lianac.models.SearchRow;

/**
 * Query builder for arXiv API supporting both basic and advanced searches.
 *
 * Basic search: single search term + field
 * Example: all:quantum or ti:"machine learning"
 *
 * Advanced search: multiple search rows with boolean operators
 * Example: ti:quantum AND au:einstein OR abs:relativity
 */
public class QueryBuilder {
    private static final String BASE_URL = "https://export.arxiv.org/api/query";
    private final QueryOptions options;

    public QueryBuilder(QueryOptions options) {
        this.options = options;
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
        if (mainSearchQuery != null && !mainSearchQuery.isEmpty()) {
            queryParts.add(mainSearchQuery);
        }

        // Add category filter
        if (options.hasCategoryFilter()) {
            String categoryQuery = buildCategoryQuery();
            if (categoryQuery != null) {
                queryParts.add("(" + categoryQuery + ")");
            }
        }

        // Add date filter
        if (options.hasDateFilter()) {
            String dateQuery = buildDateQuery();
            if (dateQuery != null) {
                queryParts.add(dateQuery);
            }
        }

        // If no query parts, return wildcard
        if (queryParts.isEmpty()) {
            return "all:*";
        }

        // Join with AND
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
            if (basicQuery != null && !basicQuery.isEmpty()) {
                parts.add(basicQuery);
            }
        }

        // Add advanced search rows
        if (options.hasRows()) {
            String advancedQuery = buildAdvancedSearchQuery();
            if (advancedQuery != null && !advancedQuery.isEmpty()) {
                // If we have a basic term, we need to connect it with the first row's operator
                if (!parts.isEmpty()) {
                    List<SearchRow> rows = options.getRows();
                    if (rows != null && !rows.isEmpty()) {
                        String firstOperator = rows.get(0).getOperator();
                        if (firstOperator != null && !firstOperator.isEmpty()) {
                            if ("ANDNOT".equalsIgnoreCase(firstOperator)) {
                                parts.add("AND NOT");
                            } else {
                                parts.add(firstOperator.toUpperCase());
                            }
                        } else {
                            parts.add("AND");  // Default to AND if no operator specified
                        }
                    }
                }
                parts.add(advancedQuery);
            }
        }

        if (parts.isEmpty()) {
            return null;
        }

        // If we have multiple parts, wrap the whole thing in parentheses
        if (parts.size() > 1) {
            return String.join(" ", parts);
        }

        return parts.get(0);
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

        for (SearchRow row : rows) {
            String value = row.getValue();
            if (value == null || value.trim().isEmpty()) {
                continue;
            }

            value = value.trim();

            // Quote multi-word searches
            if (value.contains(" ") && !value.startsWith("\"")) {
                value = "\"" + value + "\"";
            }

            String fieldCode = mapFieldCode(row.getField());
            String fieldQuery;

            fieldQuery = fieldCode + ":" + value;

            // Add boolean operator (AND, OR, ANDNOT)
            String operator = row.getOperator();
            if (operator != null && !operator.isEmpty() && !rowQueries.isEmpty()) {
                // Convert "ANDNOT" to "AND NOT" for arXiv API
                if ("NOT".equalsIgnoreCase(operator)) {
                    operator = "AND NOT";
                }
                rowQueries.add(operator.toUpperCase());
            }

            rowQueries.add(fieldQuery);
        }

        if (rowQueries.isEmpty()) {
            return null;
        }

        // Join all parts
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

        if (from != null && to != null) {
            return "submittedDate:[" + from + " TO " + to + "]";
        }
        if (from != null) {
            return "submittedDate:[" + from + " TO *]";
        }
        if (to != null) {
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
        // Remove all non-digits and take first 8 chars (YYYYMMDD)
        String cleaned = date.replaceAll("[^0-9]", "");
        return cleaned.length() >= 8 ? cleaned.substring(0, 8) : cleaned;
    }

    /**
     * Map field names to arXiv API codes
     */
    private String mapFieldCode(String field) {
        if (field == null) return "all";

        switch (field.toLowerCase()) {
            case "title": return "ti";
            case "author": return "au";
            case "abstract": return "abs";
            case "comment": return "co";
            case "journal":
            case "journal reference": return "jr";
            case "category": return "cat";
            case "report number": return "rn";
            case "id": return "id";
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

    /**
     * Static convenience method
     */
    public static String buildUrl(QueryOptions options) {
        return new QueryBuilder(options).build();
    }
}