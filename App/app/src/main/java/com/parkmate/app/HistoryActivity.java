package com.parkmate.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        findViewById(R.id.tvClose).setOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rvHistory);
        rv.setLayoutManager(new LinearLayoutManager(this));

        boolean loggingEnabled = getSharedPreferences("prefs", MODE_PRIVATE)
                .getBoolean("log_parking", true);

        if (!loggingEnabled) {
            findViewById(R.id.rvHistory).setVisibility(View.GONE);
            TextView tvMsg = new TextView(this);
            tvMsg.setText(R.string.history_empty_message);
            tvMsg.setPadding(48, 48, 48, 48);
            tvMsg.setGravity(android.view.Gravity.CENTER);
            ((ViewGroup) findViewById(R.id.rvHistory).getParent()).addView(tvMsg);
        }

        List<ParkingRecord> data = ParkingStore.getHistory(this);
        TextView tvTicketCount = findViewById(R.id.tvTicketCount);
        TextView tvDistance = findViewById(R.id.tvDistance);
        updateStats(tvTicketCount, tvDistance, data);
        findViewById(R.id.ivRefresh).setOnClickListener(v -> {
            List<ParkingRecord> latest = ParkingStore.getHistory(this);
            updateStats(tvTicketCount, tvDistance, latest);
            rv.setAdapter(buildAdapter(latest));
        });

        rv.setAdapter(buildAdapter(data));
    }

    private RecyclerView.Adapter<RecyclerView.ViewHolder> buildAdapter(List<ParkingRecord> data) {
        return new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_history, parent, false);
                return new RecyclerView.ViewHolder(v) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                ParkingRecord item = data.get(position);
                ((TextView) holder.itemView.findViewById(R.id.tvAddress)).setText(item.name);
                ((TextView) holder.itemView.findViewById(R.id.tvDate)).setText(item.date);
                ((TextView) holder.itemView.findViewById(R.id.tvTime)).setText(item.time);
            }

            @Override
            public int getItemCount() { return data.size(); }
        };
    }

    private void updateStats(TextView tvTicketCount, TextView tvDistance, List<ParkingRecord> data) {
        tvTicketCount.setText(String.valueOf(data.size()));
        // Dummy tracked distance based on trip count, for UI parity until real GPS distance is logged.
        double distanceKm = Math.max(0.0, data.size() * 0.53);
        tvDistance.setText(String.format(Locale.US, "%.1f km", distanceKm));
    }
}