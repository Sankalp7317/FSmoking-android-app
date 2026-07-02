package com.fsmoking.app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.fsmoking.app.data.AppDatabase;
import com.fsmoking.app.data.entity.Achievement;
import java.util.List;

public class AchievementsViewModel extends AndroidViewModel {

    private final LiveData<List<Achievement>> allAchievements;
    private final LiveData<Integer> unlockedCount;

    public AchievementsViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        allAchievements = db.achievementDao().getAllLive();
        unlockedCount   = db.achievementDao().getUnlockedCountLive();
    }

    public LiveData<List<Achievement>> getAllAchievements() {
        return allAchievements;
    }

    public LiveData<Integer> getUnlockedCount() {
        return unlockedCount;
    }
}