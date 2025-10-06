package vn.edu.lianac;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addTestBookmarks();

        // Load BookmarkListFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new BookmarkListFragment())
                    .commit();
        }
    }
    private void addTestBookmarks() {
        BookmarkManager manager = new BookmarkManager(this);

        // Add sample bookmarks for testing
        manager.addBookmark(new BookmarkItem(
                "test_1",
                "First Test Article",
                System.currentTimeMillis()
        ));

        manager.addBookmark(new BookmarkItem(
                "test_2",
                "Second Test Article",
                System.currentTimeMillis() - 86400000  // 1 day ago
        ));

        manager.addBookmark(new BookmarkItem(
                "test_3",
                "Third Test Article",
                System.currentTimeMillis() - 172800000  // 2 days ago
        ));
    }
}