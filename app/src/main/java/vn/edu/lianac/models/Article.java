package vn.edu.lianac.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class Article {

    private String id;
    private String title;
    private String summary;
    private List<String> authors;

    // Store raw ISO 8601 dates for sorting/filtering
    private String publishedDateRaw;
    private String updatedDateRaw;

    private String pdfUrl;
    private String absUrl;
    private List<Category> categories;
    private boolean isBookmarked = false;

    // Date formatters
    private static final SimpleDateFormat ARXIV_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT =
            new SimpleDateFormat("MMM dd, yyyy", Locale.US);

    static {
        ARXIV_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public Article() {
        authors = new ArrayList<>();
        categories = new ArrayList<>();
    }

    public Article(String id, String title, String summary, List<String> authors,
                   String publishedDateRaw, String updatedDateRaw, String pdfUrl,
                   String absUrl, List<Category> categories) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.authors = authors != null ? authors : new ArrayList<>();
        this.publishedDateRaw = publishedDateRaw;
        this.updatedDateRaw = updatedDateRaw;
        this.pdfUrl = pdfUrl;
        this.absUrl = absUrl;
        this.categories = categories != null ? categories : new ArrayList<>();
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public List<String> getAuthors() { return authors; }
    public String getPublishedDateRaw() { return publishedDateRaw; }
    public String getUpdatedDateRaw() { return updatedDateRaw; }
    public String getPdfUrl() { return pdfUrl; }
    public String getAbsUrl() { return absUrl; }
    public List<Category> getCategories() { return categories; }
    public boolean isBookmarked() {
        return isBookmarked;
    }

    // --- Setters ---
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setSummary(String summary) { this.summary = summary; }
    public void setAuthors(List<String> authors) { this.authors = authors; }
    public void setPublishedDateRaw(String publishedDateRaw) { this.publishedDateRaw = publishedDateRaw; }
    public void setUpdatedDateRaw(String updatedDateRaw) { this.updatedDateRaw = updatedDateRaw; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }
    public void setAbsUrl(String absUrl) { this.absUrl = absUrl; }
    public void setCategories(List<Category> categories) { this.categories = categories; }
    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }

    // --- Date Display Methods ---

    /**
     * Get formatted published date for display (e.g., "Jan 15, 2024")
     */
    public String getPublishedDateFormatted() {
        return formatDateForDisplay(publishedDateRaw);
    }

    /**
     * Get formatted updated date for display
     */
    public String getUpdatedDateFormatted() {
        return formatDateForDisplay(updatedDateRaw);
    }

    /**
     * Parse raw date string to Date object for sorting/comparison
     * Returns null if parsing fails
     */
    public Date getPublishedDate() {
        return parseRawDate(publishedDateRaw);
    }

    /**
     * Parse raw updated date to Date object
     */
    public Date getUpdatedDate() {
        return parseRawDate(updatedDateRaw);
    }

    /**
     * Get year from published date (for grouping/filtering)
     */
    public int getPublishedYear() {
        if (publishedDateRaw != null && publishedDateRaw.length() >= 4) {
            try {
                return Integer.parseInt(publishedDateRaw.substring(0, 4));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    // --- Private Date Helpers ---

    private static String formatDateForDisplay(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) {
            return "";
        }

        try {
            Date date = ARXIV_DATE_FORMAT.parse(rawDate);
            if (date != null) {
                return DISPLAY_DATE_FORMAT.format(date);
            }
        } catch (ParseException e) {
            // If parsing fails, try to extract just the date part
            if (rawDate.length() >= 10) {
                return rawDate.substring(0, 10); // Return YYYY-MM-DD
            }
        }

        return rawDate;
    }

    private static Date parseRawDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) {
            return null;
        }

        try {
            return ARXIV_DATE_FORMAT.parse(rawDate);
        } catch (ParseException e) {
            return null;
        }
    }

    // --- Convenience Methods ---

    public String getFormattedAuthors() {
        if (authors == null || authors.isEmpty()) return "";
        if (authors.size() == 1) return authors.get(0);
        if (authors.size() <= 3) return String.join(", ", authors);
        // More than 3 authors: show first author + et al.
        return authors.get(0) + " et al.";
    }

    public String getAllAuthorsString() {
        return authors == null ? "" : String.join(", ", authors);
    }

    public String getCategoryString() {
        if (categories == null || categories.isEmpty()) return "";
        return categories.stream()
                .map(Category::getShortName)
                .collect(Collectors.joining(", "));
    }

    public String getPrimaryCategoryShortName() {
        if (categories == null || categories.isEmpty()) return "";
        return categories.get(0).getShortName();
    }

    public Category getPrimaryCategory() {
        if (categories == null || categories.isEmpty()) return null;
        return categories.get(0);
    }

    // --- Filtering Helper Methods ---

    public boolean hasCategory(String categoryShortName) {
        if (categories == null || categoryShortName == null) return false;
        return categories.stream()
                .anyMatch(cat -> cat.getShortName().equals(categoryShortName));
    }

    public boolean hasCategoryPrefix(String prefix) {
        if (categories == null || prefix == null) return false;
        return categories.stream()
                .anyMatch(cat -> cat.getShortName().startsWith(prefix));
    }

    public boolean hasAnyCategory(List<String> categoryShortNames) {
        if (categories == null || categoryShortNames == null || categoryShortNames.isEmpty()) {
            return false;
        }
        return categories.stream()
                .anyMatch(cat -> categoryShortNames.contains(cat.getShortName()));
    }

    public boolean containsAuthor(String authorName) {
        if (authors == null || authorName == null) return false;
        String searchTerm = authorName.toLowerCase();
        return authors.stream()
                .anyMatch(author -> author.toLowerCase().contains(searchTerm));
    }

    public boolean matchesSearchTerm(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) return true;
        String term = searchTerm.toLowerCase();

        return (title != null && title.toLowerCase().contains(term)) ||
                (summary != null && summary.toLowerCase().contains(term)) ||
                containsAuthor(term);
    }

    /**
     * Check if article was published within a date range
     * @param startDate Start date in ISO format (YYYY-MM-DD)
     * @param endDate End date in ISO format (YYYY-MM-DD)
     */
    public boolean isPublishedBetween(String startDate, String endDate) {
        if (publishedDateRaw == null) return false;

        // Simple string comparison works for ISO 8601 dates
        return (startDate == null || publishedDateRaw.compareTo(startDate) >= 0) &&
                (endDate == null || publishedDateRaw.compareTo(endDate) <= 0);
    }

    @Override
    public String toString() {
        return "Article{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", authors=" + getFormattedAuthors() +
                ", publishedDate='" + getPublishedDateFormatted() + '\'' +
                ", categories=" + getCategoryString() +
                '}';
    }
}