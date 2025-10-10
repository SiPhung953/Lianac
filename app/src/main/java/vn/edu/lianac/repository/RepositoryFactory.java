package vn.edu.lianac.repository;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Factory for creating PaperRepository instances.
 * Supports switching between different paper sources (arXiv, PubMed, etc.)
 * while maintaining a single source of truth.
 */
public class RepositoryFactory {
    private static final String PREFS_NAME = "paper_source_prefs";
    private static final String KEY_SELECTED_SOURCE = "selected_source";

    // Source type constants
    public static final String SOURCE_ARXIV = "arxiv";
    // Future: public static final String SOURCE_PUBMED = "pubmed";
    // Future: public static final String SOURCE_IEEE = "ieee";

    private static PaperRepository cachedRepository;
    private static String cachedSourceType;

    /**
     * Get repository instance based on saved user preference
     * @param context Application context for reading preferences
     * @return Repository instance for the selected source
     */
    public static synchronized PaperRepository getRepository(Context context) {
        String sourceType = getSelectedSource(context);

        // Return cached instance if source hasn't changed
        if (cachedRepository != null && sourceType.equals(cachedSourceType)) {
            return cachedRepository;
        }

        // Create new repository for selected source
        cachedRepository = createRepository(sourceType);
        cachedSourceType = sourceType;

        return cachedRepository;
    }

    /**
     * Get repository for a specific source type
     * @param sourceType Source identifier (e.g., SOURCE_ARXIV)
     * @return Repository instance for that source
     */
    public static PaperRepository getRepository(String sourceType) {
        return createRepository(sourceType);
    }

    /**
     * Change the active paper source
     * @param context Application context for saving preference
     * @param sourceType New source type to use
     */
    public static synchronized void setSelectedSource(Context context, String sourceType) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_SELECTED_SOURCE, sourceType).apply();

        // Clear cache to force recreation on next getRepository() call
        cachedRepository = null;
        cachedSourceType = null;
    }

    /**
     * Get the currently selected source type
     * @param context Application context
     * @return Source type identifier
     */
    public static String getSelectedSource(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_SELECTED_SOURCE, SOURCE_ARXIV); // arXiv is default
    }

    /**
     * Create repository instance for given source type
     */
    private static PaperRepository createRepository(String sourceType) {
        switch (sourceType) {
            case SOURCE_ARXIV:
                return ArxivRepository.getInstance();

            // Future implementations:
            // case SOURCE_PUBMED:
            //     return PubMedRepository.getInstance();
            // case SOURCE_IEEE:
            //     return IEEERepository.getInstance();

            default:
                // Fallback to arXiv if unknown source
                return ArxivRepository.getInstance();
        }
    }

    /**
     * Get human-readable name for source type
     */
    public static String getSourceDisplayName(String sourceType) {
        switch (sourceType) {
            case SOURCE_ARXIV:
                return "arXiv";
            // Future:
            // case SOURCE_PUBMED:
            //     return "PubMed";
            default:
                return sourceType;
        }
    }

    /**
     * Get all available source types
     * @return Array of source type identifiers
     */
    public static String[] getAvailableSources() {
        return new String[] {
                SOURCE_ARXIV
                // Future: Add more sources here
        };
    }
}