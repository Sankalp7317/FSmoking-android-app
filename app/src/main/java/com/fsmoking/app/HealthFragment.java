package com.fsmoking.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.fsmoking.app.R;
import com.fsmoking.app.models.UserData;
import com.fsmoking.app.utils.AppStorage;
import com.fsmoking.app.utils.StatsCalculator;

public class HealthFragment extends Fragment {

    private static final Object[][] MILESTONES = {
            {"❤️",  "Heart rate normalises",   "Within 20 minutes of quitting", 20.0 / 1440},
            {"💨",  "Carbon monoxide cleared", "Within 12 hours",               0.5},
            {"👅",  "Taste & smell improve",   "After 48 hours",                2.0},
            {"🫁",  "Lung function improves", "After 2 weeks",                 14.0},
            {"🩸",  "Circulation improves",    "Within 2–12 weeks",             84.0},
            {"🛡️", "Cancer risk halved",      "After 5 years smoke-free",      1825.0},
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_health, container, false);
        LinearLayout milestoneContainer = view.findViewById(R.id.milestone_container);

        AppStorage storage = new AppStorage(requireContext());
        UserData userData = storage.getUserData();
        StatsCalculator.Stats stats = StatsCalculator.calculate(userData);
        double elapsedDays = stats != null ? stats.elapsedDays : 0;

        for (Object[] m : MILESTONES) {
            String icon       = (String) m[0];
            String title      = (String) m[1];
            String desc       = (String) m[2];
            double threshold  = (double) m[3];

            boolean unlocked  = elapsedDays >= threshold;
            double progress   = Math.min(elapsedDays / threshold, 1.0);

            View card = inflater.inflate(R.layout.item_health_milestone, milestoneContainer, false);

            TextView tvIcon  = card.findViewById(R.id.tv_milestone_icon);
            TextView tvTitle = card.findViewById(R.id.tv_milestone_title);
            TextView tvDesc  = card.findViewById(R.id.tv_milestone_desc);
            TextView tvBadge = card.findViewById(R.id.tv_milestone_badge);
            TextView tvPct   = card.findViewById(R.id.tv_milestone_pct);
            LinearProgressIndicator progressBar = card.findViewById(R.id.milestone_progress);
            View cardBg = card.findViewById(R.id.milestone_card_bg);

            tvIcon.setText(icon);
            tvTitle.setText(title);
            tvDesc.setText(desc);
            progressBar.setProgress((int) (progress * 100));

            if (unlocked) {
                tvBadge.setVisibility(View.VISIBLE);
                tvPct.setVisibility(View.GONE);
                progressBar.setIndicatorColor(getResources().getColor(R.color.primary, null));
                cardBg.setBackgroundResource(R.drawable.card_green_bg);
            } else {
                tvBadge.setVisibility(View.GONE);
                tvPct.setVisibility(View.VISIBLE);
                tvPct.setText((int) (progress * 100) + "% complete");
                progressBar.setIndicatorColor(getResources().getColor(R.color.amber, null));
            }

            milestoneContainer.addView(card);
        }

        return view;
    }
}