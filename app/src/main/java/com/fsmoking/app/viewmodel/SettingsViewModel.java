package com.fsmoking.app.viewmodel;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.fsmoking.app.data.AppDatabase;
import com.fsmoking.app.data.entity.CigaretteLog;
import com.fsmoking.app.repository.CigaretteRepository;
import com.fsmoking.app.repository.SettingsRepository;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SettingsViewModel extends AndroidViewModel {

    private final SettingsRepository settingsRepository;
    private final CigaretteRepository cigaretteRepository;

    public final MutableLiveData<String> exportResult = new MutableLiveData<>(null);
    public final MutableLiveData<Boolean> resetDone = new MutableLiveData<>(null);

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        settingsRepository  = new SettingsRepository(application);
        cigaretteRepository = new CigaretteRepository(application);
    }

    // ── Getters ─────────────────────────────────────────────────────────────

    public float getPackPrice()   { return settingsRepository.getPackPrice(); }
    public int   getCigsPerPack() { return settingsRepository.getCigsPerPack(); }
    public int   getDailyGoal()   { return settingsRepository.getDailyGoal(); }
    public String getCurrency()   { return settingsRepository.getCurrency(); }

    // ── Setters ─────────────────────────────────────────────────────────────

    public void saveSettings(float packPrice, int cigsPerPack,
                             int dailyGoal, String currency) {
        settingsRepository.setPackPrice(packPrice);
        settingsRepository.setCigsPerPack(cigsPerPack);
        settingsRepository.setDailyGoal(dailyGoal);
        settingsRepository.setCurrency(currency.trim().isEmpty() ? "₹" : currency.trim());
    }

    // ── Export CSV ──────────────────────────────────────────────────────────

    public void exportCsv(Context context) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                List<CigaretteLog> logs = cigaretteRepository.getAllLogsSync();
                if (logs.isEmpty()) {
                    exportResult.postValue("No data to export.");
                    return;
                }

                SimpleDateFormat sdf =
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat fileSdf =
                        new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());

                String fileName = "FSmoking_export_" +
                        fileSdf.format(new Date()) + ".csv";

                File downloadsDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS);
                File file = new File(downloadsDir, fileName);

                FileWriter writer = new FileWriter(file);
                writer.append("ID,Timestamp,Mood,Trigger,Location,Note\n");

                for (CigaretteLog log : logs) {
                    writer.append(String.valueOf(log.getId())).append(",");
                    writer.append(sdf.format(log.getTimestamp())).append(",");
                    writer.append(safe(log.getMood())).append(",");
                    writer.append(safe(log.getTrigger())).append(",");
                    writer.append(safe(log.getLocation())).append(",");
                    writer.append(safe(log.getNote())).append("\n");
                }

                writer.flush();
                writer.close();

                exportResult.postValue("Exported to Downloads/" + fileName);

            } catch (IOException e) {
                exportResult.postValue("Export failed: " + e.getMessage());
            }
        });
    }

    private String safe(String value) {
        if (value == null || value.isEmpty()) return "";
        // Escape commas and quotes for CSV
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // ── Reset all data ──────────────────────────────────────────────────────

    public void resetAllData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            cigaretteRepository.deleteAll();
            AppDatabase db = AppDatabase.getInstance(getApplication());
            db.achievementDao().insertAll(new java.util.ArrayList<>());

            // Reset all achievements to locked
            List<com.fsmoking.app.data.entity.Achievement> all =
                    db.achievementDao().getAllSync();
            for (com.fsmoking.app.data.entity.Achievement a : all) {
                a.setUnlocked(false);
                a.setUnlockedAt(null);
                db.achievementDao().update(a);
            }

            settingsRepository.setOnboardingDone(false);
            settingsRepository.setQuitDateMillis(System.currentTimeMillis());
            resetDone.postValue(true);
        });
    }
}