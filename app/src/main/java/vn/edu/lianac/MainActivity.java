package vn.edu.lianac;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import vn.edu.lianac.R;

public class MainActivity extends AppCompatActivity {
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new StartupFragment())
                    .commit();
        }
    }
}
        // Hiển thị SettingsFragment
        if (savedInstanceState == null) {
            SettingsFragment settingsFragment = new SettingsFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, settingsFragment);
            transaction.commit();
        }
    }
}
