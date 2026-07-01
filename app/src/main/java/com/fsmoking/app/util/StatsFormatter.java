package com.fsmoking.app.util;

import java.util.Locale;

/**
 * Formatting helpers for displaying durations and money in the UI.
 */
public class StatsFormatter {

    public static String formatElapsed(long millis) {
        if (millis < 0) millis = 0;
        long totalSecs = millis / 1000;
        long days  = totalSecs / 86400;
        long hours = (totalSecs % 86400) / 3600;
        long mins  = (totalSecs % 3600) / 60;
        long secs  = totalSecs % 60;

        if (days > 0) {
            return String.format(Locale.getDefault(), "%dd %02dh %02dm %02ds", days, hours, mins, secs);
        }
        return String.format(Locale.getDefault(), "%02dh %02dm %02ds", hours, mins, secs);
    }

    public static String formatMoney(String currencySymbol, double amount) {
        return String.format(Locale.getDefault(), "%s%.2f", currencySymbol, amount);
    }

    public static String formatAvgInterval(double avgMillis) {
        if (avgMillis <= 0) return "—";
        long totalMins = (long) (avgMillis / 60000);
        long hours = totalMins / 60;
        long mins = totalMins % 60;
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%dh %02dm", hours, mins);
        }
        return String.format(Locale.getDefault(), "%dm", mins);
    }
}