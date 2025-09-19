package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import java.util.HashMap;
import java.util.Map;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProductActivity extends AppCompatActivity {

    private Button redeemCakeButton, redeemSandwichButton, redeemWholecakeButton, redeemCakerollButton;
    private ImageButton backBtn;
    private int userPoints;
    private DatabaseReference pointsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide(); // Hide the default action bar
        }

        // Initialize Firebase reference for user points
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        pointsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("points");

        // Initialize UI elements
        backBtn = findViewById(R.id.btn_back);
        redeemCakeButton = findViewById(R.id.btnCake);
        redeemSandwichButton = findViewById(R.id.btnSandwich);
        redeemWholecakeButton = findViewById(R.id.btnWholeCake);
        redeemCakerollButton = findViewById(R.id.btnCakeRoll);
        disableButtons();
        fetchUserPoints();

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProductActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        redeemCakeButton.setOnClickListener(v -> redeemPoints(100, "Free Cake Slice"));
        redeemSandwichButton.setOnClickListener(v -> redeemPoints(300, "Free Sandwich, Salad or Wrap"));
        redeemWholecakeButton.setOnClickListener(v -> redeemPoints(200, "Free Whole Cake"));
        redeemCakerollButton.setOnClickListener(v -> redeemPoints(200, "Free Roll Cake"));
    }

    private void disableButtons() {
        redeemCakeButton.setEnabled(false);
        redeemSandwichButton.setEnabled(false);
        redeemWholecakeButton.setEnabled(false);
        redeemCakerollButton.setEnabled(false);
    }

    private void enableButtons() {
        redeemCakeButton.setEnabled(true);
        redeemSandwichButton.setEnabled(true);
        redeemWholecakeButton.setEnabled(true);
        redeemCakerollButton.setEnabled(true);
    }

    private void fetchUserPoints() {
        pointsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Handle if points are not yet initialized for this user
                userPoints = dataSnapshot.exists() ? dataSnapshot.getValue(Integer.class) : 0;

                // Enable buttons after fetching the points
                enableButtons();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error if any
                Toast.makeText(ProductActivity.this, "Failed to load points", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redeemPoints(int requiredPoints, String rewardType) {
        // Ensure that the user has enough points
        if (userPoints >= requiredPoints) {
            userPoints -= requiredPoints;
            pointsRef.setValue(userPoints); // Update Firebase with the new points value

            saveRedemptionToHistory(requiredPoints, true,rewardType);

            // Navigate to the redemption activity
            Intent intent = new Intent(ProductActivity.this, getRedemptionActivity(rewardType));
            Toast.makeText(this, "Points redeemed!", Toast.LENGTH_SHORT).show();

            // Pass the reward details
            intent.putExtra("rewardName", getRewardName(rewardType));
            intent.putExtra("rewardDescription", getRewardDescription(rewardType));
            intent.putExtra("rewardImage", getRewardImage(rewardType));
            intent.putExtra("rewardBarcode", R.drawable.barcode);

            startActivity(intent);
            } else {
            Toast.makeText(this, "Insufficient points for redemption!", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveRedemptionToHistory(int requiredPoints, boolean isRedeemed,  String rewardType) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference redemptionsRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId).child("redemptions");

        // Create a new redemption entry
        String redemptionId = redemptionsRef.push().getKey();
        String date = new java.text.SimpleDateFormat("MMM dd, yyyy").format(new java.util.Date());
        String time = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());

        // Store redemption details
        Map<String, Object> redemptionData = new HashMap<>();
        redemptionData.put("points", requiredPoints);
        redemptionData.put("rewardName", getRewardName(rewardType));
        redemptionData.put("remainingPoints", userPoints);
        redemptionData.put("date", date);
        redemptionData.put("time", time);
        redemptionData.put("type", isRedeemed ? "Points Redeemed" : "Points Earned");

        redemptionsRef.child(redemptionId).setValue(redemptionData);
    }

    // Returns the correct Redemption Activity based on the reward type
    private Class<?> getRedemptionActivity(String rewardType) {
        switch (rewardType) {
            case "Free Cake Slice":
                return Redemption1Activity.class;
            case "Free Sandwich, Salad or Wrap":
                return Redemption2Activity.class;
            case "Free Whole Cake":
                return Redemption3Activity.class;
            case "Free Roll Cake":
                return Redemption4Activity.class;
            default:
                return Redemption1Activity.class;
        }
    }

    private String getRewardName(String rewardType) {
        switch (rewardType) {
            case "Free Cake Slice": return "Blueberry Yogurt Slice Cake";
            case "Free Sandwich, Salad or Wrap": return "PB Club Sandwich";
            case "Free Whole Cake": return "Strawberry Soft Cream Cake";
            case "Free Roll Cake": return "Signature Roll Cake";
            default: return "Unknown Reward";
        }
    }

    private String getRewardDescription(String rewardType) {
        switch (rewardType) {
            case "Free Cake Slice": return "Soft sponge cake layered with creamy yogurt mousse and tangy blueberry compote";
            case "Free Sandwich, Salad or Wrap": return
                    "Multigrain toast sandwich filled with chicken breast slice, lettuce, tomato and whole grain mustard sauce";
            case "Free Whole Cake": return "Vanilla cake filled with soft cream and fresh strawberries";
            case "Free Roll Cake": return "Soft roll cake filled with sweet cream and raisins";
            default: return "No description available";
        }
    }

    private int getRewardImage(String rewardType) {
        switch (rewardType) {
            case "Free Cake Slice": return R.drawable.cake;
            case "Free Sandwich, Salad or Wrap": return R.drawable.sandwich;
            case "Free Whole Cake": return R.drawable.wholecake;
            case "Free Roll Cake": return R.drawable.cakeroll;
            default: return R.drawable.cake;
        }
    }
}
