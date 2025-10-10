package vn.edu.lianac.utils;import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import vn.edu.lianac.R;
import vn.edu.lianac.models.Category;

public class CategoryDataProvider {
    private static CategoryDataProvider instance;
    private final Map<String, Category> mainCategories; // Key is now the category ID (e.g., "physics")

    private CategoryDataProvider(Context context) {
        mainCategories = new LinkedHashMap<>();
        initializeCategories(context);
    }

    public static synchronized CategoryDataProvider getInstance(Context context) {
        if (instance == null) {
            // Use ApplicationContext to prevent leaks
            instance = new CategoryDataProvider(context.getApplicationContext());
        }
        return instance;
    }

    private void initializeCategories(Context context) {
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.categories);
            InputStreamReader reader = new InputStreamReader(inputStream);
            Gson gson = new Gson();
            Type categoryListType = new TypeToken<ArrayList<Category>>(){}.getType();
            List<Category> categories = gson.fromJson(reader, categoryListType);

            if (categories != null) {
                for (Category category : categories) {
                    mainCategories.put(category.getShortName(), category);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the display names of the main categories. Requires a Context to resolve names.
     * @param context The context for string resolution.
     * @return A list of human-readable main category names.
     */
    public List<String> getMainCategoryNames(Context context) {
        List<String> names = new ArrayList<>();
        names.add("Any Category"); // Consider making this a string resource as well
        for (Category category : mainCategories.values()) {
            names.add(category.getName(context));
        }
        return names;
    }

    /**
     * Gets a main category by its name. This now requires a context to find the matching category.
     * @param name The human-readable name of the category.
     * @param context The context for string resolution.
     * @return The matching Category object, or null if not found.
     */
    public Category getMainCategoryByName(String name, Context context) {
        if (name == null) return null;
        for (Category category : mainCategories.values()) {
            if (name.equals(category.getName(context))) {
                return category;
            }
        }
        return null;
    }

    /**
     * Gets a main category by its ID (shortName).
     * @param id The ID of the category (e.g. "physics").
     * @return The Category object, or null if not found.
     */
    public Category getMainCategoryById(String id) {
        return mainCategories.get(id);
    }

    public List<Category> getSubCategories(String mainCategoryName, Context context) {
        Category mainCat = getMainCategoryByName(mainCategoryName, context);
        if (mainCat != null && mainCat.hasSubCategories()) {
            return mainCat.getSubCategories();
        }
        return new ArrayList<>();
    }

    public Map<String, Category> getAllMainCategories() {
        return new LinkedHashMap<>(mainCategories);
    }
}
