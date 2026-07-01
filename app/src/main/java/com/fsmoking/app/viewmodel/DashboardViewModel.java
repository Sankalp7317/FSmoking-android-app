package com.fsmoking.app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;
import com.fsmoking.app.data.entity.CigaretteLog;
import com.fsmoking.app.repository.CigaretteRepository;
import com.fsmoking.app.repository.SettingsRepository;
import com.fsmoking.app.util.DateUtils;
import java.util.List;

/**
 * Backs the Dashboard screen. Exposes everything as LiveData so the UI
 * reacts automatically whenever the underlying cigarette log changes.
 */
public class DashboardViewModel extends AndroidViewModel {

    private final CigaretteRepository cigaretteRepository;
    private final SettingsRepository settingsRepository;

    private final LiveData<List<CigaretteLog>> todayLogs;
    private final LiveData<List<CigaretteLog>> weekLogs;
    private final LiveData<List<CigaretteLog>> monthLogs;
    private final LiveData<CigaretteLog> mostRecentLog;

    private final LiveData<Integer> todayCount;
    private final LiveData<Integer> weekCount;
    private final LiveData<Integer> monthCount;

    private final MediatorLiveData<Double> todayMoneySpent = new MediatorLiveData<>();
    private final MediatorLiveData<Double> avgIntervalMillis = new MediatorLiveData<>();

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        cigaretteRepository = new CigaretteRepository(application);
        settingsRepository = new SettingsRepository(application);

        long todayStart = DateUtils.startOfTodayMillis();
        long todayEnd = DateUtils.endOfTodayMillis();
        long weekStart = DateUtils.startOfWeekMillis();
        long weekEnd = DateUtils.endOfWeekMillis();
        long monthStart = DateUtils.startOfMonthMillis();
        long monthEnd = DateUtils.endOfMonthMillis();

        todayLogs = cigaretteRepository.getLogsBetweenLive(todayStart, todayEnd);
        weekLogs = cigaretteRepository.getLogsBetweenLive(weekStart, weekEnd);
        monthLogs = cigaretteRepository.getLogsBetweenLive(monthStart, monthEnd);
        mostRecentLog = cigaretteRepository.getMostRecentLive();

        todayCount = cigaretteRepository.getCountBetweenLive(todayStart, todayEnd);
        weekCount = cigaretteRepository.getCountBetweenLive(weekStart, weekEnd);
        monthCount = cigaretteRepository.getCountBetweenLive(monthStart, monthEnd);

        // Derived: today's money spent = todayCount * (packPrice / cigsPerPack)
        todayMoneySpent.addSource(todayCount, count -> recalcTodayMoney());

        // Derived: average interval between cigarettes today
        avgIntervalMillis.addSource(todayLogs, logs -> recalcAvgInterval(logs));
    }

    private void recalcTodayMoney() {
        Integer count = todayCount.getValue();
        if (count == null) count = 0;
        double pricePerCig = settingsRepository.getPackPrice() / (double) settingsRepository.getCigsPerPack();
        todayMoneySpent.setValue(count * pricePerCig);
    }

    private void recalcAvgInterval(List<CigaretteLog> logs) {
        if (logs == null || logs.size() < 2) {
            avgIntervalMillis.setValue(0d);
            return;
        }
        // logs are DESC by timestamp; compute average gap between consecutive entries
        long totalGap = 0;
        int gapCount = 0;
        for (int i = 0; i < logs.size() - 1; i++) {
            long newer = logs.get(i).getTimestamp().getTime();
            long older = logs.get(i + 1).getTimestamp().getTime();
            totalGap += (newer - older);
            gapCount++;
        }
        avgIntervalMillis.setValue(gapCount == 0 ? 0d : (double) totalGap / gapCount);
    }

    // ---- Public LiveData exposed to the UI ----

    public LiveData<Integer> getTodayCount() { return todayCount; }
    public LiveData<Integer> getWeekCount() { return weekCount; }
    public LiveData<Integer> getMonthCount() { return monthCount; }
    public LiveData<CigaretteLog> getMostRecentLog() { return mostRecentLog; }
    public LiveData<Double> getTodayMoneySpent() { return todayMoneySpent; }
    public LiveData<Double> getAvgIntervalMillis() { return avgIntervalMillis; }

    public LiveData<Integer> getDailyGoal() {
        // Wrapped as LiveData for consistency, though it's a simple pref read.
        // Re-read happens whenever logCigarette() triggers todayCount to change.
        return Transformations.map(todayCount, count -> settingsRepository.getDailyGoal());
    }

    public String getCurrencySymbol() {
        return settingsRepository.getCurrency();
    }

    public long getQuitDateMillis() {
        return settingsRepository.getQuitDateMillis();
    }

    // ---- Actions ----

    public void logCigarette() {
        cigaretteRepository.logCigarette(null, null, null, null);
        // Logging a cigarette resets the smoke-free timer reference point.
        settingsRepository.setQuitDateMillis(System.currentTimeMillis());
    }

    public void resetSmokeFreeTimer() {
        settingsRepository.setQuitDateMillis(System.currentTimeMillis());
    }

    public void logCigaretteWithDetails(String mood, String trigger,
                                        String location, String note) {
        cigaretteRepository.logCigarette(mood, trigger, location, note);
        settingsRepository.setQuitDateMillis(System.currentTimeMillis());
    }
}