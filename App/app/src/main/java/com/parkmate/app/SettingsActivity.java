package com.parkmate.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sp = getSharedPreferences("prefs", MODE_PRIVATE);

        findViewById(R.id.tvClose).setOnClickListener(v -> finish());

        SwitchCompat switchLog = findViewById(R.id.switchLogParking);
        switchLog.setChecked(sp.getBoolean("log_parking", true));
        switchLog.setOnCheckedChangeListener((btn, checked) -> 
            sp.edit().putBoolean("log_parking", checked).apply());

        RadioGroup rgMap = findViewById(R.id.rgMapMode);
        String mapMode = sp.getString("map_mode", "standard");
        if ("satellite".equals(mapMode)) rgMap.check(R.id.rbSatellite);
        else if ("hybrid".equals(mapMode)) rgMap.check(R.id.rbHybrid);
        else rgMap.check(R.id.rbStandard);

        rgMap.setOnCheckedChangeListener((g, id) -> {
            String mode = "standard";
            if (id == R.id.rbSatellite) mode = "satellite";
            else if (id == R.id.rbHybrid) mode = "hybrid";
            sp.edit().putString("map_mode", mode).apply();
            Toast.makeText(this, "Map mode updated", Toast.LENGTH_SHORT).show();
        });

        RadioGroup rgUnits = findViewById(R.id.rgUnits);
        String units = sp.getString("units", "system");
        if ("metric".equals(units)) rgUnits.check(R.id.rbMetric);
        else if ("imperial".equals(units)) rgUnits.check(R.id.rbImperial);
        else rgUnits.check(R.id.rbSystem);

        rgUnits.setOnCheckedChangeListener((g, id) -> {
            String u = "system";
            if (id == R.id.rbMetric) u = "metric";
            else if (id == R.id.rbImperial) u = "imperial";
            sp.edit().putString("units", u).apply();
        });

        findViewById(R.id.itemHistory).setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Do you want to clear your parking history?")
                        .setMessage("This action cannot be undone, all your previous parking logs will be lost completely.")
                        .setPositiveButton("Clear History", (d, w) -> {
                                ParkingStore.clearHistory(this);
                                Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show());

        findViewById(R.id.itemNotifications).setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class)));

        findViewById(R.id.itemLanguage).setOnClickListener(v ->
                startActivity(new Intent(this, LanguageActivity.class)));

        findViewById(R.id.itemTheme).setOnClickListener(v ->
                startActivity(new Intent(this, ThemeActivity.class)));

        findViewById(R.id.itemReview).setOnClickListener(v ->
                Toast.makeText(this, "Thanks for reviewing!", Toast.LENGTH_SHORT).show());
    }
}