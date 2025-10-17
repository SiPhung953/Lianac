package vn.edu.lianac;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;

import java.util.Locale;

import vn.edu.lianac.Download.DownloadFragment;
import vn.edu.lianac.bookmark.BookmarkListFragment;
import vn.edu.lianac.ui.ArticleListingFragment;
import vn.edu.lianac.ui.SearchFragment;
import vn.edu.lianac.ui.SubjectsFragment;
import vn.edu.lianac.ui.SubjectsFragment;
import vn.edu.lianac.utils.CategoryProvider;

/**
 * Main activity with container-based architecture and navigation drawer.
 */
public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    MaterialToolbar topAppBar;
    ActionBarDrawerToggle toggle;
    private boolean showSearch = false;
    private boolean searchShowing = true;

    // ADDED: Track if we're in search mode
    private boolean isSearchMode = false;

    private static final String PREFS_NAME = "AppSettings";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_THEME = "theme";

    // ADDED: Keys for savedInstanceState
    private static final String KEY_SEARCH_MODE = "search_mode";
    private static final String KEY_SEARCH_VISIBILITY = "search_visibility";
    private static final String KEY_SEARCH_ICON_SHOWING = "search_icon_showing";
    private static final String KEY_TITLE = "title";

    @Override
    protected void attachBaseContext(Context newBase) {
        // Apply language preference before creating the activity
        SharedPreferences prefs = newBase.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int languagePos = prefs.getInt(KEY_LANGUAGE, 0); // 0 for 'en', 1 for 'vi'
        String lang = (languagePos == 1) ? "vi" : "en";

        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration config = new Configuration(newBase.getResources().getConfiguration());
        config.setLocale(locale);

        Context context = newBase.createConfigurationContext(config);
        super.attachBaseContext(context);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int theme = prefs.getInt(KEY_THEME, 2); // Default to System
        int themeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        if (theme == 0) { // Light
            themeMode = AppCompatDelegate.MODE_NIGHT_NO;
        } else if (theme == 1) { // Dark
            themeMode = AppCompatDelegate.MODE_NIGHT_YES;
        }
        AppCompatDelegate.setDefaultNightMode(themeMode);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Window setup
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            return insets;
        });

        // Dirty trick for clean status bar + sidebar, avoids 1 overdraw compared to setting bg
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.lor), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, systemBars.top));
            return insets;
        });

        try {
            PackageInfo pInfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
            ((TextView) findViewById(R.id.version)).setText(" v" + pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // This hides only the nav bar
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        // Configure the behavior of the hidden system bars.
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars());

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        topAppBar = findViewById(R.id.topAppBar);

        // Set toolbar as ActionBar
        setSupportActionBar(topAppBar);

        // Setup drawer toggle (hamburger icon)
        toggle = new ActionBarDrawerToggle(this, drawerLayout, topAppBar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        ImageButton aboutButton = findViewById(R.id.about_button);
        aboutButton.setOnClickListener(v -> {
            AboutDialog bottomSheet = new AboutDialog();
            bottomSheet.show(getSupportFragmentManager(), "AboutBottomSheet");
        });

        // FIXED: Handle both initial launch and rotation
        if (savedInstanceState == null) {
            new Thread(() -> {
                CategoryProvider.getInstance(this);
            }).start();
            // First launch - show subjects
            replaceFragment(new SubjectsFragment());
            getSupportActionBar().setTitle(R.string.nav_subjects);
            navigationView.setCheckedItem(R.id.nav_subjects);
            isSearchMode = false;
        } else {
            // Rotation - restore state
            CharSequence title = savedInstanceState.getCharSequence(KEY_TITLE);
            getSupportActionBar().setTitle(title);

            // ADDED: Check if we were in search mode
            isSearchMode = savedInstanceState.getBoolean(KEY_SEARCH_MODE, false);

            if (isSearchMode) {
                // Restore search mode
                searchShowing = savedInstanceState.getBoolean(KEY_SEARCH_VISIBILITY, true);
                boolean searchIconShowing = savedInstanceState.getBoolean(KEY_SEARCH_ICON_SHOWING, true);

                // Recreate search UI
                prepareContainers();

                // FragmentManager will automatically restore fragments
                // but we need to ensure the containers are ready
                toggleSearchAction(searchIconShowing);
                navigationView.setCheckedItem(R.id.nav_search);
            }
            // For other fragments, FragmentManager will restore them automatically
        }

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            Fragment fragment = null;

            int id = item.getItemId();
            if (id == R.id.nav_subjects) {
                fragment = new SubjectsFragment();
                getSupportActionBar().setTitle(R.string.nav_subjects);
                isSearchMode = false;
            } else if (id == R.id.nav_downloads) {
                fragment = new DownloadFragment();
                getSupportActionBar().setTitle(R.string.nav_downloads);
                isSearchMode = false;
            } else if (id == R.id.nav_bookmarks) {
                fragment = new BookmarkListFragment();
                getSupportActionBar().setTitle(R.string.nav_bookmarks);
                isSearchMode = false;
            } else if (id == R.id.nav_math) {
                fragment = new MathFragment();
                getSupportActionBar().setTitle(R.string.nav_math);
                isSearchMode = false;
            } else if (id == R.id.nav_settings) {
                fragment = new SettingsFragment();
                getSupportActionBar().setTitle(R.string.nav_settings);
                isSearchMode = false;
            } else if (id == R.id.nav_search) {
                handleSearch();
                getSupportActionBar().setTitle(R.string.nav_search);
                toggleSearchAction(true);
                isSearchMode = true;
                drawerLayout.closeDrawers();
                return true; // Early return for search
            }

            if (fragment != null) {
                toggleSearchAction(false);
                replaceFragment(fragment);
                item.setChecked(true);
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return true;
    }

    public void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        ((ViewGroup) findViewById(R.id.content_frame)).removeAllViews();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();
    }

    private void handleSearch() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.remove(fragment).commit();
        }
        prepareContainers();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.searchContainer, new SearchFragment())
                .replace(R.id.resultsContainer, new ArticleListingFragment())
                .commit();
    }

    // Prepares fragment containers for search section
    private void prepareContainers() {
        ViewGroup container = findViewById(R.id.content_frame);
        container.removeAllViews();
        ViewGroup view = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.search_containers, container, false);

        View sContainer = view.findViewById(R.id.searchContainer);
        View rContainer = view.findViewById(R.id.resultsContainer);
        view.removeView(sContainer);
        view.removeView(rContainer);
        container.addView(sContainer);
        container.addView(rContainer);
        findViewById(R.id.searchContainer).setVisibility(searchShowing ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem action = menu.findItem(R.id.action_search);
        action.setVisible(showSearch);
        return super.onPrepareOptionsMenu(menu);
    }

    private void toggleSearchAction(boolean visible) {
        showSearch = visible;
        invalidateOptionsMenu(); // triggers onPrepareOptionsMenu() again
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        if (item.getItemId() == R.id.action_search) {
//            findViewById(R.id.searchContainer).setVisibility(searchShowing ? View.GONE : View.VISIBLE);
            searchShowing = !searchShowing;
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(KEY_TITLE, getSupportActionBar().getTitle());
        outState.putBoolean(KEY_SEARCH_MODE, isSearchMode);
        outState.putBoolean(KEY_SEARCH_VISIBILITY, searchShowing);
        outState.putBoolean(KEY_SEARCH_ICON_SHOWING, showSearch);
    }
}