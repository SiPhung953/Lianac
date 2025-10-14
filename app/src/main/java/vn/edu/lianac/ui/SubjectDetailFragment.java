package vn.edu.lianac.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

import vn.edu.lianac.R;
import vn.edu.lianac.utils.CategoryProvider;

/**
 * Fragment to display detailed information about a subject
 */
public class SubjectDetailFragment extends Fragment {

    private static final String ARG_CATEGORY_ID = "category_id";
    private static final String ARG_CATEGORY_NAME = "category_name";

    private String categoryId;
    private String categoryName;

    private TextView subjectHeader;
    private TextView subjectDescription;
    private TextView subcategoriesTitle;
    private Button btnNew, btnRecent, btnPopular;

    private LinearLayout subcategoriesContainer;

    public static SubjectDetailFragment newInstance(String categoryId, String categoryName) {
        SubjectDetailFragment fragment = new SubjectDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_ID, categoryId);
        args.putString(ARG_CATEGORY_NAME, categoryName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString(ARG_CATEGORY_ID);
            categoryName = getArguments().getString(ARG_CATEGORY_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subject_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupClickListeners();
        loadSubjectData();
    }

    private void initViews(View view) {
        subjectHeader = view.findViewById(R.id.subjectHeader);
        subjectDescription = view.findViewById(R.id.subjectDescription);
        subcategoriesTitle = view.findViewById(R.id.subcategoriesTitle);
        subcategoriesContainer = view.findViewById(R.id.subcategoriesContainer);
        btnNew = view.findViewById(R.id.btnNew);
        btnRecent = view.findViewById(R.id.btnRecent);
        btnPopular = view.findViewById(R.id.btnPopular);
    }

    private void setupClickListeners() {
        btnNew.setOnClickListener(v -> showPapers("new"));
        btnRecent.setOnClickListener(v -> showPapers("recent"));
        btnPopular.setOnClickListener(v -> showPapers("popular"));
    }

    private void loadSubjectData() {
        // Set header with name and code
        subjectHeader.setText(String.format("%s (%s)", categoryName, categoryId));

        // Set description
        String description = getSubjectDescription(categoryId);
        subjectDescription.setText(description);

        // Load subcategories if available
        loadSubcategories();
    }

    private String getSubjectDescription(String categoryId) {
        String resourceName = "category_desc_" + categoryId.toLowerCase().replace(".", "_").replace("-", "_");
        int resId = requireContext().getResources().getIdentifier(
                resourceName, "string", requireContext().getPackageName());

        if (resId != 0) {
            return requireContext().getString(resId);
        }

        // Fallback description
        return String.format("Comprehensive collection of research papers, preprints, and publications in %s. " +
                        "Browse the latest research, explore subfields, and discover cutting-edge developments in this field.",
                categoryName);
    }

    private void loadSubcategories() {
        List<String> subcategories = CategoryProvider.getSubcategories(categoryId);

        if (subcategories != null && !subcategories.isEmpty()) {
            subcategoriesTitle.setVisibility(View.VISIBLE);
            subcategoriesContainer.setVisibility(View.VISIBLE);

            // Clear any existing views
            subcategoriesContainer.removeAllViews();

            // Inflate and add subcategory items dynamically
            LayoutInflater inflater = LayoutInflater.from(requireContext());

            for (String subcategoryId : subcategories) {
                View subcategoryView = inflater.inflate(R.layout.item_subject,
                        subcategoriesContainer, false);

                setupSubcategoryView(subcategoryView, subcategoryId);
                subcategoriesContainer.addView(subcategoryView);
            }
        } else {
            subcategoriesTitle.setVisibility(View.GONE);
            subcategoriesContainer.setVisibility(View.GONE);
        }
    }

    private void setupSubcategoryView(View subcategoryView, String subcategoryId) {
        TextView subjectName = subcategoryView.findViewById(R.id.subjectName);
        TextView subjectDescription = subcategoryView.findViewById(R.id.subjectDescription);

        String displayName = CategoryProvider.getCategoryName(subcategoryId);
        String description = getSubcategoryDescription(subcategoryId, displayName);

        subjectName.setText(String.format("%s (%s)", displayName, subcategoryId));
        subjectDescription.setText(description);

        // Set click listener
        subcategoryView.setOnClickListener(v -> {
            // Navigate to dummy destination for now
            Toast.makeText(requireContext(),
                    "Subcategory: " + displayName + " (" + subcategoryId + ")",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private String getSubcategoryDescription(String categoryId, String displayName) {
        String resourceName = "category_desc_" + categoryId.toLowerCase().replace(".", "_").replace("-", "_");
        int resId = requireContext().getResources().getIdentifier(
                resourceName, "string", requireContext().getPackageName());

        if (resId != 0) {
            return requireContext().getString(resId);
        }

        // Fallback description for subcategories
        return String.format("Research papers and preprints in %s", displayName);
    }

    private void showPapers(String type) {
        // For now, just show a toast
        String message = String.format("Showing %s papers in %s", type, categoryName);
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

        // TODO: Later implement actual paper browsing
        // This could navigate to ArticleListingFragment with pre-configured search
    }
}