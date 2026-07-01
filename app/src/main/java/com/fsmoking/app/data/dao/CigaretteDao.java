// Returns count of cigarettes grouped by hour (0-23) for a given period
@Query("SELECT strftime('%H', datetime(timestamp/1000, 'unixepoch', 'localtime')) as hour, " +
        "COUNT(*) as count FROM cigarette_log " +
        "WHERE timestamp BETWEEN :start AND :end " +
        "GROUP BY hour ORDER BY hour ASC")
List<HourCount> getHourlyCountSync(long start, long end);

// Returns count per day for a given period
@Query("SELECT strftime('%Y-%m-%d', datetime(timestamp/1000, 'unixepoch', 'localtime')) as day, " +
        "COUNT(*) as count FROM cigarette_log " +
        "WHERE timestamp BETWEEN :start AND :end " +
        "GROUP BY day ORDER BY day ASC")
List<DayCount> getDailyCountSync(long start, long end);

@Query("SELECT COUNT(*) FROM cigarette_log WHERE timestamp BETWEEN :start AND :end")
int getTotalCountSync(long start, long end);

@Query("SELECT COUNT(*) FROM cigarette_log")
LiveData<Integer> getTotalAllTimeLive();

// Simple data classes for grouped queries
class HourCount {
    public String hour;
    public int count;
}

class DayCount {
    public String day;
    public int count;
}