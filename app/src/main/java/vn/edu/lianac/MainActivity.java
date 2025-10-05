package vn.edu.lianac;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import vn.edu.lianac.R;

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
