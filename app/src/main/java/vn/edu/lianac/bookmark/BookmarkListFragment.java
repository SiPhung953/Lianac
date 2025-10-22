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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import vn.edu.lianac.R;
import vn.edu.lianac.models.Article;
import vn.edu.lianac.network.ArxivAPIService;
import vn.edu.lianac.DetailFragment;

public class BookmarkListFragment extends Fragment implements BookmarkAdapter.OnBookmarkClickListener {
    private RecyclerView recyclerView;
    private BookmarkAdapter adapter;
    private BookmarkManager bookmarkManager;
    private TextView tvEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmark_list, container, false);

        recyclerView = view.findViewById(R.id.rv_bookmarks);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);

        bookmarkManager = new BookmarkManager(requireContext());

        setupRecyclerView();
        loadBookmarks();

        return view;
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
       ArxivAPIService apiService = ArxivAPIService.getInstance();
        apiService.fetchArticleById(item.getId().replaceFirst("(?i)^arxiv:", ""), new ArxivAPIService.ArxivSingleArticleListener() {
            @Override
            public void onSuccess(Article article) {
                if (isAdded() && getActivity() != null) {
                    DetailFragment detailFragment = new DetailFragment();
                    Bundle args = new Bundle();
                    args.putParcelable("article", article);
                    detailFragment.setArguments(args);

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_frame, detailFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }

            @Override
            public void onError(Exception e) {
                if (isAdded() && getContext() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getContext(), "Failed to load article", Toast.LENGTH_SHORT).show();
                            Log.e("BookmarkListFragment", "Error fetching article", e);
                        }
                    });
                }
            }
        });
        Log.d("BookmarkListFragment", "Fetching article with ID: " + item.getId());
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
}
