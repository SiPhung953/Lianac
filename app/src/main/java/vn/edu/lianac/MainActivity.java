package vn.edu.lianac;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import vn.edu.lianac.R;
import vn.edu.lianac.bookmark.BookmarkListFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    MaterialToolbar topAppBar;
    ActionBarDrawerToggle toggle;

    // TODO: fix janky startup animations
    // TODO: Fix sidebar icons, change to smaller sidebar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Fragment pager = new CollectionFragment();
//        getSupportFragmentManager().beginTransaction()
//                .add(R.id.pager, pager)
//                .commit();
//        ViewPager2 pager = findViewById(R.id.pager);
//        FragmentAdapter adapter = new FragmentAdapter(this);
//        pager.setAdapter(adapter);
//        TabLayout tabLayout = findViewById(R.id.tab_layout);
//        new TabLayoutMediator(tabLayout, pager, (tab, position) -> tab.setText("Page " + (position + 1))).attach();
//        for (StackTraceElement stelement : Thread.currentThread().getStackTrace()) {
//            Log.i("STLog", stelement.toString());
//        }
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
        replaceFragment(new SubjectFragment());
        getSupportActionBar().setTitle("Subjects");

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

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
//        // TODO: Someone should fix this
//        // Hiển thị SettingsFragment
//        if (savedInstanceState == null) {
//            SettingsFragment settingsFragment = new SettingsFragment();
//            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//            transaction.replace(R.id.fragment_container, settingsFragment);
//            transaction.commit();
//        }
//    }
//}
