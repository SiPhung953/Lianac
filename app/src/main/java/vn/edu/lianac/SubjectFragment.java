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

public class SubjectFragment extends Fragment {

    private ImageView downloadButton, bookmarkButton, menuButton;
    private TextView subjectTitle;
    private LinearLayout listContainer;
    private boolean isBookmarked = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_subject, container, false);

        // Ánh xạ View
        subjectTitle = view.findViewById(R.id.subject_title);
        listContainer = view.findViewById(R.id.list_container);
        downloadButton = view.findViewById(R.id.download_icon);
        bookmarkButton = view.findViewById(R.id.bookmark_icon);
        menuButton = view.findViewById(R.id.menu_icon);

        // Nhận tên Subject từ arguments
        Bundle args = getArguments();
        String subjectName = (args != null) ? args.getString("subject_name", "Subject") : "Subject";
        subjectTitle.setText(subjectName);

        // Giả lập danh sách subclass trong subject
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

        // Hiển thị danh sách subclass
        for (String subclass : subclasses) {
            TextView item = new TextView(getContext());
            item.setText("📘 " + subclass);
            item.setTextSize(16);
            item.setPadding(16, 16, 16, 16);
            item.setTextColor(0xFF000000);
            item.setOnClickListener(v -> openSubclassFragment(subjectName, subclass));
            listContainer.addView(item);
        }

        // Menu
        if (menuButton != null) {
            menuButton.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Menu clicked", Toast.LENGTH_SHORT).show()
            );
        }

        // Bookmark
        if (bookmarkButton != null) {
            bookmarkButton.setOnClickListener(v -> {
                isBookmarked = !isBookmarked;
                if (isBookmarked) {
                    bookmarkButton.setImageResource(R.drawable.bookmark_done);
                    Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
                } else {
                    bookmarkButton.setImageResource(R.drawable.bookmark);
                    Toast.makeText(getContext(), "Unsaved", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Download (popup)
        if (downloadButton != null) {
            downloadButton.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Loading...", Toast.LENGTH_SHORT).show();
                downloadButton.setEnabled(false);

                new Handler().postDelayed(() -> {
                    boolean success = Math.random() > 0.3; // 70% success rate
                    if (success) {
                        downloadButton.setImageResource(R.drawable.check);
                        Toast.makeText(getContext(), "Done!", Toast.LENGTH_SHORT).show();
                    } else {
                        downloadButton.setImageResource(R.drawable.cancel);
                        Toast.makeText(getContext(), "Error!", Toast.LENGTH_SHORT).show();
                    }
                    downloadButton.setEnabled(true);
                }, 2000);
            });
        }

        return view;
    }

    private void openSubclassFragment(String subjectName, String subclassName) {
        Fragment fragment = new SubclassFragment();
        Bundle args = new Bundle();
        args.putString("subject_name", subjectName);
        args.putString("subclass_name", subclassName);
        fragment.setArguments(args);

        ((MainActivity) getActivity()).replaceFragment(fragment);
    }
}
