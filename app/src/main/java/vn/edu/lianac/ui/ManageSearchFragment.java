package vn.edu.lianac.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
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
import java.util.List;

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
    private Spinner mainCategorySpinner;
    private Button addCategoryButton;
    private TextView selectedCategoriesLabel;
    private ChipGroup selectedCategoriesChipGroup;

    // State
    private final List<String> selectedCategories = new ArrayList<>();
    private SearchViewModel searchViewModel;

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
        setupCategorySpinner();
        setupDatePicker();

        // Always start with at least one search field row
        if (searchFieldsContainer.getChildCount() == 0) {
            addSearchFieldRow(null);
        }

        // Restore previous filters if they exist
        restoreFiltersFromViewModel();
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
        mainCategorySpinner = view.findViewById(R.id.mainCategorySpinner);
        addCategoryButton = view.findViewById(R.id.addCategoryButton);
        selectedCategoriesLabel = view.findViewById(R.id.selectedCategoriesLabel);
        selectedCategoriesChipGroup = view.findViewById(R.id.selectedCategoriesChipGroup);

        // Header click listeners for collapsible sections
        view.findViewById(R.id.searchFieldsHeader).setOnClickListener(this::onToggleClicked);
        view.findViewById(R.id.categoriesHeader).setOnClickListener(this::onToggleClicked);
        view.findViewById(R.id.dateRangeHeader).setOnClickListener(this::onToggleClicked);
    }

    private void setupEventListeners() {
        closeDrawerButton.setOnClickListener(v -> {
            removeEmptyRows();
            closeDrawer();
        });

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
            searchViewModel.refresh();
            closeDrawer();
        });

        resetButton.setOnClickListener(v -> resetAllFilters());
        addFieldButton.setOnClickListener(v -> addSearchFieldRow(null));
        addCategoryButton.setOnClickListener(v -> addSelectedCategory());
    }

    // ==================== SEARCH FIELD ROWS ====================

    /**
     * Add a new search field row with field selector, value input, and boolean operator
     */
    private void addSearchFieldRow(@Nullable SearchRow row) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View rowView = inflater.inflate(R.layout.item_search_field_row, searchFieldsContainer, false);

        // Setup remove button
        ImageButton removeButton = rowView.findViewById(R.id.removeButton);
        removeButton.setOnClickListener(v -> {
            if (searchFieldsContainer.getChildCount() > 1) {
                searchFieldsContainer.removeView(rowView);
                updateRemoveButtonsVisibility();
            }
        });

        // Setup field spinner (All, Title, Author, etc.)
        Spinner fieldSpinner = rowView.findViewById(R.id.fieldSpinner);
        ArrayAdapter<CharSequence> fieldAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.search_fields, android.R.layout.simple_spinner_item);
        fieldAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fieldSpinner.setAdapter(fieldAdapter);

        // Setup boolean operator spinner (AND, OR, ANDNOT)
        Spinner booleanOperatorSpinner = rowView.findViewById(R.id.booleanOperatorSpinner);
        ArrayAdapter<CharSequence> booleanAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.boolean_operators, android.R.layout.simple_spinner_item);
        booleanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        booleanOperatorSpinner.setAdapter(booleanAdapter);

        // Populate from existing SearchRow if provided
        if (row != null) {
            EditText valueField = rowView.findViewById(R.id.valueField);
            valueField.setText(row.getValue());
            setSpinnerSelection(fieldSpinner, fieldAdapter, row.getField());
            setSpinnerSelection(booleanOperatorSpinner, booleanAdapter, row.getOperator());
        }

        searchFieldsContainer.addView(rowView);
        updateRemoveButtonsVisibility();
    }

    private void setSpinnerSelection(Spinner spinner, ArrayAdapter<?> adapter, String value) {
        if (value == null) return;
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    /**
     * Remove button should be disabled if there's only 1 row
     */
    private void updateRemoveButtonsVisibility() {
        int childCount = searchFieldsContainer.getChildCount();
        boolean canRemove = childCount > 1;

        for (int i = 0; i < childCount; i++) {
            View rowView = searchFieldsContainer.getChildAt(i);
            ImageButton removeButton = rowView.findViewById(R.id.removeButton);
            removeButton.setEnabled(canRemove);
            removeButton.setAlpha(canRemove ? 1.0f : 0.3f);
        }
    }

    /**
     * Remove empty rows but always keep at least one
     */
    private void removeEmptyRows() {
        List<View> rowsToRemove = new ArrayList<>();

        for (int i = 0; i < searchFieldsContainer.getChildCount(); i++) {
            View rowView = searchFieldsContainer.getChildAt(i);
            EditText valueField = rowView.findViewById(R.id.valueField);
            String value = valueField.getText().toString().trim();

            if (value.isEmpty() && searchFieldsContainer.getChildCount() - rowsToRemove.size() > 1) {
                rowsToRemove.add(rowView);
            }
        }

        for (View rowView : rowsToRemove) {
            searchFieldsContainer.removeView(rowView);
        }

        updateRemoveButtonsVisibility();
    }

    // ==================== CATEGORY LOGIC ====================

    private void setupCategorySpinner() {
        List<String> allCategoryNames = buildCategoryListForSpinner();

        ArrayAdapter<String> mainAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                allCategoryNames
        );
        mainAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mainCategorySpinner.setAdapter(mainAdapter);
    }

    /**
     * Build flat list of all categories for the spinner using CategoryProvider
     */
    private List<String> buildCategoryListForSpinner() {
        List<String> categoryList = new ArrayList<>();
        categoryList.add("Select a category...");

        // Get all main categories
        for (String mainCat : CategoryProvider.getMainCategories()) {
            String mainName = CategoryProvider.getCategoryName(mainCat);
            categoryList.add(mainName + " (" + mainCat + ")");

            // Add subcategories
            List<String> subcats = CategoryProvider.getSubcategories(mainCat);
            for (String subCat : subcats) {
                String subName = CategoryProvider.getSubcategoryName(subCat);
                // Format: "  code - Name" (consistent for all subcategories)
                categoryList.add("  " + subCat + " - " + subName);
            }
        }

        return categoryList;
    }

    private void addSelectedCategory() {
        Object selectedItem = mainCategorySpinner.getSelectedItem();
        if (selectedItem == null || mainCategorySpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(getContext(), "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedCategoryName = selectedItem.toString();
        String categoryCode = extractCategoryCode(selectedCategoryName);

        if (categoryCode == null) {
            Toast.makeText(getContext(), "Invalid category", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCategories.contains(categoryCode)) {
            Toast.makeText(getContext(), "Category already added", Toast.LENGTH_SHORT).show();
            return;
        }

        addCategoryChip(selectedCategoryName, categoryCode);
    }

    /**
     * Extract category code from spinner text
     * Formats: "Computer Science (cs)" -> "cs"
     *          "  cs.AI - Artificial Intelligence" -> "cs.AI"
     */
    private String extractCategoryCode(String spinnerText) {
        spinnerText = spinnerText.trim();

        // Main category format: "Name (code)"
        if (spinnerText.contains("(") && spinnerText.contains(")")) {
            int start = spinnerText.indexOf("(") + 1;
            int end = spinnerText.indexOf(")");
            return spinnerText.substring(start, end);
        }

        // Subcategory format: "code - Name"
        if (spinnerText.contains(" - ")) {
            return spinnerText.substring(0, spinnerText.indexOf(" - ")).trim();
        }

        return null;
    }

    private void addCategoryChip(String chipText, String categoryCode) {
        if (selectedCategories.contains(categoryCode)) return;

        selectedCategories.add(categoryCode);

        Chip chip = new Chip(requireContext());
        chip.setText(chipText);
        chip.setCloseIconVisible(true);
        chip.setClickable(false);
        chip.setCheckable(false);
        chip.setTag(categoryCode);
        chip.setOnCloseIconClickListener(v -> {
            selectedCategories.remove(categoryCode);
            selectedCategoriesChipGroup.removeView(chip);
            updateChipGroupVisibility();
        });

        selectedCategoriesChipGroup.addView(chip);
        updateChipGroupVisibility();
        mainCategorySpinner.setSelection(0);
    }

    private void updateChipGroupVisibility() {
        boolean hasChips = selectedCategoriesChipGroup.getChildCount() > 0;
        selectedCategoriesLabel.setVisibility(hasChips ? View.VISIBLE : View.GONE);
        selectedCategoriesChipGroup.setVisibility(hasChips ? View.VISIBLE : View.GONE);
    }

    // ==================== DATE PICKER ====================

    private void setupDatePicker() {
        dateFromField.setOnClickListener(v -> showDatePicker(dateFromField));
        dateToField.setOnClickListener(v -> showDatePicker(dateToField));
    }

    private void showDatePicker(final EditText dateField) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
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

    /**
     * Collect all filters and build QueryOptions
     */
    /**
     * Collect all filters and build QueryOptions, preserving basic search if it exists
     */
    /**
     * Collect all filters and build QueryOptions, preserving basic search if it exists
     */
    private void applyFilters() {
        removeEmptyRows();

        QueryOptions.Builder builder = new QueryOptions.Builder();

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
            // Fallback: try to get from existing query
            QueryOptions existingQuery = searchViewModel.getCurrentQuery().getValue();
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

            String value = valueField.getText().toString().trim();
            if (!value.isEmpty()) {
                String field = getFieldValue(fieldSpinner.getSelectedItemPosition());
                String operator = booleanSpinner.getSelectedItem().toString();
                rows.add(new SearchRow(field, value, operator));
            }
        }

        builder.rows(rows);
        builder.categories(new ArrayList<>(selectedCategories));

        String fromDate = dateFromField.getText().toString().trim();
        String toDate = dateToField.getText().toString().trim();
        if (!fromDate.isEmpty()) builder.dateFrom(fromDate);
        if (!toDate.isEmpty()) builder.dateTo(toDate);

        builder.start(0);

        QueryOptions newQuery = builder.build();
        searchViewModel.search(newQuery);
    }

    private String getFieldValue(int position) {
        String[] fieldValues = {"all", "ti", "au", "abs", "co", "jr", "cat", "rn", "id"};
        return (position >= 0 && position < fieldValues.length) ? fieldValues[position] : "all";
    }

    /**
     * Reset all filters to default state
     */
    private void resetAllFilters() {
        // Clear search rows
        searchFieldsContainer.removeAllViews();
        addSearchFieldRow(null);

        // Clear categories
        selectedCategories.clear();
        selectedCategoriesChipGroup.removeAllViews();
        updateChipGroupVisibility();
        mainCategorySpinner.setSelection(0);

        // Clear dates
        dateFromField.setText("");
        dateToField.setText("");

        // Reset query in ViewModel
        QueryOptions basicQuery = new QueryOptions.Builder().start(0).build();
        searchViewModel.search(basicQuery);

        Toast.makeText(getContext(), "Advanced filters cleared", Toast.LENGTH_SHORT).show();
    }

    /**
     * Restore filters from ViewModel when drawer is reopened
     */
    private void restoreFiltersFromViewModel() {
        QueryOptions currentQuery = searchViewModel.getCurrentQuery().getValue();

        // Clear UI
        searchFieldsContainer.removeAllViews();
        selectedCategories.clear();
        selectedCategoriesChipGroup.removeAllViews();

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
        if (currentQuery.getCategories() != null) {
            for (String categoryCode : currentQuery.getCategories()) {
                String displayName = getCategoryDisplayName(categoryCode);
                if (displayName != null) {
                    addCategoryChip(displayName, categoryCode);
                }
            }
        }

        // Restore dates
        if (currentQuery.getDateFrom() != null && !currentQuery.getDateFrom().isEmpty()) {
            dateFromField.setText(currentQuery.getDateFrom());
        }
        if (currentQuery.getDateTo() != null && !currentQuery.getDateTo().isEmpty()) {
            dateToField.setText(currentQuery.getDateTo());
        }

        updateChipGroupVisibility();
    }

    /**
     * Get display name for a category code
     */
    private String getCategoryDisplayName(String categoryCode) {
        if (categoryCode == null || categoryCode.isEmpty()) {
            return categoryCode;
        }

        // Get the actual name from CategoryProvider
        String name = CategoryProvider.getCategoryName(categoryCode);

        // Check if we got a real name (not just the code echoed back)
        if (name != null && !name.equals(categoryCode)) {
            // Check if it's a main category (no dot or dash in middle of string after first char)
            boolean isMainCategory = CategoryProvider.getInstance(requireContext()).isMainCategory(categoryCode);

            if (isMainCategory) {
                return name + " (" + categoryCode + ")";
            } else {
                // It's a subcategory
                return categoryCode + " - " + name;
            }
        }

        // Fallback to just the code if lookup failed
        return categoryCode;
    }

    // ==================== DRAWER CONTROL ====================

    /**
     * Close the drawer (call parent SearchFragment's method)
     */
    private void closeDrawer() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof SearchFragment) {
            ((SearchFragment) parentFragment).closeDrawer();
        }
    }
}