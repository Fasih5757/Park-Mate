package com.parkmate.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Splash screen logic: Decide where to go
        new Handler().postDelayed(() -> {
            boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                    .getBoolean("isFirstRun", true);

            if (isFirstRun) {
                // First time launch - Go to Onboarding
                startActivity(new Intent(MainActivity.this, OnboardingActivity.class));
            } else {
                // Not first time - Check if user is logged in AND stayLoggedIn is true
                boolean stayLoggedIn = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                        .getBoolean("stayLoggedIn", true);

                if (FirebaseAuth.getInstance().getCurrentUser() != null && stayLoggedIn) {
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                } else {
                    // Sign out if stayLoggedIn was false but Firebase session exists
                    if (!stayLoggedIn) {
                        FirebaseAuth.getInstance().signOut();
                    }
                    startActivity(new Intent(MainActivity.this, SignInActivity.class));
                }
            }
            finish();
        }, 2500);
    }
}