package vn.edu.lianac.utils;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import vn.edu.lianac.models.QueryOptions;
import vn.edu.lianac.models.SearchRow;

public class QueryBuilder {
    private static final String TAG = "QueryBuilder";
    private static final String BASE_URL = "https://export.arxiv.org/api/query";

    private final QueryOptions options;

    public QueryBuilder(QueryOptions options) {
        this.options = options;
    }

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

        String mainSearchQuery = buildCombinedSearchQuery();
        if (mainSearchQuery != null && !mainSearchQuery.trim().isEmpty()) {
            queryParts.add(mainSearchQuery);
        }

        if (options.hasCategoryFilter()) {
            String categoryQuery = buildCategoryQuery();
            if (categoryQuery != null && !categoryQuery.trim().isEmpty()) {
                queryParts.add(categoryQuery);
            }
        }

        if (options.hasDateFilter()) {
            String dateQuery = buildDateQuery();
            if (dateQuery != null && !dateQuery.trim().isEmpty()) {
                queryParts.add(dateQuery);
            }
        }

        if (queryParts.isEmpty()) {
            return "all:*";
        }

        return String.join(" AND ", queryParts);
    }

    private String buildCombinedSearchQuery() {
        List<String> parts = new ArrayList<>();

        if (options.hasSearchTerm()) {
            String basicQuery = buildBasicSearchQuery();
            if (basicQuery != null && !basicQuery.trim().isEmpty()) {
                parts.add(basicQuery);
            }
        }

        if (options.hasRows() && options.getRows() != null && !options.getRows().isEmpty()) {
            String advancedQuery = buildAdvancedSearchQuery();
            if (advancedQuery != null && !advancedQuery.trim().isEmpty()) {
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

        if (parts.isEmpty()) {
            return null;
        }

        return String.join(" ", parts);
    }


    private String buildBasicSearchQuery() {
        String term = options.getSearchTerm();
        String field = options.getSearchField();

        if (term == null || term.trim().isEmpty()) {
            return null;
        }

        term = term.trim();

        if (term.contains(" ") && !term.startsWith("\"")) {
            term = "\"" + term + "\"";
        }

        String fieldCode = mapFieldCode(field);

        if ("all".equals(fieldCode)) {
            return "all:" + term;
        }

        return fieldCode + ":" + term;
    }

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
                continue;
            }

            value = value.trim();

            if (value.contains(" ") && !value.startsWith("\"")) {
                value = "\"" + value + "\"";
            }

            String fieldCode = mapFieldCode(row.getField());
            String fieldQuery = fieldCode + ":" + value;

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
                    rowQueries.add("AND");
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


        private String buildCategoryQuery() {
            List<String> cats = options.getCategories();
            if (cats == null || cats.isEmpty()) {
                return null;
            }

            if (cats.size() == 1) {
                return "cat:" + cats.get(0);
            }

            return "cat:(" + String.join(" OR ", cats) + ")";
        }

        private String buildDateQuery() {
            String from = formatDate(options.getDateFrom());
            String to = formatDate(options.getDateTo());

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

        private String formatDate(String date) {
            if (date == null || date.trim().isEmpty()) {
                return null;
            }

            String cleaned = date.replaceAll("[^0-9]", "");

            if (cleaned.length() < 8) {
                return null;
            }

            return cleaned.substring(0, 8);
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

        private String urlEncode(String str) {
            try {
                String encoded = URLEncoder.encode(str, StandardCharsets.UTF_8.name());
                return encoded
                        .replace("%28", "(")
                        .replace("%29", ")")
                        .replace("%5B", "[")
                        .replace("%5D", "]")
                        .replace("%2A", "*")
                        .replace("%3A", ":")
                        .replace("%22", "\"");
            } catch (Exception e) {
                return str;
            }
        }

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
}