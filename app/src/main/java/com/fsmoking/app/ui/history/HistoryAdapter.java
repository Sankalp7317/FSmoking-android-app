package com.fsmoking.app.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.fsmoking.app.R;
import com.fsmoking.app.data.entity.CigaretteLog;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class HistoryAdapter extends ListAdapter<CigaretteLog, HistoryAdapter.LogViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(CigaretteLog log);
    }

    public interface OnItemDeleteListener {
        void onItemDelete(CigaretteLog log);
    }

    private OnItemClickListener clickListener;
    private OnItemDeleteListener deleteListener;

    private static final SimpleDateFormat TIME_FMT =
            new SimpleDateFormat("hh:mm a", Locale.getDefault());
    private static final SimpleDateFormat DATE_FMT =
            new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());

    public HistoryAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cigarette_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class LogViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTime, tvDate, tvMood, tvTrigger, tvLocation, tvNote;
        private final View btnDelete;

        LogViewHolder(View itemView) {
            super(itemView);
            tvTime     = itemView.findViewById(R.id.tv_log_time);
            tvDate     = itemView.findViewById(R.id.tv_log_date);
            tvMood     = itemView.findViewById(R.id.tv_log_mood);
            tvTrigger  = itemView.findViewById(R.id.tv_log_trigger);
            tvLocation = itemView.findViewById(R.id.tv_log_location);
            tvNote     = itemView.findViewById(R.id.tv_log_note);
            btnDelete  = itemView.findViewById(R.id.btn_log_delete);
        }

        void bind(CigaretteLog log) {
            tvTime.setText(TIME_FMT.format(log.getTimestamp()));
            tvDate.setText(DATE_FMT.format(log.getTimestamp()));

            setOrHide(tvMood,     log.getMood(),     "😤 ");
            setOrHide(tvTrigger,  log.getTrigger(),  "⚡ ");
            setOrHide(tvLocation, log.getLocation(), "📍 ");
            setOrHide(tvNote,     log.getNote(),     "📝 ");

            itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onItemClick(log);
            });

            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) deleteListener.onItemDelete(log);
            });
        }

        private void setOrHide(TextView tv, String value, String prefix) {
            if (value != null && !value.trim().isEmpty()) {
                tv.setText(prefix + value);
                tv.setVisibility(View.VISIBLE);
            } else {
                tv.setVisibility(View.GONE);
            }
        }
    }

    private static final DiffUtil.ItemCallback<CigaretteLog> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CigaretteLog>() {
                @Override
                public boolean areItemsTheSame(@NonNull CigaretteLog a, @NonNull CigaretteLog b) {
                    return a.getId() == b.getId();
                }
                @Override
                public boolean areContentsTheSame(@NonNull CigaretteLog a, @NonNull CigaretteLog b) {
                    return a.getId() == b.getId()
                            && a.getTimestamp().equals(b.getTimestamp())
                            && equals(a.getMood(), b.getMood())
                            && equals(a.getTrigger(), b.getTrigger())
                            && equals(a.getLocation(), b.getLocation())
                            && equals(a.getNote(), b.getNote());
                }
                private boolean equals(String a, String b) {
                    if (a == null && b == null) return true;
                    if (a == null || b == null) return false;
                    return a.equals(b);
                }
            };
}