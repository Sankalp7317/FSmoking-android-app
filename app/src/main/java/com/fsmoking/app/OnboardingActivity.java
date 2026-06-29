package com.fsmoking.app;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.fsmoking.app.models.UserData;
import com.fsmoking.app.utils.AppStorage;
import java.util.Calendar;
import java.util.Locale;

public class OnboardingActivity extends AppCompatActivity {

    private int currentStep = 0;
    private String selectedDate = "";
    private String selectedTime = "";

    private TextView tvSelectedDate, tvSelectedTime;
    private TextInputEditText etCigsPerDay, etPackSize, etCurrency, etPackCost;
    private MaterialButton btnNext, btnBack;
    private LinearLayout step1, step2, step3;
    private View dot1, dot2, dot3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Bind views
        step1 = findViewById(R.id.step_1);
        step2 = findViewById(R.id.step_2);
        step3 = findViewById(R.id.step_3);

        dot1 = findViewById(R.id.dot_1);
        dot2 = findViewById(R.id.dot_2);
        dot3 = findViewById(R.id.dot_3);

        tvSelectedDate = findViewById(R.id.tv_selected_date);
        tvSelectedTime = findViewById(R.id.tv_selected_time);
        etCigsPerDay   = findViewById(R.id.et_cigs_per_day);
        etPackSize     = findViewById(R.id.et_pack_size);
        etCurrency     = findViewById(R.id.et_currency);
        etPackCost     = findViewById(R.id.et_pack_cost);
        btnNext        = findViewById(R.id.btn_next);
        btnBack        = findViewById(R.id.btn_back);

        // Set default date/time to now
        Calendar now = Calendar.getInstance();
        selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH) + 1,
                now.get(Calendar.DAY_OF_MONTH));
        selectedTime = String.format(Locale.getDefault(), "%02d:%02d",
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE));
        tvSelectedDate.setText(selectedDate);
        tvSelectedTime.setText(selectedTime);

        // Date picker
        findViewById(R.id.btn_pick_date).setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) -> {
                selectedDate = String.format(Locale.getDefault(),
                        "%04d-%02d-%02d", y, m + 1, d);
                tvSelectedDate.setText(selectedDate);
            }, c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Time picker
        findViewById(R.id.btn_pick_time).setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this, (view, h, min) -> {
                selectedTime = String.format(Locale.getDefault(),
                        "%02d:%02d", h, min);
                tvSelectedTime.setText(selectedTime);
            }, c.get(Calendar.HOUR_OF_DAY),
                    c.get(Calendar.MINUTE), true).show();
        });

        btnNext.setOnClickListener(v -> nextStep());
        btnBack.setOnClickListener(v -> prevStep());

        updateUI();
    }

    private void nextStep() {
        if (currentStep == 2) {
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
        // Show correct step
        step1.setVisibility(currentStep == 0 ? View.VISIBLE : View.GONE);
        step2.setVisibility(currentStep == 1 ? View.VISIBLE : View.GONE);
        step3.setVisibility(currentStep == 2 ? View.VISIBLE : View.GONE);

        // Update dots
        dot1.setBackgroundResource(currentStep == 0 ? R.drawable.dot_active :
                (currentStep > 0 ? R.drawable.dot_done : R.drawable.dot_inactive));
        dot2.setBackgroundResource(currentStep == 1 ? R.drawable.dot_active :
                (currentStep > 1 ? R.drawable.dot_done : R.drawable.dot_inactive));
        dot3.setBackgroundResource(currentStep == 2 ? R.drawable.dot_active : R.drawable.dot_inactive);

        // Update buttons
        btnBack.setVisibility(currentStep > 0 ? View.VISIBLE : View.GONE);
        btnNext.setText(currentStep == 2 ?
                getString(R.string.onb_start) : getString(R.string.onb_next));
    }

    private void saveAndFinish() {
        try {
            String quitDateTime = selectedDate + " " + selectedTime;

            float cpd  = Float.parseFloat(
                    etCigsPerDay.getText().toString().trim().isEmpty()
                            ? "20" : etCigsPerDay.getText().toString().trim());
            float ps   = Float.parseFloat(
                    etPackSize.getText().toString().trim().isEmpty()
                            ? "20" : etPackSize.getText().toString().trim());
            float cost = Float.parseFloat(
                    etPackCost.getText().toString().trim().isEmpty()
                            ? "10" : etPackCost.getText().toString().trim());
            String cur = etCurrency.getText().toString().trim().isEmpty()
                    ? "₹" : etCurrency.getText().toString().trim();

            UserData data = new UserData(quitDateTime, cpd, ps, cost, cur);
            new AppStorage(this).saveUserData(data);

            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Please fill in all fields correctly",
                    Toast.LENGTH_SHORT).show();
        }
    }
}