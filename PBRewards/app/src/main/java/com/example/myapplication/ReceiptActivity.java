package com.example.myapplication;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReceiptActivity extends AppCompatActivity {
    private TextView remainingPointsTextView;
    private TextView pointsTextView;
    private TextView pointsTextView2;
    private TextView redemptionTimeTextView;
    private TextView rewardNameTextView;
    private TextView redemptionIdTextView;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide(); // Hide the default action bar
        }
        ImageButton backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(view -> finish());
        TextView nameText = findViewById(R.id.Username);
        TextView memberIdView = findViewById(R.id.memberID);
        db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        nameText.setText(username != null ? username : "");
                        String membershipId = documentSnapshot.getString("membershipId");
                        memberIdView.setText(membershipId != null ? membershipId : "PB-00000000");
                    }
                })
                .addOnFailureListener(e -> {
                    memberIdView.setText("Error loading ID");
                });

        remainingPointsTextView = findViewById(R.id.remainingPoints); // Assuming you have this TextView in your layout
        pointsTextView = findViewById(R.id.pointsRedeemed);
        pointsTextView2 = findViewById(R.id.points);
        redemptionTimeTextView = findViewById(R.id.redemptionTime);
        redemptionIdTextView = findViewById(R.id.invoiceId);
        rewardNameTextView = findViewById(R.id.rewardsName);

        // Retrieve the remaining points from the Intent
        int remainingPoints = getIntent().getIntExtra("remainingPoints",0);
        // Display the remaining points
        remainingPointsTextView.setText(String.valueOf(remainingPoints));

        String rewardName = getIntent().getStringExtra("rewardName");
        rewardNameTextView.setText(rewardName);

        Intent intent = getIntent();
        int points = intent.getIntExtra("pointsRedeemed", 0); // Default value: 0
        String redemptionTime = intent.getStringExtra("redemptionTime");  // Default value: null
        String redemptionId = intent.getStringExtra("invoiceId");

        pointsTextView.setText(String.valueOf(points));
        pointsTextView2.setText(points+" Points");
        redemptionTimeTextView.setText(redemptionTime);
        redemptionIdTextView.setText(redemptionId);
    }
}
