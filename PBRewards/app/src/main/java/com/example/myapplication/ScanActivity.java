package com.example.myapplication;

import android.content.Intent;
import android.widget.ProgressBar;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Hashtable;

public class ScanActivity extends AppCompatActivity {
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide(); // Hide the action bar
        }

        ImageButton closeBtn = findViewById(R.id.btn_close);
        ImageView qrImageView = findViewById(R.id.qr_code);
        TextView nameText = findViewById(R.id.nameText);
        TextView memberIdView = findViewById(R.id.memberID);
        db = FirebaseFirestore.getInstance();

        closeBtn.setOnClickListener(view -> finish());

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        // Fetch membership ID from Firestore
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        nameText.setText(username != null ? username : "");
                        String membershipId = documentSnapshot.getString("membershipId");
                        memberIdView.setText(membershipId != null ? membershipId : "PB-00000000");

                        // Generate QR Code
                        try {
                            Bitmap qrBitmap = generateQRCodeWithLogo(membershipId);
                            qrImageView.setImageBitmap(qrBitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    memberIdView.setText("Error loading ID");
                });
    }

    // Method to generate QR Code with logo in the center
    private Bitmap generateQRCodeWithLogo(String content) {
        int qrSize = 400;
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.MARGIN, 1); // Smaller white border
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, qrSize, qrSize, hints);

            Bitmap qrBitmap = Bitmap.createBitmap(qrSize, qrSize, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < qrSize; x++) {
                for (int y = 0; y < qrSize; y++) {
                    qrBitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.TRANSPARENT);
                }
            }

            Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
            return (logo != null) ? overlayLogo(qrBitmap, logo) : qrBitmap;

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap overlayLogo(Bitmap qrBitmap, Bitmap logo) {
        int qrSize = qrBitmap.getWidth();
        int logoSize = qrSize / 5;
        int centerX = (qrSize - logoSize) / 2;
        int centerY = (qrSize - logoSize) / 2;

        Bitmap scaledLogo = Bitmap.createScaledBitmap(logo, logoSize, logoSize, false);
        Bitmap finalBitmap = qrBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(finalBitmap);

        // Draw white circular background to avoid overlap with black dots
        Paint whitePaint = new Paint();
        whitePaint.setColor(Color.WHITE);
        whitePaint.setAntiAlias(true);
        canvas.drawCircle(centerX + logoSize / 2f, centerY + logoSize / 2f, logoSize / 2f + 10, whitePaint);

        canvas.drawBitmap(scaledLogo, centerX, centerY, null);
        return finalBitmap;
    }

}