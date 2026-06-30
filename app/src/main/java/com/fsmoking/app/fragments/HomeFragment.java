package com.fsmoking.app.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.fsmoking.app.R;
import com.fsmoking.app.models.UserData;
import com.fsmoking.app.utils.AppStorage;
import com.fsmoking.app.utils.StatsCalculator;
import java.util.Locale;
import java.util.Random;

public class HomeFragment extends Fragment {

    private static final String[] TIPS = {
            "Every cigarette not smoked adds minutes back to your life.",
            "Cravings are like waves — they peak and pass within minutes.",
            "Your lungs are already healing right now.",
            "Replace the habit with deep breathing or a short walk.",
            "Celebrate small wins — every smoke-free day matters.",
            "Drink more water — it helps flush nicotine faster.",
            "Tell someone about your journey for extra accountability.",
            "The first 3 days are the hardest. After that, it gets easier.",
            "You're saving money every single hour you don't smoke.",
            "Exercise releases dopamine — nature's best cigarette replacement.",
    };

    private TextView tvDays, tvHours, tvMins, tvSecs;
    private TextView tvMoneySaved, tvCigsAvoided, tvLifeRegained, tvStreak;
    private TextView tvHeaderDays, tvTip;
    private LinearLayout weekContainer;
    private Handler handler;
    private Runnable timerRunnable;
    private AppStorage storage;
    private UserData userData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        storage = new AppStorage(requireContext());
        userData = storage.getUserData();
        handler = new Handler(Looper.getMainLooper());

        tvDays         = view.findViewById(R.id.tv_timer_days);
        tvHours        = view.findViewById(R.id.tv_timer_hours);
        tvMins         = view.findViewById(R.id.tv_timer_mins);
        tvSecs         = view.findViewById(R.id.tv_timer_secs);
        tvHeaderDays   = view.findViewById(R.id.tv_header_days);
        tvMoneySaved   = view.findViewById(R.id.tv_money_saved);
        tvCigsAvoided  = view.findViewById(R.id.tv_cigs_avoided);
        tvLifeRegained = view.findViewById(R.id.tv_life_regained);
        tvStreak       = view.findViewById(R.id.tv_streak);
        weekContainer  = view.findViewById(R.id.week_container);
        tvTip          = view.findViewById(R.id.tv_tip);

        tvTip.setText(TIPS[new Random().nextInt(TIPS.length)]);

        updateStats();
        startTimer();
        buildWeekStreak();

        return view;
    }

    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                updateStats();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(timerRunnable);
    }

    private void updateStats() {
        if (userData == null || getContext() == null) return;
        StatsCalculator.Stats s = StatsCalculator.calculate(userData);
        if (s == null) return;

        tvDays.setText(String.format(Locale.getDefault(), "%02d", s.days));
        tvHours.setText(String.format(Locale.getDefault(), "%02d", s.hours));
        tvMins.setText(String.format(Locale.getDefault(), "%02d", s.mins));
        tvSecs.setText(String.format(Locale.getDefault(), "%02d", s.secs));
        tvHeaderDays.setText(s.days + " days");

        String cur = userData.getCurrency();
        tvMoneySaved.setText(String.format(Locale.getDefault(), "%s%.2f", cur, s.moneySaved));
        tvCigsAvoided.setText(String.valueOf(s.cigsAvoided));

        int lifeH = s.minutesRegained / 60;
        int lifeM = s.minutesRegained % 60;
        tvLifeRegained.setText(lifeH + "h " + lifeM + "m");
        tvStreak.setText((int) s.elapsedDays + "d 🔥");
    }

    private void buildWeekStreak() {
        if (weekContainer == null) return;
        weekContainer.removeAllViews();
        String[] dayLabels = {"M","T","W","T","F","S","S"};
        boolean[] streak = StatsCalculator.getWeekStreak(userData);
        int todayIdx = StatsCalculator.getTodayIndex();

        for (int i = 0; i < 7; i++) {
            View dotView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_day_dot, weekContainer, false);
            TextView tv = dotView.findViewById(R.id.tv_day);
            View circle = dotView.findViewById(R.id.v_circle);
            boolean isToday = i == todayIdx;
            boolean done = streak[i];

            if (isToday) {
                circle.setBackgroundResource(R.drawable.circle_primary);
                tv.setText(dayLabels[i]);
                tv.setTextColor(getResources().getColor(R.color.white, null));
            } else if (done) {
                circle.setBackgroundResource(R.drawable.circle_light);
                tv.setText("✓");
                tv.setTextColor(getResources().getColor(R.color.primary_dark, null));
            } else {
                circle.setBackgroundResource(R.drawable.circle_surface);
                tv.setText(dayLabels[i]);
                tv.setTextColor(getResources().getColor(R.color.text_muted, null));
            }
            weekContainer.addView(dotView);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null && timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }
    }
}