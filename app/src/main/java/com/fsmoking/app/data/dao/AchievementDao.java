package com.fsmoking.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.fsmoking.app.data.entity.Achievement;
import java.util.List;

@Dao
public interface AchievementDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<Achievement> achievements);

    @Update
    void update(Achievement achievement);

    @Query("SELECT * FROM achievement ORDER BY unlocked DESC, key ASC")
    LiveData<List<Achievement>> getAllLive();

    @Query("SELECT * FROM achievement WHERE key = :key LIMIT 1")
    Achievement getByKeySync(String key);

    @Query("SELECT COUNT(*) FROM achievement WHERE unlocked = 1")
    LiveData<Integer> getUnlockedCountLive();

    @Query("SELECT COUNT(*) FROM achievement")
    int getTotalCountSync();

}