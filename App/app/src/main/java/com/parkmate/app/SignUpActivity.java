package com.parkmate.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private String name, email, phone, pass;
    private boolean isEmailOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBar);

        TextInputEditText etFullName = findViewById(R.id.etFullName);
        TextInputEditText etEmail = findViewById(R.id.etEmail);
        TextInputEditText etPhone = findViewById(R.id.etPhone);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        TextInputEditText etConfirm = findViewById(R.id.etConfirmPassword);
        RadioGroup rgOtpMethod = findViewById(R.id.rgOtpMethod);
        CheckBox cbTerms = findViewById(R.id.cbTerms);
        Button btnSignUp = findViewById(R.id.btnSignUp);
        TextView tvSignIn = findViewById(R.id.tvSignIn);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        btnSignUp.setOnClickListener(v -> {
            name = etFullName.getText().toString().trim();
            email = etEmail.getText().toString().trim();
            phone = etPhone.getText().toString().trim();
            pass = etPassword.getText().toString().trim();
            String confirm = etConfirm.getText().toString().trim();

            if (!validateInput(name, email, phone, pass, confirm, cbTerms)) {
                return;
            }

            isEmailOtp = rgOtpMethod.getCheckedRadioButtonId() == R.id.rbEmail;
            
            if (isEmailOtp) {
                sendEmailOtp();
            } else {
                sendPhoneOtp();
            }
        });

        tvSignIn.setOnClickListener(v -> finish());
    }

    private void sendPhoneOtp() {
        progressBar.setVisibility(View.VISIBLE);
        
        String phoneNumber = phone;
        if (!phoneNumber.startsWith("+")) {
            phoneNumber = "+92" + phoneNumber; // Adjust prefix as needed
        }

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = 
        new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
            progressBar.setVisibility(View.GONE);
            // Instant verification - proceed to register
            signInWithPhoneCredential(credential);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            progressBar.setVisibility(View.GONE);
            Log.e("PhoneAuth", "Verification failed", e);
            Toast.makeText(SignUpActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // Fallback: If it's a config error, still allow simulation for testing if needed?
            // No, better to fix the config.
        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                @NonNull PhoneAuthProvider.ForceResendingToken token) {
            progressBar.setVisibility(View.GONE);
            goToOtpActivity(verificationId, "phone", null);
        }
    };

    private void signInWithPhoneCredential(PhoneAuthCredential credential) {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        registerEmailPasswordUser();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Phone Sign-in failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendEmailOtp() {
        String otpCode = String.format("%06d", new Random().nextInt(1000000));
        Toast.makeText(this, "OTP: " + otpCode, Toast.LENGTH_LONG).show();
        goToOtpActivity(null, "email", otpCode);
    }

    private void goToOtpActivity(String verificationId, String method, String otpCode) {
        Intent intent = new Intent(this, OtpActivity.class);
        intent.putExtra("verificationId", verificationId);
        intent.putExtra("otp_code", otpCode);
        intent.putExtra("email", email);
        intent.putExtra("phone", phone);
        intent.putExtra("name", name);
        intent.putExtra("password", pass);
        intent.putExtra("method", method);
        startActivity(intent);
    }

    private void registerEmailPasswordUser() {
        // This is called after phone is verified to link or create the email account
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() || mAuth.getCurrentUser() != null) {
                        saveUserDetails();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Auth failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    startActivity(new Intent(this, HomeActivity.class));
                    finishAffinity();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateInput(String name, String email, String phone, String pass, String confirm, CheckBox cb) {
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (pass.length() < 8 || !pass.matches(".*[A-Z].*") || !pass.matches(".*[0-9].*")) {
            Toast.makeText(this, "Password: 8+ chars, 1 Upper, 1 Digit", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!pass.equals(confirm)) {
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!cb.isChecked()) {
            Toast.makeText(this, "Agree to terms", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}