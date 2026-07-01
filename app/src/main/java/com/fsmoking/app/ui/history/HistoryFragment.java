package com.fsmoking.app.ui.history;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.fsmoking.app.R;
import com.fsmoking.app.viewmodel.HistoryViewModel;

public class HistoryFragment extends Fragment {

    private HistoryViewModel viewModel;
    private HistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(HistoryViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.rv_history);
        TextView tvEmpty = view.findViewById(R.id.tv_empty_history);
        TextInputEditText etSearch = view.findViewById(R.id.et_search);

        adapter = new HistoryAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(log -> {
            LogDetailBottomSheet sheet = LogDetailBottomSheet.newInstance(log);
            sheet.show(getParentFragmentManager(), "edit_log");
        });

        adapter.setOnItemDeleteListener(log ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete entry")
                        .setMessage("Remove this cigarette from your history?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete", (d, w) -> viewModel.delete(log))
                        .show()
        );

        viewModel.getFilteredLogs().observe(getViewLifecycleOwner(), logs -> {
            adapter.submitList(logs);
            boolean empty = logs == null || logs.isEmpty();
            tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                viewModel.setSearchQuery(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
}