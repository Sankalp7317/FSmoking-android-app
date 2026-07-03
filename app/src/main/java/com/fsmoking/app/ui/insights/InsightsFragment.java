package com.fsmoking.app.ui.insights;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.fsmoking.app.R;
import com.fsmoking.app.viewmodel.InsightsViewModel;
import java.util.List;

public class InsightsFragment extends Fragment {

    private InsightsViewModel viewModel;
    private LinearLayout insightContainer;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_insights, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel        = new ViewModelProvider(requireActivity()).get(InsightsViewModel.class);
        insightContainer = view.findViewById(R.id.insight_container);
        progressBar      = view.findViewById(R.id.progress_insights);
        tvEmpty          = view.findViewById(R.id.tv_insights_empty);

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading ->
                progressBar.setVisibility(isLoading != null && isLoading
                        ? View.VISIBLE : View.GONE));

        viewModel.getInsights().observe(getViewLifecycleOwner(),
                (List<InsightsViewModel.Insight> insights) -> renderInsights(insights));

        viewModel.loadInsights();
    }

    private void renderInsights(List<InsightsViewModel.Insight> insightList) {
        if (insightContainer == null) return;
        insightContainer.removeAllViews();

        if (insightList == null || insightList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        tvEmpty.setVisibility(View.GONE);

        for (InsightsViewModel.Insight insight : insightList) {
            View card = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_insight_card, insightContainer, false);

            ((TextView) card.findViewById(R.id.tv_insight_emoji)).setText(insight.emoji);
            ((TextView) card.findViewById(R.id.tv_insight_title)).setText(insight.title);
            ((TextView) card.findViewById(R.id.tv_insight_body)).setText(insight.body);
            card.findViewById(R.id.insight_card_root).setBackgroundResource(
                    insight.isPositive
                            ? R.drawable.card_green_bg
                            : R.drawable.card_neutral_bg);

            insightContainer.addView(card);
        }
    }
}