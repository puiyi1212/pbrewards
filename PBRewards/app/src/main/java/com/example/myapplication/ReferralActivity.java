package com.example.myapplication;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReferralActivity extends AppCompatActivity {

    private String REFERRAL_CODE;
    private static final String APP_LINK = "[YOUR_APP_LINK]"; // Replace with actual app link
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // Activity result launcher to track if sharing is completed
    private final ActivityResultLauncher<Intent> shareResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // User successfully shared, now add points
                    addPointsToUser();
                } else {
                    Toast.makeText(ReferralActivity.this, "Share not completed, no points added.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_referral);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("users");

        REFERRAL_CODE = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        // UI Elements
        ImageButton backButton = findViewById(R.id.btn_back);
        MaterialButton inviteButton = findViewById(R.id.inviteButton);
        ImageView copyIcon = findViewById(R.id.copyIcon);
        CardView referralCodeBox = findViewById(R.id.referralCodeBox);
        TextView referralCodeText = findViewById(R.id.referralCodeText);

        referralCodeText.setText(REFERRAL_CODE);

        // Set Click Listeners
        backButton.setOnClickListener(v -> finish());
        inviteButton.setOnClickListener(v -> shareReferralCode()); // No points added here
        copyIcon.setOnClickListener(v -> copyToClipboard(REFERRAL_CODE));
        referralCodeBox.setOnClickListener(v -> copyToClipboard(REFERRAL_CODE));
    }

    private void shareReferralCode() {
        String shareMessage = "Join me on PB Rewards! Use my referral code: " + REFERRAL_CODE + "\n\n" +
                "Download the app here: " + APP_LINK;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

        // Add WhatsApp as a preferred option if installed
        List<Intent> intentList = new ArrayList<>();
        if (isAppInstalled("com.whatsapp")) {
            Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
            whatsappIntent.setType("text/plain");
            whatsappIntent.setPackage("com.whatsapp");
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            intentList.add(whatsappIntent);
        }

        Intent chooserIntent = Intent.createChooser(shareIntent, "Share via");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Intent[0]));

        // Start the sharing activity and wait for result
        shareResultLauncher.launch(chooserIntent);
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Referral Code", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Referral Code Copied!", Toast.LENGTH_SHORT).show();
    }

    private boolean isAppInstalled(String packageName) {
        PackageManager packageManager = getPackageManager();
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void addPointsToUser() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference.child(userId).child("points").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int currentPoints = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                    int newPoints = currentPoints + 150;
                    saveRedemptionToHistory(150,true);

                    databaseReference.child(userId).child("points").setValue(newPoints);
                    Toast.makeText(ReferralActivity.this, "150 Points Added for Sharing!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ReferralActivity.this, "Error updating points", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void saveRedemptionToHistory(int points, boolean isEarned) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference redemptionsRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId).child("redemptions");

        // Create a new redemption entry
        String redemptionId = redemptionsRef.push().getKey();
        String date = new java.text.SimpleDateFormat("MMM dd, yyyy").format(new java.util.Date());

        // Store redemption details
        Map<String, Object> redemptionData = new HashMap<>();
        redemptionData.put("points", points);
        redemptionData.put("date", date);
        redemptionData.put("type", isEarned ? "Points Earned" : "Points Redeemed");

        redemptionsRef.child(redemptionId).setValue(redemptionData);
    }
}

