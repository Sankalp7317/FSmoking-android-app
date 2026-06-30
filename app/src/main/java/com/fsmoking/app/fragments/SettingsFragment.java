package com.fsmoking.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.fsmoking.app.OnboardingActivity;
import com.fsmoking.app.R;
import com.fsmoking.app.models.UserData;
import com.fsmoking.app.utils.AppStorage;
import com.fsmoking.app.utils.StatsCalculator;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        AppStorage storage = new AppStorage(requireContext());
        UserData userData = storage.getUserData();

        if (userData != null) {
            ((TextView) view.findViewById(R.id.tv_quit_date_value))
                    .setText(userData.getQuitDate());
            ((TextView) view.findViewById(R.id.tv_cpd_value))
                    .setText((int) userData.getCigsPerDay() + " cigarettes/day");
            ((TextView) view.findViewById(R.id.tv_pack_cost_value))
                    .setText(userData.getCurrency() + userData.getPackCost() + " per pack");

            StatsCalculator.Stats stats = StatsCalculator.calculate(userData);
            if (stats != null) {
                ((TextView) view.findViewById(R.id.tv_total_saved_value))
                        .setText(userData.getCurrency() +
                                String.format("%.2f", stats.moneySaved) + " saved so far");
            }
        }

        view.findViewById(R.id.btn_reset).setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Reset All Data")
                    .setMessage("This will erase all your progress. Are you sure?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Reset", (d, w) -> {
                        storage.clearAll();
                        startActivity(new Intent(requireContext(), OnboardingActivity.class));
                        requireActivity().finish();
                    })
                    .show();
        });

        return view;
    }
}