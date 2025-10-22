package vn.edu.lianac.models;

/**
 * Represents a single search field row in advanced search.
 * Contains a field type, search value, and boolean operator.
 * <p>
 * Example:
 * - field: "ti" (title)
 * - value: "quantum computing"
 * - operator: "AND"
 * <p>
 * This creates: ti:"quantum computing" AND
 */
public class SearchRow {
    private final String field;      // e.g., "all", "ti", "au", "abs"
    private final String value;      // search term
    private final String operator;   // "AND", "OR", "NOT"

    public SearchRow(String field, String value, String operator) {
        this.field = field != null ? field : "all";
        this.value = value != null ? value : "";
        this.operator = operator != null ? operator : "AND";
    }

    // Getters
    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }

    public String getOperator() {
        return operator;
    }

    public boolean hasValue() {
        return value != null && !value.trim().isEmpty();
    }

    @Override
    public String toString() {
        return String.format("SearchRow{field='%s', value='%s', operator='%s'}",
                field, value, operator);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchRow searchRow = (SearchRow) o;

        if (!field.equals(searchRow.field)) return false;
        if (!value.equals(searchRow.value)) return false;
        return operator.equals(searchRow.operator);
    }

    @Override
    public int hashCode() {
        int result = field.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + operator.hashCode();
        return result;
    }
}