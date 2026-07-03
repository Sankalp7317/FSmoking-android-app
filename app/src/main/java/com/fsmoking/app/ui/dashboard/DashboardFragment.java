package com.fsmoking.app.ui.dashboard;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.fsmoking.app.R;
import com.fsmoking.app.data.DaoModels;
import com.fsmoking.app.ui.history.LogDetailBottomSheet;
import com.fsmoking.app.util.DateUtils;
import com.fsmoking.app.util.StatsFormatter;
import com.fsmoking.app.viewmodel.DashboardViewModel;
import com.fsmoking.app.viewmodel.InsightsViewModel;
import com.fsmoking.app.viewmodel.StatisticsViewModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private DashboardViewModel viewModel;
    private StatisticsViewModel statsViewModel;
    private InsightsViewModel insightsViewModel;

    private TextView tvSmokeFreeTimer;
    private TextView tvTodayCountLabel;
    private TextView tvDashboardDate;
    private LinearProgressIndicator progressToday;
    private TextView tvWeekCount;
    private TextView tvMonthCount;
    private TextView tvMoneySpentToday;
    private TextView tvAvgInterval;
    private BarChart chartDashboardWeek;
    private LinearLayout insightsContainer;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private int dailyGoal = 10;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel         = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);
        statsViewModel    = new ViewModelProvider(requireActivity()).get(StatisticsViewModel.class);
        insightsViewModel = new ViewModelProvider(requireActivity()).get(InsightsViewModel.class);

        bindViews(view);
        setupChart();
        observeViewModel();
        startSmokeFreeTimer();
        loadStatsAndInsights();

        // Set today's date
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault());
        tvDashboardDate.setText(sdf.format(new Date()));

        view.findViewById(R.id.fab_log_cigarette).setOnClickListener(v -> {
            LogDetailBottomSheet sheet = LogDetailBottomSheet.newInstance(null);
            sheet.show(getParentFragmentManager(), "log_detail");
            new Handler(Looper.getMainLooper()).postDelayed(() ->
                    com.fsmoking.app.notifications.NotificationScheduler
                            .scheduleGoalWarning(requireContext()), 2000);
        });
    }

    private void bindViews(View view) {
        tvSmokeFreeTimer    = view.findViewById(R.id.tv_smoke_free_timer);
        tvTodayCountLabel   = view.findViewById(R.id.tv_today_count_label);
        tvDashboardDate     = view.findViewById(R.id.tv_dashboard_date);
        progressToday       = view.findViewById(R.id.progress_today);
        tvWeekCount         = view.findViewById(R.id.tv_week_count);
        tvMonthCount        = view.findViewById(R.id.tv_month_count);
        tvMoneySpentToday   = view.findViewById(R.id.tv_money_spent_today);
        tvAvgInterval       = view.findViewById(R.id.tv_avg_interval);
        chartDashboardWeek  = view.findViewById(R.id.chart_dashboard_week);
        insightsContainer   = view.findViewById(R.id.dashboard_insights_container);
    }

    private void setupChart() {
        chartDashboardWeek.getDescription().setEnabled(false);
        chartDashboardWeek.setDrawGridBackground(false);
        chartDashboardWeek.setDrawBorders(false);
        chartDashboardWeek.getAxisRight().setEnabled(false);
        chartDashboardWeek.getAxisLeft().setDrawGridLines(false);
        chartDashboardWeek.getAxisLeft().setTextColor(Color.parseColor("#7A9589"));
        chartDashboardWeek.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chartDashboardWeek.getXAxis().setDrawGridLines(false);
        chartDashboardWeek.getXAxis().setTextColor(Color.parseColor("#7A9589"));
        chartDashboardWeek.getLegend().setEnabled(false);
        chartDashboardWeek.setTouchEnabled(false);
        chartDashboardWeek.setScaleEnabled(false);
        chartDashboardWeek.animateY(600);
    }

    private void observeViewModel() {
        viewModel.getDailyGoal().observe(getViewLifecycleOwner(), goal -> {
            dailyGoal = goal != null ? goal : 10;
            updateTodayProgress();
        });

        viewModel.getTodayCount().observe(getViewLifecycleOwner(),
                count -> updateTodayProgress());

        viewModel.getWeekCount().observe(getViewLifecycleOwner(), count ->
                tvWeekCount.setText(String.valueOf(count != null ? count : 0)));

        viewModel.getMonthCount().observe(getViewLifecycleOwner(), count ->
                tvMonthCount.setText(String.valueOf(count != null ? count : 0)));

        viewModel.getTodayMoneySpent().observe(getViewLifecycleOwner(), money ->
                tvMoneySpentToday.setText(StatsFormatter.formatMoney(
                        viewModel.getCurrencySymbol(), money != null ? money : 0.0)));

        viewModel.getAvgIntervalMillis().observe(getViewLifecycleOwner(), avg ->
                tvAvgInterval.setText(StatsFormatter.formatAvgInterval(
                        avg != null ? avg : 0.0)));

        // Reload chart + insights when logs change
        viewModel.getMostRecentLog().observe(getViewLifecycleOwner(),
                log -> loadStatsAndInsights());
    }

    private void loadStatsAndInsights() {
        statsViewModel.loadStats();
        statsViewModel.getStatsData().observe(getViewLifecycleOwner(), data -> {
            if (data != null) renderWeekChart(data.last7Days);
        });

        insightsViewModel.loadInsights();
        insightsViewModel.getInsights().observe(getViewLifecycleOwner(),
                (List<InsightsViewModel.Insight> insights) -> {
                    if (insightsContainer == null || insights == null) return;
                    insightsContainer.removeAllViews();
                    int max = Math.min(insights.size(), 3);
                    for (int i = 0; i < max; i++) {
                        InsightsViewModel.Insight insight = insights.get(i);
                        View card = LayoutInflater.from(requireContext())
                                .inflate(R.layout.item_insight_card, insightsContainer, false);
                        ((TextView) card.findViewById(R.id.tv_insight_emoji)).setText(insight.emoji);
                        ((TextView) card.findViewById(R.id.tv_insight_title)).setText(insight.title);
                        ((TextView) card.findViewById(R.id.tv_insight_body)).setText(insight.body);
                        card.findViewById(R.id.insight_card_root).setBackgroundResource(
                                insight.isPositive
                                        ? R.drawable.card_green_bg
                                        : R.drawable.card_neutral_bg);
                        insightsContainer.addView(card);
                    }
                });
    }

    private void renderWeekChart(List<DaoModels.DayCount> dayCounts) {
        if (chartDashboardWeek == null || dayCounts == null) return;

        String[] dayLabels = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Map<String, Integer> countMap = new HashMap<>();
        for (DaoModels.DayCount dc : dayCounts) countMap.put(dc.day, dc.count);

        List<BarEntry> entries = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        for (int i = 0; i < 7; i++) {
            String dayKey = sdf.format(cal.getTime());
            int count = countMap.containsKey(dayKey) ? countMap.get(dayKey) : 0;
            entries.add(new BarEntry(i, count));
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(Color.parseColor("#1D9E75"));
        dataSet.setValueTextColor(Color.parseColor("#4A6658"));
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);
        chartDashboardWeek.setData(barData);
        chartDashboardWeek.getXAxis().setValueFormatter(
                new IndexAxisValueFormatter(dayLabels));
        chartDashboardWeek.getXAxis().setGranularity(1f);
        chartDashboardWeek.invalidate();
    }

    private void updateTodayProgress() {
        Integer count = viewModel.getTodayCount().getValue();
        int safeCount = count != null ? count : 0;
        int safeGoal  = Math.max(dailyGoal, 1);

        tvTodayCountLabel.setText(safeCount + " of " + dailyGoal + " goal");

        int percent = Math.min((int)(((double) safeCount / safeGoal) * 100), 100);
        progressToday.setProgress(percent);

        int colorRes;
        if (safeCount < safeGoal * 0.7) {
            colorRes = R.color.progress_fill_safe;
        } else if (safeCount < safeGoal) {
            colorRes = R.color.progress_fill_warning;
        } else {
            colorRes = R.color.progress_fill_danger;
        }
        progressToday.setIndicatorColor(getResources().getColor(colorRes, null));
    }

    private void startSmokeFreeTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (tvSmokeFreeTimer == null) return;
                long elapsed = System.currentTimeMillis() - viewModel.getQuitDateMillis();
                tvSmokeFreeTimer.setText(StatsFormatter.formatElapsed(elapsed));
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(timerRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timerRunnable != null) handler.removeCallbacks(timerRunnable);
    }
}