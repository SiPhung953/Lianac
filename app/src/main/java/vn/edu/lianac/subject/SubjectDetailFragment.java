package vn.edu.lianac.subject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import vn.edu.lianac.MainActivity;
import vn.edu.lianac.R;
import vn.edu.lianac.search.ArticleListingFragment;
import vn.edu.lianac.search.SearchViewModel;
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
        // Always show breadcrumbs at all levels
        hierarchyNavigation.setVisibility(View.VISIBLE);
        breadcrumbContainer.removeAllViews();

        // Add "Lianac" home link
        TextView homeLink = createBreadcrumbLink(getString(R.string.app_name), null);
        homeLink.setOnClickListener(v -> navigateToHome());
        breadcrumbContainer.addView(homeLink);

        // Add separator
        breadcrumbContainer.addView(createBreadcrumbSeparator());

        // Add each breadcrumb item from the path
        for (int i = 0; i < breadcrumbPath.size(); i++) {
            BreadcrumbItem item = breadcrumbPath.get(i);

            // Display the ID, all items are clickable
            TextView link = createBreadcrumbLink(item.categoryId, item);
            final int index = i;
            link.setOnClickListener(v -> navigateToBreadcrumb(index));
            breadcrumbContainer.addView(link);

            // Add separator
            breadcrumbContainer.addView(createBreadcrumbSeparator());
        }

        // Add current category as clickable item (displays ID)
        TextView currentLink = createBreadcrumbLink(categoryId, null);
        currentLink.setOnClickListener(v -> {
            // Clicking current category reloads the same page (scrolls to top if needed)
            SubjectDetailFragment fragment = SubjectDetailFragment.newInstance(
                    categoryId,
                    categoryName,
                    breadcrumbPath
            );
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).replaceFragment(fragment);
            }
        });
        breadcrumbContainer.addView(currentLink);
    }

    private TextView createBreadcrumbLink(String text, BreadcrumbItem item) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setTextSize(14);
        textView.setClickable(true);
        textView.setFocusable(true);

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
            ((MainActivity) getActivity()).replaceFragment(new SubjectListingFragment());
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
            ((MainActivity) getActivity()).replaceFragment(fragment);
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
        return getString(R.string.category_description, categoryName);
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
                ((MainActivity) getActivity()).replaceFragment(fragment);
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
        return getString(R.string.category_short_description, displayName);
    }

    private void showPapers(String sortBy) {
        if (getActivity() instanceof MainActivity) {
            // Get ViewModel and execute category search
            androidx.lifecycle.ViewModelProvider viewModelProvider =
                    new androidx.lifecycle.ViewModelProvider(requireActivity());
            SearchViewModel viewModel =
                    viewModelProvider.get(SearchViewModel.class);

            // Mark as category browse search
            viewModel.searchByCategory(categoryId, sortBy, true);

            // Navigate to ArticleListingFragment
            ((MainActivity) getActivity()).replaceFragment(new ArticleListingFragment());
        }
    }

    public static class BreadcrumbItem implements android.os.Parcelable {
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
    }
}