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
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import vn.edu.lianac.R;
import vn.edu.lianac.models.Category;
import vn.edu.lianac.models.SearchRow;
import vn.edu.lianac.utils.CategoryDataProvider;
import vn.edu.lianac.utils.QueryOptions;
import vn.edu.lianac.viewmodel.SearchViewModel;

public class ManageSearchFragment extends Fragment {

    private ImageButton closeDrawerButton;
    private Button applyFiltersButton, resetButton, searchButton, addFieldButton;
    private LinearLayout searchFieldsContent, categoriesContent, dateRangeContent;
    private TextView searchFieldsToggle, categoriesToggle, dateRangeToggle;

    // Date fields
    private EditText dateFromField, dateToField;
    private LinearLayout searchFieldsContainer;

    // --- REWORKED Category UI ---
    private Spinner mainCategorySpinner;
    private Button addCategoryButton;
    private TextView selectedCategoriesLabel;
    private ChipGroup selectedCategoriesChipGroup;
    private final List<String> selectedCategories = new ArrayList<>();
    // --- End REWORKED Category UI ---

    private SearchViewModel searchViewModel;
    private CategoryDataProvider categoryDataProvider;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchViewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
        categoryDataProvider = CategoryDataProvider.getInstance(requireContext());

        bindViews(view);
        setupEventListeners();
        setupToggleSections();
        setupCategorySpinner();
        setupDatePicker();

        // Initialize with one search row if there are no existing filters
        if (searchFieldsContainer.getChildCount() == 0) {
            addSearchFieldRow(null);
        }

        reapplyExistingFilters();
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

