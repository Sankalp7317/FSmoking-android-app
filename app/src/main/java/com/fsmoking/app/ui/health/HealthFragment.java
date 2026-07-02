package com.fsmoking.app.ui.health;

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
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.fsmoking.app.R;
import com.fsmoking.app.repository.SettingsRepository;
import com.fsmoking.app.util.StatsFormatter;

public class HealthFragment extends Fragment {

    /**
     * Each milestone: { emoji, title, description, threshold_in_minutes }
     */
    private static final Object[][] MILESTONES = {
            {"❤️",  "Heart rate normalises",
                    "Your heart rate and blood pressure begin to drop.",
                    20.0},
            {"🫁",  "Oxygen levels improve",
                    "Carbon monoxide levels in your blood drop to normal.",
                    480.0},
            {"💨",  "Breathing improves",
                    "Lung function begins to improve noticeably.",
                    720.0},
            {"👅",  "Taste & smell return",
                    "Your senses of taste and smell start to recover.",
                    2880.0},
            {"🩺",  "Nicotine cleared",
                    "Nicotine and most of its by-products leave your body.",
                    4320.0},
            {"🏃",  "Energy levels up",
                    "Circulation improves and physical activity gets easier.",
                    20160.0},
            {"🫀",  "Cilia regenerate",
                    "Cilia in your lungs recover, reducing infection risk.",
                    129600.0},
            {"💪",  "Lung capacity improves",
                    "Lung function increases by up to 10% compared to peak smoking.",
                    262800.0},
            {"🧬",  "Heart disease risk halved",
                    "Your risk of coronary heart disease is now half that of a smoker.",
                    525600.0},
            {"🏆",  "Stroke risk normalised",
                    "Your risk of stroke is the same as someone who has never smoked.",
                    2628000.0},
    };

    private LinearLayout milestoneContainer;
    private TextView tvSmokeFreeTime;
    private SettingsRepository settingsRepository;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_health, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        settingsRepository = new SettingsRepository(requireContext());
        milestoneContainer = view.findViewById(R.id.milestone_container);
        tvSmokeFreeTime    = view.findViewById(R.id.tv_health_smoke_free_time);

        renderMilestones();
        startTimer();
    }

    private void renderMilestones() {
        if (milestoneContainer == null) return;
        milestoneContainer.removeAllViews();

        long elapsedMs = System.currentTimeMillis() - settingsRepository.getQuitDateMillis();
        double elapsedMinutes = elapsedMs / 60000.0;

        for (Object[] m : MILESTONES) {
            String emoji     = (String) m[0];
            String title     = (String) m[1];
            String desc      = (String) m[2];
            double threshold = (double) m[3];

            boolean unlocked = elapsedMinutes >= threshold;
            double progress  = Math.min(elapsedMinutes / threshold, 1.0);

            View card = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_health_milestone, milestoneContainer, false);

            TextView tvEmoji   = card.findViewById(R.id.tv_milestone_emoji);
            TextView tvTitle   = card.findViewById(R.id.tv_milestone_title);
            TextView tvDesc    = card.findViewById(R.id.tv_milestone_desc);
            TextView tvStatus  = card.findViewById(R.id.tv_milestone_status);
            TextView tvTime    = card.findViewById(R.id.tv_milestone_time);
            LinearProgressIndicator progressBar = card.findViewById(R.id.milestone_progress);
            View cardRoot = card.findViewById(R.id.milestone_card_root);

            tvEmoji.setText(emoji);
            tvTitle.setText(title);
            tvDesc.setText(desc);
            progressBar.setProgress((int)(progress * 100));

            if (unlocked) {
                tvStatus.setText("✓ Unlocked");
                tvStatus.setTextColor(requireContext().getColor(R.color.primary));
                tvTime.setVisibility(View.GONE);
                progressBar.setIndicatorColor(requireContext().getColor(R.color.primary));
                cardRoot.setBackgroundResource(R.drawable.card_green_bg);
            } else {
                tvStatus.setText((int)(progress * 100) + "% complete");
                tvStatus.setTextColor(requireContext().getColor(R.color.text_muted));
                progressBar.setIndicatorColor(requireContext().getColor(R.color.amber));
                tvTime.setVisibility(View.VISIBLE);
                tvTime.setText(formatTimeRemaining(threshold - elapsedMinutes));
            }

            milestoneContainer.addView(card);
        }
    }

    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (tvSmokeFreeTime == null) return;
                long elapsed = System.currentTimeMillis() - settingsRepository.getQuitDateMillis();
                tvSmokeFreeTime.setText(StatsFormatter.formatElapsed(elapsed));
                renderMilestones();
                handler.postDelayed(this, 60000); // update every minute
            }
        };
        handler.post(timerRunnable);
    }

    private String formatTimeRemaining(double minutesLeft) {
        if (minutesLeft <= 0) return "";
        long mins = (long) minutesLeft;
        if (mins < 60) return "in " + mins + " min";
        long hours = mins / 60;
        if (hours < 24) return "in " + hours + "h " + (mins % 60) + "m";
        long days = hours / 24;
        if (days < 365) return "in " + days + " day" + (days == 1 ? "" : "s");
        long years = days / 365;
        return "in " + years + " year" + (years == 1 ? "" : "s");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timerRunnable != null) handler.removeCallbacks(timerRunnable);
    }
}