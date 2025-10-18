package vn.edu.lianac.bookmark;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.edu.lianac.R;
import vn.edu.lianac.models.Article;
import vn.edu.lianac.models.QueryOptions;
import vn.edu.lianac.viewmodel.SearchViewModel;

public class BookmarkListFragment extends Fragment implements BookmarkAdapter.OnBookmarkClickListener {
    private static final String TAG = "BookmarkListFragment";

    private RecyclerView recyclerView;
    private BookmarkAdapter adapter;
    private BookmarkManager bookmarkManager;
    private TextView tvEmptyState;
    private SearchViewModel searchViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmark_list, container, false);

        recyclerView = view.findViewById(R.id.rv_bookmarks);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);

        bookmarkManager = new BookmarkManager(requireContext());
        searchViewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);

        setupRecyclerView();
        loadBookmarks();

        return view;
    }

    @Override
    public void onBookmarkRemove(BookmarkItem item) {
        bookmarkManager.removeBookmark(item.getId());
        loadBookmarks();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh bookmarks when returning to this fragment
        loadBookmarks();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BookmarkAdapter(bookmarkManager.getBookmarks(), this);
        recyclerView.setAdapter(adapter);
    }

    private void loadBookmarks() {
        List<BookmarkItem> bookmarks = bookmarkManager.getBookmarks();
        adapter.updateBookmarks(bookmarks);

        // Show/hide empty state
        if (bookmarks.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBookmarkClick(BookmarkItem item) {
        // Fetch the full article data from arXiv API using the article ID
        String articleId = item.getId();

        // Show loading indicator
        Toast.makeText(requireContext(), "Loading article...", Toast.LENGTH_SHORT).show();

        // Use SearchViewModel to search by article ID
        QueryOptions query = new QueryOptions.Builder()
                .searchTerm(articleId)
                .searchField("id")
                .maxResults(1)
                .build();

        searchViewModel.search(query);

        // Observe the result and navigate to DetailFragment
        searchViewModel.getArticles().observe(getViewLifecycleOwner(), articles -> {
            if (articles != null && !articles.isEmpty()) {
                Article article = articles.get(0);
                navigateToDetail(article);

                // Remove observer after first result
                searchViewModel.getArticles().removeObservers(getViewLifecycleOwner());
            }
        });

        // Observe errors
        searchViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), "Error loading article: " + error, Toast.LENGTH_SHORT).show();
                searchViewModel.getErrorMessage().removeObservers(getViewLifecycleOwner());
            }
        });
    }

    private void navigateToDetail(Article article) {
        try {
            NavController navController = Navigation.findNavController(requireView());
            Bundle args = new Bundle();
            args.putParcelable("article", article);

            navController.navigate(R.id.action_bookmarkListFragment_to_detailFragment, args);

        } catch (Exception ex) {
            Log.e(TAG, "Failed to navigate to detail", ex);
            Toast.makeText(requireContext(), "Error opening article details", Toast.LENGTH_SHORT).show();
        }
    }
}