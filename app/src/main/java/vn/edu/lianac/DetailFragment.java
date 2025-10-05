package vn.edu.lianac;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class DetailFragment extends Fragment {

    private ImageView downloadIcon, bookmarkIcon, menuIcon;
    private TextView statusText;
    private boolean isBookmarked = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        downloadIcon = view.findViewById(R.id.download_icon);
        bookmarkIcon = view.findViewById(R.id.bookmark_icon);
        menuIcon = view.findViewById(R.id.menu_icon);
        statusText = view.findViewById(R.id.download_status);

        // --- MENU ---
        menuIcon.setOnClickListener(v ->
                Toast.makeText(getContext(), "Menu clicked", Toast.LENGTH_SHORT).show()
        );

        // --- BOOKMARK ---
        bookmarkIcon.setOnClickListener(v -> {
            isBookmarked = !isBookmarked;
            if (isBookmarked) {
                bookmarkIcon.setImageResource(R.drawable.bookmark_done); // icon filled màu đen
                Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
            } else {
                bookmarkIcon.setImageResource(R.drawable.bookmark);
                Toast.makeText(getContext(), "Unsaved", Toast.LENGTH_SHORT).show();
            }
        });

        // --- DOWNLOAD ---
        downloadIcon.setOnClickListener(v -> {
            statusText.setText("Loading...");
            downloadIcon.setEnabled(false);

            // Giả lập quá trình tải
            new Handler().postDelayed(() -> {
                boolean success = Math.random() > 0.3; // 70% thành công
                if (success) {
                    downloadIcon.setImageResource(R.drawable.check); // icon tick
                    statusText.setText("Done!");
                } else {
                    downloadIcon.setImageResource(R.drawable.cancel); // icon error
                    statusText.setText("Error!");
                }
                downloadIcon.setEnabled(true);
            }, 2000); // 2 giây
        });

        return view;
    }
}
