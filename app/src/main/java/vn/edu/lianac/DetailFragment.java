package vn.edu.lianac;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import vn.edu.lianac.bookmark.BookmarkItem;
import vn.edu.lianac.bookmark.BookmarkManager;

public class DetailFragment extends Fragment {

    private ImageView downloadIcon, bookmarkIcon, menuIcon;
    private BookmarkManager bookmarkManager;
    private TextView readButton, textSubject, textSubclass;

    // You will need to get these values from the fragment's arguments
    private String articleId = "default_article_id"; // Placeholder
    private String subjectTitle = "Default Subject Title"; // Placeholder

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        // --- Init views ---
        downloadIcon = view.findViewById(R.id.download_icon);
        bookmarkIcon = view.findViewById(R.id.bookmark_icon);
        menuIcon = view.findViewById(R.id.menu_icon);
        readButton = view.findViewById(R.id.read_button);
        textSubject = view.findViewById(R.id.text_subject);
        textSubclass = view.findViewById(R.id.text_subclass);

        // Initialize BookmarkManager
        bookmarkManager = new BookmarkManager(requireContext());

        // --- Lấy dữ liệu từ arguments (nếu có) ---
        Bundle args = getArguments();
        String subjectName = args != null ? args.getString("subject_name", "Subject Name") : "Subject Name";
        String subclassName = args != null ? args.getString("subclass_name", "Subclass") : "Subclass";
        String pdfUrl = args != null ? args.getString("pdf_url", "") : "";
        // Example of how you might get articleId and subjectTitle from the bundle
        // articleId = args != null ? args.getString("article_id", articleId) : articleId;
        // subjectTitle = subjectName;

        textSubject.setText(subjectName);
        textSubclass.setText(subclassName);

        // --- Khi ấn vào Subject Name ---
        textSubject.setOnClickListener(v -> openSubjectFragment(subjectName));

        // --- Khi ấn vào Subclass ---
        textSubclass.setOnClickListener(v -> openSubclassFragment(subjectName, subclassName));

        // --- MENU ---
        if (menuIcon != null) {
            menuIcon.setOnClickListener(v ->
                    Toast.makeText(getContext(), R.string.menu_clicked, Toast.LENGTH_SHORT).show()
            );
        }

        // --- BOOKMARK ---
        updateBookmarkIcon(); // Set the initial icon state
        if (bookmarkIcon != null) {
            bookmarkIcon.setOnClickListener(v -> {
                toggleBookmark();
                updateBookmarkIcon(); // Update the icon after toggling
            });
        }

        // --- DOWNLOAD (popup) ---
        if (downloadIcon != null) {
            downloadIcon.setOnClickListener(v -> {
                Toast.makeText(getContext(), R.string.loading, Toast.LENGTH_SHORT).show();
                downloadIcon.setEnabled(false);

                new Handler().postDelayed(() -> {
                    boolean success = Math.random() > 0.3;
                    if (success) {
                        downloadIcon.setImageResource(R.drawable.check);
                        Toast.makeText(getContext(), R.string.done, Toast.LENGTH_SHORT).show();
                    } else {
                        downloadIcon.setImageResource(R.drawable.cancel);
                        Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                    }
                    downloadIcon.setEnabled(true);
                }, 2000);
            });
        }

        // --- READ BUTTON ---
        if (readButton != null) {
            readButton.setOnClickListener(v -> {
                if (pdfUrl.isEmpty()) {
                    Toast.makeText(getContext(), R.string.no_pdf_link_available, Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(pdfUrl), "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), R.string.no_app_found_to_open_pdf, Toast.LENGTH_SHORT).show();
                }
            });
        }

        return view;
    }

    private void toggleBookmark() {
        if (bookmarkManager.isBookmarked(articleId)) {
            bookmarkManager.removeBookmark(articleId);
            Toast.makeText(requireContext(), R.string.bookmark_removed, Toast.LENGTH_SHORT).show();
        } else {
            BookmarkItem item = new BookmarkItem(
                    articleId,
                    subjectTitle,
                    System.currentTimeMillis()
            );
            bookmarkManager.addBookmark(item);
            Toast.makeText(requireContext(), R.string.bookmarked, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBookmarkIcon() {
        if (bookmarkManager != null && bookmarkManager.isBookmarked(articleId)) {
            bookmarkIcon.setImageResource(R.drawable.bookmark_done);
        } else if (bookmarkIcon != null) {
            bookmarkIcon.setImageResource(R.drawable.bookmark);
        }
    }

    // --- Mở SubjectFragment (hiển thị danh sách subject) ---
    private void openSubjectFragment(String subjectName) {
        if (getActivity() instanceof MainActivity) {
            Fragment fragment = new SubjectFragment();
            Bundle args = new Bundle();
            args.putString("subject_name", subjectName);
            fragment.setArguments(args);
            ((MainActivity) getActivity()).replaceFragment(fragment);
        }
    }

    // --- SubclassFragment ---
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
