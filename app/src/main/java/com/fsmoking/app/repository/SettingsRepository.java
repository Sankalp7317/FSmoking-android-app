package com.fsmoking.app.repository;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Wraps SharedPreferences for lightweight user settings
 * (pack price, cigarettes per pack, daily goal, etc).
 * Not used for cigarette log data — that lives in Room.
 */
public class SettingsRepository {

    private static final String PREF_NAME = "fsmoking_settings";

    private static final String KEY_PACK_PRICE = "pack_price";
    private static final String KEY_CIGS_PER_PACK = "cigs_per_pack";
    private static final String KEY_DAILY_GOAL = "daily_goal";
    private static final String KEY_CURRENCY = "currency";
    private static final String KEY_QUIT_DATE = "quit_date_millis"; // smoke-free timer reset point
    private static final String KEY_ONBOARDING_DONE = "onboarding_done";

    private final SharedPreferences prefs;

    public SettingsRepository(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public float getPackPrice() {
        return prefs.getFloat(KEY_PACK_PRICE, 10f);
    }

    public void setPackPrice(float price) {
        prefs.edit().putFloat(KEY_PACK_PRICE, price).apply();
    }

    public int getCigsPerPack() {
        return prefs.getInt(KEY_CIGS_PER_PACK, 20);
    }

    public void setCigsPerPack(int count) {
        prefs.edit().putInt(KEY_CIGS_PER_PACK, count).apply();
    }

    public int getDailyGoal() {
        return prefs.getInt(KEY_DAILY_GOAL, 10);
    }

    public void setDailyGoal(int goal) {
        prefs.edit().putInt(KEY_DAILY_GOAL, goal).apply();
    }

    public String getCurrency() {
        return prefs.getString(KEY_CURRENCY, "₹");
    }

    public void setCurrency(String currency) {
        prefs.edit().putString(KEY_CURRENCY, currency).apply();
    }

    /**
     * Timestamp the "smoke-free timer" counts from.
     * Updated automatically every time a cigarette is logged
     * (handled by the Dashboard ViewModel), or manually reset by the user.
     */
    public long getQuitDateMillis() {
        return prefs.getLong(KEY_QUIT_DATE, System.currentTimeMillis());
    }

    public void setQuitDateMillis(long millis) {
        prefs.edit().putLong(KEY_QUIT_DATE, millis).apply();
    }

    public boolean isOnboardingDone() {
        return prefs.getBoolean(KEY_ONBOARDING_DONE, false);
    }

    public void setOnboardingDone(boolean done) {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, done).apply();
    }
}