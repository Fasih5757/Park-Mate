package com.parkmate.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class OtpActivity extends AppCompatActivity {

    private EditText[] otpBoxes = new EditText[6];
    private String sentOtp, verificationId, email, phone, name, password, method;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        sentOtp = getIntent().getStringExtra("otp_code");
        verificationId = getIntent().getStringExtra("verificationId");
        email = getIntent().getStringExtra("email");
        phone = getIntent().getStringExtra("phone");
        name = getIntent().getStringExtra("name");
        password = getIntent().getStringExtra("password");
        method = getIntent().getStringExtra("method");

        TextView tvHint = findViewById(R.id.tvEmailHint);
        progressBar = findViewById(R.id.progressBar);

        if ("email".equals(method)) {
            tvHint.setText("Enter simulation code sent to " + email);
        } else {
            tvHint.setText("Enter verification code sent to " + phone);
        }

        otpBoxes[0] = findViewById(R.id.otp1);
        otpBoxes[1] = findViewById(R.id.otp2);
        otpBoxes[2] = findViewById(R.id.otp3);
        otpBoxes[3] = findViewById(R.id.otp4);
        otpBoxes[4] = findViewById(R.id.otp5);
        otpBoxes[5] = findViewById(R.id.otp6);

        setupOtpInputs();

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        ((Button) findViewById(R.id.btnVerify)).setOnClickListener(v -> {
            StringBuilder enteredOtp = new StringBuilder();
            for (EditText box : otpBoxes) enteredOtp.append(box.getText().toString());
            
            if (enteredOtp.length() < 6) {
                Toast.makeText(this, "Enter 6-digit OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            verifyCode(enteredOtp.toString());
        });
    }

    private void verifyCode(String code) {
        if ("email".equals(method)) {
            if (code.equals(sentOtp)) {
                registerUser();
            } else {
                Toast.makeText(this, "Invalid simulation code", Toast.LENGTH_SHORT).show();
            }
        } else {
            progressBar.setVisibility(View.VISIBLE);
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            signInWithPhoneCredential(credential);
        }
    }

    private void signInWithPhoneCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        registerUser();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Phone verification failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser() {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() || mAuth.getCurrentUser() != null) {
                        saveUserDetails();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserDetails() {
        String userId = mAuth.getCurrentUser().getUid();
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("phone", phone);
        user.put("userId", userId);
        user.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Account Created!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupOtpInputs() {
        for (int i = 0; i < 6; i++) {
            final int index = i;
            otpBoxes[i].addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int b, int count) {
                    if (s.length() == 1 && index < 5) otpBoxes[index + 1].requestFocus();
                }
                public void afterTextChanged(Editable s) {}
            });
            otpBoxes[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL && otpBoxes[index].getText().length() == 0 && index > 0) {
                    otpBoxes[index - 1].requestFocus();
                }
                return false;
            });
        }
    }
}