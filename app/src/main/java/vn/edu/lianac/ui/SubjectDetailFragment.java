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

import java.util.ArrayList;
import java.util.List;

import vn.edu.lianac.MainActivity;
import vn.edu.lianac.R;
import vn.edu.lianac.utils.CategoryProvider;

/**
 * Fragment to display detailed information about a subject with hierarchical navigation
 */
public class SubjectDetailFragment extends Fragment {

    private static final String ARG_CATEGORY_ID = "category_id";
    private static final String ARG_CATEGORY_NAME = "category_name";
    private static final String ARG_BREADCRUMB_PATH = "breadcrumb_path";

    private String categoryId;
    private String categoryName;
    private ArrayList<BreadcrumbItem> breadcrumbPath;

    private LinearLayout hierarchyNavigation;
    private LinearLayout breadcrumbContainer;
    private TextView subjectHeader;
    private TextView subjectDescription;
    private TextView subcategoriesTitle;
    private Button btnNew, btnRecent, btnPopular;
    private LinearLayout subcategoriesContainer;

    /**
     * Create new instance with breadcrumb tracking
     */
    public static SubjectDetailFragment newInstance(String categoryId, String categoryName) {
        return newInstance(categoryId, categoryName, new ArrayList<>());
    }

    public static SubjectDetailFragment newInstance(String categoryId, String categoryName,
                                                    ArrayList<BreadcrumbItem> breadcrumbPath) {
        SubjectDetailFragment fragment = new SubjectDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_ID, categoryId);
        args.putString(ARG_CATEGORY_NAME, categoryName);
        args.putParcelableArrayList(ARG_BREADCRUMB_PATH, breadcrumbPath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString(ARG_CATEGORY_ID);
            categoryName = getArguments().getString(ARG_CATEGORY_NAME);
            breadcrumbPath = getArguments().getParcelableArrayList(ARG_BREADCRUMB_PATH);
            if (breadcrumbPath == null) {
                breadcrumbPath = new ArrayList<>();
            }
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
        setupBreadcrumbs();
        setupClickListeners();
        loadSubjectData();
    }

    private void initViews(View view) {
        hierarchyNavigation = view.findViewById(R.id.hierarchyNavigation);
        breadcrumbContainer = view.findViewById(R.id.breadcrumbContainer);
        subjectHeader = view.findViewById(R.id.subjectHeader);
        subjectDescription = view.findViewById(R.id.subjectDescription);
        subcategoriesTitle = view.findViewById(R.id.subcategoriesTitle);
        subcategoriesContainer = view.findViewById(R.id.subcategoriesContainer);
        btnNew = view.findViewById(R.id.btnNew);
        btnRecent = view.findViewById(R.id.btnRecent);
        btnPopular = view.findViewById(R.id.btnPopular);
    }

    private void setupBreadcrumbs() {
        if (breadcrumbPath.isEmpty()) {
            // No breadcrumbs for top-level categories
            hierarchyNavigation.setVisibility(View.GONE);
            return;
        }

        hierarchyNavigation.setVisibility(View.VISIBLE);
        breadcrumbContainer.removeAllViews();

        // Add "Lianac" home link
        TextView homeLink = createBreadcrumbLink("Lianac", null);
        homeLink.setOnClickListener(v -> navigateToHome());
        breadcrumbContainer.addView(homeLink);

        // Add separator
        breadcrumbContainer.addView(createBreadcrumbSeparator());

        // Add each breadcrumb item
        for (int i = 0; i < breadcrumbPath.size(); i++) {
            BreadcrumbItem item = breadcrumbPath.get(i);

            // All parent items are clickable (navigate back to them)
            TextView link = createBreadcrumbLink(item.name, item);
            final int index = i;
            link.setOnClickListener(v -> navigateToBreadcrumb(index));
            breadcrumbContainer.addView(link);

            // Add separator after each item (including last one for current category)
            breadcrumbContainer.addView(createBreadcrumbSeparator());
        }

        // Add current category as non-clickable last item
        TextView currentLink = createBreadcrumbCurrent(categoryName);
        breadcrumbContainer.addView(currentLink);
    }

