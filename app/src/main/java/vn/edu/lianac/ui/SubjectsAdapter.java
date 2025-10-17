package vn.edu.lianac.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.edu.lianac.R;
import vn.edu.lianac.utils.CategoryProvider;

/**
 * Adapter for displaying subject categories in a RecyclerView
 */
public class SubjectsAdapter extends RecyclerView.Adapter<SubjectsAdapter.SubjectViewHolder> {

    private final List<String> subjects;
    private final OnSubjectClickListener listener;

    public SubjectsAdapter(List<String> subjects, OnSubjectClickListener listener) {
        this.subjects = subjects;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        String categoryId = subjects.get(position);
        holder.bind(categoryId);
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    public interface OnSubjectClickListener {
        void onSubjectClick(String categoryId, String displayName);
    }

    class SubjectViewHolder extends RecyclerView.ViewHolder {
        private final TextView subjectName;
        private final TextView subjectDescription;

        SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectName = itemView.findViewById(R.id.subjectName);
            subjectDescription = itemView.findViewById(R.id.subjectDescription);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    String categoryId = subjects.get(position);
                    String displayName = CategoryProvider.getCategoryName(categoryId);
                    listener.onSubjectClick(categoryId, displayName);
                }
            });
        }

        void bind(String categoryId) {
            String displayName = CategoryProvider.getCategoryName(categoryId);
            String description = getSubjectDescription(categoryId, displayName);

            // Format: "Subject Name (code)"
            subjectName.setText(String.format("%s (%s)", displayName, categoryId));
            subjectDescription.setText(description);
        }

        private String getSubjectDescription(String categoryId, String displayName) {
            String resourceName = "category_desc_" + categoryId.toLowerCase().replace(".", "_").replace("-", "_");
            int resId = itemView.getContext().getResources().getIdentifier(
                    resourceName, "string", itemView.getContext().getPackageName());

            if (resId != 0) {
                return itemView.getContext().getString(resId);
            }

            // ✅ Use Android string resource fallback
            return itemView.getContext().getString(R.string.category_short_description, displayName);
        }

    }
}