package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswdActivity extends AppCompatActivity {

    private TextInputEditText emailInput;
    private MaterialButton sendResetLinkButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_passwd);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        Log.d("FirebaseAuth", "🔥 FirebaseAuth initialized successfully!");

        ImageButton backButton = findViewById(R.id.btn_back);
        emailInput = findViewById(R.id.email); // Ensure this matches your XML ID
        sendResetLinkButton = findViewById(R.id.resetButton);

        // Back button functionality
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswdActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Send Reset Link functionality
        sendResetLinkButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(ForgotPasswdActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPasswdActivity.this, "Reset link sent! Check your email.",
                                    Toast.LENGTH_LONG).show();
                            Log.d("ForgotPassword", "Password reset email sent.");
                        } else {
                            String errorMessage = task.getException() != null ? task.getException().getMessage() :
                                    "Unknown error occurred";
                            Toast.makeText(ForgotPasswdActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                            Log.e("ForgotPassword", "Failed to send reset email: " + errorMessage);
                        }
                    });
        });

    }
}



