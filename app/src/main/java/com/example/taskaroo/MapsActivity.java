package com.example.taskaroo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng selectedLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button selectLocationButton = findViewById(R.id.button_select_location);
        selectLocationButton.setOnClickListener(v -> {
            if (selectedLocation != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selected_lat", selectedLocation.latitude);
                resultIntent.putExtra("selected_lng", selectedLocation.longitude);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                // Handle the case where no location was selected
                // For example, you can display a toast message or provide a UI indication
                Toast.makeText(this, "Please select a location on the map.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Set default location (e.g., Colombo, Sri Lanka)
        LatLng defaultLocation = new LatLng(6.9271, 79.8612);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));

        // Set a click listener on the map
        mMap.setOnMapClickListener(latLng -> {
            // Clear previous markers
            mMap.clear();
            // Update the selected location
            selectedLocation = latLng;
            // Add a marker at the selected location
            mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            // Move the camera to the selected location
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        });
    }
}
