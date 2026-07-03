package com.fsmoking.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.fsmoking.app.notifications.NotificationHelper;
import com.fsmoking.app.notifications.NotificationScheduler;
import com.fsmoking.app.repository.SettingsRepository;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SettingsRepository settings = new SettingsRepository(this);
        applyTheme(settings.getThemeMode());

        setContentView(R.layout.activity_splash);

        TextView logo    = findViewById(R.id.tv_logo);
        TextView appName = findViewById(R.id.tv_app_name);
        TextView tagline = findViewById(R.id.tv_tagline);

        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(900);
        logo.startAnimation(fadeIn);
        appName.startAnimation(fadeIn);
        tagline.startAnimation(fadeIn);

        // Set up notification channel and schedule workers
        NotificationHelper.createChannel(this);
        NotificationScheduler.scheduleAll(this);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SettingsRepository settings1 = new SettingsRepository(this);
            Intent intent = settings1.isOnboardingDone()
                    ? new Intent(this, MainActivity.class)
                    : new Intent(this, OnboardingActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 2000);
    }

    private void applyTheme(int mode) {
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
    }
}