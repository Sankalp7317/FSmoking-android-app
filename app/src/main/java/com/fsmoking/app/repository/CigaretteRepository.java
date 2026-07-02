package com.fsmoking.app.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.fsmoking.app.data.AppDatabase;
import com.fsmoking.app.data.DaoModels;
import com.fsmoking.app.data.dao.CigaretteDao;
import com.fsmoking.app.data.entity.CigaretteLog;
import java.util.Date;
import java.util.List;

public class CigaretteRepository {

    private final CigaretteDao cigaretteDao;

    public CigaretteRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        cigaretteDao = db.cigaretteDao();
    }

    public void logCigarette(String mood, String trigger, String location, String note) {
        CigaretteLog log = new CigaretteLog(new Date(), mood, trigger, location, note);
        AppDatabase.databaseWriteExecutor.execute(() -> cigaretteDao.insert(log));
    }

    public void update(CigaretteLog log) {
        AppDatabase.databaseWriteExecutor.execute(() -> cigaretteDao.update(log));
    }

    public void delete(CigaretteLog log) {
        AppDatabase.databaseWriteExecutor.execute(() -> cigaretteDao.delete(log));
    }

    public LiveData<List<CigaretteLog>> getAllLogsLive() {
        return cigaretteDao.getAllLogsLive();
    }

    public LiveData<List<CigaretteLog>> getLogsBetweenLive(long start, long end) {
        return cigaretteDao.getLogsBetweenLive(start, end);
    }

    public LiveData<Integer> getCountBetweenLive(long start, long end) {
        return cigaretteDao.getCountBetweenLive(start, end);
    }

    public LiveData<CigaretteLog> getMostRecentLive() {
        return cigaretteDao.getMostRecentLive();
    }

    public LiveData<List<CigaretteLog>> searchLogsLive(String query) {
        return cigaretteDao.searchLogsLive(query);
    }

    public LiveData<Integer> getTotalAllTimeLive() {
        return cigaretteDao.getTotalAllTimeLive();
    }

    public List<CigaretteLog> getAllLogsSync() {
        return cigaretteDao.getAllLogsSync();
    }

    public List<CigaretteLog> getLogsBetweenSync(long start, long end) {
        return cigaretteDao.getLogsBetweenSync(start, end);
    }

    public int getCountBetweenSync(long start, long end) {
        return cigaretteDao.getCountBetweenSync(start, end);
    }

    public int getTotalCountSync(long start, long end) {
        return cigaretteDao.getTotalCountSync(start, end);
    }

    public List<DaoModels.HourCount> getHourlyCountSync(long start, long end) {
        return cigaretteDao.getHourlyCountSync(start, end);
    }

    public List<DaoModels.DayCount> getDailyCountSync(long start, long end) {
        return cigaretteDao.getDailyCountSync(start, end);
    }

    public CigaretteLog getMostRecentSync() {
        return cigaretteDao.getMostRecentSync();
    }

    public void deleteAll() {
        AppDatabase.databaseWriteExecutor.execute(cigaretteDao::deleteAll);
    }
}