package vn.edu.lianac;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

public class SettingFragment extends Fragment {

    private RadioGroup rgThemeMode;
    private Spinner spinnerTextDensity, spinnerLanguage, spinnerViewer, spinnerScroll;
    private Switch switchCompact, switchAnnotations, switchAutosave;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        rgThemeMode = view.findViewById(R.id.rg_theme_mode);
        spinnerTextDensity = view.findViewById(R.id.spinner_text_density);
        spinnerLanguage = view.findViewById(R.id.spinner_language);
        spinnerViewer = view.findViewById(R.id.spinner_viewer);
        spinnerScroll = view.findViewById(R.id.spinner_scroll);
        switchCompact = view.findViewById(R.id.switch_compact);
        switchAnnotations = view.findViewById(R.id.switch_annotations);
        switchAutosave = view.findViewById(R.id.switch_autosave);

        setupSpinner(spinnerTextDensity, R.array.text_density_options);
        setupSpinner(spinnerLanguage, R.array.language_options);
        setupSpinner(spinnerViewer, R.array.viewer_options);
        setupSpinner(spinnerScroll, R.array.scroll_options);

        rgThemeMode.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_light) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else if (checkedId == R.id.rb_dark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        });

        return view;
    }

    private void setupSpinner(Spinner spinner, int arrayResId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                arrayResId,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
}