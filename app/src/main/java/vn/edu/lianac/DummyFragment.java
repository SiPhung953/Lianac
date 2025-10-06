package vn.edu.lianac;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.Nullable;



public class DummyFragment extends Fragment {



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dummy, container, false);
    }

    private BookmarkManager bookmarkManager;
    private String articleId = "dummy";
    private String articleTitle = "DUMMY"; // Title for bookmark list

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bookmarkManager = new BookmarkManager(requireContext());
        toggleBookmark();
    }

    private void toggleBookmark() {
        if (bookmarkManager.isBookmarked(articleId)) {
            bookmarkManager.removeBookmark(articleId);
            Toast.makeText(requireContext(), "Bookmark removed", Toast.LENGTH_SHORT).show();
        } else {
            BookmarkItem item = new BookmarkItem(
                    articleId,
                    articleTitle,
                    System.currentTimeMillis()
            );
            bookmarkManager.addBookmark(item);
            Toast.makeText(requireContext(), "Bookmarked!", Toast.LENGTH_SHORT).show();
        }
    }
}