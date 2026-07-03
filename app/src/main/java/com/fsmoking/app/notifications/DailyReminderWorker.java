package com.fsmoking.app.notifications;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.fsmoking.app.repository.CigaretteRepository;
import com.fsmoking.app.util.DateUtils;

public class DailyReminderWorker extends Worker {

    public DailyReminderWorker(@NonNull Context context,
                               @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        NotificationHelper.createChannel(ctx);

        // Check if user has logged anything today already
        // If yes, skip the reminder — they're already tracking
        try {
            android.app.Application app = (android.app.Application) ctx;
            CigaretteRepository repo = new CigaretteRepository(app);
            int todayCount = repo.getCountBetweenSync(
                    DateUtils.startOfTodayMillis(),
                    DateUtils.endOfTodayMillis());

            if (todayCount == 0) {
                NotificationHelper.show(ctx,
                        NotificationHelper.ID_DAILY_REMINDER,
                        "Don't forget to track today 🚬",
                        "Open FSmoking to log your cigarettes and stay on top of your habit.");
            }
        } catch (Exception e) {
            NotificationHelper.show(ctx,
                    NotificationHelper.ID_DAILY_REMINDER,
                    "FSmoking reminder",
                    "Open the app to track your smoking for today.");
        }

        return Result.success();
    }
}