        // Header views for toggle listeners
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
            // Trigger actual search
            searchViewModel.search(searchViewModel.getCurrentQuery());
            closeDrawer();
        });

        resetButton.setOnClickListener(v -> resetAllFilters());
        addFieldButton.setOnClickListener(v -> addSearchFieldRow(null));
        addCategoryButton.setOnClickListener(v -> addSelectedCategory());
    }


    // --- REWORKED Category Logic ---

    private void setupCategorySpinner() {
        // Build the list of categories from the hierarchical data provider
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
     * Traverses the hierarchical category data to build a flat list of names for the spinner.
     */
    private List<String> buildCategoryListForSpinner() {
        List<String> categoryList = new ArrayList<>();
        categoryList.add(getString(R.string.select_category_placeholder));

        Map<String, Category> topLevelCategories = categoryDataProvider.getAllMainCategories();
        for (Category topCat : topLevelCategories.values()) {
            // Start recursion from the top-level category itself
            addCategoriesToList(topCat, categoryList);
        }
        return categoryList;
    }

    /**
     * Helper method to recursively add category names to a list.
     * @param categories The list of categories to process.
     * @param categoryList The flat list to add names to.
     */
    /**
     * Helper method to recursively add category names to a list.
     * Now accepts a single Category instead of a list to handle top-level properly.
     * @param category The category to process.
     * @param categoryList The flat list to add names to.
     */
    private void addCategoriesToList(Category category, List<String> categoryList) {
        if (category == null) return;

        // Check if it's a leaf node (has no sub-categories)
        if (!category.hasSubCategories()) {
            categoryList.add(category.getFullName(requireContext()));
        } else {
            // This is a group, recurse into children
            List<Category> subCats = category.getSubCategories();
            if (subCats != null) {
                for (Category subCat : subCats) {
                    addCategoriesToList(subCat, categoryList);
                }
            }
        }
    }

    private void addSelectedCategory() {
        Object selectedItem = mainCategorySpinner.getSelectedItem();
        if (selectedItem == null || mainCategorySpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(getContext(), R.string.select_valid_category_toast, Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedCategoryName = selectedItem.toString();

        // Find the category code (e.g., "cs.AI") from the full name
        String categoryCode = findCategoryCodeInProvider(selectedCategoryName);

        if (categoryCode == null) {
            Toast.makeText(getContext(), R.string.invalid_category_toast, Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCategories.contains(categoryCode)) {
            Toast.makeText(getContext(), R.string.category_already_added_toast, Toast.LENGTH_SHORT).show();
            return;
        }

        // The text for the chip is the same as the one selected from the spinner
        addCategoryChip(selectedCategoryName, categoryCode);
    }

    /**
     * Finds a category code by its full display name by searching the data provider.
     * @param fullName The display name like "cs.AI - Artificial Intelligence".
     * @return The short code like "cs.AI", or null if not found.
     */
    private String findCategoryCodeInProvider(String fullName) {
        for (Category topCat : categoryDataProvider.getAllMainCategories().values()) {
            String foundCode = findCodeRecursive(topCat.getSubCategories(), fullName);
            if (foundCode != null) {
                return foundCode;
            }
        }
        return null;
    }

    private String findCodeRecursive(List<Category> categories, String fullName) {
        if (categories == null) return null;
        for (Category cat : categories) {
            // Check leaf nodes first
            if (!cat.hasSubCategories()) {
                if (cat.getFullName(requireContext()).equals(fullName)) {
                    return cat.getShortName();
                }
            } else {
                // Recurse into sub-categories
                String foundCode = findCodeRecursive(cat.getSubCategories(), fullName);
                if (foundCode != null) return foundCode;
            }
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
        chip.setTag(categoryCode); // Use tag to easily find the chip later if needed
        chip.setOnCloseIconClickListener(v -> {
            selectedCategories.remove(categoryCode);
            selectedCategoriesChipGroup.removeView(chip);
            updateChipGroupVisibility();
        });
        selectedCategoriesChipGroup.addView(chip);
        updateChipGroupVisibility();
        mainCategorySpinner.setSelection(0); // Reset spinner
    }

    private void updateChipGroupVisibility() {
        boolean hasChips = selectedCategoriesChipGroup.getChildCount() > 0;
        selectedCategoriesLabel.setVisibility(hasChips ? View.VISIBLE : View.GONE);
        selectedCategoriesChipGroup.setVisibility(hasChips ? View.VISIBLE : View.GONE);
    }

    // --- End REWORKED Category Logic ---

    private void addSearchFieldRow(@Nullable SearchRow row) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View rowView = inflater.inflate(R.layout.item_search_field_row, searchFieldsContainer, false);

        ImageButton removeButton = rowView.findViewById(R.id.removeButton);

        removeButton.setOnClickListener(v -> {
            // Only remove if there's more than 1 row
            if (searchFieldsContainer.getChildCount() > 1) {
                searchFieldsContainer.removeView(rowView);
                updateRemoveButtonsVisibility();
            }
        });

        // Setup spinners
        Spinner fieldSpinner = rowView.findViewById(R.id.fieldSpinner);
        ArrayAdapter<CharSequence> fieldAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.search_fields, android.R.layout.simple_spinner_item);
        fieldAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fieldSpinner.setAdapter(fieldAdapter);

        Spinner booleanOperatorSpinner = rowView.findViewById(R.id.booleanOperatorSpinner);
        ArrayAdapter<CharSequence> booleanAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.boolean_operators, android.R.layout.simple_spinner_item);
        booleanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        booleanOperatorSpinner.setAdapter(booleanAdapter);

        // If a SearchRow is provided, populate the fields
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

    private void removeEmptyRows() {
        // Remove all empty rows except keep at least one
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

    private void applyFilters() {
        removeEmptyRows();

        QueryOptions currentQuery = searchViewModel.getCurrentQuery();
        QueryOptions.Builder builder = new QueryOptions.Builder();

        // Collect rows from UI
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
                // Now matches constructor: (field, value, operator)
                rows.add(new SearchRow(field, value, operator));
            }
        }

        // Convert basic search term to a row if it exists
        if (currentQuery != null && currentQuery.hasSearchTerm()) {
            String searchTerm = currentQuery.getSearchTerm();
            String searchField = currentQuery.getSearchField();

            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                // Add basic search as the FIRST row with AND operator
                SearchRow basicSearchRow = new SearchRow(searchField, searchTerm, "AND");
                rows.add(0, basicSearchRow);
            }
        }

        // Set rows (will be empty if no search term and no manual rows)
        builder.rows(rows);

        // Clear searchTerm since we've converted it to rows
        builder.searchTerm(null);
        builder.searchField("all");

        // Always preserve categories and dates
        builder.categories(new ArrayList<>(selectedCategories));

        String fromDate = dateFromField.getText().toString().trim();
        String toDate = dateToField.getText().toString().trim();
        if (!fromDate.isEmpty()) builder.dateFrom(fromDate);
        if (!toDate.isEmpty()) builder.dateTo(toDate);

        builder.start(0);

        QueryOptions newQuery = builder.build();
        searchViewModel.currentQuery = newQuery;
    }

    private String getFieldValue(int position) {
        String[] fieldValues = {"all", "ti", "au", "abs", "co", "jr", "cat", "rn", "id"};
        if (position >= 0 && position < fieldValues.length) {
            return fieldValues[position];
        }
        return "all";
    }

    private void resetAllFilters() {
        searchFieldsContainer.removeAllViews();
        // Re-add one empty row
        addSearchFieldRow(null);

        selectedCategories.clear();
        selectedCategoriesChipGroup.removeAllViews();
        updateChipGroupVisibility();
        mainCategorySpinner.setSelection(0);
        dateFromField.setText("");
        dateToField.setText("");

        QueryOptions currentQuery = searchViewModel.getCurrentQuery();
        QueryOptions basicQuery;
        if (currentQuery != null && currentQuery.getSearchTerm() != null && !currentQuery.getSearchTerm().trim().isEmpty()) {
            basicQuery = new QueryOptions.Builder()
                    .searchTerm(currentQuery.getSearchTerm())
                    .searchField(currentQuery.getSearchField())
                    .start(0)
                    .build();
        } else {
            basicQuery = new QueryOptions.Builder().start(0).build();
        }
        searchViewModel.search(basicQuery);

        Toast.makeText(getContext(), R.string.advanced_filters_cleared_toast, Toast.LENGTH_SHORT).show();
    }

    private void reapplyExistingFilters() {
        QueryOptions currentQuery = searchViewModel.getCurrentQuery();

        // Clear existing dynamic views
        searchFieldsContainer.removeAllViews();
        selectedCategories.clear();
        selectedCategoriesChipGroup.removeAllViews();

        if (currentQuery == null) {
            // Make sure there's at least one row
            addSearchFieldRow(null);
            return;
        }

        // Re-add search rows
        if (currentQuery.getRows() != null && !currentQuery.getRows().isEmpty()) {
            for (SearchRow row : currentQuery.getRows()) {
                addSearchFieldRow(row);
            }
        } else {
            // Always have at least one row
            addSearchFieldRow(null);
        }

        // Re-add category chips
        if (currentQuery.getCategories() != null) {
            for (String categoryCode : currentQuery.getCategories()) {
                String fullName = findCategoryFullNameByCode(categoryCode);
                if (fullName != null) {
                    addCategoryChip(fullName, categoryCode);
                }
            }
        }

        // Set date fields - populate if they exist
        String dateFrom = currentQuery.getDateFrom();
        String dateTo = currentQuery.getDateTo();

        if (dateFrom != null && !dateFrom.isEmpty()) {
            dateFromField.setText(dateFrom);
        }
        if (dateTo != null && !dateTo.isEmpty()) {
            dateToField.setText(dateTo);
        }

        updateChipGroupVisibility();
    }



    private void setupToggleSections() {
        // Correctly set initial state based on layout file defaults
        // For searchFields, default is visible, so we make it visible
        searchFieldsContent.setVisibility(View.VISIBLE);
        searchFieldsToggle.setText("▼");

        // For categories and date, default is gone, so we make them gone
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


    private void setupDatePicker() {
        dateFromField.setOnClickListener(v -> showDatePicker(dateFromField));
        dateToField.setOnClickListener(v -> showDatePicker(dateToField));
    }

    private void showDatePicker(final EditText dateField) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, selectedYear, selectedMonth, selectedDay) -> {
            String date = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
            dateField.setText(date);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void closeDrawer() {
        // ManageSearchFragment is a child of SearchFragment
        // We need to call the parent's closeDrawer method
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof SearchFragment) {
            ((SearchFragment) parentFragment).closeDrawer();
        }
    }


    private String findCategoryFullNameByCode(String code) {
        for (Category topCat : categoryDataProvider.getAllMainCategories().values()) {
            String fullName = findFullNameRecursive(topCat, code);
            if (fullName != null) {
                return fullName;
            }
        }
        return null;
    }

    private String findFullNameRecursive(Category category, String code) {
        if (category == null) return null;
        if (category.getShortName().equals(code)) {
            return category.getFullName(requireContext());
        }
        if (category.hasSubCategories()) {
            for (Category subCat : category.getSubCategories()) {
                String foundName = findFullNameRecursive(subCat, code);
                if (foundName != null) return foundName;
            }
        }
        return null;
    }
}
