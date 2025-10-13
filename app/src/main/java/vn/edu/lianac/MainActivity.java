package vn.edu.lianac;

import androidx.fragment.app.FragmentManager;
import vn.edu.lianac.Download.DownloadFragment;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import vn.edu.lianac.bookmark.BookmarkListFragment;

import vn.edu.lianac.R;
import vn.edu.lianac.ui.ArticleListingFragment;
import vn.edu.lianac.ui.SearchFragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;


public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    MaterialToolbar topAppBar;
    ActionBarDrawerToggle toggle;
    private boolean showSearch = false;
    private boolean searchShowing = true;

    // TODO: fix janky startup animations
    // TODO: Fix sidebar icons

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);
        int themeMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
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
        // Development purposes, ignore the cannot resolve symbol R warning its wrong
        int verticalPadding = getResources().getDimensionPixelSize(
                com.google.android.material.R.dimen.mtrl_navigation_item_shape_vertical_margin);

        // Setup drawer toggle (hamburger icon)
        toggle = new ActionBarDrawerToggle(this, drawerLayout, topAppBar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            replaceFragment(new SubjectFragment());
            getSupportActionBar().setTitle("Subjects");
            navigationView.setCheckedItem(R.id.nav_subjects);
        } else {
            CharSequence title = savedInstanceState.getCharSequence("title");
            getSupportActionBar().setTitle(title);
            if (savedInstanceState.getCharSequence("title").toString().equals("Search")) {
                searchShowing = savedInstanceState.getBoolean("searchVisibility");
                prepareContainers();
                toggleSearchAction(savedInstanceState.getBoolean("searchIconShowing"));
            }
        }

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            Fragment fragment = null;

            int id = item.getItemId();
            if (id == R.id.nav_subjects) {
                fragment = new SubjectFragment();
                getSupportActionBar().setTitle("Subjects");
            } else if (id == R.id.nav_downloads) {
                fragment = new DownloadFragment();
                getSupportActionBar().setTitle("Downloads");
            } else if (id == R.id.nav_bookmarks) {
                fragment = new BookmarkListFragment();
                getSupportActionBar().setTitle("Bookmarks");
            } else if (id == R.id.nav_math) { // Xử lý sự kiện click cho item mới (Math)
                fragment = new MathFragment();
                getSupportActionBar().setTitle("Math");
            } else if (id == R.id.nav_settings) {
                fragment = new SettingsFragment();
                getSupportActionBar().setTitle("Settings");
            } else if (id == R.id.nav_search) {
                handleSearch();
                getSupportActionBar().setTitle("Search");
                toggleSearchAction(true);
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
        // really not sure why this is needed given that transaction.replace should already do this
        // internally, some1 with spare time plaese educate me on this
        ((ViewGroup) findViewById(R.id.content_frame)).removeAllViews();
        // Giúp animation tốt hơn
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();
    }

    // TODO: investigate performance impact
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
            findViewById(R.id.searchContainer).setVisibility(searchShowing ? View.GONE : View.VISIBLE);
            searchShowing = !searchShowing;
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

     // magic, do not touch
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("title", getSupportActionBar().getTitle());
        // I am touching magic
        outState.putBoolean("searchVisibility", searchShowing);
        outState.putBoolean("searchIconShowing", showSearch);
    }
}
