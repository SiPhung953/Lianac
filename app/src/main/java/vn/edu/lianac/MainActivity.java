package vn.edu.lianac;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;

import vn.edu.lianac.ui.ArticleListingFragment;
import vn.edu.lianac.ui.SearchFragment;
import vn.edu.lianac.ui.SubjectsFragment;
import vn.edu.lianac.utils.CategoryProvider;

/**
 * Main activity with container-based architecture and navigation drawer.
 */
public class MainActivity extends AppCompatActivity {

    private FrameLayout searchContainerWrapper;
    private boolean isSearchVisible = false;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize CategoryProvider
        new Thread(() -> {
            CategoryProvider.getInstance(this);
        }).start();

        setupDrawer();
        setupToolbar();
        setupFragments();
    }

    private void setupDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // Setup navigation item selection
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_subjects) {
                // Navigate to SubjectsFragment
                navigateToContent(new SubjectsFragment());
                drawerLayout.closeDrawer(navigationView);
                return true;
            }

            drawerLayout.closeDrawer(navigationView);
            return true;
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        searchContainerWrapper = findViewById(R.id.searchContainerWrapper);

        // Search icon toggles search fragment visibility
        ImageView searchIcon = findViewById(R.id.search_icon);
        searchIcon.setOnClickListener(v -> toggleSearch());

        // Hamburger menu opens navigation drawer
        ImageView hamburgerIcon = findViewById(R.id.hamburger_icon);
        hamburgerIcon.setOnClickListener(v -> {
            drawerLayout.openDrawer(navigationView);
        });

        TextView appName = findViewById(R.id.app_name);
        appName.setOnClickListener(v -> navigateToHome());
    }

    public void navigateToHome() {
        // Clear back stack
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        // Reset ViewModel to default query
        androidx.lifecycle.ViewModelProvider viewModelProvider =
                new androidx.lifecycle.ViewModelProvider(this);
        vn.edu.lianac.viewmodel.SearchViewModel viewModel =
                viewModelProvider.get(vn.edu.lianac.viewmodel.SearchViewModel.class);
        viewModel.resetToDefault();

        // Load home fragment without adding to back stack
        loadContentFragment(new ArticleListingFragment(), false);

        // Hide search when going home
        hideSearch();
    }

    private void setupFragments() {
        // Hide search by default
        searchContainerWrapper.setVisibility(View.GONE);

        // Initialize SearchFragment (always present, just hidden)
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.searchContainer, new SearchFragment())
                .commit();

        // Initialize default content fragment (ArticleListingFragment)
        loadContentFragment(new ArticleListingFragment(), false);
    }

    /**
     * Toggle search fragment visibility
     */
    private void toggleSearch() {
        if (isSearchVisible) {
            hideSearch();
        } else {
            showSearch();
        }
    }

    /**
     * Show the search fragment
     */
    public void showSearch() {
        searchContainerWrapper.setVisibility(View.VISIBLE);
        isSearchVisible = true;
    }

    /**
     * Hide the search fragment
     */
    public void hideSearch() {
        searchContainerWrapper.setVisibility(View.GONE);
        isSearchVisible = false;
    }

    /**
     * Load a fragment into the content container.
     * This is the main navigation method for switching between different screens.
     *
     * @param fragment The fragment to display
     * @param addToBackStack Whether to add this transaction to the back stack
     */
    public void loadContentFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.replace(R.id.contentContainer, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }

    /**
     * Navigate to a specific content fragment.
     * Examples of future usage:
     * - navigateToContent(new BookmarksFragment())
     * - navigateToContent(new ArticleDetailFragment(articleId))
     * - navigateToContent(new SettingsFragment())
     */
    public void navigateToContent(Fragment fragment) {
        loadContentFragment(fragment, true);
    }

    /**
     * A temporary method to replace a fragment, used for compatibility.
     *
     * @param fragment The fragment to display.
     */
    public void replaceFragment(Fragment fragment) {
        loadContentFragment(fragment, true);
    }

    @Override
    public void onBackPressed() {
        // First, close the drawer if it's open
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView);
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();

        // If we have fragments in the back stack, pop them
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            // Otherwise, use default back behavior
            super.onBackPressed();
        }
    }

    /**
     * Check if search is currently visible
     */
    public boolean isSearchVisible() {
        return isSearchVisible;
    }
}
