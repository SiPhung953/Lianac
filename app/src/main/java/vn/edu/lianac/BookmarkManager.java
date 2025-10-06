package vn.edu.lianac;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class BookmarkManager {
    private static final String PREF_NAME = "BookmarkPreferences";
    private static final String KEY_BOOKMARKS = "bookmarks";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public BookmarkManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Save a bookmark
    public void addBookmark(BookmarkItem item) {
        List<BookmarkItem> bookmarks = getBookmarks();

        // Check if already bookmarked
        for (BookmarkItem bookmark : bookmarks) {
            if (bookmark.getId().equals(item.getId())) {
                return; // Already exists
            }
        }

        bookmarks.add(item);
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
        for (BookmarkItem item : bookmarks) {
            if (item.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    // Get all bookmarks
    public List<BookmarkItem> getBookmarks() {
        List<BookmarkItem> bookmarks = new ArrayList<>();
        String json = prefs.getString(KEY_BOOKMARKS, "[]");

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                BookmarkItem item = new BookmarkItem(
                        obj.getString("id"),
                        obj.getString("title"),
                        obj.getLong("timestamp")
                );
                bookmarks.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return bookmarks;
    }

    // Save bookmarks to SharedPreferences
    private void saveBookmarks(List<BookmarkItem> bookmarks) {
        JSONArray array = new JSONArray();

        try {
            for (BookmarkItem item : bookmarks) {
                JSONObject obj = new JSONObject();
                obj.put("id", item.getId());
                obj.put("title", item.getTitle());
                obj.put("timestamp", item.getTimestamp());
                array.put(obj);
            }

            editor.putString(KEY_BOOKMARKS, array.toString());
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Clear all bookmarks
    public void clearAllBookmarks() {
        editor.remove(KEY_BOOKMARKS);
        editor.apply();
    }
}