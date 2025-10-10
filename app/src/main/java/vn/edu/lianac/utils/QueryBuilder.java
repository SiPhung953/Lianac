package vn.edu.lianac.utils;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QueryBuilder {
    private static final String TAG = "QueryBuilder";
    private static final String BASE_URL = "https://export.arxiv.org/api/query";
    private static final int MAX_RESULTS_LIMIT = 2000;

    private final QueryOptions options;

    public QueryBuilder(QueryOptions options) {
        this.options = Objects.requireNonNull(options, "QueryOptions cannot be null");
    }

    public String build() {
        if (!options.isValid()) {
            Log.w(TAG, "Attempted to build URL from an invalid QueryOptions object.");
            return "";
        }

        try {
            String rawSearchQuery = buildRawSearchQuery();
            String encodedSearchQuery = encode(rawSearchQuery);

            return String.format(
                    "%s?search_query=%s&start=%d&max_results=%d&sortBy=%s&sortOrder=%s",
                    BASE_URL,
                    encodedSearchQuery,
                    options.start,
                    Math.min(options.maxResults, MAX_RESULTS_LIMIT),
                    options.sortBy,
                    options.sortOrder
            );
        } catch (Exception e) {
            Log.e(TAG, "Error building query URL", e);
            return "";
        }
    }

    private String buildRawSearchQuery() {
        List<String> clauses = new ArrayList<>();
        String searchTermClause = null;

        // Handle multi-row search fields (this now includes converted basic search)
        if (!options.rows.isEmpty()) {
            List<String> rowClauses = new ArrayList<>();
            for (int i = 0; i < options.rows.size(); i++) {
                vn.edu.lianac.models.SearchRow row = options.rows.get(i);
                if (row != null && row.isValid()) {
                    String field = row.getField();
                    String value = row.getValue();

                    // Convert field display name to field code
                    String fieldCode = convertFieldDisplayNameToCode(field);

                    String rowClause;
                    if (fieldCode != null && !fieldCode.equals("all") && isPrefix(fieldCode)) {
                        rowClause = fieldCode + ":" + quoteValue(value);
                    } else {
                        rowClause = quoteValue(value);
                    }

                    // Add operator before the clause (except for the first one)
                    if (i > 0) {
                        String operator = row.getOperator();
                        if (operator != null && !operator.isEmpty()) {
                            rowClauses.add(operator.toUpperCase());
                        }
                    }
                    rowClauses.add(rowClause);
                }
            }

            if (!rowClauses.isEmpty()) {
                searchTermClause = "(" + String.join(" ", rowClauses) + ")";
            }
        } else if (isNonEmpty(options.searchTerm)) {
            // Fallback: Handle basic search term if no rows exist
            String field = options.searchField;
            if (field != null && !field.equals("all") && isPrefix(field)) {
                searchTermClause = field + ":" + quoteValue(options.searchTerm);
            } else {
                searchTermClause = quoteValue(options.searchTerm);
            }
        }

        // Add other clauses (categories, dates)
        if (isNonEmpty(options.title)) {
            clauses.add("ti:" + quoteValue(options.title));
        }
        if (isNonEmpty(options.author)) {
            clauses.add("au:" + quoteValue(options.author));
        }
        if (isNonEmpty(options.abstractTerm)) {
            clauses.add("abs:" + quoteValue(options.abstractTerm));
        }
        if (!options.categories.isEmpty()) {
            clauses.add(buildCategoryQuery());
        }
        String dateQuery = buildDateQuery();
        if (isNonEmpty(dateQuery)) {
            clauses.add(dateQuery);
        }

        // Handle the search term clause
        if (searchTermClause != null) {
            if (clauses.isEmpty()) {
                return searchTermClause;
            } else {
                clauses.add(0, searchTermClause);
            }
        }

        if (clauses.isEmpty()) {
            return "all";
        }

        return String.join(" AND ", clauses);
    }

    private String convertFieldDisplayNameToCode(String displayName) {
        if (displayName == null) return "all";

        switch (displayName.toLowerCase()) {
            case "title": return "ti";
            case "author": return "au";
            case "abstract": return "abs";
            case "comment": return "co";
            case "journal reference": return "jr";
            case "category": return "cat";
            case "report number": return "rn";
            case "id": return "id";
            case "all": return "all";
            default:
                // If it's already a code, return as-is
                if (isPrefix(displayName)) {
                    return displayName;
                }
                return "all";
        }
    }


    private String buildCategoryQuery() {
        if (options.categories.isEmpty()) return "";
        if (options.categories.size() == 1) return "cat:" + options.categories.get(0);
        return "cat:(" + String.join(" OR ", options.categories) + ")";
    }

    private String buildDateQuery() {
        String fromDate = formatDate(options.dateFrom);
        String toDate = formatDate(options.dateTo);
        if (fromDate != null && toDate != null) return "submittedDate:[" + fromDate + " TO " + toDate + "]";
        if (fromDate != null) return "submittedDate:[" + fromDate + " TO *]";
        if (toDate != null) return "submittedDate:[* TO " + toDate + "]";
        return "";
    }

    private String formatDate(String date) {
        if (!isNonEmpty(date)) return null;
        String cleaned = date.replaceAll("[^0-9]", "");
        return cleaned.length() >= 8 ? cleaned.substring(0, 8) : cleaned;
    }

    private String quoteValue(String value) {
        if (value == null) return "";
        String trimmed = value.trim();

        if ((trimmed.contains(" ") || trimmed.contains(":")) && !(trimmed.startsWith("\"") && trimmed.endsWith("\""))) {
            return "\"" + trimmed + "\"";
        }
        return trimmed;
    }

    private boolean isNonEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private boolean isPrefix(String field) {
        switch (field) {
            case "ti": case "au": case "abs": case "co": case "jr":
            case "cat": case "rn": case "id": case "doi": case "orcid":
            case "auid": case "help": case "all":
                return true;
            default:
                return false;
        }
    }

    private String encode(String str) {
        if (str == null || str.isEmpty()) return "";
        try {
            String encoded = URLEncoder.encode(str, StandardCharsets.UTF_8.name());
            return encoded
                    .replace("%28", "(")
                    .replace("%29", ")")
                    .replace("%5B", "[")
                    .replace("%5D", "]")
                    .replace("%27", "'")
                    .replace("%2A", "*");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UTF-8 encoding not supported", e);
            return str;
        }
    }

    public static String buildUrl(QueryOptions options) { return new QueryBuilder(options).build(); }
}

