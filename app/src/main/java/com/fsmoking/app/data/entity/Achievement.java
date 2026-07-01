package com.fsmoking.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents an achievement/badge the user can unlock.
 * Rows are pre-seeded with a fixed key; "unlocked" and "unlockedAt"
 * are updated when criteria are met.
 */
@Entity(tableName = "achievement")
public class Achievement {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "key")
    private String key; // e.g. "first_log", "one_smoke_free_day", "one_week", ...

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "icon_emoji")
    private String iconEmoji;

    @ColumnInfo(name = "unlocked")
    private boolean unlocked;

    @ColumnInfo(name = "unlocked_at")
    private Long unlockedAt; // epoch millis, nullable

    public Achievement(@NonNull String key, String title, String description,
                       String iconEmoji, boolean unlocked, Long unlockedAt) {
        this.key = key;
        this.title = title;
        this.description = description;
        this.iconEmoji = iconEmoji;
        this.unlocked = unlocked;
        this.unlockedAt = unlockedAt;
    }

    @NonNull
    public String getKey() { return key; }
    public void setKey(@NonNull String key) { this.key = key; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconEmoji() { return iconEmoji; }
    public void setIconEmoji(String iconEmoji) { this.iconEmoji = iconEmoji; }

    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }

    public Long getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(Long unlockedAt) { this.unlockedAt = unlockedAt; }
}