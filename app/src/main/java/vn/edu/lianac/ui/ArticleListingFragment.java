package vn.edu.lianac.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import vn.edu.lianac.R;
import vn.edu.lianac.viewmodel.SearchViewModel;

public class ArticleListingFragment extends Fragment {

    private RecyclerView recyclerView;
    private Spinner sortSpinner, pageSizeSpinner;
    private TextView resultsCountText, pageInfoText, errorText;
    private ProgressBar progressBar;
    private LinearLayout pageSizeContainer, paginationButtonsContainer;
    private Button firstPageButton, prevPageButton, nextPageButton, lastPageButton;

    private SearchViewModel searchViewModel;
    private ArticleAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchViewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article_listing, container, false);

        bindViews(view);
        setupRecyclerView();
        setupSpinners();
        setupPaginationButtons();
        observeViewModel();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Load initial data AFTER all observers are properly set up
        searchViewModel.loadInitialData();
    }

    private void bindViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        sortSpinner = view.findViewById(R.id.sortSpinner);
        pageSizeSpinner = view.findViewById(R.id.pageSizeSpinner);
        resultsCountText = view.findViewById(R.id.resultsCountText);
        pageInfoText = view.findViewById(R.id.pageInfoText);
        errorText = view.findViewById(R.id.errorText);
        progressBar = view.findViewById(R.id.progressBar);
        pageSizeContainer = view.findViewById(R.id.pageSizeContainer);
        paginationButtonsContainer = view.findViewById(R.id.paginationButtonsContainer);

        firstPageButton = view.findViewById(R.id.firstPageButton);
        prevPageButton = view.findViewById(R.id.prevPageButton);
        nextPageButton = view.findViewById(R.id.nextPageButton);
        lastPageButton = view.findViewById(R.id.lastPageButton);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ArticleAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void setupSpinners() {
        // Sort spinner
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.sort_options,
                android.R.layout.simple_spinner_item
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean isFirstSelection = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isFirstSelection) {
                    isFirstSelection = false;
                    return;  // Skip the automatic first trigger
                }
                String sortBy = getSortByValue(position);
                String sortOrder = getSortOrderValue(position);
                searchViewModel.updateSort(sortBy, sortOrder);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Page size spinner
        ArrayAdapter<CharSequence> pageSizeAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.page_sizes,
                android.R.layout.simple_spinner_item
        );
        pageSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pageSizeSpinner.setAdapter(pageSizeAdapter);

        pageSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean isFirstSelection = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isFirstSelection) {
                    isFirstSelection = false;
                    return;  // Skip the automatic first trigger
                }
                int pageSize = getPageSizeValue(position);
                searchViewModel.updatePageSize(pageSize);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupPaginationButtons() {
        firstPageButton.setOnClickListener(v -> searchViewModel.goToFirstPage());
        prevPageButton.setOnClickListener(v -> searchViewModel.previousPage());
        nextPageButton.setOnClickListener(v -> searchViewModel.nextPage());
        lastPageButton.setOnClickListener(v -> searchViewModel.goToLastPage());
    }

    private void observeViewModel() {
        // Observe search results
        searchViewModel.getSearchResults().observe(getViewLifecycleOwner(), articles -> {
            if (articles != null) {
                Log.d("ArticleListingFragment", "Received " + articles.size() + " articles");

                // Handle API limitation: empty results on non-first pages
                Integer currentPage = searchViewModel.getCurrentPage().getValue();
                Integer totalResults = searchViewModel.getTotalResults().getValue();

                if (articles.isEmpty()) {
                    if (currentPage != null && currentPage > 1 && totalResults != null && totalResults > 0) {
                        // API hit its limitation
                        int maxAccessibleResults = (currentPage - 1) * 10;
                        errorText.setText(String.format(Locale.US,
                                "API limitation reached. Only the first ~%d results are available for this search. " +
                                        "Try using more specific search terms for better results.",
                                maxAccessibleResults));
                        updateUIVisibility(false, true);
                    } else {
                        // First page is empty - no results found
                        errorText.setText("No results found");
                        updateUIVisibility(false, true);
                    }
                    return;
                }

                adapter.submitList(articles);
                updateUIVisibility(false, false);
            }
        });

        // Observe loading state
        searchViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            // Hide error text while loading
            if (isLoading) {
                errorText.setVisibility(View.GONE);
            }
        });

        // Observe errors
        searchViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                errorText.setText(error);
                updateUIVisibility(false, true);
            } else {
                // Clear error when it's null
                errorText.setVisibility(View.GONE);
            }
        });


        // Observe individual pagination values instead of a PaginationInfo object
        searchViewModel.getTotalResults().observe(getViewLifecycleOwner(), totalResults -> {
            resultsCountText.setText(String.format(Locale.US, "%d results", totalResults));
            updatePaginationUI();
        });

        searchViewModel.getCurrentPage().observe(getViewLifecycleOwner(), currentPage -> updatePaginationUI());
        searchViewModel.getTotalPages().observe(getViewLifecycleOwner(), totalPages -> updatePaginationUI());
    }

    private void updateUIVisibility(boolean loading, boolean error) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        errorText.setVisibility(error ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(!loading && !error ? View.VISIBLE : View.GONE);
    }

    private void updatePaginationUI() {
        Integer currentPage = searchViewModel.getCurrentPage().getValue();
        Integer totalPages = searchViewModel.getTotalPages().getValue();
        Integer totalResults = searchViewModel.getTotalResults().getValue();

        if (currentPage == null || totalPages == null || totalResults == null || totalResults == 0) {
            resultsCountText.setVisibility(View.GONE);
            pageInfoText.setVisibility(View.GONE);
            pageSizeContainer.setVisibility(View.GONE);
            paginationButtonsContainer.setVisibility(View.GONE);
            return;
        }

        resultsCountText.setVisibility(View.VISIBLE);
        pageInfoText.setVisibility(View.VISIBLE);
        pageSizeContainer.setVisibility(View.VISIBLE);
        paginationButtonsContainer.setVisibility(View.VISIBLE);

        // Get maximum reliable page based on search type
        int maxReliablePage = searchViewModel.getMaxReliablePage();
        int effectiveTotalPages = Math.min(totalPages, maxReliablePage);

        // Always show normal page display, but limit navigation
        pageInfoText.setText(String.format(Locale.US, "Page %d of %d", currentPage, totalPages));

        // Disable navigation beyond the reliable page limit for broad searches
        boolean canGoForward = currentPage < totalPages;
        if (searchViewModel.isBroadSearch()) {
            canGoForward = currentPage < effectiveTotalPages;
        }

        firstPageButton.setEnabled(currentPage > 1);
        prevPageButton.setEnabled(currentPage > 1);
        nextPageButton.setEnabled(canGoForward);
        lastPageButton.setEnabled(canGoForward);
    }

    // Helper methods
    private String getSortByValue(int position) {
        String[] values = {"submittedDate", "lastUpdatedDate", "relevance"};
        if (position >= 0 && position < values.length) {
            return values[position];
        }
        return "submittedDate";
    }

    private String getSortOrderValue(int position) {
        // All default sort orders are descending in the provided example
        return "descending";
    }

    private int getPageSizeValue(int position) {
        int[] sizes = {10, 25, 50, 100, 200};
        if (position >= 0 && position < sizes.length) {
            return sizes[position];
        }
        return 25;
    }


}
