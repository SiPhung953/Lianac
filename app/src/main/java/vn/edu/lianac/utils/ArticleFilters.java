package vn.edu.lianac.utils;

import java.util.Comparator;
import java.util.List;

import vn.edu.lianac.models.Article;
import vn.edu.lianac.repository.PaperRepository;

/**
 * Utility class providing common filters and comparators for articles.
 * These are source-agnostic and work with any Article implementation.
 */
public class ArticleFilters {

    // Private constructor to prevent instantiation
    private ArticleFilters() {}

    // --- Filters ---

    /**
     * Filter articles by category short name
     * @param categoryShortName Category code (e.g., "cs.AI", "q-bio.NC")
     */
    public static PaperRepository.ArticleFilter byCategory(final String categoryShortName) {
        return article -> article.hasCategory(categoryShortName);
    }

    /**
     * Filter articles by author name (case-insensitive partial match)
     * @param authorName Author name or partial name to search for
     */
    public static PaperRepository.ArticleFilter byAuthor(final String authorName) {
        return article -> article.containsAuthor(authorName);
    }

    /**
     * Filter articles by search term (searches title, summary, authors)
     * @param searchTerm Term to search for
     */
    public static PaperRepository.ArticleFilter bySearchTerm(final String searchTerm) {
        return article -> article.matchesSearchTerm(searchTerm);
    }

    /**
     * Filter articles published after a certain date
     * @param dateString ISO 8601 date string (e.g., "2024-01-01T00:00:00Z")
     */
    public static PaperRepository.ArticleFilter publishedAfter(final String dateString) {
        return article -> {
            String publishedDate = article.getPublishedDateRaw();
            return publishedDate != null && publishedDate.compareTo(dateString) > 0;
        };
    }

    /**
     * Filter articles published before a certain date
     * @param dateString ISO 8601 date string
     */
    public static PaperRepository.ArticleFilter publishedBefore(final String dateString) {
        return article -> {
            String publishedDate = article.getPublishedDateRaw();
            return publishedDate != null && publishedDate.compareTo(dateString) < 0;
        };
    }

    /**
     * Combine multiple filters with AND logic
     * Article must match ALL filters
     */
    public static PaperRepository.ArticleFilter and(final PaperRepository.ArticleFilter... filters) {
        return article -> {
            for (PaperRepository.ArticleFilter filter : filters) {
                if (!filter.matches(article)) {
                    return false;
                }
            }
            return true;
        };
    }

    /**
     * Combine multiple filters with OR logic
     * Article must match AT LEAST ONE filter
     */
    public static PaperRepository.ArticleFilter or(final PaperRepository.ArticleFilter... filters) {
        return article -> {
            for (PaperRepository.ArticleFilter filter : filters) {
                if (filter.matches(article)) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Negate a filter
     */
    public static PaperRepository.ArticleFilter not(final PaperRepository.ArticleFilter filter) {
        return article -> !filter.matches(article);
    }

    // --- Comparators ---

    /**
     * Sort by publication date
     * @param descending true for newest first, false for oldest first
     */
    public static Comparator<Article> byDate(final boolean descending) {
        return (a1, a2) -> {
            String date1 = a1.getPublishedDateRaw();
            String date2 = a2.getPublishedDateRaw();

            if (date1 == null) return 1;
            if (date2 == null) return -1;

            int result = date1.compareTo(date2);
            return descending ? -result : result;
        };
    }

    /**
     * Sort by title alphabetically (case-insensitive)
     */
    public static Comparator<Article> byTitle() {
        return (a1, a2) -> {
            String title1 = a1.getTitle();
            String title2 = a2.getTitle();

            if (title1 == null) return 1;
            if (title2 == null) return -1;

            return title1.compareToIgnoreCase(title2);
        };
    }

    /**
     * Sort by first author name alphabetically (case-insensitive)
     */
    public static Comparator<Article> byFirstAuthor() {
        return (a1, a2) -> {
            List<String> authors1 = a1.getAuthors();
            List<String> authors2 = a2.getAuthors();

            if (authors1 == null || authors1.isEmpty()) return 1;
            if (authors2 == null || authors2.isEmpty()) return -1;

            return authors1.get(0).compareToIgnoreCase(authors2.get(0));
        };
    }

    /**
     * Sort by number of authors
     * @param descending true for most authors first
     */
    public static Comparator<Article> byAuthorCount(final boolean descending) {
        return (a1, a2) -> {
            List<String> authors1 = a1.getAuthors();
            List<String> authors2 = a2.getAuthors();

            int count1 = authors1 != null ? authors1.size() : 0;
            int count2 = authors2 != null ? authors2.size() : 0;

            int result = Integer.compare(count1, count2);
            return descending ? -result : result;
        };
    }
}