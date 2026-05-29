package com.parkmate.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ParkDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park_details);

        SwitchCompat switchHistory = findViewById(R.id.switchHistory);
        SwitchCompat switchRemind = findViewById(R.id.switchRemind);
        Button btnPark = findViewById(R.id.btnPark);
        TextView tvAddress = findViewById(R.id.tvAddress);
        TextView tvDate = findViewById(R.id.tvDate);
        TextView tvTime = findViewById(R.id.tvTime);

        String incomingName = getIntent().getStringExtra("spot_name");
        double lat = getIntent().getDoubleExtra("spot_lat", 40.7128);
        double lng = getIntent().getDoubleExtra("spot_lng", -74.0060);
        if (incomingName == null || incomingName.trim().isEmpty()) {
            incomingName = getString(R.string.parking_address_hint);
        }
        final String name = incomingName;

        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
        tvAddress.setText(name);
        tvDate.setText(date);
        tvTime.setText(time);

        findViewById(R.id.tvCancel).setOnClickListener(v -> finish());

        findViewById(R.id.btnTicket).setOnClickListener(v ->
                Toast.makeText(this, "Ticket details coming soon", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnCar).setOnClickListener(v ->
                Toast.makeText(this, "Car info coming soon", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnNotes).setOnClickListener(v ->
                Toast.makeText(this, "Add notes here", Toast.LENGTH_SHORT).show());

        btnPark.setOnClickListener(v -> {
            ParkingRecord record = new ParkingRecord(
                    name,
                    lat,
                    lng,
                    date,
                    time,
                    switchHistory.isChecked(),
                    switchRemind.isChecked()
            );
            ParkingStore.saveActive(this, record);
            Toast.makeText(this, "Car parked successfully!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}