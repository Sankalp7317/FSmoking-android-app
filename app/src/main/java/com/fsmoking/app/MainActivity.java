package com.fsmoking.app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.fsmoking.app.ui.achievements.AchievementsFragment;
import com.fsmoking.app.ui.dashboard.DashboardFragment;
import com.fsmoking.app.ui.health.HealthFragment;
import com.fsmoking.app.ui.history.HistoryFragment;
import com.fsmoking.app.ui.settings.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment;
            int id = item.getItemId();
            if (id == R.id.nav_history) {
                fragment = new HistoryFragment();
            } else if (id == R.id.nav_health) {
                fragment = new HealthFragment();
            } else if (id == R.id.nav_achievements) {
                fragment = new AchievementsFragment();
            } else if (id == R.id.nav_settings) {
                fragment = new SettingsFragment();
            } else {
                fragment = new DashboardFragment();
            }
            loadFragment(fragment);
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void navigateTo(Fragment fragment) {
        loadFragment(fragment);
    }
}