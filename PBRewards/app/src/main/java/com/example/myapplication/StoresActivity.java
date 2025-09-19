package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import com.google.android.gms.maps.model.LatLngBounds;
import android.widget.ScrollView;
import android.content.res.ColorStateList;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.widget.FrameLayout;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;


public class StoresActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Button btnList, btnMap;
    private ScrollView listScrollView;
    private FrameLayout mapContainer;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stores);

        btnList = findViewById(R.id.btnList);
        btnMap = findViewById(R.id.btnMap);
        listScrollView = findViewById(R.id.scrollView);
        mapContainer = findViewById(R.id.map_container);

        listScrollView.setVisibility(View.VISIBLE);  // Make sure this is correctly triggered
        mapContainer.setVisibility(View.GONE);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        btnList.setOnClickListener(v -> {
            listScrollView.setVisibility(View.VISIBLE);
            mapContainer.setVisibility(View.GONE);
            btnMap.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F7C500")));
            btnList.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1A0D64")));
            btnMap.setTextColor(Color.BLACK);
            btnList.setTextColor(Color.WHITE);
        });

        btnMap.setOnClickListener(v -> {
            listScrollView.setVisibility(View.GONE);
            mapContainer.setVisibility(View.VISIBLE);
            btnMap.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1A0D64")));
            btnList.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F7C500")));
            btnMap.setTextColor(Color.WHITE);
            btnList.setTextColor(Color.BLACK);
        });

        ImageButton backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> finish());

        // Load the map if not already added
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_container);

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.map_container, mapFragment)
                    .commit();

            mapFragment.getMapAsync(this);
        }
    }

            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker();

                    addOutletMarker(new LatLng(3.1480, 101.7135), "Paris Baguette Pavilion KL",
                            icon, builder);
                    addOutletMarker(new LatLng(3.0733, 101.6079), "Paris Baguette Sunway Pyramid",
                            icon, builder);
                    addOutletMarker(new LatLng(3.1496, 101.6168), "Paris Baguette 1 Utama", icon, builder);
                    addOutletMarker(new LatLng(3.1174, 101.6770), "Paris Baguette Mid Valley Megamall",
                            icon, builder);
                    addOutletMarker(new LatLng(3.0515, 101.6807), "Paris Baguette Pavilion Bukit Jalil",
                            icon, builder);
                    addOutletMarker(new LatLng(3.1422, 101.7104), "Paris Baguette TRX", icon, builder);
                    addOutletMarker(new LatLng(3.1343, 101.6865), "Paris Baguette NU Sentral", icon, builder);
                    addOutletMarker(new LatLng(3.1459, 101.6443), "Paris Baguette Pavilion Damansara Heights",
                            icon, builder);
                    addOutletMarker(new LatLng(3.4212, 101.7930), "Paris Baguette Genting Highlands",
                            icon, builder);

                // Create the bounds from all the markers
                LatLngBounds bounds = builder.build();

                // Adjust the camera to fit all markers within the map's view
                int padding = 100; // Optional: Use padding to give some space around the markers
                // After setting the bounds, set a specific zoom level
                LatLng center = bounds.getCenter();
                float zoomLevel = 10.5f;  // Set an appropriate zoom level based on your preference
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, zoomLevel));
                }

    private void addOutletMarker(LatLng location, String title, BitmapDescriptor icon, LatLngBounds.Builder builder) {
        mMap.addMarker(new MarkerOptions()
                .position(location)
                .title(title)
                .icon(icon));
            // Include the marker in the bounds calculation
            builder.include(location);
    }
}
