package com.fsmoking.app.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.fsmoking.app.data.dao.AchievementDao;
import com.fsmoking.app.data.dao.CigaretteDao;
import com.fsmoking.app.data.entity.Achievement;
import com.fsmoking.app.data.entity.CigaretteLog;
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

    // Shared background executor for all DB writes across the app.
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
                            .build();
                }
            }
        }
        return instance;
    }
}