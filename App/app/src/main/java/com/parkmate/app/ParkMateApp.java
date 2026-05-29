package com.parkmate.app;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ParkMateApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sp = getSharedPreferences("prefs", MODE_PRIVATE);
        
        // Apply Language
        String lang = sp.getString("Locale.Helper.Selected.Language", "en");
        LocaleHelper.setLocale(this, lang);

        // Apply Theme
        int themeMode = sp.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }
}
