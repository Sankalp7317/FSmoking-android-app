package com.fsmoking.app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.fsmoking.app.data.entity.CigaretteLog;
import com.fsmoking.app.repository.CigaretteRepository;
import java.util.List;

public class HistoryViewModel extends AndroidViewModel {

    private final CigaretteRepository repository;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final LiveData<List<CigaretteLog>> filteredLogs;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        repository = new CigaretteRepository(application);

        filteredLogs = Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.trim().isEmpty()) {
                return repository.getAllLogsLive();
            }
            return repository.searchLogsLive(query.trim());
        });
    }

    public LiveData<List<CigaretteLog>> getFilteredLogs() {
        return filteredLogs;
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void delete(CigaretteLog log) {
        repository.delete(log);
    }

    public void update(CigaretteLog log) {
        repository.update(log);
    }
}