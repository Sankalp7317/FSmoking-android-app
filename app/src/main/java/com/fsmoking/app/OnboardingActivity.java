package com.fsmoking.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.fsmoking.app.repository.SettingsRepository;

public class OnboardingActivity extends AppCompatActivity {

    private int currentStep = 0;

    private TextInputEditText etCigsPerDay, etPackSize, etCurrency, etPackCost, etDailyGoal;
    private MaterialButton btnNext, btnBack;
    private LinearLayout step1, step2;
    private View dot1, dot2;

    private SettingsRepository settingsRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        settingsRepository = new SettingsRepository(this);

        step1 = findViewById(R.id.step_1);
        step2 = findViewById(R.id.step_2);

        dot1 = findViewById(R.id.dot_1);
        dot2 = findViewById(R.id.dot_2);

        etCigsPerDay = findViewById(R.id.et_cigs_per_day);
        etPackSize   = findViewById(R.id.et_pack_size);
        etDailyGoal  = findViewById(R.id.et_daily_goal);
        etCurrency   = findViewById(R.id.et_currency);
        etPackCost   = findViewById(R.id.et_pack_cost);

        btnNext = findViewById(R.id.btn_next);
        btnBack = findViewById(R.id.btn_back);

        btnNext.setOnClickListener(v -> nextStep());
        btnBack.setOnClickListener(v -> prevStep());

        updateUI();
    }

    private void nextStep() {
        if (currentStep == 1) {
            saveAndFinish();
            return;
        }
        currentStep++;
        updateUI();
    }

    private void prevStep() {
        if (currentStep == 0) return;
        currentStep--;
        updateUI();
    }

    private void updateUI() {
        step1.setVisibility(currentStep == 0 ? View.VISIBLE : View.GONE);
        step2.setVisibility(currentStep == 1 ? View.VISIBLE : View.GONE);

        dot1.setBackgroundResource(currentStep == 0 ? R.drawable.dot_active : R.drawable.dot_done);
        dot2.setBackgroundResource(currentStep == 1 ? R.drawable.dot_active : R.drawable.dot_inactive);

        btnBack.setVisibility(currentStep > 0 ? View.VISIBLE : View.GONE);
        btnNext.setText(currentStep == 1 ? getString(R.string.onb_start) : getString(R.string.onb_next));
    }

    private void saveAndFinish() {
        try {
            float cpd  = parseOrDefault(etCigsPerDay, 20f);
            float ps   = parseOrDefault(etPackSize, 20f);
            float cost = parseOrDefault(etPackCost, 10f);
            int goal   = (int) parseOrDefault(etDailyGoal, 10f);
            String cur = etCurrency.getText().toString().trim().isEmpty()
                    ? "₹" : etCurrency.getText().toString().trim();

            settingsRepository.setCigsPerPack((int) ps);
            settingsRepository.setPackPrice(cost);
            settingsRepository.setDailyGoal(goal);
            settingsRepository.setCurrency(cur);
            settingsRepository.setQuitDateMillis(System.currentTimeMillis());
            settingsRepository.setOnboardingDone(true);

            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Please fill in all fields correctly", Toast.LENGTH_SHORT).show();
        }
    }

    private float parseOrDefault(TextInputEditText field, float fallback) {
        String text = field.getText() != null ? field.getText().toString().trim() : "";
        if (text.isEmpty()) return fallback;
        try {
            return Float.parseFloat(text);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}