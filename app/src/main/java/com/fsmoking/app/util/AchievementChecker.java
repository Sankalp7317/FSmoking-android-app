package com.fsmoking.app.util;

import android.content.Context;
import com.fsmoking.app.data.AppDatabase;
import com.fsmoking.app.data.dao.AchievementDao;
import com.fsmoking.app.data.dao.CigaretteDao;
import com.fsmoking.app.data.entity.Achievement;
import com.fsmoking.app.repository.SettingsRepository;

/**
 * Evaluates all achievement unlock conditions and updates the database.
 * Always call from a background thread.
 */
public class AchievementChecker {

    private final AchievementDao achievementDao;
    private final CigaretteDao cigaretteDao;
    private final SettingsRepository settingsRepository;

    public AchievementChecker(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        achievementDao = db.achievementDao();
        cigaretteDao   = db.cigaretteDao();
        settingsRepository = new SettingsRepository(context);
    }

    public void checkAll() {
        long now = System.currentTimeMillis();
        long quitMillis = settingsRepository.getQuitDateMillis();
        long smokeFreeMins = (now - quitMillis) / 60000;

        // Total all-time logs
        int totalLogs = cigaretteDao.getTotalCountSync(0, now);

        // Today's count
        long todayStart = DateUtils.startOfTodayMillis();
        long todayEnd   = DateUtils.endOfTodayMillis();
        int todayCount  = cigaretteDao.getCountBetweenSync(todayStart, todayEnd);

        int dailyGoal = settingsRepository.getDailyGoal();

        // Smoke-free days
        double smokeFreeDays = smokeFreeMins / 1440.0;

        // Money saved
        double costPerCig = settingsRepository.getPackPrice()
                / (double) settingsRepository.getCigsPerPack();
        double moneySaved = smokeFreeDays * settingsRepository.getDailyGoal() * costPerCig;

        checkAndUnlock("first_log",
                totalLogs >= 1, now);

        checkAndUnlock("smoke_free_1h",
                smokeFreeMins >= 60, now);

        checkAndUnlock("smoke_free_24h",
                smokeFreeMins >= 1440, now);

        checkAndUnlock("smoke_free_1week",
                smokeFreeDays >= 7, now);

        checkAndUnlock("smoke_free_1month",
                smokeFreeDays >= 30, now);

        checkAndUnlock("smoke_free_3months",
                smokeFreeDays >= 90, now);

        checkAndUnlock("smoke_free_1year",
                smokeFreeDays >= 365, now);

        checkAndUnlock("daily_goal_met",
                todayCount > 0 && todayCount <= dailyGoal, now);

        checkAndUnlock("logged_10",
                totalLogs >= 10, now);

        checkAndUnlock("logged_50",
                totalLogs >= 50, now);

        checkAndUnlock("money_saved_100",
                moneySaved >= 100, now);

        checkAndUnlock("money_saved_1000",
                moneySaved >= 1000, now);
    }

    private void checkAndUnlock(String key, boolean condition, long now) {
        if (!condition) return;
        Achievement a = achievementDao.getByKeySync(key);
        if (a != null && !a.isUnlocked()) {
            a.setUnlocked(true);
            a.setUnlockedAt(now);
            achievementDao.update(a);
        }
    }
}