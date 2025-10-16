package vn.edu.lianac.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.edu.lianac.R;
import vn.edu.lianac.models.QueryOptions;
import vn.edu.lianac.viewmodel.SearchViewModel;

/**
 * Article listing fragment with enhanced pagination controls.
 */
public class ArticleListingFragment extends Fragment {
    private static final String TAG = "ArticleListingFragment";
    private final Button[] pageButtons = new Button[7];
    private SearchViewModel viewModel;
    private ArticleAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView errorText;
    private TextView resultsText;
    private TextView pageTextTop;
    private TextView pageTextBottom;
    private Spinner sortSpinner;
    private Spinner pageSizeSpinner;
    private LinearLayout bottomPaginationContainer;
    // Pagination buttons
    private Button prevBtn, nextBtn;
    // State tracking to prevent circular updates
    private boolean isUpdatingSpinners = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        return inflater.inflate(R.layout.fragment_article_listing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called");

        viewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);

        initViews(view);
        setupRecyclerView();
        setupSpinners();
        setupPaginationButtons();
        observeViewModel();

        viewModel.loadInitialData();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        errorText = view.findViewById(R.id.errorText);
        resultsText = view.findViewById(R.id.resultsCountText);
        pageTextTop = view.findViewById(R.id.pageInfoTextTop);
        pageTextBottom = view.findViewById(R.id.pageInfoTextBottom);
        sortSpinner = view.findViewById(R.id.sortSpinner);
        pageSizeSpinner = view.findViewById(R.id.pageSizeSpinner);
        bottomPaginationContainer = view.findViewById(R.id.bottomPaginationContainer);

        prevBtn = view.findViewById(R.id.prevPageButton);
        nextBtn = view.findViewById(R.id.nextPageButton);

        // Initialize all 7 page buttons
        pageButtons[0] = view.findViewById(R.id.pageButton1);
        pageButtons[1] = view.findViewById(R.id.pageButton2);
        pageButtons[2] = view.findViewById(R.id.pageButton3);
        pageButtons[3] = view.findViewById(R.id.pageButton4);
        pageButtons[4] = view.findViewById(R.id.pageButton5);
        pageButtons[5] = view.findViewById(R.id.pageButton6);
        pageButtons[6] = view.findViewById(R.id.pageButton7);

