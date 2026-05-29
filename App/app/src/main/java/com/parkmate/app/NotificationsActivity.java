package com.parkmate.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.Toast;

public class NotificationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        findViewById(R.id.tvBack).setOnClickListener(v -> finish());

        SharedPreferences sp = getSharedPreferences("prefs", MODE_PRIVATE);
        SwitchCompat sw = findViewById(R.id.switchNotifications);
        sw.setChecked(sp.getBoolean("notifications_enabled", true));

        sw.setOnCheckedChangeListener((btn, checked) -> {
            sp.edit().putBoolean("notifications_enabled", checked).apply();
            Toast.makeText(this, checked ? "Notifications Enabled" : "Notifications Disabled", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.ivSystemSettings).setOnClickListener(v -> 
            Toast.makeText(this, "Opening System Settings...", Toast.LENGTH_SHORT).show());
    }
}
