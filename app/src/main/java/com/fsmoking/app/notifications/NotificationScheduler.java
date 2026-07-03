package com.fsmoking.app.notifications;

import android.content.Context;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class NotificationScheduler {

    private static final String WORK_DAILY_REMINDER  = "daily_reminder";
    private static final String WORK_GOAL_WARNING    = "goal_warning";
    private static final String WORK_MOTIVATIONAL    = "motivational";

    /**
     * Schedules all periodic workers.
     * Call this from SplashActivity and BootReceiver.
     */
    public static void scheduleAll(Context context) {
        scheduleDailyReminder(context);
        scheduleGoalWarning(context);
        scheduleMotivational(context);
    }

    public static void scheduleDailyReminder(Context context) {
        // Fire daily, with initial delay calculated to hit 8:00 PM today
        long delayMins = minutesUntilHour(20); // 8 PM

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                DailyReminderWorker.class, 24, TimeUnit.HOURS)
                .setInitialDelay(delayMins, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_DAILY_REMINDER,
                ExistingPeriodicWorkPolicy.KEEP,
                request);
    }

    public static void scheduleGoalWarning(Context context) {
        // Check every 2 hours whether user is close to goal
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                GoalWarningWorker.class, 2, TimeUnit.HOURS)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_GOAL_WARNING,
                ExistingPeriodicWorkPolicy.KEEP,
                request);
    }

    public static void scheduleMotivational(Context context) {
        // Fire daily at 8:00 AM
        long delayMins = minutesUntilHour(8);

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                MotivationalWorker.class, 24, TimeUnit.HOURS)
                .setInitialDelay(delayMins, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_MOTIVATIONAL,
                ExistingPeriodicWorkPolicy.KEEP,
                request);
    }

    public static void cancelAll(Context context) {
        WorkManager.getInstance(context).cancelAllWork();
    }

    /**
     * Returns minutes until the next occurrence of the given hour (24h).
     * If that hour has already passed today, targets tomorrow.
     */
    private static long minutesUntilHour(int targetHour) {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, targetHour);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        if (target.before(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1);
        }

        long diffMs = target.getTimeInMillis() - now.getTimeInMillis();
        return TimeUnit.MILLISECONDS.toMinutes(diffMs);
    }
}