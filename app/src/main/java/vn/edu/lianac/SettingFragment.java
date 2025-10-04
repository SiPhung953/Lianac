package vn.edu.lianac;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatDelegate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Spinner;
import android.widget.SeekBar;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.CompoundButton;

public class SettingFragment extends Fragment {

    private RadioGroup rgTheme, rgReadMode;
    private Switch switchCompactList, switchReflow, switchContinuous,
            switchNightMode, switchAnnotations, switchAutosave,
            switchTTS, switchHighContrast;
    private Spinner spinnerTextDensity, spinnerLanguage,
            spinnerViewer, spinnerPageFit;
    private SeekBar seekbarFontSize;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        // ========== Ánh xạ View ==========
        rgTheme = view.findViewById(R.id.rg_theme);
        switchCompactList = view.findViewById(R.id.switch_compact_list);
        spinnerTextDensity = view.findViewById(R.id.spinner_text_density);
        spinnerLanguage = view.findViewById(R.id.spinner_language);
        spinnerViewer = view.findViewById(R.id.spinner_viewer);
        switchReflow = view.findViewById(R.id.switch_reflow);
        switchContinuous = view.findViewById(R.id.switch_continuous);
        rgReadMode = view.findViewById(R.id.rg_read_mode);
        seekbarFontSize = view.findViewById(R.id.seekbar_font_size);
        switchNightMode = view.findViewById(R.id.switch_nightmode);
        switchAnnotations = view.findViewById(R.id.switch_annotations);
        switchAutosave = view.findViewById(R.id.switch_autosave);
        spinnerPageFit = view.findViewById(R.id.spinner_page_fit);
        switchTTS = view.findViewById(R.id.switch_tts);
        switchHighContrast = view.findViewById(R.id.switch_high_contrast);

        // ========== Theme ==========
        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_light) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Toast.makeText(getContext(), "Light theme", Toast.LENGTH_SHORT).show();
            } else if (checkedId == R.id.rb_dark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                Toast.makeText(getContext(), "Dark theme", Toast.LENGTH_SHORT).show();
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                Toast.makeText(getContext(), "System theme", Toast.LENGTH_SHORT).show();
            }
        });

        // ========== Switch listeners ==========
        switchCompactList.setOnCheckedChangeListener(this::showSwitchToast);
        switchReflow.setOnCheckedChangeListener(this::showSwitchToast);
        switchContinuous.setOnCheckedChangeListener(this::showSwitchToast);
        switchNightMode.setOnCheckedChangeListener(this::showSwitchToast);
        switchAnnotations.setOnCheckedChangeListener(this::showSwitchToast);
        switchAutosave.setOnCheckedChangeListener(this::showSwitchToast);
        switchTTS.setOnCheckedChangeListener(this::showSwitchToast);
        switchHighContrast.setOnCheckedChangeListener(this::showSwitchToast);

        // ========== Spinner setup ==========
        setupSpinner(spinnerTextDensity, R.array.text_density_options);
        setupSpinner(spinnerLanguage, R.array.language_options);
        setupSpinner(spinnerViewer, R.array.pdf_viewer_options);
        setupSpinner(spinnerPageFit, R.array.page_fit_options);

        // ========== Read mode ==========
        rgReadMode.setOnCheckedChangeListener((group, checkedId) -> {
            String mode = "PDF";
            if (checkedId == R.id.rb_text_only) mode = "Text-only";
            else if (checkedId == R.id.rb_structured) mode = "Structured";
            Toast.makeText(getContext(), "Read mode: " + mode, Toast.LENGTH_SHORT).show();
        });

        // ========== Font size ==========
        seekbarFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Toast.makeText(getContext(), "Font size: " + progress, Toast.LENGTH_SHORT).show();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        return view;
    }

    // ====== Hàm phụ ======
    private void setupSpinner(Spinner spinner, int arrayRes) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                arrayRes,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void showSwitchToast(CompoundButton button, boolean isChecked) {
        String label = button.getResources().getResourceEntryName(button.getId());
        Toast.makeText(getContext(), label + " " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
    }
}
