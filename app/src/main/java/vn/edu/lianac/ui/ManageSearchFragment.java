package vn.edu.lianac.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import vn.edu.lianac.R;
import vn.edu.lianac.models.SearchRow;
import vn.edu.lianac.models.QueryOptions;
import vn.edu.lianac.utils.CategoryProvider;
import vn.edu.lianac.viewmodel.SearchViewModel;

/**
 * Advanced search filters fragment.
 *
 * Features:
 * - Additional search fields (dynamic rows with boolean operators)
 * - Category selection with chips
 * - Date range picker
 * - Apply/Reset/Search buttons
 *
 * This fragment is loaded into SearchFragment's drawer.
 */
public class ManageSearchFragment extends Fragment {

    // UI Components
    private ImageButton closeDrawerButton;
    private Button applyFiltersButton, resetButton, searchButton, addFieldButton;
    private LinearLayout searchFieldsContent, categoriesContent, dateRangeContent;
    private TextView searchFieldsToggle, categoriesToggle, dateRangeToggle;
    private EditText dateFromField, dateToField;
    private LinearLayout searchFieldsContainer;

    // State
    private final List<String> selectedCategories = new ArrayList<>();
    private SearchViewModel searchViewModel;

    // Category chip system
    private ChipGroup mainCategoryChipGroup;
    private ChipGroup physicsFilterChipGroup;
    private ChipGroup leafCategoryChipGroup;
    private TextView physicsFilterLabel;
    private TextView leafCategoryLabel;
    private TextView selectedCategoriesSummary;

