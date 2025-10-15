package vn.edu.lianac.models;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an arXiv category/subject classification
 * Now supports hierarchical structure with sub-categories
 */
public class Category {
    private String shortName;  // e.g., "cs.AI", "math.GT", "physics"
    private String name;        // e.g., "Artificial Intelligence", "Geometric Topology"
    private int nameResId;      // String resource ID for localization
    private List<Category> subCategories; // Sub-categories (null if leaf node)

    public Category(String shortName) {
        this.shortName = shortName;
        this.name = shortName; // Default to shortName if full name not available
        this.subCategories = null;
    }

    public Category(String shortName, String name) {
        this.shortName = shortName;
        this.name = name;
        this.subCategories = null;
    }

    public Category(String shortName, int nameResId) {
        this.shortName = shortName;
        this.nameResId = nameResId;
        this.subCategories = null;
    }

    // Getters and Setters
    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getName() {
        return name;
    }

    /**
     * Get name with Context for localization support
     * @param context Android context for string resources
     * @return Localized name if nameResId is set, otherwise returns name field
     */
    public String getName(Context context) {
        if (nameResId != 0 && context != null) {
            return context.getString(nameResId);
        }
        return name != null ? name : shortName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNameResId() {
        return nameResId;
    }

    public void setNameResId(int nameResId) {
        this.nameResId = nameResId;
    }

    public List<Category> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(List<Category> subCategories) {
        this.subCategories = subCategories;
    }

    /**
     * Check if this category has sub-categories
     * @return true if this is a parent category with children
     */
    public boolean hasSubCategories() {
        return subCategories != null && !subCategories.isEmpty();
    }

    /**
     * Get full display name (e.g., "cs.AI - Artificial Intelligence")
     * @param context Android context for localization
     * @return Full formatted name
     */
    public String getFullName(Context context) {
        String displayName = getName(context);
        if (displayName.equals(shortName)) {
            return shortName; // Don't duplicate if name is same as code
        }
        return shortName + " - " + displayName;
    }

    /**
     * Add a sub-category to this category
     * @param subCategory The category to add
     */
    public void addSubCategory(Category subCategory) {
        if (subCategories == null) {
            subCategories = new ArrayList<>();
        }
        subCategories.add(subCategory);
    }

    /**
     * Find a sub-category by its short name (recursively)
     * @param shortName The short name to search for
     * @return The found category or null
     */
    public Category findSubCategory(String shortName) {
        if (this.shortName.equals(shortName)) {
            return this;
        }
        if (hasSubCategories()) {
            for (Category sub : subCategories) {
                Category found = sub.findSubCategory(shortName);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return shortName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Category category = (Category) obj;
        return shortName != null && shortName.equals(category.shortName);
    }

    @Override
    public int hashCode() {
        return shortName != null ? shortName.hashCode() : 0;
    }
}