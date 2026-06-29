package com.fsmoking.app.models;

public class Craving {
    private String date;
    private String time;
    private int durationSeconds;

    public Craving() {}

    public Craving(String date, String time, int durationSeconds) {
        this.date = date;
        this.time = time;
        this.durationSeconds = durationSeconds;
    }

    public String getDate() { return date; }
    public String getTime() { return time; }
    public int getDurationSeconds() { return durationSeconds; }

    public String getFormattedDuration() {
        int m = durationSeconds / 60;
        int s = durationSeconds % 60;
        return m > 0 ? m + "m " + s + "s" : s + "s";
    }
}