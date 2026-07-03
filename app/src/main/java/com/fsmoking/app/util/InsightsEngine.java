package com.fsmoking.app.util;

import com.fsmoking.app.data.entity.CigaretteLog;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Pure computation class — no Android context needed.
 * All methods are stateless and take the full log list as input.
 * Always call from a background thread.
 */
public class InsightsEngine {

    public static class Insights {
        public String peakHour         = "—";
        public String busiestDay       = "—";
        public String mostCommonTrigger = "—";
        public String mostCommonMood   = "—";
        public String avgInterval      = "—";
        public String longestStreak    = "—";
        public String totalToday       = "0";
        public String totalThisWeek    = "0";
        public String motivationalNote = "";
        public boolean hasEnoughData   = false;
    }

    public static Insights analyse(List<CigaretteLog> logs) {
        Insights result = new Insights();
        if (logs == null || logs.isEmpty()) {
            result.motivationalNote = "Log your first cigarette to start seeing insights.";
            return result;
        }

        result.hasEnoughData = logs.size() >= 3;

        // ── Peak hour ────────────────────────────────────────────────────────
        Map<Integer, Integer> hourMap = new HashMap<>();
        for (CigaretteLog log : logs) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(log.getTimestamp());
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            hourMap.put(hour, hourMap.getOrDefault(hour, 0) + 1);
        }
        int peakHour = maxKey(hourMap);
        if (peakHour >= 0) {
            String ampm = peakHour < 12 ? "AM" : "PM";
            int h12 = peakHour == 0 ? 12 : (peakHour > 12 ? peakHour - 12 : peakHour);
            result.peakHour = h12 + ":00 " + ampm;
        }

        // ── Busiest day of week ──────────────────────────────────────────────
        Map<Integer, Integer> dayMap = new HashMap<>();
        String[] dayNames = {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
        for (CigaretteLog log : logs) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(log.getTimestamp());
            int day = cal.get(Calendar.DAY_OF_WEEK);
            dayMap.put(day, dayMap.getOrDefault(day, 0) + 1);
        }
        int busiestDay = maxKey(dayMap);
        if (busiestDay >= 1 && busiestDay <= 7) {
            result.busiestDay = dayNames[busiestDay - 1];
        }

        // ── Most common trigger ──────────────────────────────────────────────
        Map<String, Integer> triggerMap = new HashMap<>();
        for (CigaretteLog log : logs) {
            if (log.getTrigger() != null && !log.getTrigger().trim().isEmpty()) {
                String t = log.getTrigger().trim();
                triggerMap.put(t, triggerMap.getOrDefault(t, 0) + 1);
            }
        }
        if (!triggerMap.isEmpty()) {
            result.mostCommonTrigger = maxStringKey(triggerMap);
        }

        // ── Most common mood ─────────────────────────────────────────────────
        Map<String, Integer> moodMap = new HashMap<>();
        for (CigaretteLog log : logs) {
            if (log.getMood() != null && !log.getMood().trim().isEmpty()) {
                String mood = log.getMood().trim();
                moodMap.put(mood, moodMap.getOrDefault(mood, 0) + 1);
            }
        }
        if (!moodMap.isEmpty()) {
            result.mostCommonMood = maxStringKey(moodMap);
        }

        // ── Average interval ─────────────────────────────────────────────────
        if (logs.size() >= 2) {
            long totalGap = 0;
            int gapCount  = 0;
            // logs are DESC by timestamp
            for (int i = 0; i < logs.size() - 1; i++) {
                long newer = logs.get(i).getTimestamp().getTime();
                long older = logs.get(i + 1).getTimestamp().getTime();
                long gap   = newer - older;
                if (gap > 0 && gap < 86400000L) { // ignore gaps > 24h (different days)
                    totalGap += gap;
                    gapCount++;
                }
            }
            if (gapCount > 0) {
                result.avgInterval = StatsFormatter.formatAvgInterval(
                        (double) totalGap / gapCount);
            }
        }

        // ── Longest smoke-free streak (in hours) ─────────────────────────────
        if (logs.size() >= 2) {
            long maxGap = 0;
            for (int i = 0; i < logs.size() - 1; i++) {
                long newer = logs.get(i).getTimestamp().getTime();
                long older = logs.get(i + 1).getTimestamp().getTime();
                long gap   = newer - older;
                if (gap > maxGap) maxGap = gap;
            }
            long maxHours = maxGap / 3600000;
            long maxMins  = (maxGap % 3600000) / 60000;
            if (maxHours > 0) {
                result.longestStreak = maxHours + "h " + maxMins + "m";
            } else {
                result.longestStreak = maxMins + " min";
            }
        }

        // ── Today / this week count ───────────────────────────────────────────
        long todayStart = DateUtils.startOfTodayMillis();
        long todayEnd   = DateUtils.endOfTodayMillis();
        long weekStart  = DateUtils.startOfWeekMillis();
        long weekEnd    = DateUtils.endOfWeekMillis();

        int todayCount = 0;
        int weekCount  = 0;
        for (CigaretteLog log : logs) {
            long t = log.getTimestamp().getTime();
            if (t >= todayStart && t <= todayEnd) todayCount++;
            if (t >= weekStart  && t <= weekEnd)  weekCount++;
        }
        result.totalToday    = String.valueOf(todayCount);
        result.totalThisWeek = String.valueOf(weekCount);

        // ── Motivational note ─────────────────────────────────────────────────
        result.motivationalNote = buildMotivation(result, logs.size());

        return result;
    }

    private static String buildMotivation(Insights ins, int totalLogs) {
        if (!ins.hasEnoughData) {
            return "Keep logging — insights improve with more data.";
        }
        if (!ins.peakHour.equals("—")) {
            return "You tend to smoke most around " + ins.peakHour
                    + ". Try planning an alternative activity at that time.";
        }
        if (!ins.mostCommonTrigger.equals("—")) {
            return "\"" + ins.mostCommonTrigger + "\" is your most common trigger. "
                    + "Awareness is the first step to change.";
        }
        return "You've logged " + totalLogs + " cigarettes. Every insight brings you closer to quitting.";
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static int maxKey(Map<Integer, Integer> map) {
        int maxKey = -1;
        int maxVal = -1;
        for (Map.Entry<Integer, Integer> e : map.entrySet()) {
            if (e.getValue() > maxVal) {
                maxVal = e.getValue();
                maxKey = e.getKey();
            }
        }
        return maxKey;
    }

    private static String maxStringKey(Map<String, Integer> map) {
        String maxKey = "—";
        int maxVal = -1;
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            if (e.getValue() > maxVal) {
                maxVal = e.getValue();
                maxKey = e.getKey();
            }
        }
        return maxKey;
    }
}