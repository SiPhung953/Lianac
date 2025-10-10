package vn.edu.lianac.models;

public class SearchRow {
    private final String operator;
    private final String field;
    private final String value;

    // FIXED: Changed parameter order to (field, value, operator)
    public SearchRow(String field, String value, String operator) {
        this.operator = operator != null ? operator : "AND";
        this.field = field != null ? field : "all";
        this.value = value != null ? value : "";
    }

    public String getOperator() {
        return operator;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }

    public boolean isValid() {
        return value != null && !value.trim().isEmpty();
    }

    public String toQueryString() {
        if (!isValid()) {
            return "";
        }

        String term = value.trim();

        if (term.contains(" ") && !term.startsWith("\"")) {
            term = "\"" + term + "\"";
        }

        if ("all".equals(field)) {
            return term;
        }

        return field + ":" + term;
    }

    @Override
    public String toString() {
        return "SearchRow{" +
                "operator='" + operator + '\'' +
                ", field='" + field + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchRow searchRow = (SearchRow) o;

        if (!operator.equals(searchRow.operator)) return false;
        if (!field.equals(searchRow.field)) return false;
        return value.equals(searchRow.value);
    }

    @Override
    public int hashCode() {
        int result = operator.hashCode();
        result = 31 * result + field.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}