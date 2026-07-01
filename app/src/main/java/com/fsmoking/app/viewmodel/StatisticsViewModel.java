package com.fsmoking.app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.fsmoking.app.data.dao.CigaretteDao;
import com.fsmoking.app.data.AppDatabase;
import com.fsmoking.app.repository.CigaretteRepository;
import com.fsmoking.app.repository.SettingsRepository;
import com.fsmoking.app.util.DateUtils;
import java.util.Calendar;
import java.util.List;

public class StatisticsViewModel extends AndroidViewModel {

    public static class StatsData {
        public List<CigaretteDao.DayCount> last7Days;
        public List<CigaretteDao.DayCount> last30Days;
        public List<CigaretteDao.HourCount> peakHours;
        public int totalThisMonth;
        public int totalThisWeek;
        public int totalAllTime;
        public double avgPerDay;
        public double moneySpentThisMonth;
        public String peakHourLabel;
        public int bestDayCount;
        public int worstDayCount;
    }

    private final CigaretteRepository repository;
    private final SettingsRepository settingsRepository;
    private final MutableLiveData<StatsData> statsData = new MutableLiveData<>();
    private final LiveData<Integer> totalAllTime;

    public StatisticsViewModel(@NonNull Application application) {
        super(application);
        repository = new CigaretteRepository(application);
        settingsRepository = new SettingsRepository(application);
        totalAllTime = repository.getTotalAllTimeLive();
    }

    public LiveData<StatsData> getStatsData() { return statsData; }
    public LiveData<Integer> getTotalAllTime() { return totalAllTime; }

    public void loadStats() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            StatsData data = new StatsData();

            // Last 7 days
            long weekStart = DateUtils.startOfWeekMillis();
            long weekEnd   = DateUtils.endOfWeekMillis();
            data.last7Days = repository.getDailyCountSync(weekStart, weekEnd);
            data.totalThisWeek = repository.getTotalCountSync(weekStart, weekEnd);

            // Last 30 days
            Calendar cal30 = Calendar.getInstance();
            cal30.add(Calendar.DAY_OF_MONTH, -29);
            cal30.set(Calendar.HOUR_OF_DAY, 0);
            cal30.set(Calendar.MINUTE, 0);
            cal30.set(Calendar.SECOND, 0);
            cal30.set(Calendar.MILLISECOND, 0);
            long thirtyDaysAgo = cal30.getTimeInMillis();
            data.last30Days = repository.getDailyCountSync(thirtyDaysAgo, System.currentTimeMillis());

            // This month
            long monthStart = DateUtils.startOfMonthMillis();
            long monthEnd   = DateUtils.endOfMonthMillis();
            data.totalThisMonth = repository.getTotalCountSync(monthStart, monthEnd);

            // Money this month
            double costPerCig = settingsRepository.getPackPrice()
                    / (double) settingsRepository.getCigsPerPack();
            data.moneySpentThisMonth = data.totalThisMonth * costPerCig;

            // Avg per day (last 30 days, only days with data)
            int daysWithData = data.last30Days.size();
            int totalLast30 = 0;
            for (CigaretteDao.DayCount dc : data.last30Days) totalLast30 += dc.count;
            data.avgPerDay = daysWithData > 0 ? (double) totalLast30 / daysWithData : 0;

            // Best and worst day (last 30 days)
            data.bestDayCount  = Integer.MAX_VALUE;
            data.worstDayCount = 0;
            for (CigaretteDao.DayCount dc : data.last30Days) {
                if (dc.count < data.bestDayCount) data.bestDayCount = dc.count;
                if (dc.count > data.worstDayCount) data.worstDayCount = dc.count;
            }
            if (data.bestDayCount == Integer.MAX_VALUE) data.bestDayCount = 0;

            // Peak hours (last 30 days)
            data.peakHours = repository.getHourlyCountSync(thirtyDaysAgo,
                    System.currentTimeMillis());

            // Peak hour label
            int peakCount = 0;
            String peakHour = "—";
            for (CigaretteDao.HourCount hc : data.peakHours) {
                if (hc.count > peakCount) {
                    peakCount = hc.count;
                    int h = Integer.parseInt(hc.hour);
                    String ampm = h < 12 ? "AM" : "PM";
                    int h12 = h == 0 ? 12 : (h > 12 ? h - 12 : h);
                    peakHour = h12 + ":00 " + ampm;
                }
            }
            data.peakHourLabel = peakHour;

            statsData.postValue(data);
        });
    }
}