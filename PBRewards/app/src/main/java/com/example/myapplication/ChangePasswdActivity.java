package com.example.myapplication;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import androidx.appcompat.app.AppCompatActivity;


public class ChangePasswdActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_passwd);
        backButton=findViewById(R.id.btn_back);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide(); // Hide the default action bar
        }

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ChangePasswdActivity.this,  AccountActivity.class);
            startActivity(intent);
        });

        auth = FirebaseAuth.getInstance();
    }

    // This method will be called when the user clicks the "Change Password" button
    public void onChangePassword(View view) {
        TextInputEditText oldPasswordField = findViewById(R.id.password);
        TextInputEditText newPasswordField = findViewById(R.id.newPassword);
        TextInputEditText confirmNewPasswordField = findViewById(R.id.confirmPassword);

        String oldPassword = oldPasswordField.getText().toString().trim();
        String newPassword = newPasswordField.getText().toString().trim();
        String confirmNewPassword = confirmNewPasswordField.getText().toString().trim();

        if (!newPassword.equals(confirmNewPassword)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);

            user.reauthenticate(credential).addOnSuccessListener(aVoid -> {
                user.updatePassword(newPassword)
                        .addOnSuccessListener(aVoid1 -> {
                            Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_SHORT).show();
                            finish(); // Close the activity after successful password change
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to change password: "
                                + e.getMessage(), Toast.LENGTH_SHORT).show());
            }).addOnFailureListener(e -> Toast.makeText(this, "Re-authentication failed: "
                    + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
        }
    }
}
