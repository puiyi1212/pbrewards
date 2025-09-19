package com.example.myapplication;  // Change this to your actual package name

import android.content.Intent;
import android.os.Bundle;
import java.util.UUID;
import android.util.Log;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.CheckBox;
import android.app.DatePickerDialog;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private TextInputEditText emailField, passwordField, usernameField, birthdayField, referralField;
    private CheckBox termsCheckBox;
    private MaterialButton registerButton;
    private final Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide(); // Hide the default action bar
        }

        // Initialize Firebase

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bind UI elements
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        usernameField = findViewById(R.id.username);
        birthdayField = findViewById(R.id.birthday);
        referralField = findViewById(R.id.referral);
        termsCheckBox = findViewById(R.id.termsCheckBox);
        registerButton = findViewById(R.id.registerButton);
        registerButton.setEnabled(false);

        birthdayField.setOnClickListener(view -> showDatePicker());

        TextView login = findViewById(R.id.loginLink);
        login.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        ImageButton backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
        });

        termsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            registerButton.setEnabled(isChecked);
        });

        registerButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            String username = usernameField.getText().toString().trim();
            String birthday = birthdayField.getText().toString().trim();
            String referral = referralField.getText().toString().trim(); // Optional

            boolean isValid = true;

            if (email.isEmpty()) {
                ((TextInputLayout) emailField.getParent().getParent()).setError("Email is required");
                isValid = false;
            } else {
                ((TextInputLayout) emailField.getParent().getParent()).setError(null);
            }

            if (password.isEmpty()) {
                ((TextInputLayout) passwordField.getParent().getParent()).setError("Password is required");
                isValid = false;
            } else {
                ((TextInputLayout) passwordField.getParent().getParent()).setError(null);
            }

            if (username.isEmpty()) {
                ((TextInputLayout) usernameField.getParent().getParent()).setError("Username is required");
                isValid = false;
            } else {
                ((TextInputLayout) usernameField.getParent().getParent()).setError(null);
            }

            if (birthday.isEmpty()) {
                ((TextInputLayout) birthdayField.getParent().getParent()).setError("Birthday is required");
                isValid = false;
            } else {
                ((TextInputLayout) birthdayField.getParent().getParent()).setError(null);
            }

            if (!isValid) {
                return; // Stop registration if any required field is empty
            }

            // Proceed with registration if all required fields are filled
            registerUser(username, email, birthday, password, referral);
        });
    }
    private void showDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(Calendar.YEAR, selectedYear);
                    calendar.set(Calendar.MONTH, selectedMonth);
                    calendar.set(Calendar.DAY_OF_MONTH, selectedDay);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    birthdayField.setText(dateFormat.format(calendar.getTime()));
                }, year, month, day);

        datePickerDialog.show();
    }

    private void registerUser(String username, String email, String birthday, String password, String referral) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user.getUid(), username, email, birthday, referral);
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "❌ Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(String userId, String username, String email, String birthday, String referral) {
        String memberId = "PB-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        Map<String, Object> user = new HashMap<>();
        user.put("membershipId", memberId);
        user.put("username", username);
        user.put("email", email);
        user.put("birthday", birthday);
        user.put("referral", referral);

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "✅ User data saved successfully!");
                    Toast.makeText(RegisterActivity.this, "✅ Registered Successfully!", Toast.LENGTH_SHORT).show();

                    // 🚀 Navigate to HomeActivity after successful registration
                    Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish(); // Close RegisterActivity to prevent going back
                })
                .addOnFailureListener(e -> Log.e("Firestore", "❌ Error saving user data", e));
    }
}

