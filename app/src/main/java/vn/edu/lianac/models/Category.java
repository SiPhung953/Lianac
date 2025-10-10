package vn.edu.lianac.models;

import android.content.Context;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;public class Category {

    @SerializedName("id")
    private String shortName;   // arXiv code (e.g. "cs.AI")

    private String archive;     // Top-level archive (e.g. "cs" from "cs.AI")

    @SerializedName("subCategories")
    private List<Category> subCategories;

    // This field is no longer populated by Gson, but can be used for caching the resolved name
    private transient String name;

    public Category() {
        subCategories = new ArrayList<>();
    }

    public Category(String shortName, String name) {
        this.shortName = shortName;
        this.name = name;
        this.subCategories = new ArrayList<>();
        extractArchive();
    }

    // --- Getters & Setters ---

    /**
     * Gets the human-readable name of the category.
     * This method resolves the name from string resources. It requires a Context.
     *
     * @param context The context to access resources.
     * @return The display name of the category.
     */
    public String getName(Context context) {
        if (name == null) {
            // Generate the resource key from the ID (e.g., "cs.AI" -> "category_cs_ai")
            String resourceKey = "category_" + shortName.replace('.', '_').replace('-', '_').toLowerCase();
            int resourceId = context.getResources().getIdentifier(resourceKey, "string", context.getPackageName());
            // If the resource is found, set the name, otherwise fallback to the shortName
            name = (resourceId != 0) ? context.getString(resourceId) : shortName;
        }
        return name;
    }

    public void setName(String name) { this.name = name; }

    public String getShortName() { return shortName; }
    public void setShortName(String shortName) {
        this.shortName = shortName;
        extractArchive();
    }

    public String getArchive() {
        if (archive == null) {
            extractArchive();
        }
        return archive;
    }

    public List<Category> getSubCategories() { return subCategories; }

    public void setSubCategories(List<Category> subCategories) {
        this.subCategories = subCategories != null ? subCategories : new ArrayList<>();
    }

    public void addSubCategory(Category subCategory) {
        if (subCategories == null) {
            subCategories = new ArrayList<>();
        }
        if (subCategory != null) subCategories.add(subCategory);
    }

    public boolean hasSubCategories() {
        return subCategories != null && !subCategories.isEmpty();
    }

    // --- Query Building ---
    public String toQueryParam() {
        return "cat:" + shortName;
    }

    // --- Utility Methods ---
    private void extractArchive() {
        if (shortName != null && shortName.contains(".")) {
            archive = shortName.substring(0, shortName.indexOf("."));
        } else {
            archive = shortName;
        }
    }

    public boolean isArchiveLevel() {
        return shortName != null && !shortName.contains(".");
    }

    public boolean isSubjectLevel() {
        return shortName != null && shortName.contains(".");
    }

    public boolean belongsToArchive(String archiveName) {
        return getArchive() != null && getArchive().equals(archiveName);
    }

    public boolean matches(String categoryShortName) {
        return shortName != null && shortName.equals(categoryShortName);
    }

    public boolean matchesPrefix(String prefix) {
        return shortName != null && shortName.startsWith(prefix);
    }

    // --- Equals & HashCode for proper comparison ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;
        Category category = (Category) o;
        return Objects.equals(shortName, category.shortName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shortName);
    }

    @Override
    public String toString() {
        return shortName != null ? shortName : "Unknown Category";
    }

    // --- Display name for UI ---

    public String getDisplayName(Context context) {
        String displayName = getName(context);
        if (!displayName.equals(shortName)) {
            return displayName + " (" + shortName + ")";
        }
        return shortName != null ? shortName : "Unknown";
    }

    public String getFullName(Context context) {
        // Return the display name format used in your spinner
        String displayName = getName(context);
        if (!displayName.equals(shortName)) {
            return shortName + " - " + displayName;
        }
        return shortName;
    }
}
