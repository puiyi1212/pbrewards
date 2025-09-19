package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class PointHistoryActivity extends AppCompatActivity {
    private LinearLayout historyLayout; // Container for the redemption history

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        ImageButton backBtn = findViewById(R.id.btn_back);
        backBtn.setOnClickListener(v -> finish());
        historyLayout = findViewById(R.id.historyLayout); // Reference to the history layout

        // Load points history when the activity is created
        loadPointsHistory();
    }
    private void loadPointsHistory() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference redemptionsRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId).child("redemptions");

        // Add listener to fetch the redemption history data
        redemptionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                historyLayout.removeAllViews();  // Remove old views before adding new ones
                for (DataSnapshot data : snapshot.getChildren()) {
                    // Get the redemption data
                    Map<String, Object> redemptionData = (Map<String, Object>) data.getValue();
                    Long pointsLong = (Long) redemptionData.get("points");
                    int points = pointsLong != null ? pointsLong.intValue() : 0;
                    String date = (String) redemptionData.get("date");
                    String type = (String) redemptionData.get("type");
                    String time = (String) redemptionData.get("time");
                    String rewardName = (String) redemptionData.get("rewardName");
                    Long remainingPointsLong = (Long) redemptionData.get("remainingPoints");
                    int remainingPoints = remainingPointsLong != null ? remainingPointsLong.intValue() : 0;  // Default to 0 if null
                    String redemptionId = data.getKey();

                    // Determine sign
                    String sign = type.equals("Points Redeemed") ? "-" : "+";

                    // Create a new LinearLayout for each redemption item
                    LinearLayout itemLayout = new LinearLayout(PointHistoryActivity.this);
                    itemLayout.setOrientation(LinearLayout.VERTICAL);
                    itemLayout.setPadding(8, 8, 8, 8);
                    //itemLayout.setBackgroundResource(R.drawable.history_background);  // Add background style

                    // Create the first horizontal layout for "Points Redeemed"
                    LinearLayout pointsLayout = new LinearLayout(PointHistoryActivity.this);
                    pointsLayout.setOrientation(LinearLayout.HORIZONTAL);

                    TextView typeTextView = new TextView(PointHistoryActivity.this);
                    typeTextView.setText(type);  // Set the type (Points Redeemed or Earned)
                    typeTextView.setTextColor(getResources().getColor(R.color.dark_blue));  // Change color as needed
                    typeTextView.setTextSize(15);
                    typeTextView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

                    // Create TextView for points
                    TextView pointsTextView = new TextView(PointHistoryActivity.this);
                    pointsTextView.setText(sign + points);
                    pointsTextView.setTextColor(getResources().getColor(R.color.dark_blue));
                    pointsTextView.setTextSize(15);
                    pointsTextView.setPadding(10, 0, 0, 0);
                    pointsTextView.setTypeface(null, android.graphics.Typeface.BOLD); // Bold text

                    pointsLayout.addView(typeTextView);
                    pointsLayout.addView(pointsTextView);

                    // Create TextView for Date
                    TextView dateTextView = new TextView(PointHistoryActivity.this);
                    dateTextView.setText(date);
                    dateTextView.setTextColor(getResources().getColor(R.color.dark_blue));
                    dateTextView.setTextSize(14);
                    dateTextView.setPadding(0, 2, 0, 0);

                    // Add all TextViews to the item layout
                    itemLayout.addView(pointsLayout);
                    itemLayout.addView(dateTextView);

                    // Create TextView for "View E-Receipt"
                    if (type.equals("Points Redeemed")) {
                        TextView receiptTextView = new TextView(PointHistoryActivity.this);
                        receiptTextView.setText("View E-Receipt");
                        receiptTextView.setTextColor(getResources().getColor(R.color.dark_blue));
                        receiptTextView.setTextSize(14);
                        receiptTextView.setPadding(0, 4, 0, 0);
                        receiptTextView.setTypeface(null, android.graphics.Typeface.BOLD); // Bold text

                        // Set the TextView to be clickable
                        receiptTextView.setClickable(true);
                        receiptTextView.setOnClickListener(view -> {
                            // Create an intent to navigate to the Receipt page
                            Intent intent = new Intent(PointHistoryActivity.this, ReceiptActivity.class);

                            // Optionally, pass extra data if needed
                            intent.putExtra("pointsRedeemed", points);
                            intent.putExtra("redemptionTime", date+" "+time);
                            intent.putExtra("invoiceId", redemptionId);
                            intent.putExtra("rewardName", rewardName);
                            intent.putExtra("remainingPoints", remainingPoints);

                            // Start the Receipt activity
                            startActivity(intent);
                        });

                        itemLayout.addView(receiptTextView);
                    }

                        // Create a divider view
                        View divider = new View(PointHistoryActivity.this);
                        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                1  // 1dp height
                        );
                        dividerParams.setMargins(0, 24, 0, 24);  // Add 16dp space above and below the divider
                        divider.setLayoutParams(dividerParams);
                        divider.setBackgroundColor(0xFFDDDDDD); // Light gray color


                        historyLayout.addView(divider, 0);
                        // Add item layout to the main history layout
                        historyLayout.addView(itemLayout,0);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(PointHistoryActivity.this, "Failed to load history", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }