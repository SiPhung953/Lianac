package vn.edu.lianac;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class SettingsFragment extends Fragment {


    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "AppSettings";
    private static final String KEY_THEME_MODE = "theme";
    private static final String KEY_LANGUAGE = "language";
    private RadioGroup rgThemeMode;
    private Spinner spinnerLanguage;
    private boolean isLanguageInitialLoad = true;
    private boolean isChangingLanguage = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        initViews(view);

        setupSpinners();

        loadSettings();

        setupListeners();

        return view;
    }

    private void initViews(View view) {
        rgThemeMode = view.findViewById(R.id.rgThemeMode);
        spinnerLanguage = view.findViewById(R.id.spinnerLanguage);
    }

    private void setupSpinners() {

        ArrayAdapter<CharSequence> languageAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.language_options,
                android.R.layout.simple_spinner_item
        );
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(languageAdapter);
    }

    private void loadSettings() {

        int themeMode = sharedPreferences.getInt(KEY_THEME_MODE, 2);
        switch (themeMode) {
            case 0:
                rgThemeMode.check(R.id.rbLight);
                break;
            case 1:
                rgThemeMode.check(R.id.rbDark);
                break;
            default:
                rgThemeMode.check(R.id.rbSystem);
                break;
        }


        int language = sharedPreferences.getInt(KEY_LANGUAGE, 0);
        isLanguageInitialLoad = true;
        spinnerLanguage.setSelection(language, false);
        isLanguageInitialLoad = false;
    }

    private void setupListeners() {

        rgThemeMode.setOnCheckedChangeListener((group, checkedId) -> {
            int mode;
            int modeValue;
            if (checkedId == R.id.rbLight) {
                mode = AppCompatDelegate.MODE_NIGHT_NO;
                modeValue = 0;
            } else if (checkedId == R.id.rbDark) {
                mode = AppCompatDelegate.MODE_NIGHT_YES;
                modeValue = 1;
            } else {
                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                modeValue = 2;
            }

            sharedPreferences.edit().putInt(KEY_THEME_MODE, modeValue).apply();
            AppCompatDelegate.setDefaultNightMode(mode);
        });


        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isLanguageInitialLoad || isChangingLanguage) {
                    return;
                }

                int currentLanguage = sharedPreferences.getInt(KEY_LANGUAGE, 0);
                if (currentLanguage != position) {
                    changeLanguage(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void changeLanguage(int position) {
        isChangingLanguage = true;


        sharedPreferences.edit().putInt(KEY_LANGUAGE, position).apply();

        String languageCode = (position == 0) ? "en" : "vi";
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);
        requireContext().getResources().updateConfiguration(config, requireContext().getResources().getDisplayMetrics());

        String message = (position == 0) ? getString(R.string.language_changed_to_english) : getString(R.string.language_changed_to_vietnamese);
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();


        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isChangingLanguage = false;
            requireActivity().recreate();
        }, 800);
    }
}