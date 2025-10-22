package vn.edu.lianac.search;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import vn.edu.lianac.R;
import vn.edu.lianac.models.QueryOptions;
import vn.edu.lianac.models.SearchRow;
import vn.edu.lianac.utils.CategoryProvider;

/**
 * Advanced search filters fragment - cleaned & refactored.
 * Removed basic search display section - focuses purely on advanced filters.
 */
public class ManageSearchFragment extends Fragment {

    // State
    private final List<String> selectedCategories = new ArrayList<>();
    private final Set<String> selectedMainCategories = new LinkedHashSet<>();

    // UI Components
    private ImageButton closeDrawerButton;
    private Button applyFiltersButton, resetButton, searchButton, addFieldButton;
    private LinearLayout searchFieldsContent, categoriesContent, dateRangeContent;
    private TextView searchFieldsToggle, categoriesToggle, dateRangeToggle;
    private EditText dateFromField, dateToField;
    private LinearLayout searchFieldsContainer;

    // Category chip system
    private ChipGroup mainCategoryChipGroup;
    private ChipGroup leafCategoryChipGroup;
    private TextView leafCategoryLabel;
    private TextView selectedCategoriesSummary, mainCategoryLabel;
    private LinearLayout mainCategoryHeader, leafCategoryHeader;
    private TextView mainCategoryToggle, leafCategoryToggle;
    private SearchViewModel searchViewModel;
    private CheckBox includeCrossListCheckbox;

