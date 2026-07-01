package com.fsmoking.app.ui.dashboard;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.fsmoking.app.R;
import com.fsmoking.app.ui.history.LogDetailBottomSheet;
import com.fsmoking.app.util.StatsFormatter;
import com.fsmoking.app.viewmodel.DashboardViewModel;

public class DashboardFragment extends Fragment {

    private DashboardViewModel viewModel;

    private TextView tvSmokeFreeTimer;
    private TextView tvTodayCountLabel;
    private LinearProgressIndicator progressToday;
    private TextView tvWeekCount;
    private TextView tvMonthCount;
    private TextView tvMoneySpentToday;
    private TextView tvAvgInterval;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private int dailyGoal = 10;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        bindViews(view);
        observeViewModel();
        startSmokeFreeTimer();

        view.findViewById(R.id.fab_log_cigarette).setOnClickListener(v -> {
            LogDetailBottomSheet sheet = LogDetailBottomSheet.newInstance(null);
            sheet.show(getParentFragmentManager(), "log_detail");
        });
    }

    private void bindViews(View view) {
        tvSmokeFreeTimer  = view.findViewById(R.id.tv_smoke_free_timer);
        tvTodayCountLabel = view.findViewById(R.id.tv_today_count_label);
        progressToday     = view.findViewById(R.id.progress_today);
        tvWeekCount       = view.findViewById(R.id.tv_week_count);
        tvMonthCount      = view.findViewById(R.id.tv_month_count);
        tvMoneySpentToday = view.findViewById(R.id.tv_money_spent_today);
        tvAvgInterval     = view.findViewById(R.id.tv_avg_interval);
    }

    private void observeViewModel() {
        viewModel.getDailyGoal().observe(getViewLifecycleOwner(), goal -> {
            dailyGoal = goal != null ? goal : 10;
            updateTodayProgress();
        });

        viewModel.getTodayCount().observe(getViewLifecycleOwner(), count -> updateTodayProgress());

        viewModel.getWeekCount().observe(getViewLifecycleOwner(), count ->
                tvWeekCount.setText(String.valueOf(count != null ? count : 0)));

        viewModel.getMonthCount().observe(getViewLifecycleOwner(), count ->
                tvMonthCount.setText(String.valueOf(count != null ? count : 0)));

        viewModel.getTodayMoneySpent().observe(getViewLifecycleOwner(), money ->
                tvMoneySpentToday.setText(StatsFormatter.formatMoney(
                        viewModel.getCurrencySymbol(), money != null ? money : 0.0)));

        viewModel.getAvgIntervalMillis().observe(getViewLifecycleOwner(), avg ->
                tvAvgInterval.setText(StatsFormatter.formatAvgInterval(avg != null ? avg : 0.0)));
    }

    private void updateTodayProgress() {
        Integer count = viewModel.getTodayCount().getValue();
        int safeCount = count != null ? count : 0;
        int safeGoal  = Math.max(dailyGoal, 1);

        tvTodayCountLabel.setText(safeCount + " " + getString(R.string.of_goal_format, dailyGoal));

        int percent = Math.min((int) (((double) safeCount / safeGoal) * 100), 100);
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