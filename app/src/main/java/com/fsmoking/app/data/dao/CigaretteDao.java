package com.fsmoking.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.fsmoking.app.data.DaoModels;
import com.fsmoking.app.data.entity.CigaretteLog;
import java.util.List;

@Dao
public interface CigaretteDao {

    @Insert
    long insert(CigaretteLog log);

    @Update
    void update(CigaretteLog log);

    @Delete
    void delete(CigaretteLog log);

    @Query("SELECT * FROM cigarette_log ORDER BY timestamp DESC")
    LiveData<List<CigaretteLog>> getAllLogsLive();

    @Query("SELECT * FROM cigarette_log ORDER BY timestamp DESC")
    List<CigaretteLog> getAllLogsSync();

    @Query("SELECT * FROM cigarette_log WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    LiveData<List<CigaretteLog>> getLogsBetweenLive(long start, long end);

    @Query("SELECT * FROM cigarette_log WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    List<CigaretteLog> getLogsBetweenSync(long start, long end);

    @Query("SELECT COUNT(*) FROM cigarette_log WHERE timestamp BETWEEN :start AND :end")
    LiveData<Integer> getCountBetweenLive(long start, long end);

    @Query("SELECT COUNT(*) FROM cigarette_log WHERE timestamp BETWEEN :start AND :end")
    int getCountBetweenSync(long start, long end);

    @Query("SELECT * FROM cigarette_log ORDER BY timestamp DESC LIMIT 1")
    CigaretteLog getMostRecentSync();

    @Query("SELECT * FROM cigarette_log ORDER BY timestamp DESC LIMIT 1")
    LiveData<CigaretteLog> getMostRecentLive();

    @Query("SELECT * FROM cigarette_log WHERE " +
            "(mood LIKE '%' || :query || '%') OR " +
            "(trigger LIKE '%' || :query || '%') OR " +
            "(location LIKE '%' || :query || '%') OR " +
            "(note LIKE '%' || :query || '%') " +
            "ORDER BY timestamp DESC")
    LiveData<List<CigaretteLog>> searchLogsLive(String query);

    @Query("DELETE FROM cigarette_log")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM cigarette_log")
    LiveData<Integer> getTotalAllTimeLive();

    @Query("SELECT COUNT(*) FROM cigarette_log WHERE timestamp BETWEEN :start AND :end")
    int getTotalCountSync(long start, long end);

    @Query("SELECT strftime('%H', datetime(timestamp/1000, 'unixepoch', 'localtime')) as hour, " +
            "COUNT(*) as count FROM cigarette_log " +
            "WHERE timestamp BETWEEN :start AND :end " +
            "GROUP BY hour ORDER BY hour ASC")
    List<DaoModels.HourCount> getHourlyCountSync(long start, long end);

    @Query("SELECT strftime('%Y-%m-%d', datetime(timestamp/1000, 'unixepoch', 'localtime')) as day, " +
            "COUNT(*) as count FROM cigarette_log " +
            "WHERE timestamp BETWEEN :start AND :end " +
            "GROUP BY day ORDER BY day ASC")
    List<DaoModels.DayCount> getDailyCountSync(long start, long end);
}