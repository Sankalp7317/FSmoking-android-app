package com.fsmoking.app.fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.fsmoking.app.R;
import com.fsmoking.app.models.Craving;
import com.fsmoking.app.utils.AppStorage;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class SOSFragment extends Fragment {

    private static final String[] SOS_TIPS = {
            "Try 4-7-8 breathing: inhale 4 seconds, hold 7 seconds, exhale 8 seconds.",
            "Drink a large glass of cold water slowly — one sip at a time.",
            "Go for a brisk 5-minute walk outside right now.",
            "Call or text a friend or family member immediately.",
            "Chew some sugar-free gum or eat a healthy snack.",
            "Do 10 push-ups, squats, or jumping jacks to release tension.",
            "Squeeze your fists tight for 30 seconds, then slowly release.",
    };

    private static final int TOTAL_SECS = 300; // 5 minutes

    private View sosButton, activePanel, beatenPanel;
    private TextView tvTimer, tvTip;
    private LinearLayout cravingLogContainer;
    private CountDownTimer countDownTimer;
    private long startTime;
    private AppStorage storage;
    private AnimatorSet pulseSet;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sos, container, false);

        storage = new AppStorage(requireContext());

        sosButton   = view.findViewById(R.id.btn_sos_trigger);
        activePanel = view.findViewById(R.id.panel_active);
        beatenPanel = view.findViewById(R.id.panel_beaten);
        tvTimer     = view.findViewById(R.id.tv_sos_timer);
        tvTip       = view.findViewById(R.id.tv_sos_tip);
        cravingLogContainer = view.findViewById(R.id.craving_log_container);

        tvTip.setText(SOS_TIPS[new Random().nextInt(SOS_TIPS.length)]);

        sosButton.setOnClickListener(v -> startCraving());
        view.findViewById(R.id.btn_beat_craving).setOnClickListener(v -> beatCraving());
        view.findViewById(R.id.btn_another_craving).setOnClickListener(v -> resetToIdle());

        loadTodayLog(view);
        return view;
    }

    private void startCraving() {
        startTime = System.currentTimeMillis();
        sosButton.setVisibility(View.GONE);
        beatenPanel.setVisibility(View.GONE);
        activePanel.setVisibility(View.VISIBLE);

        View circle = requireView().findViewById(R.id.timer_circle);
        ObjectAnimator pulseX = ObjectAnimator.ofFloat(circle, "scaleX", 1f, 1.05f);
        ObjectAnimator pulseY = ObjectAnimator.ofFloat(circle, "scaleY", 1f, 1.05f);
        pulseX.setRepeatMode(ValueAnimator.REVERSE);
        pulseX.setRepeatCount(ValueAnimator.INFINITE);
        pulseX.setDuration(900);
        pulseY.setRepeatMode(ValueAnimator.REVERSE);
        pulseY.setRepeatCount(ValueAnimator.INFINITE);
        pulseY.setDuration(900);
        pulseSet = new AnimatorSet();
        pulseSet.playTogether(pulseX, pulseY);
        pulseSet.start();

        countDownTimer = new CountDownTimer(TOTAL_SECS * 1000L, 1000) {
            @Override
            public void onTick(long ms) {
                int s = (int) (ms / 1000);
                tvTimer.setText(String.format(Locale.getDefault(), "%d:%02d", s / 60, s % 60));
            }
            @Override
            public void onFinish() {
                tvTimer.setText("0:00");
            }
        }.start();
    }

    private void beatCraving() {
        if (countDownTimer != null) countDownTimer.cancel();
        if (pulseSet != null) pulseSet.cancel();

        int duration = (int) ((System.currentTimeMillis() - startTime) / 1000);
        Calendar cal = Calendar.getInstance();
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.getTime());

        storage.addCraving(new Craving(date, time, duration));

        activePanel.setVisibility(View.GONE);
        beatenPanel.setVisibility(View.VISIBLE);

        loadTodayLog(requireView());
    }

    private void resetToIdle() {
        beatenPanel.setVisibility(View.GONE);
        activePanel.setVisibility(View.GONE);
        sosButton.setVisibility(View.VISIBLE);
        tvTip.setText(SOS_TIPS[new Random().nextInt(SOS_TIPS.length)]);
    }

    private void loadTodayLog(View view) {
        if (cravingLogContainer == null) return;
        cravingLogContainer.removeAllViews();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        List<Craving> cravings = storage.getCravings();
        boolean hasToday = false;

        for (Craving c : cravings) {
            if (!today.equals(c.getDate())) continue;
            hasToday = true;
            View row = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_craving_log, cravingLogContainer, false);
            ((TextView) row.findViewById(R.id.tv_craving_time)).setText(c.getTime());
            ((TextView) row.findViewById(R.id.tv_craving_duration))
                    .setText("✓ beaten in " + c.getFormattedDuration());
            cravingLogContainer.addView(row);
        }
        view.findViewById(R.id.tv_today_log_title)
                .setVisibility(hasToday ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}