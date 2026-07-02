package com.fsmoking.app.ui.achievements;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.fsmoking.app.R;
import com.fsmoking.app.data.entity.Achievement;
import com.fsmoking.app.viewmodel.AchievementsViewModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AchievementsFragment extends Fragment {

    private AchievementsViewModel viewModel;
    private LinearLayout earnedContainer;
    private LinearLayout lockedContainer;
    private TextView tvSubtitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_achievements, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AchievementsViewModel.class);

        earnedContainer = view.findViewById(R.id.container_earned);
        lockedContainer = view.findViewById(R.id.container_locked);
        tvSubtitle      = view.findViewById(R.id.tv_achievements_subtitle);

        viewModel.getAllAchievements().observe(getViewLifecycleOwner(), this::renderAchievements);

        viewModel.getUnlockedCount().observe(getViewLifecycleOwner(), count -> {
            int total = 12;
            tvSubtitle.setText((count != null ? count : 0) + " of " + total + " earned");
        });
    }

    private void renderAchievements(List<Achievement> achievements) {
        if (achievements == null) return;
        earnedContainer.removeAllViews();
        lockedContainer.removeAllViews();

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        for (Achievement a : achievements) {
            View card = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_achievement, null);

            TextView tvEmoji  = card.findViewById(R.id.tv_achievement_emoji);
            TextView tvTitle  = card.findViewById(R.id.tv_achievement_title);
            TextView tvDesc   = card.findViewById(R.id.tv_achievement_desc);
            TextView tvDate   = card.findViewById(R.id.tv_achievement_date);
            View cardRoot     = card.findViewById(R.id.achievement_card_root);

            tvEmoji.setText(a.getIconEmoji());
            tvTitle.setText(a.getTitle());
            tvDesc.setText(a.getDescription());

            if (a.isUnlocked()) {
                tvEmoji.setAlpha(1f);
                tvTitle.setTextColor(requireContext().getColor(R.color.text_primary));
                cardRoot.setBackgroundResource(R.drawable.card_achievement_unlocked);
                if (a.getUnlockedAt() != null) {
                    tvDate.setVisibility(View.VISIBLE);
                    tvDate.setText("Unlocked " + sdf.format(new Date(a.getUnlockedAt())));
                } else {
                    tvDate.setVisibility(View.GONE);
                }
                earnedContainer.addView(card);
            } else {
                tvEmoji.setAlpha(0.25f);
                tvTitle.setTextColor(requireContext().getColor(R.color.text_muted));
                tvDate.setVisibility(View.GONE);
                lockedContainer.addView(card);
            }
        }
    }
}