    private final Set<String> selectedMainCategories = new HashSet<>();
    private final Set<String> selectedPhysicsSubjects = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchViewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);

        // Initialize CategoryProvider
        CategoryProvider.getInstance(requireContext());

        bindViews(view);
        setupEventListeners();
        setupToggleSections();
        setupCategoryChipSystem();
        setupDatePicker();

        // Always start with at least one search field row
        if (searchFieldsContainer.getChildCount() == 0) {
            addSearchFieldRow(null);
        }

        // Restore previous filters if they exist
        restoreFiltersFromViewModel();
    }

    public void onDrawerClosed() {
        removeEmptyRows();
    }

    private void bindViews(View view) {
        closeDrawerButton = view.findViewById(R.id.closeDrawerButton);
        applyFiltersButton = view.findViewById(R.id.applyFiltersButton);
        resetButton = view.findViewById(R.id.resetButton);
        searchButton = view.findViewById(R.id.searchButton);
        addFieldButton = view.findViewById(R.id.addFieldButton);
        searchFieldsContent = view.findViewById(R.id.searchFieldsContent);
        categoriesContent = view.findViewById(R.id.categoriesContent);
        dateRangeContent = view.findViewById(R.id.dateRangeContent);
        searchFieldsToggle = view.findViewById(R.id.searchFieldsToggle);
        categoriesToggle = view.findViewById(R.id.categoriesToggle);
        dateRangeToggle = view.findViewById(R.id.dateRangeToggle);
        dateFromField = view.findViewById(R.id.dateFromField);
        dateToField = view.findViewById(R.id.dateToField);
        searchFieldsContainer = view.findViewById(R.id.searchFieldsContainer);

        // Category chip system views
        mainCategoryChipGroup = view.findViewById(R.id.mainCategoryChipGroup);
        physicsFilterChipGroup = view.findViewById(R.id.physicsFilterChipGroup);
        leafCategoryChipGroup = view.findViewById(R.id.leafCategoryChipGroup);
        physicsFilterLabel = view.findViewById(R.id.physicsFilterLabel);
        leafCategoryLabel = view.findViewById(R.id.leafCategoryLabel);
        selectedCategoriesSummary = view.findViewById(R.id.selectedCategoriesSummary);

        // Header click listeners for collapsible sections
        view.findViewById(R.id.searchFieldsHeader).setOnClickListener(this::onToggleClicked);
        view.findViewById(R.id.categoriesHeader).setOnClickListener(this::onToggleClicked);
        view.findViewById(R.id.dateRangeHeader).setOnClickListener(this::onToggleClicked);
    }

    private void setupEventListeners() {
        closeDrawerButton.setOnClickListener(v -> closeDrawer());

        applyFiltersButton.setOnClickListener(v -> {
            applyFilters();
            closeDrawer();
        });

        searchButton.setOnClickListener(v -> {
            applyFilters();
            QueryOptions currentQuery = searchViewModel.getCurrentQuery().getValue();
            if (currentQuery != null) {
                searchViewModel.search(currentQuery);
            }
            closeDrawer();
        });

        resetButton.setOnClickListener(v -> resetAllFilters());
        addFieldButton.setOnClickListener(v -> addSearchFieldRow(null));
    }

    private void setupCategoryChipSystem() {
        populateMainCategoryChips();
        updateSelectedCategoriesSummary();
    }

    private void populateMainCategoryChips() {
        mainCategoryChipGroup.removeAllViews();

        List<String> mainCategories = CategoryProvider.getMainCategories();

        for (String categoryId : mainCategories) {
            String displayName = CategoryProvider.getCategoryName(categoryId);
            Chip chip = createMainCategoryChip(displayName, categoryId);
            mainCategoryChipGroup.addView(chip);
        }
    }

    private Chip createMainCategoryChip(String displayName, String categoryId) {
        Chip chip = new Chip(requireContext());
        chip.setText(displayName);
        chip.setCheckable(true);
        chip.setCheckedIconVisible(true);
        chip.setTag(categoryId);

        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedMainCategories.add(categoryId);
            } else {
                selectedMainCategories.remove(categoryId);
            }
            updateLeafCategoriesForAllSelected();
            updateSelectedCategoriesFromUI();
            updateSelectedCategoriesSummary();
        });

        return chip;
    }

    private void updateLeafCategoriesForAllSelected() {
        if (selectedMainCategories.isEmpty()) {
            hideLeafCategoryRow();
            return;
        }

        showLeafCategoryRow();

        // Collect ALL leaf categories from ALL selected main categories
        Set<String> allLeafCategories = new HashSet<>();

        for (String mainCategoryId : selectedMainCategories) {
            if ("physics".equals(mainCategoryId)) {
                // For physics, use the selected physics subjects to get leaf categories
                if (selectedPhysicsSubjects.isEmpty()) {
                    // If no specific physics subjects selected, show all physics leaf categories
                    List<String> physicsLeafCats = CategoryProvider.getInstance(requireContext())
                            .getAllLeafCategories("physics");
                    allLeafCategories.addAll(physicsLeafCats);
                } else {
                    // Only show leaf categories from selected physics subjects
                    for (String physicsSubject : selectedPhysicsSubjects) {
                        List<String> leafCats = CategoryProvider.getInstance(requireContext())
                                .getAllLeafCategories(physicsSubject);
                        allLeafCategories.addAll(leafCats);
                    }
                }
            } else {
                // For non-physics categories, get all leaf categories
                List<String> leafCats = CategoryProvider.getInstance(requireContext())
                        .getAllLeafCategories(mainCategoryId);
                allLeafCategories.addAll(leafCats);
            }
        }

        populateLeafCategoryChips(new ArrayList<>(allLeafCategories));
    }

    private void showPhysicsFilterRow() {
        physicsFilterChipGroup.setVisibility(View.VISIBLE);
        physicsFilterLabel.setVisibility(View.VISIBLE);

        physicsFilterChipGroup.removeAllViews();
        selectedPhysicsSubjects.clear();

        List<String> expandablePhysics = getExpandablePhysicsCategories();

        for (String categoryId : expandablePhysics) {
            String displayName = CategoryProvider.getCategoryName(categoryId);
            Chip chip = createPhysicsFilterChip(displayName, categoryId);
            physicsFilterChipGroup.addView(chip);
        }

        // Also add standalone physics categories as options
        List<String> standalonePhysics = getStandalonePhysicsCategories();
        for (String categoryId : standalonePhysics) {
            String displayName = CategoryProvider.getCategoryName(categoryId);
            Chip chip = createPhysicsFilterChip(displayName, categoryId);
            physicsFilterChipGroup.addView(chip);
        }
    }

    private Chip createPhysicsFilterChip(String displayName, String categoryId) {
        Chip chip = new Chip(requireContext());
        chip.setText(displayName);
        chip.setCheckable(true);
        chip.setCheckedIconVisible(true);
        chip.setTag(categoryId);

        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedPhysicsSubjects.add(categoryId);
            } else {
                selectedPhysicsSubjects.remove(categoryId);
            }
            // Update leaf categories when physics subjects change
            if (selectedMainCategories.contains("physics")) {
                updateLeafCategoriesForAllSelected();
            }
            updateSelectedCategoriesFromUI();
            updateSelectedCategoriesSummary();
        });

        return chip;
    }

    private List<String> getExpandablePhysicsCategories() {
        return CategoryProvider.getInstance(requireContext()).getExpandableCategories("physics");
    }

    private List<String> getStandalonePhysicsCategories() {
        List<String> standalone = new ArrayList<>();
        List<String> physicsSubcats = CategoryProvider.getSubcategories("physics");

        for (String subcat : physicsSubcats) {
            List<String> subSubcats = CategoryProvider.getSubcategories(subcat);
            if (subSubcats == null || subSubcats.isEmpty()) {
                standalone.add(subcat);
            }
        }

        return standalone;
    }

    private void showLeafCategoryRow() {
        leafCategoryChipGroup.setVisibility(View.VISIBLE);
        leafCategoryLabel.setVisibility(View.VISIBLE);
    }

    private void hideLeafCategoryRow() {
        leafCategoryChipGroup.setVisibility(View.GONE);
        leafCategoryLabel.setVisibility(View.GONE);
        leafCategoryChipGroup.removeAllViews();
    }

    private void hidePhysicsFilterRow() {
        physicsFilterChipGroup.setVisibility(View.GONE);
        physicsFilterLabel.setVisibility(View.GONE);
        physicsFilterChipGroup.removeAllViews();
        selectedPhysicsSubjects.clear();
    }

    private void populateLeafCategoryChips(List<String> leafCategories) {
        leafCategoryChipGroup.removeAllViews();

        // Sort categories for better UX
        List<String> sortedCategories = new ArrayList<>(leafCategories);
        sortedCategories.sort(String::compareTo);

        for (String categoryId : sortedCategories) {
            String displayName = getCategoryDisplayName(categoryId);
            Chip chip = createLeafCategoryChip(displayName, categoryId);
            leafCategoryChipGroup.addView(chip);
        }
    }

    private Chip createLeafCategoryChip(String displayName, String categoryId) {
        Chip chip = new Chip(requireContext());
        chip.setText(displayName);
        chip.setCheckable(true);
        chip.setCheckedIconVisible(true);
        chip.setTag(categoryId);

        // Check if already in selected categories
        chip.setChecked(selectedCategories.contains(categoryId));

        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedCategories.contains(categoryId)) {
                    selectedCategories.add(categoryId);
                }
            } else {
                selectedCategories.remove(categoryId);
            }
            updateSelectedCategoriesSummary();
        });

        return chip;
    }

    private void updateLeafCategoryChips() {
        for (int i = 0; i < leafCategoryChipGroup.getChildCount(); i++) {
            View view = leafCategoryChipGroup.getChildAt(i);
            if (view instanceof Chip) {
                Chip chip = (Chip) view;
                String categoryId = (String) chip.getTag();
                chip.setChecked(selectedCategories.contains(categoryId));
            }
        }
    }

    private void updateSelectedCategoriesFromUI() {
        // Clear current selections and rebuild from UI state
        selectedCategories.clear();

        // Add selected main categories
        selectedCategories.addAll(selectedMainCategories);

        // Add selected leaf categories from leafCategoryChipGroup
        for (int i = 0; i < leafCategoryChipGroup.getChildCount(); i++) {
            View view = leafCategoryChipGroup.getChildAt(i);
            if (view instanceof Chip && ((Chip) view).isChecked()) {
                String categoryId = (String) view.getTag();
                if (!selectedCategories.contains(categoryId)) {
                    selectedCategories.add(categoryId);
                }
            }
        }
    }

    private void updateSelectedCategoriesSummary() {
        updateSelectedCategoriesFromUI();

        int totalSelected = selectedCategories.size();
        if (totalSelected == 0) {
            selectedCategoriesSummary.setText("No categories selected");
            selectedCategoriesSummary.setVisibility(View.VISIBLE);
        } else if (totalSelected == 1) {
            selectedCategoriesSummary.setText("1 category selected");
            selectedCategoriesSummary.setVisibility(View.VISIBLE);
        } else {
            selectedCategoriesSummary.setText(totalSelected + " categories selected");
            selectedCategoriesSummary.setVisibility(View.VISIBLE);
        }
    }

    // ==================== SEARCH FIELD ROWS ====================

    private void addSearchFieldRow(@Nullable SearchRow row) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View rowView = inflater.inflate(R.layout.item_search_field_row, searchFieldsContainer, false);

        if (rowView == null) {
            Log.e("ManageSearchFragment", "Failed to inflate search field row");
            return;
        }

        // Setup remove button
        ImageButton removeButton = rowView.findViewById(R.id.removeButton);
        if (removeButton != null) {
            removeButton.setOnClickListener(v -> {
                if (searchFieldsContainer.getChildCount() > 1) {
                    searchFieldsContainer.removeView(rowView);
                    updateRemoveButtonsVisibility();
                }
            });
        }

        // Setup field spinner (All, Title, Author, etc.)
        Spinner fieldSpinner = rowView.findViewById(R.id.fieldSpinner);
        if (fieldSpinner != null) {
            ArrayAdapter<CharSequence> fieldAdapter = ArrayAdapter.createFromResource(
                    requireContext(), R.array.search_fields, android.R.layout.simple_spinner_item);
            fieldAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            fieldSpinner.setAdapter(fieldAdapter);
        }

        // Setup boolean operator spinner (AND, OR, ANDNOT)
        Spinner booleanOperatorSpinner = rowView.findViewById(R.id.booleanOperatorSpinner);
        if (booleanOperatorSpinner != null) {
            ArrayAdapter<CharSequence> booleanAdapter = ArrayAdapter.createFromResource(
                    requireContext(), R.array.boolean_operators, android.R.layout.simple_spinner_item);
            booleanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            booleanOperatorSpinner.setAdapter(booleanAdapter);
        }

        // Populate from existing SearchRow if provided
        if (row != null) {
            EditText valueField = rowView.findViewById(R.id.valueField);
            if (valueField != null) {
                valueField.setText(row.getValue());
            }
            if (fieldSpinner != null) {
                setSpinnerSelection(fieldSpinner, row.getField());
            }
            if (booleanOperatorSpinner != null) {
                setSpinnerSelection(booleanOperatorSpinner, row.getOperator());
            }
        }

        searchFieldsContainer.addView(rowView);
        updateRemoveButtonsVisibility();
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null || spinner == null) return;

        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void updateRemoveButtonsVisibility() {
        int childCount = searchFieldsContainer.getChildCount();
        boolean canRemove = childCount > 1;

        for (int i = 0; i < childCount; i++) {
            View rowView = searchFieldsContainer.getChildAt(i);
            ImageButton removeButton = rowView.findViewById(R.id.removeButton);
            if (removeButton != null) {
                removeButton.setEnabled(canRemove);
                removeButton.setAlpha(canRemove ? 1.0f : 0.3f);
            }
        }
    }

    private void removeEmptyRows() {
        List<View> rowsToRemove = new ArrayList<>();
        int filledRowCount = 0;

        for (int i = 0; i < searchFieldsContainer.getChildCount(); i++) {
            View rowView = searchFieldsContainer.getChildAt(i);
            EditText valueField = rowView.findViewById(R.id.valueField);
            if (valueField != null) {
                String value = valueField.getText().toString().trim();
                if (value.isEmpty()) {
                    rowsToRemove.add(rowView);
                } else {
                    filledRowCount++;
                }
            }
        }

        if (filledRowCount > 0) {
            for (View rowView : rowsToRemove) {
                searchFieldsContainer.removeView(rowView);
            }
        } else {
            // Keep at least one row
            for (int i = 1; i < rowsToRemove.size(); i++) {
                searchFieldsContainer.removeView(rowsToRemove.get(i));
            }
        }

        updateRemoveButtonsVisibility();
    }

    // ==================== DATE PICKER ====================

    private void setupDatePicker() {
        dateFromField.setOnClickListener(v -> showDatePicker(dateFromField));
        dateToField.setOnClickListener(v -> showDatePicker(dateToField));
    }

    private void showDatePicker(final EditText dateField) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, day) -> {
                    String date = String.format("%d-%02d-%02d", year, month + 1, day);
                    dateField.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    // ==================== COLLAPSIBLE SECTIONS ====================

    private void setupToggleSections() {
        // Search fields visible by default
        searchFieldsContent.setVisibility(View.VISIBLE);
        searchFieldsToggle.setText("▼");

        // Categories and date collapsed by default
        categoriesContent.setVisibility(View.GONE);
        categoriesToggle.setText("▶");
        dateRangeContent.setVisibility(View.GONE);
        dateRangeToggle.setText("▶");
    }

    private void onToggleClicked(View v) {
        View content = null;
        TextView toggle = null;

        int id = v.getId();
        if (id == R.id.searchFieldsHeader) {
            content = searchFieldsContent;
            toggle = searchFieldsToggle;
        } else if (id == R.id.categoriesHeader) {
            content = categoriesContent;
            toggle = categoriesToggle;

            // Show/hide physics filter based on physics selection
            if (content.getVisibility() == View.VISIBLE && selectedMainCategories.contains("physics")) {
                showPhysicsFilterRow();
            } else {
                hidePhysicsFilterRow();
            }
        } else if (id == R.id.dateRangeHeader) {
            content = dateRangeContent;
            toggle = dateRangeToggle;
        }

        if (content != null && toggle != null) {
            if (content.getVisibility() == View.VISIBLE) {
                content.setVisibility(View.GONE);
                toggle.setText("▶");
            } else {
                content.setVisibility(View.VISIBLE);
                toggle.setText("▼");
            }
        }
    }

    // ==================== APPLY/RESET FILTERS ====================

    private void applyFilters() {
        removeEmptyRows();

        QueryOptions existingQuery = searchViewModel.getCurrentQuery().getValue();
        QueryOptions.Builder builder = new QueryOptions.Builder();

        if (existingQuery != null) {
            builder.sortBy(existingQuery.getSortBy())
                    .sortOrder(existingQuery.getSortOrder())
                    .maxResults(existingQuery.getMaxResults());
        }

        // Try to get basic search term from parent SearchFragment
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof SearchFragment) {
            SearchFragment searchFragment = (SearchFragment) parentFragment;
            String basicTerm = searchFragment.getCurrentSearchTerm();
            String basicField = searchFragment.getCurrentSearchField();

            if (basicTerm != null && !basicTerm.isEmpty()) {
                builder.searchTerm(basicTerm)
                        .searchField(basicField);
            }
        } else {
            if (existingQuery != null && existingQuery.hasSearchTerm()) {
                builder.searchTerm(existingQuery.getSearchTerm())
                        .searchField(existingQuery.getSearchField());
            }
        }

        // Collect search rows from drawer
        List<SearchRow> rows = new ArrayList<>();
        for (int i = 0; i < searchFieldsContainer.getChildCount(); i++) {
            View rowView = searchFieldsContainer.getChildAt(i);
            EditText valueField = rowView.findViewById(R.id.valueField);
            Spinner fieldSpinner = rowView.findViewById(R.id.fieldSpinner);
            Spinner booleanSpinner = rowView.findViewById(R.id.booleanOperatorSpinner);

            if (valueField != null && fieldSpinner != null && booleanSpinner != null) {
                String value = valueField.getText().toString().trim();
                if (!value.isEmpty()) {
                    String field = getFieldValue(fieldSpinner.getSelectedItemPosition());
                    String operator = booleanSpinner.getSelectedItem().toString();
                    rows.add(new SearchRow(field, value, operator));
                }
            }
        }

        builder.rows(rows);

        // Include both main categories and leaf categories in the filter
        updateSelectedCategoriesFromUI();
        builder.categories(new ArrayList<>(selectedCategories));

        String fromDate = dateFromField.getText().toString().trim();
        String toDate = dateToField.getText().toString().trim();
        if (!fromDate.isEmpty()) builder.dateFrom(fromDate);
        if (!toDate.isEmpty()) builder.dateTo(toDate);

        builder.start(0);  // Reset to first page when applying filters

        QueryOptions newQuery = builder.build();
        searchViewModel.updateQueryOptions(newQuery);
        updateFilterButtonIndicator();

        Toast.makeText(requireContext(), "Filters applied", Toast.LENGTH_SHORT).show();
    }

    private String getFieldValue(int position) {
        String[] fieldValues = getResources().getStringArray(R.array.search_field_values);
        return (position >= 0 && position < fieldValues.length) ? fieldValues[position] : "all";
    }

    private void resetAllFilters() {
        // Clear search rows
        searchFieldsContainer.removeAllViews();
        addSearchFieldRow(null);

        // Clear category selections
        selectedMainCategories.clear();
        selectedPhysicsSubjects.clear();
        selectedCategories.clear();

        // Uncheck all chips
        mainCategoryChipGroup.clearCheck();
        physicsFilterChipGroup.clearCheck();
        leafCategoryChipGroup.clearCheck();

        hidePhysicsFilterRow();
        hideLeafCategoryRow();

        // Clear dates
        dateFromField.setText("");
        dateToField.setText("");

        // Update summary
        updateSelectedCategoriesSummary();

        // Reset query in ViewModel
        QueryOptions basicQuery = new QueryOptions.Builder().start(0).build();
        searchViewModel.updateQueryOptions(basicQuery);
        updateFilterButtonIndicator();

        Toast.makeText(requireContext(), "Advanced filters cleared", Toast.LENGTH_SHORT).show();
    }

    private void restoreFiltersFromViewModel() {
        QueryOptions currentQuery = searchViewModel.getCurrentQuery().getValue();

        // Clear UI
        searchFieldsContainer.removeAllViews();
        selectedCategories.clear();
        selectedMainCategories.clear();
        selectedPhysicsSubjects.clear();

        if (currentQuery == null) {
            addSearchFieldRow(null);
            return;
        }

        // Restore search rows
        if (currentQuery.getRows() != null && !currentQuery.getRows().isEmpty()) {
            for (SearchRow row : currentQuery.getRows()) {
                addSearchFieldRow(row);
            }
        } else {
            addSearchFieldRow(null);
        }

        // Restore categories
        if (currentQuery.getCategories() != null && !currentQuery.getCategories().isEmpty()) {
            selectedCategories.addAll(currentQuery.getCategories());
            restoreCategoryChipsFromList(currentQuery.getCategories());
        }

        // Restore dates
        if (currentQuery.getDateFrom() != null && !currentQuery.getDateFrom().isEmpty()) {
            dateFromField.setText(currentQuery.getDateFrom());
        }
        if (currentQuery.getDateTo() != null && !currentQuery.getDateTo().isEmpty()) {
            dateToField.setText(currentQuery.getDateTo());
        }

        updateSelectedCategoriesSummary();
    }

    private void restoreCategoryChipsFromList(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return;
        }

        // Restore main category chips
        for (String category : categories) {
            // Check if this is a main category
            if (CategoryProvider.getInstance(requireContext()).isMainCategory(category)) {
                selectedMainCategories.add(category);
                // Check the corresponding chip
                for (int i = 0; i < mainCategoryChipGroup.getChildCount(); i++) {
                    View view = mainCategoryChipGroup.getChildAt(i);
                    if (view instanceof Chip && category.equals(view.getTag())) {
                        ((Chip) view).setChecked(true);
                        break;
                    }
                }
            }
        }

        // Update leaf categories based on selected main categories
        updateLeafCategoriesForAllSelected();

        // For leaf categories, update the leaf category chips
        updateLeafCategoryChips();
    }

    private String getCategoryDisplayName(String categoryCode) {
        if (categoryCode == null || categoryCode.isEmpty()) {
            return categoryCode;
        }

        String name = CategoryProvider.getCategoryName(categoryCode);
        if (name != null && !name.equals(categoryCode)) {
            return name;
        }

        return categoryCode;
    }

    private boolean hasAdvancedFilters() {
        int nonEmptyRows = 0;
        for (int i = 0; i < searchFieldsContainer.getChildCount(); i++) {
            View rowView = searchFieldsContainer.getChildAt(i);
            EditText valueField = rowView.findViewById(R.id.valueField);
            if (valueField != null && !valueField.getText().toString().trim().isEmpty()) {
                nonEmptyRows++;
            }
        }

        return nonEmptyRows > 0 ||
                !selectedCategories.isEmpty() ||
                !dateFromField.getText().toString().trim().isEmpty() ||
                !dateToField.getText().toString().trim().isEmpty();
    }

    private void updateFilterButtonIndicator() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof SearchFragment) {
            ((SearchFragment) parentFragment).updateFilterIndicator(hasAdvancedFilters());
        }
    }

    private void closeDrawer() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof SearchFragment) {
            ((SearchFragment) parentFragment).closeDrawer();
        }
    }
}