    // State

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        searchViewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);

        // Ensure CategoryProvider is initialized
        CategoryProvider.getInstance(requireContext());

        bindViews(view);
        setupEventListeners();
        setupToggleSections();
        setupCategoryChipSystem();
        setupDatePicker();

        // Ensure at least one search row
        if (searchFieldsContainer != null && searchFieldsContainer.getChildCount() == 0) {
            addSearchFieldRow(null);
        }

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

        mainCategoryChipGroup = view.findViewById(R.id.mainCategoryChipGroup);
        leafCategoryChipGroup = view.findViewById(R.id.leafCategoryChipGroup);
        selectedCategoriesSummary = view.findViewById(R.id.selectedCategoriesSummary);

        mainCategoryHeader = view.findViewById(R.id.mainCategoryHeader);
        mainCategoryToggle = view.findViewById(R.id.mainCategoryToggle);
        mainCategoryLabel = view.findViewById(R.id.mainCategoryLabel);
        leafCategoryHeader = view.findViewById(R.id.leafCategoryHeader);
        leafCategoryToggle = view.findViewById(R.id.leafCategoryToggle);
        leafCategoryLabel = view.findViewById(R.id.leafCategoryLabel);

        //CheckBox includeCrossListCheckbox = view.findViewById(R.id.includeCrossListCheckbox);

        // Header click listeners
        View sfh = view.findViewById(R.id.searchFieldsHeader);
        View ch = view.findViewById(R.id.categoriesHeader);
        View drh = view.findViewById(R.id.dateRangeHeader);
        if (sfh != null) sfh.setOnClickListener(this::onToggleClicked);
        if (ch != null) ch.setOnClickListener(this::onToggleClicked);
        if (drh != null) drh.setOnClickListener(this::onToggleClicked);

        if (mainCategoryHeader != null)
            mainCategoryHeader.setOnClickListener(this::onCategorySubsectionToggle);
        if (leafCategoryHeader != null)
            leafCategoryHeader.setOnClickListener(this::onCategorySubsectionToggle);
    }

    private void setupEventListeners() {
        if (closeDrawerButton != null) closeDrawerButton.setOnClickListener(v -> closeDrawer());
        if (applyFiltersButton != null) applyFiltersButton.setOnClickListener(v -> {
            if (applyFilters()) {
                closeDrawer();
            }
        });
        if (searchButton != null) searchButton.setOnClickListener(v -> {
            if (applyFilters()) {
                closeDrawer();
            }
        });
        if (resetButton != null) resetButton.setOnClickListener(v -> resetAllFilters());
        if (addFieldButton != null) addFieldButton.setOnClickListener(v -> addSearchFieldRow(null));
    }

    private void setupCategoryChipSystem() {
        populateMainCategoryChips();
        updateSelectedCategoriesSummary();
    }

    private void populateMainCategoryChips() {
        if (mainCategoryChipGroup == null) return;
        mainCategoryChipGroup.removeAllViews();

        List<String> mainCategories = CategoryProvider.getMainCategories();
        if (mainCategories == null) return;

        for (String categoryId : mainCategories) {
            String displayName = CategoryProvider.getCategoryName(categoryId);
            Chip chip = createMainCategoryChip(displayName != null ? displayName : categoryId, categoryId);
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
            updateCategoryHeaderCounts();
        });

        return chip;
    }

    private void onCategorySubsectionToggle(View v) {
        View content = null;
        TextView toggle = null;

        int id = v.getId();
        if (id == R.id.mainCategoryHeader) {
            content = mainCategoryChipGroup;
            toggle = mainCategoryToggle;
        } else if (id == R.id.leafCategoryHeader) {
            content = leafCategoryChipGroup;
            toggle = leafCategoryToggle;
        }

        if (content != null && toggle != null) {
            if (content.getVisibility() == View.VISIBLE) {
                content.setVisibility(View.GONE);
                toggle.setText(R.string.toggle_collapsed);
            } else {
                content.setVisibility(View.VISIBLE);
                toggle.setText(R.string.toggle_expanded);
            }
        }
    }

    private void updateLeafCategoriesForAllSelected() {
        if (selectedMainCategories.isEmpty()) {
            hideLeafCategoryRow();
            return;
        }

        // Build a deterministic (insertion-order) set of leaf categories
        Set<String> allLeafCategories = new LinkedHashSet<>();
        for (String mainCategoryId : selectedMainCategories) {
            List<String> leaves = CategoryProvider.getInstance(requireContext()).getAllLeafCategories(mainCategoryId);
            if (leaves != null) allLeafCategories.addAll(leaves);
        }

        populateLeafCategoryChips(new ArrayList<>(allLeafCategories));
        showLeafCategoryRow();
    }

    private void showLeafCategoryRow() {
        if (leafCategoryHeader != null) {
            leafCategoryHeader.setVisibility(View.VISIBLE);
        }
        if (leafCategoryChipGroup != null) {
            leafCategoryChipGroup.setVisibility(View.VISIBLE);
        }
        if (leafCategoryToggle != null) {
            leafCategoryToggle.setText(R.string.toggle_expanded);
        }
    }

    private void hideLeafCategoryRow() {
        if (leafCategoryHeader != null) {
            leafCategoryHeader.setVisibility(View.GONE);
        }
        if (leafCategoryChipGroup != null) {
            leafCategoryChipGroup.setVisibility(View.GONE);
            leafCategoryChipGroup.removeAllViews();
        }
    }

    private void populateLeafCategoryChips(List<String> leafCategories) {
        if (leafCategoryChipGroup == null) return;
        leafCategoryChipGroup.removeAllViews();

        if (leafCategories == null || leafCategories.isEmpty()) return;

        List<String> sorted = new ArrayList<>(leafCategories);
        sorted.sort(String::compareTo);

        for (String id : sorted) {
            String display = getCategoryDisplayName(id);
            Chip chip = createLeafCategoryChip(display, id);
            leafCategoryChipGroup.addView(chip);
        }
    }

    private Chip createLeafCategoryChip(String displayName, String categoryId) {
        Chip chip = new Chip(requireContext());
        chip.setText(displayName);
        chip.setCheckable(true);
        chip.setCheckedIconVisible(true);
        chip.setTag(categoryId);

        chip.setChecked(selectedCategories.contains(categoryId));

        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedCategories.contains(categoryId)) selectedCategories.add(categoryId);
            } else {
                selectedCategories.remove(categoryId);
            }
            updateSelectedCategoriesSummary();
            updateCategoryHeaderCounts();
        });

        return chip;
    }

    private void updateLeafCategoryChips() {
        if (leafCategoryChipGroup == null) return;
        for (int i = 0; i < leafCategoryChipGroup.getChildCount(); i++) {
            View child = leafCategoryChipGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip c = (Chip) child;
                String id = (String) c.getTag();
                c.setChecked(selectedCategories.contains(id));
            }
        }
    }

    private void updateSelectedCategoriesFromUI() {
        selectedCategories.clear();
        selectedCategories.addAll(selectedMainCategories);

        if (leafCategoryChipGroup != null) {
            for (int i = 0; i < leafCategoryChipGroup.getChildCount(); i++) {
                View v = leafCategoryChipGroup.getChildAt(i);
                if (v instanceof Chip) {
                    Chip c = (Chip) v;
                    if (c.isChecked()) {
                        String id = (String) c.getTag();
                        if (!selectedCategories.contains(id)) selectedCategories.add(id);
                    }
                }
            }
        }
    }

    private void updateSelectedCategoriesSummary() {
        if (selectedCategoriesSummary == null) return;

        updateSelectedCategoriesFromUI();
        int total = selectedCategories.size();
        selectedCategoriesSummary.setVisibility(View.VISIBLE);

        if (total == 0) {
            selectedCategoriesSummary.setText(R.string.no_categories_selected);
        } else {
            selectedCategoriesSummary.setText(
                    getResources().getQuantityString(R.plurals.selected_categories, total, total)
            );
        }
    }

    private void addSearchFieldRow(@Nullable SearchRow row) {
        if (searchFieldsContainer == null) return;

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View rowView = inflater.inflate(R.layout.item_search_field_row, searchFieldsContainer, false);
        if (rowView == null) {
            return;
        }

        // Remove button
        ImageButton removeButton = rowView.findViewById(R.id.removeButton);
        if (removeButton != null) {
            removeButton.setOnClickListener(v -> {
                if (searchFieldsContainer.getChildCount() > 1) {
                    searchFieldsContainer.removeView(rowView);
                    updateRemoveButtonsVisibility();
                }
            });
        }

        Spinner fieldSpinner = rowView.findViewById(R.id.fieldSpinner);
        if (fieldSpinner != null) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    requireContext(), R.array.search_fields, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            fieldSpinner.setAdapter(adapter);
        }

        // Boolean operator spinner
        Spinner booleanOperatorSpinner = rowView.findViewById(R.id.booleanOperatorSpinner);
        if (booleanOperatorSpinner != null) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    requireContext(), R.array.boolean_operators, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            booleanOperatorSpinner.setAdapter(adapter);
        }

        if (row != null) {
            EditText valueField = rowView.findViewById(R.id.valueField);
            if (valueField != null) valueField.setText(row.getValue());
            if (fieldSpinner != null) setSpinnerSelection(fieldSpinner, row.getField());
            if (booleanOperatorSpinner != null)
                setSpinnerSelection(booleanOperatorSpinner, row.getOperator());
        }

        searchFieldsContainer.addView(rowView);
        updateRemoveButtonsVisibility();
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (spinner == null || value == null) return;
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter == null) return;
        for (int i = 0; i < adapter.getCount(); i++) {
            Object item = adapter.getItem(i);
            if (item != null && value.equalsIgnoreCase(item.toString())) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void updateRemoveButtonsVisibility() {
        if (searchFieldsContainer == null) return;
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
        if (searchFieldsContainer == null) return;
        List<View> rowsToRemove = new ArrayList<>();
        int filled = 0;
        for (int i = 0; i < searchFieldsContainer.getChildCount(); i++) {
            View rv = searchFieldsContainer.getChildAt(i);
            EditText value = rv.findViewById(R.id.valueField);
            if (value != null) {
                String t = value.getText().toString().trim();
                if (t.isEmpty()) rowsToRemove.add(rv);
                else filled++;
            }
        }

        if (filled > 0) {
            for (View r : rowsToRemove) searchFieldsContainer.removeView(r);
        } else if (!rowsToRemove.isEmpty()) {
            for (int i = 1; i < rowsToRemove.size(); i++)
                searchFieldsContainer.removeView(rowsToRemove.get(i));
        }

        updateRemoveButtonsVisibility();
    }

    private void setupDatePicker() {
        if (dateFromField != null)
            dateFromField.setOnClickListener(v -> showDatePicker(dateFromField));
        if (dateToField != null) dateToField.setOnClickListener(v -> showDatePicker(dateToField));
    }

    private void showDatePicker(final EditText dateField) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dp = new DatePickerDialog(
                requireContext(),
                (view, year, month, day) -> {
                    String date = getString(R.string.date_format, year, month + 1, day);
                    dateField.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dp.show();
    }

    // ------------------- Toggles -------------------
    private void setupToggleSections() {
        if (searchFieldsContent != null) searchFieldsContent.setVisibility(View.VISIBLE);
        if (searchFieldsToggle != null) searchFieldsToggle.setText(R.string.toggle_expanded);

        if (categoriesContent != null) categoriesContent.setVisibility(View.GONE);
        if (categoriesToggle != null) categoriesToggle.setText(R.string.toggle_collapsed);

        if (dateRangeContent != null) dateRangeContent.setVisibility(View.GONE);
        if (dateRangeToggle != null) dateRangeToggle.setText(R.string.toggle_collapsed);
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
                toggle.setText(R.string.toggle_collapsed);
            } else {
                content.setVisibility(View.VISIBLE);
                toggle.setText(R.string.toggle_expanded);
            }
        }
    }

    // ------------------- Apply / Reset -------------------

    private boolean applyFilters() {
        removeEmptyRows();

        QueryOptions existingQuery = searchViewModel.getCurrentQuery().getValue();
        QueryOptions.Builder builder;

        // Start from existing query to preserve sort settings and other properties
        if (existingQuery != null) {
            builder = existingQuery.toBuilder();
        } else {
            builder = new QueryOptions.Builder();
        }

        // CRITICAL FIX: Read the ACTUAL current value from SearchFragment's UI
        // This ensures we get the real-time value, not stale ViewModel data
        Fragment parent = getParentFragment();
        if (parent instanceof SearchFragment) {
            String currentSearchTerm = ((SearchFragment) parent).getCurrentSearchTerm();
            String currentSearchField = ((SearchFragment) parent).getCurrentSearchField();

            // Update with current values from UI
            if (currentSearchTerm != null && !currentSearchTerm.isEmpty()) {
                builder.searchTerm(currentSearchTerm).searchField(currentSearchField);
            } else {
                // User cleared the search bar - clear it in query too
                builder.searchTerm(null).searchField(null);
            }
        }

        // Collect search rows from advanced filter UI
        List<SearchRow> rows = new ArrayList<>();
        if (searchFieldsContainer != null) {
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
        }

        builder.rows(rows);

        // Include categories with smart filtering
        updateSelectedCategoriesFromUI();
        List<String> filteredCategories = filterRedundantCategories(new ArrayList<>(selectedCategories));
        builder.categories(filteredCategories);

        // Include dates
        String fromDate = dateFromField != null ? dateFromField.getText().toString().trim() : "";
        String toDate = dateToField != null ? dateToField.getText().toString().trim() : "";
        if (!fromDate.isEmpty()) {
            builder.dateFrom(fromDate);
        } else {
            builder.dateFrom(null); // Clear if empty
        }
        if (!toDate.isEmpty()) {
            builder.dateTo(toDate);
        } else {
            builder.dateTo(null); // Clear if empty
        }

        builder.start(0); // Reset to first page

        // UPDATED VALIDATION: Check the actual current search term from UI
        String actualSearchTerm = null;
        if (parent instanceof SearchFragment) {
            actualSearchTerm = ((SearchFragment) parent).getCurrentSearchTerm();
        }

        boolean hasSearchTerm = actualSearchTerm != null && !actualSearchTerm.isEmpty();
        boolean hasSearchRows = !rows.isEmpty();
        boolean hasCategories = !filteredCategories.isEmpty();
        boolean hasDateRange = !fromDate.isEmpty() || !toDate.isEmpty();

        if (!hasSearchTerm && !hasSearchRows && !hasCategories && !hasDateRange) {
            Toast.makeText(requireContext(),
                    R.string.validation_no_filters,
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        QueryOptions newQuery = builder.build();
        searchViewModel.updateQueryOptions(newQuery);
        searchViewModel.search(newQuery);

        updateFilterButtonIndicator();

        Toast.makeText(requireContext(), R.string.filters_applied, Toast.LENGTH_SHORT).show();
        return true;
    }

    private List<String> filterRedundantCategories(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return categories;
        }

        List<String> filtered = new ArrayList<>();

        for (String category : categories) {
            boolean hasSubcategorySelected = false;

            // Check if any other selected category is a subcategory of this one
            for (String otherCategory : categories) {
                if (!otherCategory.equals(category) && otherCategory.startsWith(category + ".")) {
                    // Found a subcategory of this category
                    hasSubcategorySelected = true;
                    break;
                }
            }

            // Only include this category if no subcategories are selected
            if (!hasSubcategorySelected) {
                filtered.add(category);
            }
        }

        return filtered;
    }

    private String getFieldValue(int position) {
        String[] fieldValues = getResources().getStringArray(R.array.search_field_values);
        return (position >= 0 && position < fieldValues.length) ? fieldValues[position] : "all";
    }

    private void resetAllFilters() {
        // Clear search rows
        if (searchFieldsContainer != null) {
            searchFieldsContainer.removeAllViews();
            addSearchFieldRow(null);
        }

        // Clear category selections
        selectedMainCategories.clear();
        selectedCategories.clear();

        if (mainCategoryChipGroup != null) {
            mainCategoryChipGroup.clearCheck();
        }
        if (leafCategoryChipGroup != null) {
            leafCategoryChipGroup.clearCheck();
        }

        hideLeafCategoryRow();

        // Clear dates
        if (dateFromField != null) {
            dateFromField.setText("");
        }
        if (dateToField != null) {
            dateToField.setText("");
        }

        updateSelectedCategoriesSummary();

        // FIXED: Read current search term from SearchFragment's UI, not from stale ViewModel
        Fragment parent = getParentFragment();
        String currentSearchTerm = null;
        String currentSearchField = null;

        if (parent instanceof SearchFragment) {
            currentSearchTerm = ((SearchFragment) parent).getCurrentSearchTerm();
            currentSearchField = ((SearchFragment) parent).getCurrentSearchField();
        }

        QueryOptions.Builder builder = new QueryOptions.Builder()
                .start(0)
                .maxResults(10)
                .sortBy("submittedDate")
                .sortOrder("descending");

        // Preserve basic search term and field if they exist in the UI
        if (currentSearchTerm != null && !currentSearchTerm.isEmpty()) {
            builder.searchTerm(currentSearchTerm).searchField(currentSearchField);
        }

        QueryOptions clearedQuery = builder.build();
        searchViewModel.updateQueryOptions(clearedQuery);
        searchViewModel.search(clearedQuery);

        updateFilterButtonIndicator();

        Toast.makeText(requireContext(), R.string.cleared_filter, Toast.LENGTH_SHORT).show();
    }

    public void onDrawerClosed() {
        removeEmptyRows();
    }

    private void updateCategoryHeaderCounts() {
        if (mainCategoryLabel != null) {
            int mainCount = selectedMainCategories.size();
            if (mainCount > 0) {
                String text = getString(R.string.category_header_with_count,
                        getString(R.string.select_main_categories),
                        mainCount
                );
                mainCategoryLabel.setText(text);
            } else {
                mainCategoryLabel.setText(R.string.select_main_categories);
            }
        }

        // FIXED: Remove visibility check - update the label regardless of visibility
        // The label should be updated even when the section is hidden
        if (leafCategoryLabel != null) {
            int leafCount = 0;
            if (leafCategoryChipGroup != null) {
                for (int i = 0; i < leafCategoryChipGroup.getChildCount(); i++) {
                    View v = leafCategoryChipGroup.getChildAt(i);
                    if (v instanceof Chip && ((Chip) v).isChecked()) {
                        leafCount++;
                    }
                }
            }

            if (leafCount > 0) {
                String text = getString(R.string.category_header_with_count,
                        getString(R.string.select_specific_categories),
                        leafCount
                );
                leafCategoryLabel.setText(text);
            } else {
                leafCategoryLabel.setText(R.string.select_specific_categories);
            }
        }
    }


    private void restoreFiltersFromViewModel() {
        QueryOptions current = searchViewModel.getCurrentQuery().getValue();

        if (searchFieldsContainer != null) searchFieldsContainer.removeAllViews();
        selectedCategories.clear();
        selectedMainCategories.clear();

        if (current == null) {
            addSearchFieldRow(null);
            return;
        }

        // NEW: Sync the basic search bar with what's in the query
        Fragment parent = getParentFragment();
        if (parent instanceof SearchFragment) {
            ((SearchFragment) parent).setSearchTerm(current.getSearchTerm());
            ((SearchFragment) parent).setSearchField(current.getSearchField());
        }

        if (current.getRows() != null && !current.getRows().isEmpty()) {
            for (SearchRow r : current.getRows()) addSearchFieldRow(r);
        } else addSearchFieldRow(null);

        if (current.getCategories() != null && !current.getCategories().isEmpty()) {
            selectedCategories.addAll(current.getCategories());
            restoreCategoryChipsFromList(current.getCategories());
        }

        if (current.getDateFrom() != null) {
            if (dateFromField != null) dateFromField.setText(current.getDateFrom());
        }
        if (current.getDateTo() != null) {
            if (dateToField != null) dateToField.setText(current.getDateTo());
        }

        updateSelectedCategoriesSummary();
        updateCategoryHeaderCounts();
    }

    private void restoreCategoryChipsFromList(List<String> categories) {
        if (categories == null || categories.isEmpty() || mainCategoryChipGroup == null) return;

        for (String category : categories) {
            if (CategoryProvider.getInstance(requireContext()).isMainCategory(category)) {
                selectedMainCategories.add(category);
                for (int i = 0; i < mainCategoryChipGroup.getChildCount(); i++) {
                    View v = mainCategoryChipGroup.getChildAt(i);
                    if (v instanceof Chip && category.equals(v.getTag())) {
                        ((Chip) v).setChecked(true);
                        break;
                    }
                }
            }
        }

        updateLeafCategoriesForAllSelected();
        updateLeafCategoryChips();
    }

    private String getCategoryDisplayName(String code) {
        if (code == null || code.isEmpty()) return code;
        String name = CategoryProvider.getCategoryName(code);
        return (name != null && !name.equals(code)) ? name : code;
    }

    private boolean hasAdvancedFilters() {
        int nonEmpty = 0;
        if (searchFieldsContainer != null) {
            for (int i = 0; i < searchFieldsContainer.getChildCount(); i++) {
                View rv = searchFieldsContainer.getChildAt(i);
                EditText val = rv.findViewById(R.id.valueField);
                if (val != null && !val.getText().toString().trim().isEmpty()) nonEmpty++;
            }
        }

        boolean dateSet = (dateFromField != null && !dateFromField.getText().toString().trim().isEmpty()) ||
                (dateToField != null && !dateToField.getText().toString().trim().isEmpty());

        return nonEmpty > 0 || !selectedCategories.isEmpty() || dateSet;
    }

    private void updateFilterButtonIndicator() {
        Fragment parent = getParentFragment();
        if (parent instanceof SearchFragment) {
            ((SearchFragment) parent).updateFilterIndicator(hasAdvancedFilters());
        }
    }

    private void closeDrawer() {
        Fragment parent = getParentFragment();
        if (parent instanceof SearchFragment) {
            ((SearchFragment) parent).closeDrawer();
        }
    }
}