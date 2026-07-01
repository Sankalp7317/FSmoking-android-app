public List<CigaretteDao.HourCount> getHourlyCountSync(long start, long end) {
    return cigaretteDao.getHourlyCountSync(start, end);
}

public List<CigaretteDao.DayCount> getDailyCountSync(long start, long end) {
    return cigaretteDao.getDailyCountSync(start, end);
}

public int getTotalCountSync(long start, long end) {
    return cigaretteDao.getTotalCountSync(start, end);
}

public LiveData<Integer> getTotalAllTimeLive() {
    return cigaretteDao.getTotalAllTimeLive();
}