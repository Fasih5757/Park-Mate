package com.parkmate.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.webkit.JavascriptInterface;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private WebView webViewMap;
    private View btnParkContainer;
    private ImageView ivPark;
    private TextView tvParkLabel;
    private TextView tvCardAddress;
    private TextView tvCardTitle;
    private TextView tvCardSubtitle;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private GeolocationPermissions.Callback pendingGeoCallback;
    private String pendingGeoOrigin;
    private String selectedSpotName = "Downtown Parking";
    private double selectedSpotLat = 40.7128;
    private double selectedSpotLng = -74.0060;
    private boolean pageLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        webViewMap = findViewById(R.id.webViewMap);
        btnParkContainer = findViewById(R.id.btnPark);
        ivPark = findViewById(R.id.ivPark);
        tvParkLabel = findViewById(R.id.tvParkLabel);
        tvCardAddress = findViewById(R.id.tvCardAddress);
        tvCardTitle = findViewById(R.id.tvCardTitle);
        tvCardSubtitle = findViewById(R.id.tvCardSubtitle);
        WebSettings settings = webViewMap.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);

        webViewMap.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                pageLoaded = true;
                refreshUiForActiveParking();
            }
        });
        webViewMap.addJavascriptInterface(new MapBridge(), "AndroidBridge");
        webViewMap.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                if (ContextCompat.checkSelfPermission(
                        HomeActivity.this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) {
                    callback.invoke(origin, true, false);
                    return;
                }

                pendingGeoOrigin = origin;
                pendingGeoCallback = callback;
                ActivityCompat.requestPermissions(
                        HomeActivity.this,
                        new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                        },
                        LOCATION_PERMISSION_REQUEST_CODE
                );
            }
        });
        webViewMap.loadUrl("file:///android_asset/map.html");

        ensureLocationPermission();

        findViewById(R.id.ivSettings).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        findViewById(R.id.ivHistory).setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));

        btnParkContainer.setOnClickListener(v -> {
            ParkingRecord active = ParkingStore.getActive(this);
            if (active != null) {
                showDoneConfirmation();
                return;
            }
            Intent intent = new Intent(this, ParkDetailsActivity.class);
            intent.putExtra("spot_name", selectedSpotName);
            intent.putExtra("spot_lat", selectedSpotLat);
            intent.putExtra("spot_lng", selectedSpotLng);
            startActivity(intent);
        });

        findViewById(R.id.btnNavigate).setOnClickListener(v -> locateParkedCar());

        findViewById(R.id.btnLocate).setOnClickListener(v -> locateUserOnMap());
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyPreferences();
        refreshUiForActiveParking();
    }

    private void applyPreferences() {
        SharedPreferences sp = getSharedPreferences("prefs", MODE_PRIVATE);
        
        // Apply Map Mode
        String mapMode = sp.getString("map_mode", "standard");
        runJs(String.format("window.setMapMode('%s');", mapMode));
    }

    private void ensureLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (pendingGeoCallback != null && pendingGeoOrigin != null) {
            pendingGeoCallback.invoke(pendingGeoOrigin, granted, false);
            pendingGeoCallback = null;
            pendingGeoOrigin = null;
        }
    }

    private void refreshUiForActiveParking() {
        ParkingRecord active = ParkingStore.getActive(this);
        if (active == null) {
            tvParkLabel.setText(getString(R.string.park));
            ivPark.setImageResource(R.drawable.park);
            tvCardTitle.setText(getString(R.string.home_ready_to_park));
            tvCardAddress.setText(selectedSpotName);
            tvCardSubtitle.setText(getString(R.string.home_find_car_help));
            runJs("window.setActiveParking(null);");
            return;
        }
        tvParkLabel.setText(getString(R.string.done));
        ivPark.setImageResource(R.drawable.done);
        tvCardTitle.setText(getString(R.string.home_find_car_title));
        tvCardAddress.setText(active.name);
        tvCardSubtitle.setText(getString(R.string.home_find_car_subtitle));
        String js = String.format(
                Locale.US,
                "window.setActiveParking({name:%s,lat:%s,lng:%s});",
                quoted(active.name),
                active.lat,
                active.lng
        );
        runJs(js);
    }

    private void locateParkedCar() {
        ParkingRecord active = ParkingStore.getActive(this);
        if (active == null) {
            Toast.makeText(this, "No parked car found. Park first.", Toast.LENGTH_SHORT).show();
            return;
        }
        String js = String.format(
                Locale.US,
                "window.locateParkedCar({lat:%s,lng:%s,name:%s});",
                active.lat,
                active.lng,
                quoted(active.name)
        );
        runJs(js);
    }

    private void locateUserOnMap() {
        runJs("window.locateUser();");
    }

    private void runJs(String script) {
        if (!pageLoaded) {
            return;
        }
        webViewMap.evaluateJavascript(script, null);
    }

    private String quoted(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\"", "\\\"") + "\"";
    }

    private class MapBridge {
        @JavascriptInterface
        public void onParkingSpotSelected(String name, double lat, double lng) {
            runOnUiThread(() -> {
                selectedSpotName = name;
                selectedSpotLat = lat;
                selectedSpotLng = lng;
                if (ParkingStore.getActive(HomeActivity.this) == null) {
                    tvCardAddress.setText(selectedSpotName);
                }
            });
        }

        @JavascriptInterface
        public void onDistanceCalculated(String distanceKm) {
            runOnUiThread(() -> {
                if (ParkingStore.getActive(HomeActivity.this) != null) {
                    double dist = Double.parseDouble(distanceKm);
                    SharedPreferences sp = getSharedPreferences("prefs", MODE_PRIVATE);
                    String units = sp.getString("units", "system");
                    
                    String displayDistance;
                    if ("imperial".equals(units)) {
                        double miles = dist * 0.621371;
                        displayDistance = String.format(Locale.US, "%.2f mi", miles);
                    } else {
                        displayDistance = String.format(Locale.US, "%.2f km", dist);
                    }
                    tvCardSubtitle.setText(getString(R.string.distance_to_car, displayDistance));
                }
            });
        }
    }

    private void showDoneConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.done_confirm_title)
                .setMessage(R.string.done_confirm_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.done, (dialog, which) -> {
                    ParkingStore.completeActive(this);
                    Toast.makeText(this, "Parking session completed", Toast.LENGTH_SHORT).show();
                    refreshUiForActiveParking();
                })
                .show();
    }
}