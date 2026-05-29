package com.parkmate.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.widget.RadioGroup;

public class ThemeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);

        findViewById(R.id.tvBack).setOnClickListener(v -> finish());

        SharedPreferences sp = getSharedPreferences("prefs", MODE_PRIVATE);
        RadioGroup rg = findViewById(R.id.rgTheme);

        int mode = sp.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (mode == AppCompatDelegate.MODE_NIGHT_YES) rg.check(R.id.rbDark);
        else if (mode == AppCompatDelegate.MODE_NIGHT_NO) rg.check(R.id.rbLight);
        else rg.check(R.id.rbSystem);

        rg.setOnCheckedChangeListener((g, id) -> {
            int newMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            if (id == R.id.rbDark) newMode = AppCompatDelegate.MODE_NIGHT_YES;
            else if (id == R.id.rbLight) newMode = AppCompatDelegate.MODE_NIGHT_NO;

            sp.edit().putInt("theme_mode", newMode).apply();
            AppCompatDelegate.setDefaultNightMode(newMode);
            
            // Re-create to apply theme change immediately
            recreate();
        });
    }
}
