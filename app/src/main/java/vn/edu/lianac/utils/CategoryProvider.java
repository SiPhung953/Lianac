package vn.edu.lianac.utils;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import vn.edu.lianac.R;

public class CategoryProvider {
    private static CategoryProvider instance;
    private final Map<String, CategoryNode> categoryMap;
    private final List<String> mainCategoryIds;
    private final Context context;

    private CategoryProvider(Context context) {
        this.context = context.getApplicationContext();
        this.categoryMap = new LinkedHashMap<>();
        this.mainCategoryIds = new ArrayList<>();
        loadCategories();
    }

    public static synchronized CategoryProvider getInstance(Context context) {
        if (instance == null) {
            instance = new CategoryProvider(context);
        }
        return instance;
    }

    // Static methods for compatibility with SearchFragment
    public static List<String> getMainCategories() {
        if (instance == null) {
            throw new IllegalStateException("CategoryProvider not initialized. Call getInstance(context) first.");
        }
        return new ArrayList<>(instance.mainCategoryIds);
    }

    public static List<String> getSubcategories(String mainCategory) {
        if (instance == null) {
            throw new IllegalStateException("CategoryProvider not initialized. Call getInstance(context) first.");
        }
        return instance.getSubcategoriesForCategory(mainCategory);
    }

    public static String getCategoryName(String categoryId) {
        if (instance == null) {
            throw new IllegalStateException("CategoryProvider not initialized. Call getInstance(context) first.");
        }
        return instance.getCategoryDisplayName(categoryId);
    }

    public static String getSubcategoryName(String subcategoryId) {
        if (instance == null) {
            throw new IllegalStateException("CategoryProvider not initialized. Call getInstance(context) first.");
        }
        return instance.getCategoryDisplayName(subcategoryId);
    }

    private void loadCategories() {
        try {
            InputStream is = context.getResources().openRawResource(R.raw.categories);
            InputStreamReader reader = new InputStreamReader(is);

            JsonArray rootArray = new Gson().fromJson(reader, JsonArray.class);

            for (JsonElement element : rootArray) {
                JsonObject mainCategory = element.getAsJsonObject();
                String mainId = mainCategory.get("id").getAsString();

                mainCategoryIds.add(mainId);

                CategoryNode mainNode = new CategoryNode(mainId);
                categoryMap.put(mainId, mainNode);

                if (mainCategory.has("subCategories")) {
                    JsonArray subCategoriesArray = mainCategory.getAsJsonArray("subCategories");
                    processSubCategories(subCategoriesArray, mainNode);
                }
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processSubCategories(JsonArray subCategoriesArray, CategoryNode parent) {
        for (JsonElement element : subCategoriesArray) {
            JsonObject subCategory = element.getAsJsonObject();
            String subId = subCategory.get("id").getAsString();

            CategoryNode subNode = new CategoryNode(subId);
            parent.addSubcategory(subId);
            categoryMap.put(subId, subNode);

            // Recursively process nested subcategories
            if (subCategory.has("subCategories")) {
                JsonArray nestedSubs = subCategory.getAsJsonArray("subCategories");
                processSubCategories(nestedSubs, subNode);
            }
        }
    }

    public List<String> getSubcategoriesForCategory(String categoryId) {
        CategoryNode node = categoryMap.get(categoryId);
        return (node != null) ? node.getSubcategories() : new ArrayList<>();
    }

    public String getCategoryDisplayName(String categoryId) {
        if (categoryId == null || categoryId.isEmpty()) return "";

        // Convert category ID to resource name format (lowercase, replace dots and dashes with underscores)
        String resourceName = "category_" + categoryId.toLowerCase().replace(".", "_").replace("-", "_");

        int resId = context.getResources().getIdentifier(
                resourceName, "string", context.getPackageName());

        if (resId != 0) {
            return context.getString(resId);
        }

        // Fallback to ID if no resource found
        return categoryId;
    }

    public List<String> getMainCategoryIds() {
        return new ArrayList<>(mainCategoryIds);
    }

    public List<String> getAllCategoryIds() {
        return new ArrayList<>(categoryMap.keySet());
    }

    public boolean isMainCategory(String categoryId) {
        return mainCategoryIds.contains(categoryId);
    }

    public boolean hasSubcategories(String categoryId) {
        CategoryNode node = categoryMap.get(categoryId);
        return node != null && !node.getSubcategories().isEmpty();
    }

    public List<String> getAllLeafCategories(String categoryId) {
        List<String> leafCategories = new ArrayList<>();
        collectLeafCategories(categoryId, leafCategories);
        return leafCategories;
    }

    private void collectLeafCategories(String categoryId, List<String> leafCategories) {
        CategoryNode node = categoryMap.get(categoryId);
        if (node == null) return;

        List<String> subcategories = node.getSubcategories();
        if (subcategories.isEmpty()) {
            // This is a leaf category
            leafCategories.add(categoryId);
        } else {
            // Recursively collect leaf categories from subcategories
            for (String subId : subcategories) {
                collectLeafCategories(subId, leafCategories);
            }
        }
    }

    // Helper method to get expandable physics categories (categories that have subcategories)
    public List<String> getExpandableCategories(String mainCategoryId) {
        List<String> expandable = new ArrayList<>();
        List<String> subcategories = getSubcategoriesForCategory(mainCategoryId);

        if (subcategories != null) {
            for (String subcat : subcategories) {
                if (hasSubcategories(subcat)) {
                    expandable.add(subcat);
                }
            }
        }
        return expandable;
    }

    private static class CategoryNode {
        private final String id;
        private final List<String> subcategories;

        CategoryNode(String id) {
            this.id = id;
            this.subcategories = new ArrayList<>();
        }

        void addSubcategory(String subId) {
            subcategories.add(subId);
        }

        List<String> getSubcategories() {
            return new ArrayList<>(subcategories);
        }
    }
}