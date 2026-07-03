package com.fsmoking.app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.fsmoking.app.data.AppDatabase;
import com.fsmoking.app.data.DaoModels;
import com.fsmoking.app.data.entity.CigaretteLog;
import com.fsmoking.app.repository.CigaretteRepository;
import com.fsmoking.app.repository.SettingsRepository;
import com.fsmoking.app.util.DateUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InsightsViewModel extends AndroidViewModel {

    public static class Insight {
        public String emoji;
        public String title;
        public String body;
        public boolean isPositive;

        public Insight(String emoji, String title, String body, boolean isPositive) {
            this.emoji = emoji;
            this.title = title;
            this.body = body;
            this.isPositive = isPositive;
        }
    }

    private final CigaretteRepository repository;
    private final SettingsRepository settingsRepository;
    private final MutableLiveData<List<Insight>> insights = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public InsightsViewModel(@NonNull Application application) {
        super(application);
        repository = new CigaretteRepository(application);
        settingsRepository = new SettingsRepository(application);
    }

    public MutableLiveData<List<Insight>> getInsights() { return insights; }
    public MutableLiveData<Boolean> getLoading() { return loading; }

    public void loadInsights() {
        loading.postValue(true);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Insight> result = new ArrayList<>();
            long now = System.currentTimeMillis();

            Calendar cal30 = Calendar.getInstance();
            cal30.add(Calendar.DAY_OF_MONTH, -29);
            cal30.set(Calendar.HOUR_OF_DAY, 0);
            cal30.set(Calendar.MINUTE, 0);
            cal30.set(Calendar.SECOND, 0);
            cal30.set(Calendar.MILLISECOND, 0);
            long thirtyDaysAgo = cal30.getTimeInMillis();

            long thisWeekStart = DateUtils.startOfWeekMillis();
            long thisWeekEnd   = DateUtils.endOfWeekMillis();
            long lastWeekStart = thisWeekStart - 7 * 86400000L;
            long lastWeekEnd   = thisWeekStart - 1;

            int thisWeekCount = repository.getCountBetweenSync(thisWeekStart, thisWeekEnd);
            int lastWeekCount = repository.getCountBetweenSync(lastWeekStart, lastWeekEnd);

            if (lastWeekCount > 0) {
                int diff = thisWeekCount - lastWeekCount;
                if (diff < 0) {
                    result.add(new Insight("📉", "Smoking less this week",
                            "You smoked " + Math.abs(diff) + " fewer cigarettes than last week. Keep it up!",
                            true));
                } else if (diff > 0) {
                    result.add(new Insight("📈", "Smoking more this week",
                            "You smoked " + diff + " more cigarettes than last week. Try to cut back.",
                            false));
                } else {
                    result.add(new Insight("➡️", "Same as last week",
                            "Your smoking this week matches last week (" + thisWeekCount + " cigarettes).",
                            false));
                }
            }

            List<DaoModels.HourCount> hourCounts =
                    repository.getHourlyCountSync(thirtyDaysAgo, now);
            if (!hourCounts.isEmpty()) {
                DaoModels.HourCount peak = hourCounts.get(0);
                for (DaoModels.HourCount hc : hourCounts) {
                    if (hc.count > peak.count) peak = hc;
                }
                int h = Integer.parseInt(peak.hour);
                String ampm = h < 12 ? "AM" : "PM";
                int h12 = h == 0 ? 12 : (h > 12 ? h - 12 : h);
                result.add(new Insight("🕐", "Peak smoking hour",
                        "You smoke most at " + h12 + ":00 " + ampm +
                                " (" + peak.count + " cigarettes in the last 30 days).",
                        false));
            }

            List<DaoModels.DayCount> dayCounts =
                    repository.getDailyCountSync(thirtyDaysAgo, now);
            if (!dayCounts.isEmpty()) {
                int[] dayTotals = new int[7];
                String[] dayNames = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
                Calendar tmpCal = Calendar.getInstance();
                for (DaoModels.DayCount dc : dayCounts) {
                    try {
                        String[] parts = dc.day.split("-");
                        tmpCal.set(Integer.parseInt(parts[0]),
                                Integer.parseInt(parts[1]) - 1,
                                Integer.parseInt(parts[2]));
                        int dow = tmpCal.get(Calendar.DAY_OF_WEEK);
                        int idx = dow == Calendar.SUNDAY ? 6 : dow - 2;
                        if (idx >= 0 && idx < 7) dayTotals[idx] += dc.count;
                    } catch (Exception ignored) {}
                }
                int maxIdx = 0;
                for (int i = 1; i < 7; i++) {
                    if (dayTotals[i] > dayTotals[maxIdx]) maxIdx = i;
                }
                if (dayTotals[maxIdx] > 0) {
                    result.add(new Insight("📅", "Busiest day",
                            dayNames[maxIdx] + " is your highest smoking day. " +
                                    "Plan extra distractions for " + dayNames[maxIdx] + ".",
                            false));
                }
            }

            List<CigaretteLog> allLogs = repository.getAllLogsSync();
            if (allLogs.size() >= 2) {
                long totalGap = 0;
                int gapCount = 0;
                for (int i = 0; i < allLogs.size() - 1; i++) {
                    long newer = allLogs.get(i).getTimestamp().getTime();
                    long older = allLogs.get(i + 1).getTimestamp().getTime();
                    totalGap += (newer - older);
                    gapCount++;
                }
                long avgMins = (totalGap / gapCount) / 60000;
                long avgH = avgMins / 60;
                long avgM = avgMins % 60;
                String avgLabel = avgH > 0 ? avgH + "h " + avgM + "m" : avgM + " minutes";
                result.add(new Insight("⏱️", "Average interval",
                        "On average you smoke every " + avgLabel + ".",
                        avgMins >= 60));
            }

            Map<String, Integer> triggerMap = new HashMap<>();
            for (CigaretteLog log : allLogs) {
                if (log.getTrigger() != null && !log.getTrigger().isEmpty()) {
                    String t = log.getTrigger();
                    triggerMap.put(t, triggerMap.getOrDefault(t, 0) + 1);
                }
            }
            if (!triggerMap.isEmpty()) {
                String topTrigger = null;
                int topCount = 0;
                for (Map.Entry<String, Integer> e : triggerMap.entrySet()) {
                    if (e.getValue() > topCount) {
                        topCount = e.getValue();
                        topTrigger = e.getKey();
                    }
                }
                result.add(new Insight("⚡", "Top trigger",
                        "\"" + topTrigger + "\" triggers you most (" + topCount + " times).",
                        false));
            }

            Map<String, Integer> moodMap = new HashMap<>();
            for (CigaretteLog log : allLogs) {
                if (log.getMood() != null && !log.getMood().isEmpty()) {
                    String m = log.getMood();
                    moodMap.put(m, moodMap.getOrDefault(m, 0) + 1);
                }
            }
            if (!moodMap.isEmpty()) {
                String topMood = null;
                int topMoodCount = 0;
                for (Map.Entry<String, Integer> e : moodMap.entrySet()) {
                    if (e.getValue() > topMoodCount) {
                        topMoodCount = e.getValue();
                        topMood = e.getKey();
                    }
                }
                result.add(new Insight("😤", "Most common mood",
                        "You most often smoke when feeling \"" + topMood + "\".",
                        false));
            }

            if (allLogs.size() >= 2) {
                long longestGap = 0;
                for (int i = 0; i < allLogs.size() - 1; i++) {
                    long newer = allLogs.get(i).getTimestamp().getTime();
                    long older = allLogs.get(i + 1).getTimestamp().getTime();
                    long gap = newer - older;
                    if (gap > longestGap) longestGap = gap;
                }
                long longestH = longestGap / 3600000;
                long longestM = (longestGap % 3600000) / 60000;
                String streakLabel = longestH > 0
                        ? longestH + "h " + longestM + "m"
                        : longestM + " minutes";
                result.add(new Insight("🏆", "Longest streak",
                        "Your longest time without a cigarette was " + streakLabel + ".",
                        true));
            }

            double costPerCig = settingsRepository.getPackPrice()
                    / (double) settingsRepository.getCigsPerPack();
            double totalSpent = allLogs.size() * costPerCig;
            if (totalSpent > 0) {
                result.add(new Insight("💸", "Total spent on cigarettes",
                        String.format(Locale.getDefault(), "You have spent %s%.2f on cigarettes in total.",
                                settingsRepository.getCurrency(), totalSpent),
                        false));
            }

            if (result.isEmpty()) {
                result.add(new Insight("📊", "Not enough data yet",
                        "Log a few cigarettes to start seeing personalised insights.",
                        false));
            }

            insights.postValue(result);
            loading.postValue(false);
        });
    }
}