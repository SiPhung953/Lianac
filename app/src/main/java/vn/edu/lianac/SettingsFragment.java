package vn.edu.lianac;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    // SharedPreferences
    private SharedPreferences sharedPreferences;
    // FIXED: Changed to match MainActivity
    private static final String PREF_NAME = "AppSettings";

    // Keys for SharedPreferences
    // FIXED: Changed to match MainActivity
    private static final String KEY_THEME_MODE = "theme";
    private static final String KEY_COMPACT_LIST = "compact_list";
    private static final String KEY_TEXT_DENSITY = "text_density";
    // FIXED: Changed to match MainActivity
    private static final String KEY_LANGUAGE = "language";

    // UI Components
    private RadioGroup rgThemeMode;
    private SwitchCompat switchCompactList;
    private Spinner spinnerTextDensity, spinnerLanguage;

    // Flags to avoid triggering listeners on initial load
    private boolean isLanguageInitialLoad = true;
    private boolean isTextDensityInitialLoad = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Initialize views
        initViews(view);

        // Setup spinners with data from resources
        setupSpinners();

        // Load saved settings
        loadSettings();

        // Setup listeners
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        rgThemeMode = view.findViewById(R.id.rgThemeMode);
        switchCompactList = view.findViewById(R.id.switchCompactList);
        spinnerTextDensity = view.findViewById(R.id.spinnerTextDensity);
        spinnerLanguage = view.findViewById(R.id.spinnerLanguage);
    }

    private void setupSpinners() {
        // Text Density Spinner - from resource
        ArrayAdapter<CharSequence> textDensityAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.text_density_options,
                android.R.layout.simple_spinner_item
        );
        textDensityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTextDensity.setAdapter(textDensityAdapter);

        // Language Spinner - from resource
        ArrayAdapter<CharSequence> languageAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.language_options,
                android.R.layout.simple_spinner_item
        );
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(languageAdapter);
    }


    private void loadSettings() {
        // FIXED: Load Theme Mode with correct mapping
        // MainActivity uses: 0=Light, 1=Dark, 2=System
        int themeMode = sharedPreferences.getInt(KEY_THEME_MODE, 2);
        switch (themeMode) {
            case 0: // Light
                rgThemeMode.check(R.id.rbLight);
                break;
            case 1: // Dark
                rgThemeMode.check(R.id.rbDark);
                break;
            default: // System
                rgThemeMode.check(R.id.rbSystem);
                break;
        }

        // Load Compact List
        switchCompactList.setChecked(sharedPreferences.getBoolean(KEY_COMPACT_LIST, false));

        // Load Text Density
        int textDensity = sharedPreferences.getInt(KEY_TEXT_DENSITY, 0);
        spinnerTextDensity.setSelection(textDensity);

        // Load Language
        int language = sharedPreferences.getInt(KEY_LANGUAGE, 0);
        spinnerLanguage.setSelection(language);
    }

    private void setupListeners() {
        // FIXED: Theme Mode Listener
        rgThemeMode.setOnCheckedChangeListener((group, checkedId) -> {
            int mode;
            int modeValue;
            if (checkedId == R.id.rbLight) {
                mode = AppCompatDelegate.MODE_NIGHT_NO;
                modeValue = 0; // Light
            } else if (checkedId == R.id.rbDark) {
                mode = AppCompatDelegate.MODE_NIGHT_YES;
                modeValue = 1; // Dark
            } else {
                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                modeValue = 2; // System
            }

            // Save using the same format as MainActivity
            sharedPreferences.edit().putInt(KEY_THEME_MODE, modeValue).apply();
            AppCompatDelegate.setDefaultNightMode(mode);
        });

        // Compact List Listener
        switchCompactList.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_COMPACT_LIST, isChecked).apply();
        });

        // Text Density Listener
        spinnerTextDensity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isTextDensityInitialLoad) {
                    isTextDensityInitialLoad = false;
                    return;
                }

                int currentDensity = sharedPreferences.getInt(KEY_TEXT_DENSITY, 0);
                if (currentDensity != position) {
                    sharedPreferences.edit().putInt(KEY_TEXT_DENSITY, position).apply();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Language Listener
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isLanguageInitialLoad) {
                    isLanguageInitialLoad = false;
                    return;
                }

                int currentLanguage = sharedPreferences.getInt(KEY_LANGUAGE, 0);
                if (currentLanguage != position) {
                    sharedPreferences.edit().putInt(KEY_LANGUAGE, position).apply();
                    changeLanguage(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void changeLanguage(int position) {
        String languageCode = (position == 0) ? "en" : "vi";

        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        requireContext().getResources().updateConfiguration(config, requireContext().getResources().getDisplayMetrics());

        // Show toast using string resources
        String message = (position == 0) ? getString(R.string.language_changed_to_english) : getString(R.string.language_changed_to_vietnamese);
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

        // Restart Activity to apply language change
        new Handler().postDelayed(() -> requireActivity().recreate(), 500);
    }

    // Getter methods for other fragments/activities
    public boolean isCompactListEnabled() {
        return sharedPreferences.getBoolean(KEY_COMPACT_LIST, false);
    }

    public String getTextDensity() {
        int density = sharedPreferences.getInt(KEY_TEXT_DENSITY, 0);
        return density == 0 ? "comfortable" : "compact";
    }
}