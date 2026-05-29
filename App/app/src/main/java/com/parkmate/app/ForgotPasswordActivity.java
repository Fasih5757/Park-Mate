package com.parkmate.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        TextInputEditText etEmail = findViewById(R.id.etEmail);
        Button btnRecover = findViewById(R.id.btnRecover);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        btnRecover.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(this, OtpActivity.class);
            i.putExtra("email", email);
            startActivity(i);
        });
    }
}