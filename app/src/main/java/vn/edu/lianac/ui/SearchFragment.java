package vn.edu.lianac.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import vn.edu.lianac.R;
import vn.edu.lianac.models.QueryOptions;
import vn.edu.lianac.viewmodel.SearchViewModel;

/**
 * Basic search fragment with drawer for advanced filters.
 * <p>
 * Responsibilities:
 * - Basic search bar (search term + field selector)
 * - Drawer management (open/close animations)
 * - Load ManageSearchFragment into drawer
 * - Perform basic search when user presses Enter
 */
public class SearchFragment extends Fragment {

    // Basic search views
    private String[] fieldCodes;
    private EditText searchBox;
    private Spinner fieldSpinner;
    private ImageButton filterButton;

    // Drawer container
    private FrameLayout advancedDrawer;
    private View scrimOverlay;
    private boolean isDrawerOpen = false;

    // ViewModel
    private SearchViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);

        initViews(view);
        setupBasicSearch();
        loadManageSearchFragment();
        initFieldCodes();
    }

    private void initViews(View view) {
        searchBox = view.findViewById(R.id.searchBox);
        fieldSpinner = view.findViewById(R.id.fieldSpinner);
        filterButton = view.findViewById(R.id.filterButton);
        advancedDrawer = view.findViewById(R.id.advancedSearchDrawer);
        scrimOverlay = view.findViewById(R.id.scrimOverlay);
    }

    private void setupBasicSearch() {
        // Field spinner
        ArrayAdapter<CharSequence> fieldAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.search_fields, android.R.layout.simple_spinner_item);
        fieldAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fieldSpinner.setAdapter(fieldAdapter);

        // Search on Enter key
        searchBox.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performBasicSearch();
                return true;
            }
            return false;
        });

        // Filter button opens drawer
        filterButton.setOnClickListener(v -> openDrawer());

        // Scrim closes drawer
        scrimOverlay.setOnClickListener(v -> closeDrawer());
    }

    /**
     * Load ManageSearchFragment into the drawer container
     */
    private void loadManageSearchFragment() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.advancedSearchDrawer, new ManageSearchFragment())
                .commit();
    }

    public String getCurrentSearchTerm() {
        return searchBox.getText().toString().trim();
    }

    public String getCurrentSearchField() {
        return getFieldCode(fieldSpinner.getSelectedItemPosition());
    }

    /**
     * Perform basic search while preserving any existing advanced filters
     */
    private void performBasicSearch() {
        String searchTerm = searchBox.getText().toString().trim();
        String field = getFieldCode(fieldSpinner.getSelectedItemPosition());

        // Get existing query to preserve advanced filters
        QueryOptions currentQuery = viewModel.getCurrentQuery().getValue();

        // Check if we have any advanced filters
        boolean hasAdvancedFilters = currentQuery != null && (
                (currentQuery.getRows() != null && !currentQuery.getRows().isEmpty()) ||
                        (currentQuery.getCategories() != null && !currentQuery.getCategories().isEmpty()) ||
                        currentQuery.hasDateFilter()
        );

        // If no search term AND no advanced filters, show error
        if (searchTerm.isEmpty() && !hasAdvancedFilters) {
            Toast.makeText(getContext(), R.string.validation_no_search_term, Toast.LENGTH_SHORT).show();
            return;
        }

        QueryOptions.Builder builder;
        if (currentQuery != null) {
            // GOOD: Start with existing query to preserve ALL advanced filters
            builder = currentQuery.toBuilder();
        } else {
            builder = new QueryOptions.Builder();
        }

        // Update basic search term and field
        if (!searchTerm.isEmpty()) {
            builder.searchTerm(searchTerm).searchField(field);
        } else {
            // Clear search term if empty, let advanced filters drive the query
            builder.searchTerm(null).searchField(null);
        }

        builder.start(0);  // Reset to first page for new search

        QueryOptions query = builder.build();
        viewModel.updateQueryOptions(query); // Update ViewModel first
        viewModel.search(query);
    }

    // ==================== DRAWER MANAGEMENT ====================

    /**
     * Open the advanced search drawer
     */
    private void openDrawer() {
        if (isDrawerOpen) return;

        advancedDrawer.setVisibility(View.VISIBLE);
        scrimOverlay.setVisibility(View.VISIBLE);

        // Slide in from right
        advancedDrawer.animate()
                .translationX(0)
                .setDuration(300)
                .start();

        // Fade in scrim
        scrimOverlay.setAlpha(0f);
        scrimOverlay.animate()
                .alpha(1f)
                .setDuration(300)
                .start();

        isDrawerOpen = true;
    }

    /**
     * Close the advanced search drawer
     * Called by ManageSearchFragment when user applies filters or closes drawer
     */
    public void closeDrawer() {
        if (!isDrawerOpen) return;

        // Slide out to right
        advancedDrawer.animate()
                .translationX(advancedDrawer.getWidth())
                .setDuration(300)
                .start();

        // Fade out scrim
        scrimOverlay.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    scrimOverlay.setVisibility(View.GONE);
                    advancedDrawer.setVisibility(View.GONE);

                    // NEW: Notify ManageSearchFragment that drawer has closed
                    notifyDrawerClosed();
                })
                .start();

        isDrawerOpen = false;
    }

    // NEW METHOD: Call ManageSearchFragment's cleanup
    private void notifyDrawerClosed() {
        Fragment childFragment = getChildFragmentManager().findFragmentById(R.id.advancedSearchDrawer);
        if (childFragment instanceof ManageSearchFragment) {
            ((ManageSearchFragment) childFragment).onDrawerClosed();
        }
    }

    // ==================== HELPERS ====================

    public boolean isDrawerOpen() {
        return isDrawerOpen;
    }

    public void updateFilterIndicator(boolean hasFilters) {
        if (filterButton != null) {
            if (hasFilters) {
                filterButton.setBackgroundResource(R.drawable.filter_button_with_indicator);
            } else {
                filterButton.setBackgroundResource(R.drawable.filter_button_normal);
            }
        }
    }

    public void setSearchTerm(String term) {
        if (searchBox != null && term != null) {
            searchBox.setText(term);
        }
    }

    public void setSearchField(String fieldCode) {
        if (fieldSpinner != null && fieldCode != null) {
            if (fieldCodes == null) initFieldCodes();
            for (int i = 0; i < fieldCodes.length; i++) {
                if (fieldCodes[i].equalsIgnoreCase(fieldCode)) {
                    fieldSpinner.setSelection(i);
                    return;
                }
            }
            fieldSpinner.setSelection(0); // default fallback
        }
    }

    private void initFieldCodes() {
        fieldCodes = requireContext().getResources().getStringArray(R.array.search_field_values);
    }

    private String getFieldCode(int position) {
        if (fieldCodes == null) initFieldCodes();
        return (position >= 0 && position < fieldCodes.length) ? fieldCodes[position] : fieldCodes[0];
    }
}