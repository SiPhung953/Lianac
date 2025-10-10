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

import android.util.Log;
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
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatDelegate;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import vn.edu.lianac.ui.ArticleListingFragment;
import vn.edu.lianac.ui.SearchFragment;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    MaterialToolbar topAppBar;
    ActionBarDrawerToggle toggle;

    // TODO: fix janky startup animations
    // TODO: Fix sidebar icons
    private FrameLayout searchContainerWrapper;
    private boolean isSearchVisible = false;

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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            PackageInfo pInfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
            ((TextView) findViewById(R.id.version)).setText(" v" + pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // This hides only the nav bar
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        // Configure the behavior of the hidden system bars.
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars());
        searchContainerWrapper = findViewById(R.id.searchContainerWrapper);

        ImageView searchIcon = findViewById(R.id.search_icon);
        searchIcon.setOnClickListener(v -> toggleSearch());

        setupFragments();
    }

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        topAppBar = findViewById(R.id.topAppBar);
    private void setupFragments() {
        searchContainerWrapper.setVisibility(View.GONE);

        // Set toolbar as ActionBar
        setSupportActionBar(topAppBar);
        int verticalPadding = getResources().getDimensionPixelSize(
                com.google.android.material.R.dimen.mtrl_navigation_item_shape_vertical_margin);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.searchContainer, new SearchFragment())
                .commit();

        // Setup drawer toggle (hamburger icon)
        toggle = new ActionBarDrawerToggle(this, drawerLayout, topAppBar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.resultsContainer, new ArticleListingFragment())
                .commit();
    }

        if (savedInstanceState == null) {
            replaceFragment(new SubjectFragment());
            getSupportActionBar().setTitle("Subjects");
            navigationView.setCheckedItem(R.id.nav_subjects);
    private void toggleSearch() {
        if (isSearchVisible) {
            searchContainerWrapper.setVisibility(View.GONE);
            isSearchVisible = false;
        } else {
            getSupportActionBar().setTitle(savedInstanceState.getCharSequence("title"));
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
            }

            if (fragment != null) {
                replaceFragment(fragment);
                item.setChecked(true);
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }

    public void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Giúp animation tốt hơn
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();
            searchContainerWrapper.setVisibility(View.VISIBLE);
            isSearchVisible = true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
            if (toggle.onOptionsItemSelected(item)) {
                return true;
            }
            return super.onOptionsItemSelected(item);
            public void onBackPressed () {
                super.onBackPressed();
            }

            // magic, do not touch
            @Override
            protected void onSaveInstanceState (@NonNull Bundle outState){
                super.onSaveInstanceState(outState);
                outState.putCharSequence("title", getSupportActionBar().getTitle());
            }
        }
