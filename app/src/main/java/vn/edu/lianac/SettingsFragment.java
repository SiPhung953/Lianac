package vn.edu.lianac;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
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
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    // SharedPreferences
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "SettingsPrefs";

    // Keys for SharedPreferences
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_COMPACT_LIST = "compact_list";
    private static final String KEY_TEXT_DENSITY = "text_density";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_DEFAULT_VIEWER = "default_viewer";
    private static final String KEY_SCROLL_MODE = "scroll_mode";
    private static final String KEY_ANNOTATIONS = "annotations";
    private static final String KEY_AUTO_SAVE = "auto_save";

    // UI Components
    private RadioGroup rgThemeMode, rgScrollMode;
    private SwitchCompat switchCompactList, switchAnnotations, switchAutoSave;
    private Spinner spinnerTextDensity, spinnerLanguage, spinnerDefaultViewer;
    private View layoutInAppSettings;

    // Flags để tránh trigger listeners khi load settings lần đầu
    private boolean isLanguageInitialLoad = true;
    private boolean isTextDensityInitialLoad = true;
    private boolean isViewerInitialLoad = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Initialize views
        initViews(view);

        // Setup spinners
        setupSpinners();

        // Load saved settings
        loadSettings();

        // Setup listeners
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        rgThemeMode = view.findViewById(R.id.rgThemeMode);
        rgScrollMode = view.findViewById(R.id.rgScrollMode);

        switchCompactList = view.findViewById(R.id.switchCompactList);
        switchAnnotations = view.findViewById(R.id.switchAnnotations);
        switchAutoSave = view.findViewById(R.id.switchAutoSave);

        spinnerTextDensity = view.findViewById(R.id.spinnerTextDensity);
        spinnerLanguage = view.findViewById(R.id.spinnerLanguage);
        spinnerDefaultViewer = view.findViewById(R.id.spinnerDefaultViewer);

        layoutInAppSettings = view.findViewById(R.id.layoutInAppSettings);
    }

    private void setupSpinners() {
        // Text Density Spinner
        String[] textDensityOptions = {"Comfortable", "Compact"};
        ArrayAdapter<String> textDensityAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                textDensityOptions
        );
        textDensityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTextDensity.setAdapter(textDensityAdapter);

        // Language Spinner
        String[] languageOptions = {"English (EN)", "Tiếng Việt (VN)"};
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                languageOptions
        );
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(languageAdapter);

        // Default Viewer Spinner
        String[] viewerOptions = {"In-app", "External PDF app"};
        ArrayAdapter<String> viewerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                viewerOptions
        );
        viewerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDefaultViewer.setAdapter(viewerAdapter);
    }

    private void loadSettings() {
        // Load Theme Mode
        int themeMode = sharedPreferences.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        switch (themeMode) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                rgThemeMode.check(R.id.rbLight);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                rgThemeMode.check(R.id.rbDark);
                break;
            default:
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

        // Load Default Viewer
        int viewer = sharedPreferences.getInt(KEY_DEFAULT_VIEWER, 0);
        spinnerDefaultViewer.setSelection(viewer);
        layoutInAppSettings.setVisibility(viewer == 0 ? View.VISIBLE : View.GONE);

        // Load Scroll Mode
        String scrollMode = sharedPreferences.getString(KEY_SCROLL_MODE, "horizontal");
        if (scrollMode.equals("horizontal")) {
            rgScrollMode.check(R.id.rbHorizontal);
        } else {
            rgScrollMode.check(R.id.rbVertical);
        }

        // Load Annotations
        switchAnnotations.setChecked(sharedPreferences.getBoolean(KEY_ANNOTATIONS, true));

        // Load Auto-save
        switchAutoSave.setChecked(sharedPreferences.getBoolean(KEY_AUTO_SAVE, true));
    }

    private void setupListeners() {
        // Theme Mode Listener
        rgThemeMode.setOnCheckedChangeListener((group, checkedId) -> {
            int mode;
            if (checkedId == R.id.rbLight) {
                mode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.rbDark) {
                mode = AppCompatDelegate.MODE_NIGHT_YES;
            } else {
                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }

            sharedPreferences.edit().putInt(KEY_THEME_MODE, mode).apply();
            AppCompatDelegate.setDefaultNightMode(mode);
        });

        // Compact List Listener
        switchCompactList.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_COMPACT_LIST, isChecked).apply();
            // Bỏ Toast nếu không cần thiết
            // Toast.makeText(requireContext(), "Compact list: " + (isChecked ? "ON" : "OFF"),
            //         Toast.LENGTH_SHORT).show();
        });

        // Text Density Listener
        spinnerTextDensity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Bỏ qua lần đầu tiên (khi load settings)
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

        // Language Listener - ✅ FIX: Không hiện toast khi load lần đầu
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Bỏ qua lần đầu tiên (khi load settings)
                if (isLanguageInitialLoad) {
                    isLanguageInitialLoad = false;
                    return;
                }

                // Kiểm tra xem language có thực sự thay đổi không
                int currentLanguage = sharedPreferences.getInt(KEY_LANGUAGE, 0);
                if (currentLanguage != position) {
                    sharedPreferences.edit().putInt(KEY_LANGUAGE, position).apply();
                    changeLanguage(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Default Viewer Listener
        spinnerDefaultViewer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Bỏ qua lần đầu tiên (khi load settings)
                if (isViewerInitialLoad) {
                    isViewerInitialLoad = false;
                    return;
                }

                int currentViewer = sharedPreferences.getInt(KEY_DEFAULT_VIEWER, 0);
                if (currentViewer != position) {
                    sharedPreferences.edit().putInt(KEY_DEFAULT_VIEWER, position).apply();

                    if (position == 0) {
                        // In-app viewer selected
                        layoutInAppSettings.setVisibility(View.VISIBLE);
                    } else {
                        // External PDF app selected
                        layoutInAppSettings.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Scroll Mode Listener
        rgScrollMode.setOnCheckedChangeListener((group, checkedId) -> {
            String mode = (checkedId == R.id.rbHorizontal) ? "horizontal" : "vertical";
            sharedPreferences.edit().putString(KEY_SCROLL_MODE, mode).apply();
        });

        // Annotations Listener
        switchAnnotations.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_ANNOTATIONS, isChecked).apply();
        });

        // Auto-save Listener
        switchAutoSave.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_AUTO_SAVE, isChecked).apply();
        });
    }

    private void changeLanguage(int position) {
        String languageCode = (position == 0) ? "en" : "vi";

        // Lưu language preference
        sharedPreferences.edit().putString("app_language", languageCode).apply();

        // Đổi locale
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }

        requireContext().getResources().updateConfiguration(config,
                requireContext().getResources().getDisplayMetrics());

        // Hiển thị toast TRƯỚC KHI restart
        String message = position == 0 ? "Language changed to English" : "Ngôn ngữ đã đổi sang Tiếng Việt";
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

        // RESTART Activity sau một chút delay để toast hiển thị
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requireActivity().recreate();
            }
        }, 500); // 500ms delay
    }

    // Method để mở PDF với External App - ✅ FIX: Hỗ trợ FileProvider cho Android 7.0+
    public void openPDFWithExternalApp(String pdfPath) {
        int viewerPreference = sharedPreferences.getInt(KEY_DEFAULT_VIEWER, 0);

        if (viewerPreference == 1) { // External PDF app
            File file = new File(pdfPath);

            // Sử dụng FileProvider cho Android 7.0+
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().getPackageName() + ".fileprovider",
                        file
                );
            } else {
                uri = Uri.fromFile(file);
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Kiểm tra xem có app nào có thể mở PDF không
            PackageManager pm = requireContext().getPackageManager();
            List<ResolveInfo> activities = pm.queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);

            if (activities.size() > 0) {
                // Tạo chooser để người dùng chọn app
                Intent chooser = Intent.createChooser(intent, "Open PDF with");
                startActivity(chooser);
            } else {
                Toast.makeText(requireContext(),
                        "No PDF reader app found. Please install one.",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            // Mở bằng In-app viewer
            // Implement your in-app PDF viewer here
            Toast.makeText(requireContext(),
                    "Opening with in-app viewer",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Getter methods để các fragment/activity khác có thể sử dụng settings
    public boolean isCompactListEnabled() {
        return sharedPreferences.getBoolean(KEY_COMPACT_LIST, false);
    }

    public boolean isAnnotationsEnabled() {
        return sharedPreferences.getBoolean(KEY_ANNOTATIONS, true);
    }

    public boolean isAutoSaveEnabled() {
        return sharedPreferences.getBoolean(KEY_AUTO_SAVE, true);
    }

    public String getScrollMode() {
        return sharedPreferences.getString(KEY_SCROLL_MODE, "horizontal");
    }

    public int getDefaultViewer() {
        return sharedPreferences.getInt(KEY_DEFAULT_VIEWER, 0);
    }

    public String getTextDensity() {
        int density = sharedPreferences.getInt(KEY_TEXT_DENSITY, 0);
        return density == 0 ? "comfortable" : "compact";
    }
}