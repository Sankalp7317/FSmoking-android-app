package com.fsmoking.app.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.fsmoking.app.R;
import com.fsmoking.app.data.entity.CigaretteLog;
import com.fsmoking.app.viewmodel.DashboardViewModel;
import com.fsmoking.app.viewmodel.HistoryViewModel;

/**
 * Bottom sheet for logging a new cigarette (with optional details)
 * or editing an existing one.
 * Pass null for newInstance() to log fresh; pass a CigaretteLog to edit.
 */
public class LogDetailBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_LOG_ID = "log_id";

    private static final String[] MOODS = {
            "Stressed", "Bored", "Happy", "Anxious", "Relaxed", "Angry", "Sad", "After meal"
    };

    private static final String[] TRIGGERS = {
            "After meal", "Coffee", "Work break", "Social", "Alcohol", "Stress", "Habit", "Boredom"
    };

    private CigaretteLog existingLog = null;
    private AutoCompleteTextView etMood, etTrigger;
    private TextInputEditText etLocation, etNote;

    public static LogDetailBottomSheet newInstance(@Nullable CigaretteLog log) {
        LogDetailBottomSheet sheet = new LogDetailBottomSheet();
        if (log != null) {
            Bundle args = new Bundle();
            args.putLong(ARG_LOG_ID, log.getId());
            sheet.setArguments(args);
        }
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_log_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etMood     = view.findViewById(R.id.et_mood);
        etTrigger  = view.findViewById(R.id.et_trigger);
        etLocation = view.findViewById(R.id.et_location);
        etNote     = view.findViewById(R.id.et_note);
        MaterialButton btnSave   = view.findViewById(R.id.btn_save_log);
        MaterialButton btnCancel = view.findViewById(R.id.btn_cancel_log);

        // Autocomplete dropdowns
        ArrayAdapter<String> moodAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, MOODS);
        etMood.setAdapter(moodAdapter);

        ArrayAdapter<String> triggerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, TRIGGERS);
        etTrigger.setAdapter(triggerAdapter);

        // Pre-fill if editing
        if (getArguments() != null && getArguments().containsKey(ARG_LOG_ID)) {
            HistoryViewModel historyVm = new ViewModelProvider(requireActivity())
                    .get(HistoryViewModel.class);
            historyVm.getFilteredLogs().observe(getViewLifecycleOwner(), logs -> {
                if (logs == null) return;
                long targetId = getArguments().getLong(ARG_LOG_ID);
                for (CigaretteLog l : logs) {
                    if (l.getId() == targetId) {
                        existingLog = l;
                        prefill(l);
                        break;
                    }
                }
            });
        }

        btnSave.setOnClickListener(v -> save());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void prefill(CigaretteLog log) {
        if (log.getMood() != null)     etMood.setText(log.getMood());
        if (log.getTrigger() != null)  etTrigger.setText(log.getTrigger());
        if (log.getLocation() != null) etLocation.setText(log.getLocation());
        if (log.getNote() != null)     etNote.setText(log.getNote());
    }

    private void save() {
        String mood     = text(etMood);
        String trigger  = text(etTrigger);
        String location = etLocation.getText() != null
                ? etLocation.getText().toString().trim() : null;
        String note     = etNote.getText() != null
                ? etNote.getText().toString().trim() : null;

        if (existingLog != null) {
            existingLog.setMood(mood);
            existingLog.setTrigger(trigger);
            existingLog.setLocation(location);
            existingLog.setNote(note);
            new ViewModelProvider(requireActivity())
                    .get(HistoryViewModel.class).update(existingLog);
        } else {
            DashboardViewModel dashVm = new ViewModelProvider(requireActivity())
                    .get(DashboardViewModel.class);
            dashVm.logCigaretteWithDetails(mood, trigger, location, note);
        }
        dismiss();
    }

    private String text(AutoCompleteTextView tv) {
        String t = tv.getText() != null ? tv.getText().toString().trim() : null;
        return (t != null && t.isEmpty()) ? null : t;
    }
}