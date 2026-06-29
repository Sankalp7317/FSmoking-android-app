package com.fsmoking.app.utils;

import com.fsmoking.app.models.UserData;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class StatsCalculator {

    public static class Stats {
        public long elapsedMs;
        public int days, hours, mins, secs;
        public double elapsedDays;
        public int cigsAvoided;
        public double moneySaved;
        public int minutesRegained;
    }

    public static Stats calculate(UserData data) {
        if (data == null || data.getQuitDate() == null) return null;
        try {
            SimpleDateFormat sdf =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date quitDate = sdf.parse(data.getQuitDate());
            if (quitDate == null) return null;

            long elapsed = System.currentTimeMillis() - quitDate.getTime();
            if (elapsed < 0) elapsed = 0;

            Stats s        = new Stats();
            s.elapsedMs    = elapsed;
            long totalSecs = elapsed / 1000;
            s.days         = (int)(totalSecs / 86400);
            s.hours        = (int)((totalSecs % 86400) / 3600);
            s.mins         = (int)((totalSecs % 3600) / 60);
            s.secs         = (int)(totalSecs % 60);
            s.elapsedDays  = elapsed / 86400000.0;
            s.cigsAvoided  = (int)(s.elapsedDays * data.getCigsPerDay());
            double costPerCig = data.getPackCost() / data.getPackSize();
            s.moneySaved      = s.cigsAvoided * costPerCig;
            s.minutesRegained = s.cigsAvoided * 11;
            return s;

        } catch (Exception e) {
            return null;
        }
    }

    public static boolean[] getWeekStreak(UserData data) {
        boolean[] streak = new boolean[7];
        if (data == null) return streak;
        Stats s = calculate(data);
        if (s == null) return streak;
        int todayIdx = getTodayIndex();
        for (int i = 0; i <= todayIdx; i++) {
            if (s.elapsedDays >= 1) streak[i] = true;
        }
        return streak;
    }

    public static int getTodayIndex() {
        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return (day == Calendar.SUNDAY) ? 6 : day - 2;
    }
}