        Log.d(TAG, "Views initialized - RecyclerView: " + (recyclerView != null));
    }

    private void setupRecyclerView() {
        adapter = new ArticleAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        Log.d(TAG, "RecyclerView setup complete");
    }

    private void setupSpinners() {
        // Sort spinner
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.sort_options, android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isUpdatingSpinners) return; // Prevent circular updates

                String[] sortFields = {"submittedDate", "lastUpdatedDate", "relevance"};
                if (position < sortFields.length) {
                    Log.d(TAG, "User changed sort to: " + sortFields[position]);
                    viewModel.updateSort(sortFields[position], "descending");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Page size spinner
        ArrayAdapter<CharSequence> sizeAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.page_sizes, android.R.layout.simple_spinner_item);
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pageSizeSpinner.setAdapter(sizeAdapter);

        pageSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isUpdatingSpinners) return; // Prevent circular updates

                int[] sizes = {10, 25, 50, 100, 200};
                if (position < sizes.length) {
                    Log.d(TAG, "User changed page size to: " + sizes[position]);
                    viewModel.updatePageSize(sizes[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupPaginationButtons() {
        prevBtn.setOnClickListener(v -> viewModel.previousPage());
        nextBtn.setOnClickListener(v -> viewModel.nextPage());

        // Setup page number buttons
        for (int i = 0; i < pageButtons.length; i++) {
            final int index = i;
            pageButtons[i].setOnClickListener(v -> {
                String pageText = pageButtons[index].getText().toString();
                if ("...".equals(pageText)) {
                    showJumpToPageDialog();
                } else {
                    try {
                        int targetPage = Integer.parseInt(pageText);
                        goToPage(targetPage);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Invalid page number: " + pageText);
                    }
                }
            });
        }
    }

    private void showJumpToPageDialog() {
        Integer totalPages = viewModel.getTotalPages().getValue();
        if (totalPages == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Jump to Page");

        final EditText input = new EditText(requireContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("Enter page number (1-" + totalPages + ")");

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(50, 20, 50, 20);
        input.setLayoutParams(params);
        container.addView(input);

        builder.setView(container);

        builder.setPositiveButton("Go", (dialog, which) -> {
            String pageStr = input.getText().toString().trim();
            if (pageStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a page number", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int targetPage = Integer.parseInt(pageStr);
                if (targetPage > 0 && targetPage <= totalPages) {
                    goToPage(targetPage);
                } else {
                    Toast.makeText(getContext(),
                            "Please enter a page between 1 and " + totalPages,
                            Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid page number", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        input.requestFocus();
        input.postDelayed(() -> {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager)
                            requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(input, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100);
    }

    private void goToPage(int pageNumber) {
        QueryOptions current = viewModel.getCurrentQuery().getValue();
        if (current == null) return;

        int maxResults = current.getMaxResults();
        int start = (pageNumber - 1) * maxResults;

        viewModel.search(current.toBuilder()
                .start(start)
                .build());
    }

    private void observeViewModel() {
        // Articles
        viewModel.getArticles().observe(getViewLifecycleOwner(), articles -> {
            Log.d(TAG, "Articles updated: " + (articles != null ? articles.size() : "null") + " articles");
            if (articles != null && !articles.isEmpty()) {
                Log.d(TAG, "First article: " + articles.get(0).getTitle());
            }
            adapter.submitList(articles, () -> {
                Log.d(TAG, "submitList complete, item count: " + adapter.getItemCount());
                updateVisibility();
            });
        });

        // Loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            Log.d(TAG, "Loading state: " + loading);
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            if (loading) errorText.setVisibility(View.GONE);
        });

        // Errors
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            Log.d(TAG, "Error message: " + error);
            if (error != null) {
                errorText.setText(error);
                errorText.setVisibility(View.VISIBLE);
            } else {
                errorText.setVisibility(View.GONE);
            }
        });

        // Pagination info
        viewModel.getTotalResults().observe(getViewLifecycleOwner(), total -> {
            Log.d(TAG, "Total results: " + total);
            updatePaginationInfo();
        });

        viewModel.getCurrentPage().observe(getViewLifecycleOwner(), page -> {
            Log.d(TAG, "Current page: " + page);
            updatePaginationInfo();
            updatePaginationButtons();
        });

        viewModel.getTotalPages().observe(getViewLifecycleOwner(), pages -> {
            Log.d(TAG, "Total pages: " + pages);
            updatePaginationInfo();
            updatePaginationButtons();
        });

        // CRITICAL: Observe query changes to update spinners
        viewModel.getCurrentQuery().observe(getViewLifecycleOwner(), query -> {
            if (query != null) {
                Log.d(TAG, "Query changed - updating spinners");
                updateSpinnersFromQuery(query);
            }
        });
    }

    /**
     * Update spinner selections based on the current query state
     * This ensures spinners always reflect the actual state
     */
    private void updateSpinnersFromQuery(QueryOptions query) {
        isUpdatingSpinners = true;

        try {
            // Update page size spinner
            int pageSize = query.getMaxResults();
            int[] sizes = {10, 25, 50, 100, 200};

            for (int i = 0; i < sizes.length; i++) {
                if (sizes[i] == pageSize) {
                    if (pageSizeSpinner.getSelectedItemPosition() != i) {
                        pageSizeSpinner.setSelection(i, false);
                        Log.d(TAG, "Updated page size spinner to position " + i + " (" + pageSize + ")");
                    }
                    break;
                }
            }

            // Update sort spinner
            String sortBy = query.getSortBy();
            String[] sortFields = {"submittedDate", "lastUpdatedDate", "relevance"};

            for (int i = 0; i < sortFields.length; i++) {
                if (sortFields[i].equals(sortBy)) {
                    if (sortSpinner.getSelectedItemPosition() != i) {
                        sortSpinner.setSelection(i, false);
                        Log.d(TAG, "Updated sort spinner to position " + i + " (" + sortBy + ")");
                    }
                    break;
                }
            }
        } finally {
            // Always reset the flag, even if an exception occurs
            isUpdatingSpinners = false;
        }
    }

    private void updateVisibility() {
        boolean hasArticles = adapter.getItemCount() > 0;
        Boolean isLoading = viewModel.getIsLoading().getValue();

        Log.d(TAG, "updateVisibility - hasArticles: " + hasArticles + ", itemCount: " + adapter.getItemCount());

        if (!hasArticles && isLoading != null && !isLoading) {
            // No articles and not loading - show "No results"
            errorText.setText("No results found");
            errorText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            // Error text visibility is already handled by error observer
        }
    }

    private void updatePaginationInfo() {
        Integer total = viewModel.getTotalResults().getValue();
        Integer current = viewModel.getCurrentPage().getValue();
        Integer pages = viewModel.getTotalPages().getValue();
        QueryOptions currentQuery = viewModel.getCurrentQuery().getValue();

        if (total != null && total > 0 && currentQuery != null) {
            // Calculate the range of results being shown
            int pageSize = currentQuery.getMaxResults();
            int start = currentQuery.getStart();
            int firstResult = start + 1;
            int lastResult = Math.min(start + pageSize, total);

            // Check if category filter is active
            String resultsInfo;
            List<String> categories = currentQuery.getCategories();
            if (categories != null && !categories.isEmpty()) {
                // Get category display name
                String categoryId = categories.get(0);
                String displayName = vn.edu.lianac.utils.CategoryProvider.getCategoryName(categoryId);
                resultsInfo = String.format("Showing %,d-%,d of %,d results in %s (%s)",
                        firstResult, lastResult, total, displayName, categoryId);
            } else {
                resultsInfo = String.format("Showing %,d-%,d of %,d results",
                        firstResult, lastResult, total);
            }

            resultsText.setText(resultsInfo);
            resultsText.setVisibility(View.VISIBLE);

            if (current != null && pages != null) {
                String pageInfo = String.format("Page %d of %,d", current, pages);
                pageTextTop.setVisibility(View.GONE);
                pageTextBottom.setText(pageInfo);
                bottomPaginationContainer.setVisibility(View.VISIBLE);
            }
        } else {
            resultsText.setVisibility(View.GONE);
            pageTextTop.setVisibility(View.GONE);
            bottomPaginationContainer.setVisibility(View.GONE);
        }
    }

    private void updatePaginationButtons() {
        Integer currentPage = viewModel.getCurrentPage().getValue();
        Integer totalPages = viewModel.getTotalPages().getValue();

        if (currentPage == null || totalPages == null || totalPages <= 1) {
            bottomPaginationContainer.setVisibility(View.GONE);
            return;
        }

        bottomPaginationContainer.setVisibility(View.VISIBLE);

        // Enable/disable navigation buttons
        boolean hasPrev = currentPage > 1;
        boolean hasNext = currentPage < totalPages;

        prevBtn.setEnabled(hasPrev);
        nextBtn.setEnabled(hasNext);

        // Calculate which page numbers to show
        int[] pageNumbers = calculatePageNumbers(currentPage, totalPages);

        // Update page buttons
        for (int i = 0; i < pageButtons.length; i++) {
            if (pageNumbers[i] > 0) {
                pageButtons[i].setText(String.valueOf(pageNumbers[i]));
                pageButtons[i].setVisibility(View.VISIBLE);

                // Highlight current page
                if (pageNumbers[i] == currentPage) {
                    pageButtons[i].setEnabled(false);
                    pageButtons[i].setAlpha(0.5f);
                } else {
                    pageButtons[i].setEnabled(true);
                    pageButtons[i].setAlpha(1.0f);
                }
            } else if (pageNumbers[i] == -1) {
                // Ellipsis button
                pageButtons[i].setText("...");
                pageButtons[i].setVisibility(View.VISIBLE);
                pageButtons[i].setEnabled(true);
                pageButtons[i].setAlpha(1.0f);
            } else {
                pageButtons[i].setVisibility(View.GONE);
            }
        }
    }

    private int[] calculatePageNumbers(int current, int total) {
        int[] pages = new int[7];

        if (total <= 7) {
            // Show all pages if 7 or fewer
            for (int i = 0; i < total; i++) {
                pages[i] = i + 1;
            }
        } else {
            // Smart pagination with ellipsis
            pages[0] = 1;  // Always show first page
            pages[6] = total;  // Always show last page

            if (current <= 4) {
                // Near the beginning: 1, 2, 3, 4, 5, ..., total
                pages[1] = 2;
                pages[2] = 3;
                pages[3] = 4;
                pages[4] = 5;
                pages[5] = -1;  // Ellipsis
            } else if (current >= total - 3) {
                // Near the end: 1, ..., total-4, total-3, total-2, total-1, total
                pages[1] = -1;  // Ellipsis
                pages[2] = total - 4;
                pages[3] = total - 3;
                pages[4] = total - 2;
                pages[5] = total - 1;
            } else {
                // In the middle: 1, ..., current-1, current, current+1, ..., total
                pages[1] = -1;  // Ellipsis
                pages[2] = current - 1;
                pages[3] = current;
                pages[4] = current + 1;
                pages[5] = -1;  // Ellipsis
            }
        }

        return pages;
    }
}