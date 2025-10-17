package vn.edu.lianac;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import vn.edu.lianac.bookmark.BookmarkItem;
import vn.edu.lianac.bookmark.BookmarkManager;

public class SubjectFragment extends Fragment {

    private ImageView downloadButton, bookmarkButton, menuButton;
    private TextView subjectTitle;
    private LinearLayout listContainer;
    private BookmarkManager bookmarkManager;
    private String subjectName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_subject, container, false);

        subjectTitle = view.findViewById(R.id.subject_title);
        listContainer = view.findViewById(R.id.list_container);
        downloadButton = view.findViewById(R.id.download_icon);
        bookmarkButton = view.findViewById(R.id.bookmark_icon);
        menuButton = view.findViewById(R.id.menu_icon);

        bookmarkManager = new BookmarkManager(requireContext());

        Bundle args = getArguments();
        subjectName = (args != null) ? args.getString("subject_name", "Subject") : "Subject";
        subjectTitle.setText(subjectName);

        String[] subclasses;
        switch (subjectName) {
            case "Astrophysics":
                subclasses = new String[]{"Stars", "Galaxies", "Black Holes"};
                break;
            case "Mathematics":
                subclasses = new String[]{"Algebra", "Geometry", "Calculus"};
                break;
            default:
                subclasses = new String[]{"Topic A", "Topic B", "Topic C"};
                break;
        }

        // Display the list of subclasses
        for (String subclass : subclasses) {
            TextView item = new TextView(getContext());
            item.setText("📘 " + subclass);
            item.setTextSize(16);
            item.setPadding(16, 16, 16, 16);
            item.setTextColor(0xFF000000);
            item.setOnClickListener(v -> {
                String clickedSubclass = ((TextView) v).getText().toString().replace("📘 ", "");
                openSubclassFragment(subjectName, clickedSubclass);
            });
            listContainer.addView(item);
        }

        if (menuButton != null) {
            menuButton.setOnClickListener(v ->
                    Toast.makeText(getContext(), R.string.menu_clicked, Toast.LENGTH_SHORT).show()
            );
        }

        updateBookmarkIcon();
        if (bookmarkButton != null) {
            bookmarkButton.setOnClickListener(v -> {
                toggleBookmark();
                updateBookmarkIcon();
            });
        }

        if (downloadButton != null) {
            downloadButton.setOnClickListener(v -> {
                Toast.makeText(getContext(), R.string.loading, Toast.LENGTH_SHORT).show();
                downloadButton.setEnabled(false);

                new Handler().postDelayed(() -> {
                    boolean success = Math.random() > 0.3;
                    if (success) {
                        downloadButton.setImageResource(R.drawable.check);
                        Toast.makeText(getContext(), R.string.done, Toast.LENGTH_SHORT).show();
                    } else {
                        downloadButton.setImageResource(R.drawable.cancel);
                        Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                    }
                    downloadButton.setEnabled(true);
                }, 2000);
            });
        }

        return view;
    }

    private void toggleBookmark() {
        if (bookmarkManager.isBookmarked(subjectName)) {
            bookmarkManager.removeBookmark(subjectName);
            Toast.makeText(requireContext(), R.string.bookmark_removed, Toast.LENGTH_SHORT).show();
        } else {
            BookmarkItem item = new BookmarkItem(
                    subjectName,
                    subjectName,
                    System.currentTimeMillis()
            );
            bookmarkManager.addBookmark(item);
            Toast.makeText(requireContext(), R.string.bookmarked, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBookmarkIcon() {
        if (bookmarkManager != null && bookmarkManager.isBookmarked(subjectName)) {
            bookmarkButton.setImageResource(R.drawable.bookmark_done);
        } else if (bookmarkButton != null) {
            bookmarkButton.setImageResource(R.drawable.bookmark);
        }
    }

    private void openSubclassFragment(String subjectName, String subclassName) {
        if (getActivity() instanceof MainActivity) {
            Fragment fragment = new SubclassFragment();
            Bundle args = new Bundle();
            args.putString("subject_name", subjectName);
            args.putString("subclass_name", subclassName);
            fragment.setArguments(args);
            ((MainActivity) getActivity()).replaceFragment(fragment);
        }
    }
}
