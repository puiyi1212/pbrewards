package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ImageButton;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;
import android.app.DatePickerDialog;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import androidx.core.content.ContextCompat;

public class AccountActivity extends AppCompatActivity {
    // Declare UI elements
    private TextInputEditText username, email, password, birthday;
    private TextInputLayout usernameLayout, emailLayout, passwordLayout, birthdayLayout;
    private ImageButton backButton, editProfileIcon, doneButton;
    private MaterialButton logoutButton, deleteButton;
    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private ImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profile_pictures");

        // Initialize UI elements
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        birthday = findViewById(R.id.birthday);

        usernameLayout = findViewById(R.id.usernameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        birthdayLayout = findViewById(R.id.birthdayLayout);

        profileImage = findViewById(R.id.profileImage);
        editProfileIcon = findViewById(R.id.editProfileIcon);
        logoutButton = findViewById(R.id.logoutButton);
        deleteButton = findViewById(R.id.deleteButton);

        backButton = findViewById(R.id.btn_back);
        doneButton = findViewById(R.id.toolbar_right_icon);

        loadUserData();
        loadProfilePicture();

        // Setup editable fields
        setupEditableField(usernameLayout, username, "username");
        setupEditableField(emailLayout, email, "email");
        setupEditableField(passwordLayout, password, "password");
        setupBirthdayField(birthdayLayout, birthday);

        doneButton.setOnClickListener(v -> {
            // Implement action to be taken on Done button click
            // For example, saving user changes or updating Firestore
            saveProfileChanges();
            Intent intent = new Intent(AccountActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        // Handle back button click
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        editProfileIcon.setOnClickListener(v -> showImagePickerDialog());

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });

        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void saveProfileChanges() {
        // Implement your logic to save changes, update Firestore, etc.
        // For now, let's just show a simple Toast
        Toast.makeText(this, "Changes saved!", Toast.LENGTH_SHORT).show();
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Profile Picture");
        builder.setItems(new CharSequence[]{"Choose from Gallery", "Remove Picture"}, (dialog, which) -> {
            switch (which) {
                case 0:
                    Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhotoIntent, PICK_IMAGE_REQUEST);
                    break;
                case 1:
                    // Remove Picture
                    profileImage.setImageResource(R.drawable.acc);
                    deleteProfilePictureFromStorage();
                    break;
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                // Save image locally
                String localPath = saveImageLocally(imageUri);

                // Set image in ImageView immediately
                Picasso.get().load(imageUri).into(profileImage);

                // Update Firestore with local path instead of uploading to Firebase Storage
                updateFirestoreProfilePicture(localPath);
            } else {
                Toast.makeText(this, "Failed to get image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String saveImageLocally(Uri imageUri) {
        try {
            // Open input stream from selected image URI
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // Get the file path
            File directory = new File(getFilesDir(), "profile_pictures");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File imageFile = new File(directory, "profile.jpg");

            // Save the bitmap to file
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            return imageFile.getAbsolutePath(); // Return local file path
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateFirestoreProfilePicture(String localPath) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && localPath != null) {
            db.collection("users").document(user.getUid())
                    .update("profilePictureUrl", localPath)
                    .addOnSuccessListener(aVoid -> Toast.makeText(AccountActivity.this,
                            "Profile picture updated locally", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(AccountActivity.this,
                            "Failed to update profile picture in Firestore", Toast.LENGTH_SHORT).show());
        }
    }

    private void loadProfilePicture() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Get the local file path from Firestore
                            String localPath = documentSnapshot.getString("profilePictureUrl");

                            // Check if the local path is valid
                            if (localPath != null && !localPath.isEmpty()) {
                                File imgFile = new File(localPath);
                                if (imgFile.exists()) {
                                    // Load the image from the local file
                                    Picasso.get().load(imgFile).into(profileImage);
                                } else {
                                    // Default image if the local file does not exist
                                    profileImage.setImageResource(R.drawable.acc);
                                }
                            } else {
                                // Default image if there's no path saved in Firestore
                                profileImage.setImageResource(R.drawable.acc);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AccountActivity.this, "Failed to load profile picture", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void deleteProfilePictureFromStorage() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            // Get the local image file path from Firestore
            db.collection("users").document(user.getUid())
                    .update("profilePictureUrl", null) // Remove URL from Firestore
                    .addOnSuccessListener(aVoid -> {
                        // Delete the local file
                        File directory = new File(getFilesDir(), "profile_pictures");
                        File imageFile = new File(directory, "profile.jpg");

                        if (imageFile.exists()) {
                            if (imageFile.delete()) {
                                Toast.makeText(AccountActivity.this,
                                        "Profile picture removed", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AccountActivity.this,
                                        "Failed to delete local file", Toast.LENGTH_SHORT).show();
                            }
                        }

                        // Reset ImageView to default
                        profileImage.setImageResource(R.drawable.acc);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AccountActivity.this,
                                "Failed to remove profile picture", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
                builder.setTitle("⚠️ Confirm Delete Account")
                .setMessage("Are you sure you want to delete your account? This cannot be undone.");
                builder.setPositiveButton("Delete", (dialog, which) -> deleteAccount());
                builder.setNegativeButton("Cancel", null);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        // Set the custom layout as the view for the dialog
        builder.setView(layout);

        builder.show();
    }

    private void deleteAccount() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
            builder.setTitle("🔒 Verify Password")
                    .setMessage("Please enter your password to confirm account deletion.");

            // Create input field
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            input.setHint("Enter password");
            input.setTextSize(16);
            input.setPadding(50, 30, 60, 30);
            input.setBackgroundResource(R.drawable.rounded_edit_text); // Rounded text field

            LinearLayout layout = new LinearLayout(this);
            layout.setPadding(40, 20, 40, 20);
            layout.addView(input);
            builder.setView(layout);

            // Buttons
            builder.setPositiveButton("Confirm", null); // Set later to prevent auto-dismiss
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            // Show dialog
            AlertDialog dialog = builder.create();
            dialog.show();

            // Get button after showing dialog (so we can control it)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String password = input.getText().toString().trim();
                if (password.isEmpty()) {
                    input.setError("Password is required!");
                    return;
                }

                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false); // Disable button to prevent multiple taps

                // Step 1: Re-authenticate
                user.reauthenticate(credential).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Step 2: Delete user from Firestore
                        db.collection("users").document(user.getUid()).delete().addOnSuccessListener(aVoid -> {
                            // Step 3: Delete from Firebase Auth
                            user.delete().addOnCompleteListener(deleteTask -> {
                                if (deleteTask.isSuccessful()) {
                                    Toast.makeText(AccountActivity.this, "✅ Account deleted successfully", Toast.LENGTH_SHORT).show();
                                    redirectToLogin();
                                } else {
                                    showError("❌ Failed to delete: " + deleteTask.getException().getMessage());
                                }
                            });
                        }).addOnFailureListener(e -> showError("❌ Failed to delete data: " + e.getMessage()));
                    } else {
                        showError("❌ Authentication failed: " + task.getException().getMessage());
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true); // Re-enable button
                    }
                });
            });
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e("AccountDeletion", message);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadUserData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Set name from username (no edit button)
                        TextView nameText = findViewById(R.id.nameText);
                        String username = documentSnapshot.getString("username");
                        nameText.setText(username != null ? username : "");

                        // Set member ID from Firestore
                        TextView memberIdView = findViewById(R.id.memberID);
                        String membershipId = documentSnapshot.getString("membershipId");
                        memberIdView.setText(membershipId != null ? membershipId : "PB-00000000");

                        TextInputEditText usernameField = findViewById(R.id.username);
                        usernameField.setText(documentSnapshot.getString("username"));
                        email.setText(documentSnapshot.getString("email"));
                        email.setEnabled(false);
                        birthday.setText(documentSnapshot.getString("birthday"));

                        TextInputEditText passwordField = findViewById(R.id.password);
                        if (passwordField != null) {
                            passwordField.setText("••••••"); // Masked password
                        }

                        // Load profile picture if exists
                        String profilePictureUrl = documentSnapshot.getString("profilePictureUrl");
                        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                            Picasso.get().load(profilePictureUrl).into(profileImage);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AccountActivity", "Load failed", e);
                });
    }

    private void setupEditableField(TextInputLayout layout, TextInputEditText field, String fieldType) {
        if (fieldType.equals("email")) {
            // Disable editing for email
            field.setEnabled(false);
            layout.setEndIconDrawable(null); // Remove edit icon for email
            return;
        }

        layout.setEndIconDrawable(ContextCompat.getDrawable(this, R.drawable.ic_edit_foreground));
        layout.setEndIconOnClickListener(v -> {

            if (fieldType.equals("password")) {
                // Navigate to ChangePasswordActivity when clicking the edit icon for password
                Intent intent = new Intent(AccountActivity.this, ChangePasswdActivity.class);
                startActivity(intent);
            } else {
                boolean isEditing = !field.isEnabled();
                field.setEnabled(isEditing);

                if (isEditing) {
                    field.requestFocus();
                    //layout.setEndIconDrawable(ContextCompat.getDrawable(AccountActivity.this, R.drawable.ic_save_foreground));
                } else {
                    String newValue = field.getText().toString().trim();
                    if (!newValue.isEmpty()) { // Only update Firestore if field has a value
                        updateFirestore(fieldType, newValue);
                        if (fieldType.equals("username")) {
                            // Directly update the TextView with the new username
                            TextView usernameText = findViewById(R.id.nameText);
                            usernameText.setText(newValue);
                        }
                    }
                    layout.setEndIconDrawable(ContextCompat.getDrawable(AccountActivity.this, R.drawable.ic_edit_foreground));
                }
            }
        });
    }


    private void setupBirthdayField(TextInputLayout layout, TextInputEditText field) {
        layout.setEndIconDrawable(ContextCompat.getDrawable(this, R.drawable.ic_edit_foreground));

        layout.setEndIconOnClickListener(v -> showDatePickerDialog(field));
    }

    private void showDatePickerDialog(TextInputEditText field) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    field.setText(formattedDate);
                    updateFirestore("birthday", formattedDate);
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    private void updateFirestore(String fieldType, String newValue) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .update(fieldType, newValue)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, fieldType + " updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Log.e("Firestore", "❌ Error updating " + fieldType, e));
    }
}

