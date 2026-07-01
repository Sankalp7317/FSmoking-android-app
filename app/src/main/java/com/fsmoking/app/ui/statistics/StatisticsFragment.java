package com.fsmoking.app.ui.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.fsmoking.app.R;
import com.fsmoking.app.data.dao.CigaretteDao;
import com.fsmoking.app.viewmodel.StatisticsViewModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsFragment extends Fragment {

    private StatisticsViewModel viewModel;

    private BarChart barChart7Days;
    private LineChart lineChart30Days;
    private BarChart barChartPeakHours;

    private TextView tvAvgPerDay;
    private TextView tvTotalMonth;
    private TextView tvMoneyMonth;
    private TextView tvBestDay;
    private TextView tvWorstDay;
    private TextView tvPeakHour;
    private TextView tvTotalAllTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(StatisticsViewModel.class);

        bindViews(view);
        setupCharts();
        observeViewModel();

        viewModel.loadStats();
    }

    private void bindViews(View view) {
        barChart7Days      = view.findViewById(R.id.chart_7days);
        lineChart30Days    = view.findViewById(R.id.chart_30days);
        barChartPeakHours  = view.findViewById(R.id.chart_peak_hours);
        tvAvgPerDay        = view.findViewById(R.id.tv_avg_per_day);
        tvTotalMonth       = view.findViewById(R.id.tv_total_month);
        tvMoneyMonth       = view.findViewById(R.id.tv_money_month);
        tvBestDay          = view.findViewById(R.id.tv_best_day);
        tvWorstDay         = view.findViewById(R.id.tv_worst_day);
        tvPeakHour         = view.findViewById(R.id.tv_peak_hour);
        tvTotalAllTime     = view.findViewById(R.id.tv_total_all_time);
    }

    private void setupCharts() {
        styleBarChart(barChart7Days);
        styleBarChart(barChartPeakHours);
        styleLineChart(lineChart30Days);
    }

    private void styleBarChart(BarChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBorders(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisLeft().setTextColor(Color.parseColor("#7A9589"));
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setTextColor(Color.parseColor("#7A9589"));
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setScaleEnabled(false);
        chart.animateY(600);
    }

    private void styleLineChart(LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBorders(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisLeft().setTextColor(Color.parseColor("#7A9589"));
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setTextColor(Color.parseColor("#7A9589"));
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setScaleEnabled(false);
        chart.animateX(800);
    }

    private void observeViewModel() {
        viewModel.getTotalAllTime().observe(getViewLifecycleOwner(), total ->
                tvTotalAllTime.setText(String.valueOf(total != null ? total : 0)));

        viewModel.getStatsData().observe(getViewLifecycleOwner(), data -> {
            if (data == null) return;

            tvAvgPerDay.setText(String.format(Locale.getDefault(), "%.1f", data.avgPerDay));
            tvTotalMonth.setText(String.valueOf(data.totalThisMonth));
            tvMoneyMonth.setText(String.format(Locale.getDefault(), "%.2f", data.moneySpentThisMonth));
            tvBestDay.setText(String.valueOf(data.bestDayCount));
            tvWorstDay.setText(String.valueOf(data.worstDayCount));
            tvPeakHour.setText(data.peakHourLabel);

            render7DaysChart(data.last7Days);
            render30DaysChart(data.last30Days);
            renderPeakHoursChart(data.peakHours);
        });
    }

    private void render7DaysChart(List<CigaretteDao.DayCount> dayCounts) {
        // Build a full 7-day map (fills 0 for missing days)
        String[] dayLabels = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Map<String, Integer> countMap = new HashMap<>();
        for (CigaretteDao.DayCount dc : dayCounts) countMap.put(dc.day, dc.count);

        List<BarEntry> entries = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        // Go back to Monday
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
        barChart7Days.setData(barData);
        barChart7Days.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dayLabels));
        barChart7Days.getXAxis().setGranularity(1f);
        barChart7Days.invalidate();
    }

    private void render30DaysChart(List<CigaretteDao.DayCount> dayCounts) {
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        SimpleDateFormat labelFmt = new SimpleDateFormat("dd/MM", Locale.getDefault());
        SimpleDateFormat keyFmt   = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Map<String, Integer> countMap = new HashMap<>();
        for (CigaretteDao.DayCount dc : dayCounts) countMap.put(dc.day, dc.count);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -29);
        for (int i = 0; i < 30; i++) {
            String key = keyFmt.format(cal.getTime());
            int count = countMap.containsKey(key) ? countMap.get(key) : 0;
            entries.add(new Entry(i, count));
            labels.add(i % 5 == 0 ? labelFmt.format(cal.getTime()) : "");
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setColor(Color.parseColor("#1D9E75"));
        dataSet.setCircleColor(Color.parseColor("#1D9E75"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setFillAlpha(40);
        dataSet.setFillColor(Color.parseColor("#1D9E75"));
        dataSet.setDrawFilled(true);

        LineData lineData = new LineData(dataSet);
        lineChart30Days.setData(lineData);
        lineChart30Days.getXAxis().setValueFormatter(new IndexAxisValueFormatter(
                labels.toArray(new String[0])));
        lineChart30Days.getXAxis().setGranularity(1f);
        lineChart30Days.invalidate();
    }

    private void renderPeakHoursChart(List<CigaretteDao.HourCount> hourCounts) {
        Map<Integer, Integer> countMap = new HashMap<>();
        for (CigaretteDao.HourCount hc : hourCounts) {
            countMap.put(Integer.parseInt(hc.hour), hc.count);
        }

        List<BarEntry> entries = new ArrayList<>();
        String[] labels = new String[24];
        for (int h = 0; h < 24; h++) {
            entries.add(new BarEntry(h, countMap.containsKey(h) ? countMap.get(h) : 0));
            labels[h] = h == 0 ? "12a" : h < 12 ? h + "a" : h == 12 ? "12p" : (h-12) + "p";
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(Color.parseColor("#185FA5"));
        dataSet.setDrawValues(false);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.7f);
        barChartPeakHours.setData(barData);
        barChartPeakHours.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChartPeakHours.getXAxis().setGranularity(1f);
        barChartPeakHours.getXAxis().setLabelCount(8, true);
        barChartPeakHours.invalidate();
    }
}