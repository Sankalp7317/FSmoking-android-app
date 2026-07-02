package com.fsmoking.app.data;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.fsmoking.app.data.dao.AchievementDao;
import com.fsmoking.app.data.dao.CigaretteDao;
import com.fsmoking.app.data.entity.Achievement;
import com.fsmoking.app.data.entity.CigaretteLog;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {CigaretteLog.class, Achievement.class},
        version = 1,
        exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "fsmoking.db";
    private static volatile AppDatabase instance;

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    public abstract CigaretteDao cigaretteDao();
    public abstract AchievementDao achievementDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DB_NAME)
                            .fallbackToDestructiveMigration()
                            .addCallback(new SeedCallback())
                            .build();
                }
            }
        }
        return instance;
    }

    private static class SeedCallback extends RoomDatabase.Callback {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                if (instance == null) return;
                AchievementDao dao = instance.achievementDao();
                if (dao.getTotalCountSync() > 0) return; // already seeded
                dao.insertAll(buildAchievements());
            });
        }
    }

    private static List<Achievement> buildAchievements() {
        return Arrays.asList(
                new Achievement("first_log",
                        "First Cigarette Logged", "You logged your first cigarette. Awareness is step one.",
                        "🚬", false, null),
                new Achievement("smoke_free_1h",
                        "One Hour Strong", "You went 1 hour without smoking.",
                        "⏱️", false, null),
                new Achievement("smoke_free_24h",
                        "One Smoke-Free Day", "24 hours without a cigarette. Incredible!",
                        "🌅", false, null),
                new Achievement("smoke_free_1week",
                        "One Week Wonder", "7 days smoke-free. Your lungs thank you.",
                        "🌿", false, null),
                new Achievement("smoke_free_1month",
                        "One Month Hero", "30 days smoke-free. You're a champion.",
                        "📅", false, null),
                new Achievement("smoke_free_3months",
                        "Quarter Champion", "3 months smoke-free. Remarkable willpower.",
                        "🌟", false, null),
                new Achievement("smoke_free_1year",
                        "One Year Legend", "365 days smoke-free. You've changed your life.",
                        "🏆", false, null),
                new Achievement("daily_goal_met",
                        "Daily Goal Completed", "You stayed within your daily cigarette goal.",
                        "🎯", false, null),
                new Achievement("logged_10",
                        "Tracking Habit", "Logged 10 cigarettes. Knowledge is power.",
                        "📊", false, null),
                new Achievement("logged_50",
                        "Data Champion", "Logged 50 cigarettes. You're serious about tracking.",
                        "📈", false, null),
                new Achievement("money_saved_100",
                        "First Savings", "Saved your first 100 units of currency.",
                        "💰", false, null),
                new Achievement("money_saved_1000",
                        "Big Saver", "Saved 1000 units of currency by not smoking.",
                        "🤑", false, null)
        );
    }
}