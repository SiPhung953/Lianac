package vn.edu.lianac;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import vn.edu.lianac.ui.ArticleListingFragment;
import vn.edu.lianac.ui.SearchFragment;

public class MainActivity extends AppCompatActivity {

    private FrameLayout searchContainerWrapper;
    private boolean isSearchVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        searchContainerWrapper = findViewById(R.id.searchContainerWrapper);

        ImageView searchIcon = findViewById(R.id.search_icon);
        searchIcon.setOnClickListener(v -> toggleSearch());

        setupFragments();
    }

    private void setupFragments() {
        searchContainerWrapper.setVisibility(View.GONE);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.searchContainer, new SearchFragment())
                .commit();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.resultsContainer, new ArticleListingFragment())
                .commit();
    }

    private void toggleSearch() {
        if (isSearchVisible) {
            searchContainerWrapper.setVisibility(View.GONE);
            isSearchVisible = false;
        } else {
            searchContainerWrapper.setVisibility(View.VISIBLE);
            isSearchVisible = true;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}