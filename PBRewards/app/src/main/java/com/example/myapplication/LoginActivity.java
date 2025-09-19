package com.example.myapplication;  // Change this to your actual package name

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.google.android.material.button.MaterialButton;
import android.widget.ImageButton;
import com.google.android.material.textfield.TextInputLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText emailInput, passwordInput;
    private MaterialButton loginButton;
    private FirebaseAuth mAuth;
    private TextView forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        TextView reg = findViewById(R.id.registerLink);
        forgotPassword = findViewById(R.id.forgotPassword);

        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswdActivity.class);
                startActivity(intent);
            }
        });

        ImageButton backButton = findViewById(R.id.btn_back);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // UI Elements
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);

        // Login Button Click
        loginButton.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Initialize the validation flag
        boolean isValid = true;

        // Check if email is empty
        if (email.isEmpty()) {
            // Set error message for email
            ((TextInputLayout) emailInput.getParent().getParent()).setError("Email is required");
            isValid = false;
        } else {
            // Clear error message if email is not empty
            ((TextInputLayout) emailInput.getParent().getParent()).setError(null);
        }

        // Check if password is empty
        if (password.isEmpty()) {
            // Set error message for password
            ((TextInputLayout) passwordInput.getParent().getParent()).setError("Password is required");
            isValid = false;
        } else {
            // Clear error message if password is not empty
            ((TextInputLayout) passwordInput.getParent().getParent()).setError(null);
        }

        // If the inputs are not valid, stop the process
        if (!isValid) {
            return;
        }

        // Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login Failed! User not found.", Toast.LENGTH_SHORT).show();
                    }
                });

    TextView passwd = findViewById(R.id.forgotPassword);

        passwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswdActivity.class);
                startActivity(intent);
            }
        });
    }
}
