package com.fsmoking.app.data;

/**
 * Simple data holder classes for grouped Room queries.
 * Kept outside the DAO interface to avoid Java interface limitations.
 */
public class DaoModels {

    public static class HourCount {
        public String hour;
        public int count;
    }

    public static class DayCount {
        public String day;
        public int count;
    }
}