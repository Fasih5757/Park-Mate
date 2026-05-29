package com.parkmate.app;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class ParkingStore {
    private static final String PREFS_NAME = "parking_store";
    private static final String KEY_ACTIVE = "active_parking";
    private static final String KEY_HISTORY = "parking_history";
    private static final Gson GSON = new Gson();

    private ParkingStore() {
    }

    public static void saveActive(Context context, ParkingRecord record) {
        prefs(context).edit().putString(KEY_ACTIVE, GSON.toJson(record)).apply();
    }

    public static ParkingRecord getActive(Context context) {
        String json = prefs(context).getString(KEY_ACTIVE, null);
        if (json == null || json.isEmpty()) {
            return null;
        }
        return GSON.fromJson(json, ParkingRecord.class);
    }

    public static void completeActive(Context context) {
        ParkingRecord active = getActive(context);
        if (active == null) {
            return;
        }

        boolean loggingEnabled = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                .getBoolean("log_parking", true);

        if (active.includeInHistory && loggingEnabled) {
            List<ParkingRecord> history = getHistory(context);
            history.add(0, active);
            prefs(context).edit()
                    .putString(KEY_HISTORY, GSON.toJson(history))
                    .remove(KEY_ACTIVE)
                    .apply();
            return;
        }
        clearActive(context);
    }

    public static void clearHistory(Context context) {
        prefs(context).edit().remove(KEY_HISTORY).apply();
    }

    public static void clearActive(Context context) {
        prefs(context).edit().remove(KEY_ACTIVE).apply();
    }

    public static List<ParkingRecord> getHistory(Context context) {
        String json = prefs(context).getString(KEY_HISTORY, null);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<ParkingRecord>>() {}.getType();
        List<ParkingRecord> list = GSON.fromJson(json, type);
        return list == null ? new ArrayList<>() : list;
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
