package com.fsmoking.app.notifications;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.fsmoking.app.repository.SettingsRepository;
import java.util.Random;

public class MotivationalWorker extends Worker {

    private static final String[] MESSAGES = {
            "Every cigarette you don't smoke is a win. Keep going! 💪",
            "Your lungs are healing every single day. Don't stop now.",
            "The money you save by not smoking adds up fast. Check your stats! 💰",
            "One day at a time. You've got this. 🌟",
            "Remember why you started tracking. You're making real progress.",
            "The hardest part is behind you. Keep pushing forward! 🚀",
            "Every hour smoke-free is another hour your body heals.",
            "You are stronger than your cravings. Believe it. 🏆",
            "Small steps every day lead to big changes. Stay consistent.",
            "Your future self will thank you for every cigarette you skip today.",
    };

    public MotivationalWorker(@NonNull Context context,
                              @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        NotificationHelper.createChannel(ctx);

        SettingsRepository settings = new SettingsRepository(ctx);
        // Only show if user has done onboarding
        if (!settings.isOnboardingDone()) return Result.success();

        String message = MESSAGES[new Random().nextInt(MESSAGES.length)];
        NotificationHelper.show(ctx,
                NotificationHelper.ID_MOTIVATIONAL,
                "FSmoking — Daily Motivation",
                message);

        return Result.success();
    }
}