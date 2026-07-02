package com.fsmoking.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "cigarette_log")
public class CigaretteLog {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    @ColumnInfo(name = "timestamp")
    private Date timestamp;

    @ColumnInfo(name = "mood")
    private String mood;

    @ColumnInfo(name = "trigger")
    private String trigger;

    @ColumnInfo(name = "location")
    private String location;

    @ColumnInfo(name = "note")
    private String note;

    public CigaretteLog(@NonNull Date timestamp, String mood, String trigger,
                        String location, String note) {
        this.timestamp = timestamp;
        this.mood = mood;
        this.trigger = trigger;
        this.location = location;
        this.note = note;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @NonNull
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(@NonNull Date timestamp) { this.timestamp = timestamp; }

    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }

    public String getTrigger() { return trigger; }
    public void setTrigger(String trigger) { this.trigger = trigger; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}