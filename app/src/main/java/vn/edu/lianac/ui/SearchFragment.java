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

import java.util.ArrayList;

import vn.edu.lianac.R;
import vn.edu.lianac.models.QueryOptions;
import vn.edu.lianac.viewmodel.SearchViewModel;

/**
 * Basic search fragment with drawer for advanced filters.
 *
 * Responsibilities:
 * - Basic search bar (search term + field selector)
 * - Drawer management (open/close animations)
 * - Load ManageSearchFragment into drawer
 * - Perform basic search when user presses Enter
 */
public class SearchFragment extends Fragment {

    // Basic search views
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
     * Perform basic search (only search term + field, no advanced filters)
     */
    /**
     * Perform basic search while preserving any existing advanced filters
     */
    /**
     * Perform basic search while preserving any existing advanced filters
     */
    private void performBasicSearch() {
        String searchTerm = searchBox.getText().toString().trim();
        if (searchTerm.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a search term", Toast.LENGTH_SHORT).show();
            return;
        }

        String field = getFieldCode(fieldSpinner.getSelectedItemPosition());

        // Get existing query to preserve advanced filters
        QueryOptions currentQuery = viewModel.getCurrentQuery().getValue();

        QueryOptions.Builder builder;
        if (currentQuery != null) {
            // Start with existing query to preserve all filters
            builder = currentQuery.toBuilder();
        } else {
            // No existing query, create fresh builder
            builder = new QueryOptions.Builder();
        }

        // Update basic search term and field
        builder.searchTerm(searchTerm)
                .searchField(field)
                .start(0);  // Reset to first page for new search

        QueryOptions query = builder.build();
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

    private String getFieldCode(int position) {
        String[] fields = {"all", "ti", "au", "abs", "co", "jr", "cat", "rn", "id"};
        return (position >= 0 && position < fields.length) ? fields[position] : "all";
    }

    public boolean isDrawerOpen() {
        return isDrawerOpen;
    }

    public void updateFilterIndicator(boolean hasFilters) {
        if (hasFilters) {
            filterButton.setBackgroundResource(R.drawable.filter_button_with_indicator);
        } else {
            filterButton.setBackgroundResource(R.drawable.filter_button_normal);
        }
    }
}