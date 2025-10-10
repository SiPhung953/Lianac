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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import vn.edu.lianac.R;
import vn.edu.lianac.models.SearchRow;
import vn.edu.lianac.repository.ArxivRepository;
import vn.edu.lianac.utils.QueryOptions;
import vn.edu.lianac.viewmodel.SearchViewModel;
import vn.edu.lianac.viewmodel.SearchViewModelFactory;

public class SearchFragment extends Fragment {

    private EditText searchBox;
    private ImageButton filterButton;
    private Spinner fieldSpinner;
    private FrameLayout advancedSearchDrawer;
    private View scrimOverlay;
    private boolean isDrawerOpen = false;

    private SearchViewModel searchViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SearchViewModelFactory factory = new SearchViewModelFactory(ArxivRepository.getInstance());
        searchViewModel = new ViewModelProvider(requireActivity(), factory).get(SearchViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchBox = view.findViewById(R.id.searchBox);
        filterButton = view.findViewById(R.id.filterButton);
        fieldSpinner = view.findViewById(R.id.fieldSpinner);
        advancedSearchDrawer = view.findViewById(R.id.advancedSearchDrawer);
        scrimOverlay = view.findViewById(R.id.scrimOverlay);

        // Add ManageSearchFragment to the drawer
        getChildFragmentManager().beginTransaction()
                .replace(R.id.advancedSearchDrawer, new ManageSearchFragment())
                .commit();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupFieldSpinner();
        setupEventListeners();
    }

    private void setupFieldSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.search_fields,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fieldSpinner.setAdapter(adapter);
    }

    private void setupEventListeners() {
        // Toggle the advanced search drawer
        filterButton.setOnClickListener(v -> toggleDrawer());

        // Close drawer when scrim is clicked
        scrimOverlay.setOnClickListener(v -> closeDrawer());

        // Trigger basic search
        searchBox.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performBasicSearch();
                return true;
            }
            return false;
        });
    }

    private void toggleDrawer() {
        if (isDrawerOpen) {
            closeDrawer();
        } else {
            openDrawer();
        }
    }

    private void openDrawer() {
        advancedSearchDrawer.setVisibility(View.VISIBLE);
        scrimOverlay.setVisibility(View.VISIBLE);

        // Animate both drawer and scrim
        advancedSearchDrawer.animate()
                .translationX(0)
                .setDuration(300)
                .start();

        scrimOverlay.animate()
                .alpha(1f)
                .setDuration(300)
                .start();

        isDrawerOpen = true;
    }

    // Public method to be called from ManageSearchFragment
    public void closeDrawer() {
        if (isDrawerOpen) {
            closeDrawerInternal();
        }
    }

    // Private method that performs the actual closing animation
    private void closeDrawerInternal() {
        advancedSearchDrawer.animate()
                .translationX(advancedSearchDrawer.getWidth())
                .setDuration(300)
                .start();

        scrimOverlay.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    scrimOverlay.setVisibility(View.GONE);
                    advancedSearchDrawer.setVisibility(View.GONE);
                })
                .start();

        isDrawerOpen = false;
    }

    private void performBasicSearch() {
        String searchTerm = searchBox.getText().toString().trim();
        if (searchTerm.isEmpty()) {
            return;
        }

        String selectedField = getFieldValue(fieldSpinner.getSelectedItemPosition());

        QueryOptions currentQuery = searchViewModel.getCurrentQuery();
        QueryOptions.Builder builder;

        if (currentQuery != null) {
            // Start with existing query to preserve filters
            builder = currentQuery.toBuilder();

            // Get existing rows (from advanced search)
            List<SearchRow> existingRows = new ArrayList<>();
            if (currentQuery.getRows() != null && !currentQuery.getRows().isEmpty()) {
                existingRows.addAll(currentQuery.getRows());

                // Remove the first row if it was from a previous basic search
                // (We identify it by checking if it has the old searchTerm)
                if (!existingRows.isEmpty()) {
                    SearchRow firstRow = existingRows.get(0);
                    // If the first row matches the old basic search, remove it
                    if (currentQuery.hasSearchTerm() &&
                            firstRow.getValue().equals(currentQuery.getSearchTerm()) &&
                            firstRow.getField().equals(currentQuery.getSearchField())) {
                        existingRows.remove(0);
                    }
                }
            }

            // Add new basic search as first row
            SearchRow newSearchRow = new SearchRow(selectedField, searchTerm, "AND");
            existingRows.add(0, newSearchRow);

            builder.rows(existingRows);
            builder.searchTerm(searchTerm); // Store for reference
            builder.searchField(selectedField);
            builder.start(0);
        } else {
            // No existing query, create fresh
            builder = new QueryOptions.Builder();
            builder.searchTerm(searchTerm)
                    .searchField(selectedField)
                    .start(0);
        }

        searchViewModel.search(builder.build());

        if (isDrawerOpen) {
            closeDrawerInternal();
        }
    }

    private String getFieldValue(int position) {
        String[] fieldValues = {"all", "ti", "au", "abs", "co", "jr", "cat", "rn", "id"};
        if (position >= 0 && position < fieldValues.length) {
            return fieldValues[position];
        }
        return "all";
    }
}