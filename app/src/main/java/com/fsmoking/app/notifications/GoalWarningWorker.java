package com.fsmoking.app.notifications;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.fsmoking.app.repository.CigaretteRepository;
import com.fsmoking.app.repository.SettingsRepository;
import com.fsmoking.app.util.DateUtils;

public class GoalWarningWorker extends Worker {

    public GoalWarningWorker(@NonNull Context context,
                             @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        NotificationHelper.createChannel(ctx);

        try {
            android.app.Application app = (android.app.Application) ctx;
            CigaretteRepository repo = new CigaretteRepository(app);
            SettingsRepository settings = new SettingsRepository(ctx);

            int todayCount = repo.getCountBetweenSync(
                    DateUtils.startOfTodayMillis(),
                    DateUtils.endOfTodayMillis());
            int dailyGoal = settings.getDailyGoal();

            double percent = (double) todayCount / dailyGoal;

            if (percent >= 0.8 && percent < 1.0) {
                int remaining = dailyGoal - todayCount;
                NotificationHelper.show(ctx,
                        NotificationHelper.ID_GOAL_WARNING,
                        "⚠️ Approaching daily limit",
                        "You've smoked " + todayCount + " cigarettes today. " +
                                "Only " + remaining + " left before you hit your goal of " +
                                dailyGoal + ".");
            } else if (todayCount >= dailyGoal) {
                NotificationHelper.show(ctx,
                        NotificationHelper.ID_GOAL_WARNING,
                        "🚨 Daily limit reached",
                        "You've reached your daily goal of " + dailyGoal +
                                " cigarettes. Try to stop here for today.");
            }
        } catch (Exception e) {
            // Fail silently — notifications are non-critical
        }

        return Result.success();
    }
}