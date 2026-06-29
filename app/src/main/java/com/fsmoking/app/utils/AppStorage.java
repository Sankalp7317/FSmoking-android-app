package com.fsmoking.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.fsmoking.app.models.UserData;
import com.fsmoking.app.models.Craving;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AppStorage {
    private static final String PREF_NAME    = "fsmoking_prefs";
    private static final String KEY_USER     = "user_data";
    private static final String KEY_CRAVINGS = "cravings";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public AppStorage(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // ── User Data ────────────────────────────────────
    public void saveUserData(UserData data) {
        prefs.edit().putString(KEY_USER, gson.toJson(data)).apply();
    }

    public UserData getUserData() {
        String json = prefs.getString(KEY_USER, null);
        if (json == null) return null;
        return gson.fromJson(json, UserData.class);
    }

    public boolean hasUserData() {
        return prefs.contains(KEY_USER);
    }

    // ── Cravings ─────────────────────────────────────
    public void addCraving(Craving c) {
        List<Craving> list = getCravings();
        list.add(c);
        prefs.edit().putString(KEY_CRAVINGS, gson.toJson(list)).apply();
    }

    public List<Craving> getCravings() {
        String json = prefs.getString(KEY_CRAVINGS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<Craving>>(){}.getType();
        return gson.fromJson(json, type);
    }

    // ── Reset ─────────────────────────────────────────
    public void clearAll() {
        prefs.edit().clear().apply();
    }
}