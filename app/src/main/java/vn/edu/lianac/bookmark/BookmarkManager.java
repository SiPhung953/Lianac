package vn.edu.lianac.bookmark;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import vn.edu.lianac.models.Article;

public class BookmarkManager {
    private static final String PREF_NAME = "BookmarkPreferences";
    private static final String KEY_BOOKMARKS = "bookmarks";
    private final SharedPreferences prefs;
//    private SharedPreferences.Editor editor;
    private final Gson gson;

    public BookmarkManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
//        editor = prefs.edit();
        gson = new Gson();
    }

    public void addBookmark(Article article) {
        if (article == null || article.getId() == null) {
            return;
        }

        String id = article.getId();
        String title = article.getTitle() != null ? article.getTitle() : "Untitled";
        long timestamp = System.currentTimeMillis();

        BookmarkItem item = new BookmarkItem(id, title, timestamp, article);
        addBookmark(item);
    }

    // Save a bookmark
    public void addBookmark(BookmarkItem item) {
        List<BookmarkItem> bookmarks = getBookmarks();

        // Keep only the most recent 10 bookmarks
        if (bookmarks.size() >= 10) {
            bookmarks.remove(bookmarks.size() - 1);
        }

        // Update bookmark for dupes
        bookmarks.removeIf(bookmark -> bookmark.getId().equals(item.getId()));

        bookmarks.add(0, item);
        saveBookmarks(bookmarks);
    }

    // Remove a bookmark
    public void removeBookmark(String id) {
        List<BookmarkItem> bookmarks = getBookmarks();
        bookmarks.removeIf(item -> item.getId().equals(id));
        saveBookmarks(bookmarks);
    }

    // Check if item is bookmarked
    public boolean isBookmarked(String id) {
        List<BookmarkItem> bookmarks = getBookmarks();
//        for (BookmarkItem item : bookmarks) {
//            if (item.getId().equals(id)) {
//                return true;
//            }
//        }
//        return false;
        return bookmarks.stream().anyMatch(item -> item.getId().equals(id));
    }

    /**
     * Get a bookmark by ID
     * @param id The article ID
     * @return BookmarkItem or null if not found
     */
    public BookmarkItem getBookmarkById(String id) {
        List<BookmarkItem> bookmarks = getBookmarks();
        return bookmarks.stream()
                .filter(bookmark -> bookmark.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // Get all bookmarks
    public List<BookmarkItem> getBookmarks() {
        String json = prefs.getString(KEY_BOOKMARKS, null);
        if (json == null) {
            return new ArrayList<>();
        }

        try {
            Type type = new TypeToken<ArrayList<BookmarkItem>>() {}.getType();
            List<BookmarkItem> bookmarks = gson.fromJson(json, type);
            return bookmarks != null ? bookmarks : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Save bookmarks to SharedPreferences
    private void saveBookmarks(List<BookmarkItem> bookmarks) {
        String json = gson.toJson(bookmarks);
        prefs.edit().purString(KEY_BOOKMARKS, json).apply();
    }

    // Get bookmark count
    public int getBookmarkCount() {
        return getBookmarks().size();
    }

    // Clear all bookmarks
    public void clearAllBookmarks() {
        editor.remove(KEY_BOOKMARKS);
        editor.apply();
    }
}