package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Firebase initialization
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApplicationId("1:652619464806:android:8665830692172866e42da7d")
                    .setApiKey("AIzaSyDa4hU5ynf8CnvD9qOZCH4kj0cb9X6wCGY")
                    .setDatabaseUrl("https://pbrewards-cbfca-default-rtdb.firebaseio.com/") // Corrected
                    .setProjectId("pbrewards-cbfca")
                    .setStorageBucket("pbrewards-cbfca.appspot.com") // Corrected
                    .build();

            FirebaseApp.initializeApp(this, options);
        }

        Button startButton = findViewById(R.id.getStartedButton);
        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}


