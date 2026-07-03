package com.fsmoking.app.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.fsmoking.app.OnboardingActivity;
import com.fsmoking.app.R;
import com.fsmoking.app.viewmodel.SettingsViewModel;

public class SettingsFragment extends Fragment {

    private SettingsViewModel viewModel;

    private TextInputEditText etPackPrice;
    private TextInputEditText etCigsPerPack;
    private TextInputEditText etDailyGoal;
    private TextInputEditText etCurrency;

    private android.widget.RadioGroup rgTheme;
    private com.fsmoking.app.repository.SettingsRepository settingsRepo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);

        etPackPrice   = view.findViewById(R.id.et_pack_price);
        etCigsPerPack = view.findViewById(R.id.et_cigs_per_pack);
        etDailyGoal   = view.findViewById(R.id.et_daily_goal);
        etCurrency    = view.findViewById(R.id.et_currency);

        MaterialButton btnSave   = view.findViewById(R.id.btn_save_settings);
        MaterialButton btnExport = view.findViewById(R.id.btn_export_csv);
        MaterialButton btnReset  = view.findViewById(R.id.btn_reset_data);

        // Pre-fill current values
        etPackPrice.setText(String.valueOf(viewModel.getPackPrice()));
        etCigsPerPack.setText(String.valueOf(viewModel.getCigsPerPack()));
        etDailyGoal.setText(String.valueOf(viewModel.getDailyGoal()));
        etCurrency.setText(viewModel.getCurrency());

        // Observe results
        viewModel.exportResult.observe(getViewLifecycleOwner(), msg -> {
            if (msg == null) return;
            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
            viewModel.exportResult.setValue(null); // clear so it doesn't re-fire
        });

        viewModel.resetDone.observe(getViewLifecycleOwner(), done -> {
            if (done == null || !done) return;
            viewModel.resetDone.setValue(false); // clear before navigating
            startActivity(new Intent(requireContext(), OnboardingActivity.class));
            requireActivity().finish();
        });

        settingsRepo = new com.fsmoking.app.repository.SettingsRepository(requireContext());
        rgTheme = view.findViewById(R.id.rg_theme);

// Set current selection
        int currentMode = settingsRepo.getThemeMode();
        if (currentMode == 1) rgTheme.check(R.id.rb_theme_light);
        else if (currentMode == 2) rgTheme.check(R.id.rb_theme_dark);
        else rgTheme.check(R.id.rb_theme_system);

// Listen for changes
        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int mode;
            if (checkedId == R.id.rb_theme_light) mode = 1;
            else if (checkedId == R.id.rb_theme_dark) mode = 2;
            else mode = 0;

            settingsRepo.setThemeMode(mode);

            // Apply immediately
            switch (mode) {
                case 1:
                    androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
                    break;
                case 2:
                    androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
                    break;
                default:
                    androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    break;
            }
        });

        btnSave.setOnClickListener(v -> saveSettings());

        btnExport.setOnClickListener(v ->
                viewModel.exportCsv(requireContext()));

        btnReset.setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Reset all data")
                        .setMessage("This will permanently erase all your cigarette history and achievements. This cannot be undone.")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Reset", (d, w) -> viewModel.resetAllData())
                        .show());
    }

    private void saveSettings() {
        try {
            float price = Float.parseFloat(
                    etPackPrice.getText().toString().trim().isEmpty()
                            ? "10" : etPackPrice.getText().toString().trim());
            int perPack = Integer.parseInt(
                    etCigsPerPack.getText().toString().trim().isEmpty()
                            ? "20" : etCigsPerPack.getText().toString().trim());
            int goal = Integer.parseInt(
                    etDailyGoal.getText().toString().trim().isEmpty()
                            ? "10" : etDailyGoal.getText().toString().trim());
            String currency = etCurrency.getText().toString();

            viewModel.saveSettings(price, perPack, goal, currency);
            Toast.makeText(requireContext(), "Settings saved ✓", Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(),
                    "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }
}