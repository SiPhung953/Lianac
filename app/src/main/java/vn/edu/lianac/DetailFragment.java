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
import androidx.fragment.app.FragmentTransaction;

public class DetailFragment extends Fragment {

    private ImageView downloadIcon, bookmarkIcon, menuIcon;
    private TextView readButton, textSubject, textSubclass;
    private boolean isBookmarked = false;

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

        // --- Lấy dữ liệu từ arguments (nếu có) ---
        Bundle args = getArguments();
        String subjectName = args != null ? args.getString("subject_name", "Subject Name") : "Subject Name";
        String subclassName = args != null ? args.getString("subclass_name", "Subclass") : "Subclass";
        String pdfUrl = args != null ? args.getString("pdf_url", "") : "";

        textSubject.setText(subjectName);
        textSubclass.setText(subclassName);

        // --- Khi ấn vào Subject Name ---
        textSubject.setOnClickListener(v -> openSubjectFragment(subjectName));

        // --- Khi ấn vào Subclass ---
        textSubclass.setOnClickListener(v -> openSubclassFragment(subjectName, subclassName));

        // --- MENU ---
        if (menuIcon != null) {
            menuIcon.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Menu clicked", Toast.LENGTH_SHORT).show()
            );
        }

        // --- BOOKMARK ---
        if (bookmarkIcon != null) {
            bookmarkIcon.setOnClickListener(v -> {
                isBookmarked = !isBookmarked;
                if (isBookmarked) {
                    bookmarkIcon.setImageResource(R.drawable.bookmark_done);
                    Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
                } else {
                    bookmarkIcon.setImageResource(R.drawable.bookmark);
                    Toast.makeText(getContext(), "Unsaved", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // --- DOWNLOAD (popup) ---
        if (downloadIcon != null) {
            downloadIcon.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Loading...", Toast.LENGTH_SHORT).show();
                downloadIcon.setEnabled(false);

                new Handler().postDelayed(() -> {
                    boolean success = Math.random() > 0.3;
                    if (success) {
                        downloadIcon.setImageResource(R.drawable.check);
                        Toast.makeText(getContext(), "Done!", Toast.LENGTH_SHORT).show();
                    } else {
                        downloadIcon.setImageResource(R.drawable.cancel);
                        Toast.makeText(getContext(), "Error!", Toast.LENGTH_SHORT).show();
                    }
                    downloadIcon.setEnabled(true);
                }, 2000);
            });
        }

        // --- READ BUTTON ---
        if (readButton != null) {
            readButton.setOnClickListener(v -> {
                if (pdfUrl.isEmpty()) {
                    Toast.makeText(getContext(), "No PDF link available!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(pdfUrl), "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "No app found to open PDF!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        return view;
    }

    // --- Mở SubjectFragment (hiển thị danh sách subject) ---
    private void openSubjectFragment(String subjectName) {
        Fragment fragment = new SubjectFragment();
        Bundle args = new Bundle();
        args.putString("subject_name", subjectName);
        fragment.setArguments(args);

        ((MainActivity) getActivity()).replaceFragment(fragment);
    }

    // --- Mở SubclassFragment (hiển thị danh sách bài viết trong subclass) ---
    private void openSubclassFragment(String subjectName, String subclassName) {
        Fragment fragment = new SubclassFragment();
        Bundle args = new Bundle();
        args.putString("subject_name", subjectName);
        args.putString("subclass_name", subclassName);
        fragment.setArguments(args);

        ((MainActivity) getActivity()).replaceFragment(fragment);
    }
}
