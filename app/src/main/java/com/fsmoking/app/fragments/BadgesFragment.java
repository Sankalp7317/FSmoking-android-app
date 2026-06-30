package com.fsmoking.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.fsmoking.app.R;
import com.fsmoking.app.models.UserData;
import com.fsmoking.app.utils.AppStorage;
import com.fsmoking.app.utils.StatsCalculator;

public class BadgesFragment extends Fragment {

    private static final Object[][] ALL_BADGES = {
            {"🌅", "First Sunrise",     "24 hours smoke-free",   1.0 / 24},
            {"💪", "3-Day Warrior",      "72 hours strong",        3.0},
            {"🌿", "One Week Wonder",    "7 days clean",           7.0},
            {"🏆", "Two-Week Titan",     "14 days smoke-free",    14.0},
            {"📅", "One Month Hero",     "30 days smoke-free",    30.0},
            {"🌟", "Quarter Champion",   "3 months smoke-free",   90.0},
            {"🎖️","Half-Year Legend",   "6 months smoke-free",  180.0},
            {"🏅", "One Year Champion",  "365 days smoke-free",  365.0},
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_badges, container, false);

        GridLayout earnedGrid   = view.findViewById(R.id.grid_earned);
        GridLayout upcomingGrid = view.findViewById(R.id.grid_upcoming);
        TextView tvSubtitle     = view.findViewById(R.id.tv_badges_subtitle);

        AppStorage storage = new AppStorage(requireContext());
        UserData userData = storage.getUserData();
        StatsCalculator.Stats stats = StatsCalculator.calculate(userData);
        double elapsedDays = stats != null ? stats.elapsedDays : 0;

        int earnedCount = 0;

        for (Object[] b : ALL_BADGES) {
            String emoji = (String) b[0];
            String name  = (String) b[1];
            String desc  = (String) b[2];
            double threshold = (double) b[3];

            boolean earned = elapsedDays >= threshold;
            if (earned) earnedCount++;

            View card = inflater.inflate(R.layout.item_badge, null);
            TextView tvEmoji = card.findViewById(R.id.tv_badge_emoji);
            TextView tvName  = card.findViewById(R.id.tv_badge_name);
            TextView tvDesc  = card.findViewById(R.id.tv_badge_desc);
            View cardRoot    = card.findViewById(R.id.badge_card_root);

            tvEmoji.setText(emoji);
            tvName.setText(name);
            tvDesc.setText(desc);

            if (earned) {
                tvEmoji.setAlpha(1f);
                tvName.setTextColor(getResources().getColor(R.color.text_primary, null));
                cardRoot.setBackgroundResource(R.drawable.card_green_bg);
                earnedGrid.addView(card);
            } else {
                tvEmoji.setAlpha(0.25f);
                tvName.setTextColor(getResources().getColor(R.color.text_muted, null));
                upcomingGrid.addView(card);
            }
        }

        tvSubtitle.setText(earnedCount + " earned · " +
                (ALL_BADGES.length - earnedCount) + " to unlock");

        return view;
    }
}