    private TextView createBreadcrumbLink(String text, BreadcrumbItem item) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setTextColor(getResources().getColor(R.color.cornell_red, null));
        textView.setTextSize(14);
        textView.setClickable(true);
        textView.setFocusable(true);
        textView.setBackgroundResource(android.R.drawable.list_selector_background);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 4, 0);
        textView.setLayoutParams(params);

        if (item != null) {
            textView.setTag(item);
        }

        return textView;
    }

    private TextView createBreadcrumbCurrent(String text) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setTextColor(getResources().getColor(android.R.color.black, null));
        textView.setTextSize(14);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textView.setLayoutParams(params);

        return textView;
    }

    private TextView createBreadcrumbSeparator() {
        TextView separator = new TextView(requireContext());
        separator.setText(" > ");
        separator.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
        separator.setTextSize(14);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(4, 0, 4, 0);
        separator.setLayoutParams(params);

        return separator;
    }

    private void navigateToHome() {
        // Navigate back to SubjectsFragment (main categories list)
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToContent(new SubjectsFragment());
        }
    }

    private void navigateToBreadcrumb(int index) {
        // Navigate to a parent category in the breadcrumb path
        BreadcrumbItem item = breadcrumbPath.get(index);

        // Create new breadcrumb path up to this point
        ArrayList<BreadcrumbItem> newPath = new ArrayList<>(breadcrumbPath.subList(0, index));

        SubjectDetailFragment fragment = SubjectDetailFragment.newInstance(
                item.categoryId,
                item.name,
                newPath
        );

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).loadContentFragment(fragment, false);
        }
    }

    private void setupClickListeners() {
        btnNew.setOnClickListener(v -> showPapers("submittedDate"));
        btnRecent.setOnClickListener(v -> showPapers("lastUpdatedDate"));
        btnPopular.setOnClickListener(v -> showPapers("relevance"));
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

        // Set click listener to navigate to subcategory detail
        subcategoryView.setOnClickListener(v -> {
            // Create new breadcrumb path including current category
            ArrayList<BreadcrumbItem> newPath = new ArrayList<>(breadcrumbPath);
            newPath.add(new BreadcrumbItem(categoryId, categoryName));

            // Navigate to subcategory detail
            SubjectDetailFragment fragment = SubjectDetailFragment.newInstance(
                    subcategoryId,
                    displayName,
                    newPath
            );

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToContent(fragment);
            }
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

    private void showPapers(String sortBy) {
        if (getActivity() instanceof MainActivity) {
            // Get ViewModel and execute category search
            androidx.lifecycle.ViewModelProvider viewModelProvider =
                    new androidx.lifecycle.ViewModelProvider(requireActivity());
            vn.edu.lianac.viewmodel.SearchViewModel viewModel =
                    viewModelProvider.get(vn.edu.lianac.viewmodel.SearchViewModel.class);
            viewModel.searchByCategory(categoryId, sortBy);

            // Navigate to ArticleListingFragment
            ((MainActivity) getActivity()).navigateToContent(new ArticleListingFragment());
        }
    }
    /**
     * Parcelable class to store breadcrumb information
     */
    public static class BreadcrumbItem implements android.os.Parcelable {
        public final String categoryId;
        public final String name;

        public BreadcrumbItem(String categoryId, String name) {
            this.categoryId = categoryId;
            this.name = name;
        }

        protected BreadcrumbItem(android.os.Parcel in) {
            categoryId = in.readString();
            name = in.readString();
        }

        @Override
        public void writeToParcel(android.os.Parcel dest, int flags) {
            dest.writeString(categoryId);
            dest.writeString(name);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<BreadcrumbItem> CREATOR = new Creator<BreadcrumbItem>() {
            @Override
            public BreadcrumbItem createFromParcel(android.os.Parcel in) {
                return new BreadcrumbItem(in);
            }

            @Override
            public BreadcrumbItem[] newArray(int size) {
                return new BreadcrumbItem[size];
            }
        };
    }
}