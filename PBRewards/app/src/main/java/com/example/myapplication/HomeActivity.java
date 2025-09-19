package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.database.DataSnapshot;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.Gravity;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private FirebaseDatabase database;
    private FirebaseFirestore db;
    private DatabaseReference databaseReference;
    private TextView pointsTextView;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide(); // Hide the default action bar
        }
        database = FirebaseDatabase.getInstance();
        db = FirebaseFirestore.getInstance();

        TextView user = findViewById(R.id.Username);
        TextView user2 = findViewById(R.id.username);
        TextView userID = findViewById(R.id.user_id);
        TextView user3 = findViewById(R.id.user_name);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();

        // Fetch membership ID from Firestore
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        user.setText(username != null ? username : "");
                        String username2 = documentSnapshot.getString("username");
                        user2.setText(username2 != null ? username2 : "");
                        String username3 = documentSnapshot.getString("username");
                        user3.setText(username3 != null ? username3 : "");
                        String membershipId = documentSnapshot.getString("membershipId");
                        userID.setText(membershipId != null ? membershipId : "PB-00000000");
                    }
                })
                .addOnFailureListener(e -> {
                        Log.e("AccountActivity", "Load failed", e);
                });

        databaseReference = database.getInstance().getReference("users");

        // Find account button
        ImageButton accButton = findViewById(R.id.toolbar_right_icon);

        // Set click listener to open MenuActivity
        accButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AccountActivity.class);
                startActivity(intent);
            }
        });

        ImageView rewards = findViewById(R.id.rewards_card);
        // Set click listener to open MenuActivity
        rewards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ScanActivity.class);
                startActivity(intent);
            }
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        ImageButton menuButton = findViewById(R.id.menu_button);

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(android.view.Gravity.LEFT)) {
                    drawerLayout.closeDrawer(android.view.Gravity.LEFT);
                } else {
                    drawerLayout.openDrawer(android.view.Gravity.LEFT);
                }
            }
        });

        ImageView promo2 =  findViewById(R.id.promo2);
        promo2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, Promo2Activity.class);
                startActivity(intent);
            }
        });
        ImageView promo1 =  findViewById(R.id.promo1);
        promo1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, Promo1Activity.class);
                startActivity(intent);
            }
        });

        Button redeem = findViewById(R.id.redeemBtn);
        redeem.setOnClickListener(v -> {
            int pointsToDeduct=0;
            deductPoints(pointsToDeduct);
            Intent intent = new Intent(HomeActivity.this, ProductActivity.class);
            startActivity(intent);
        });

        TextView history = findViewById(R.id.nav_points_history);
        history.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PointHistoryActivity.class);
            startActivity(intent);
        });

        TextView product = findViewById(R.id.nav_product);
        product.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProductActivity.class);
            startActivity(intent);
        });

        TextView location = findViewById(R.id.nav_stores);
        location.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, StoresActivity.class);
            startActivity(intent);
        });

        TextView referral = findViewById(R.id.nav_referral);
        referral.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ReferralActivity.class);
            startActivity(intent);
        });

        // Points Display
        pointsTextView = findViewById(R.id.points);
        loadUserPoints();
    }
    private void loadUserPoints() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference.child(userId).child("points").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        int points = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                        pointsTextView.setText(String.valueOf(points)); // Update UI
                    } else {
                        pointsTextView.setText("0");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    pointsTextView.setText("Error");
                }
            });
        }
    }

    // Method to update points in Firebase
    private void updateUserPoints(int newPoints) {
        if (currentUser != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            databaseReference.child(userId).child("points").setValue(newPoints);
        }
    }

    // Method to deduct points
    private void deductPoints(int pointsToDeduct) {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference.child(userId).child("points").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int currentPoints = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                    int newPoints = Math.max(0, currentPoints - pointsToDeduct); // Prevent negative points
                    updateUserPoints(newPoints); // Update with new points
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        }
    }
}


