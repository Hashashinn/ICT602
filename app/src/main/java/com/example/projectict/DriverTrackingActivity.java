package com.example.projectict;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverTrackingActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private DatabaseReference locationRef;
    private String driverId;
    private TextView gpsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_tracking);

        // Initialize TextView
        gpsText = findViewById(R.id.gpsText);

        // Get driver ID from QR
        driverId = getIntent().getStringExtra("driverId");

        if (driverId == null || driverId.isEmpty()) {
            Toast.makeText(this, "Driver ID not found. Please scan again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Firebase + GPS setup
        locationRef = FirebaseDatabase.getInstance().getReference("bus_locations").child(driverId);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }

        LocationRequest request = LocationRequest.create()
                .setInterval(5000)
                .setFastestInterval(3000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                Location location = result.getLastLocation();
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    gpsText.setText("Lat: " + latitude + "\nLng: " + longitude);

                    // Send location to Firebase
                    locationRef.setValue(new BusLocation(latitude, longitude))
                            .addOnSuccessListener(unused ->
                                    Log.d("Firebase", "Location sent!")
                            )
                            .addOnFailureListener(e ->
                                    Log.e("Firebase", "Failed: " + e.getMessage())
                            );
                }
            }
        };


        fusedLocationClient.requestLocationUpdates(request, locationCallback, getMainLooper());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    // Simple model class
    public static class BusLocation {
        public double latitude, longitude;

        public BusLocation() {}

        public BusLocation(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
