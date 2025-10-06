package vn.edu.lianac;

import androidx.fragment.app.FragmentManager;

import vn.edu.lianac.Download.DownloadFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            DownloadFragment downloadFragment = new DownloadFragment();
            ft.replace(R.id.fragment_container, downloadFragment);
            ft.commit();
        }
    }
}
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import vn.edu.lianac.bookmark.BookmarkListFragment;
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
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    MaterialToolbar topAppBar;
    ActionBarDrawerToggle toggle;

    // TODO: fix janky startup animations
    // TODO: Fix sidebar icons, change to smaller sidebar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);
        int themeMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
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

        if (savedInstanceState == null) {
            replaceFragment(new SubjectFragment());
            getSupportActionBar().setTitle("Subjects");
            navigationView.setCheckedItem(R.id.nav_subjects);
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
//                TODO: make this button work once downloads stuff is ready
//                getSupportActionBar().setTitle("Downloacds");
//                fragment = new DownloadsFragment();
            } else if (id == R.id.nav_bookmarks) {
                getSupportActionBar().setTitle("Bookmarks");
                fragment = new BookmarkListFragment();
            } else if (id == R.id.nav_settings) {
                getSupportActionBar().setTitle("Settings");
                fragment = new SettingsFragment();
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
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("title", getSupportActionBar().getTitle());
    }
}
