package vn.edu.lianac;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);
        int themeMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hiển thị SettingsFragment
        if (savedInstanceState == null) {
            SettingsFragment settingsFragment = new SettingsFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, settingsFragment);
            transaction.commit();
        }
    }
}