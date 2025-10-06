package vn.edu.lianac;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class SubclassFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subject, container, false);

        LinearLayout listContainer = view.findViewById(R.id.list_container);
        TextView subjectTitle = view.findViewById(R.id.subject_title);

        // --- Nhận dữ liệu từ DetailFragment ---
        Bundle args = getArguments();
        String subjectName = args != null ? args.getString("subject_name", "Unknown Subject") : "Unknown Subject";
        String subclassName = args != null ? args.getString("subclass_name", "Unknown Subclass") : "Unknown Subclass";

        subjectTitle.setText("📚 " + subjectName + " → " + subclassName);

        // --- Giả lập danh sách paper ---
        String[] papers = {
                "Paper 1 in " + subclassName,
                "Paper 2 in " + subclassName,
                "Paper 3 in " + subclassName
        };

        for (String paper : papers) {
            TextView item = new TextView(getContext());
            item.setText("📄 " + paper);
            item.setTextSize(16);
            item.setPadding(16, 16, 16, 16);
            item.setTextColor(0xFF000000);
            listContainer.addView(item);
        }

        return view;
    }
}
