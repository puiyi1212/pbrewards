package com.example.myapplication;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class Redemption2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redemption_2);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide(); // Hide the default action bar
        }
        ImageButton closeBtn = findViewById(R.id.btn_close);
        closeBtn.setOnClickListener(view -> finish());
    }
}