package com.example.projectict;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import java.util.List;

public class DriverQr extends AppCompatActivity{
        private static final String TAG = "Driver QR";
        private DecoratedBarcodeView barcodeView;
        private FrameLayout frameLayoutCamera;
        private TextView gpsText;
        private GoogleMap mMap;

        //BACKGROUND SERVICE
        private boolean isTracking = false;
        private static final String PREFS = "tracking_prefs";
        private static final String KEY_TRACKING = "isTracking";
        private static final String KEY_DRIVER_ID = "driverId";
        private final ActivityResultLauncher<String> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "Camera permission granted. Setting up scanner.");
                        setupAndStartScanner();
                    } else {
                        Log.w(TAG, "Camera permission denied.");
                        Toast.makeText(this, "Camera permission is required to scan codes.",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                });

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.driver_qr);
            gpsText = findViewById(R.id.gpsText);
            frameLayoutCamera = findViewById(R.id.frame_layout_camera);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        "tracking_channel",
                        "Bus Tracking",
                        NotificationManager.IMPORTANCE_LOW
                );
                NotificationManager manager = getSystemService(NotificationManager.class);
                manager.createNotificationChannel(channel);
            }
            SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
            boolean wasTracking = prefs.getBoolean(KEY_TRACKING, false);
            String savedDriverId = prefs.getString(KEY_DRIVER_ID, null);

            if (wasTracking && savedDriverId != null) {
                isTracking = true;
                startTrackingWith(savedDriverId); // resume map directly
            } else {
                checkCameraPermission(); // fresh start
            }
        }
        private void checkCameraPermission() {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                setupAndStartScanner();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        }
        private void setupAndStartScanner() {
            if (barcodeView == null) {
                barcodeView = new DecoratedBarcodeView(this);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                barcodeView.setLayoutParams(layoutParams);
                frameLayoutCamera.addView(barcodeView);
                barcodeView.decodeContinuous(barcodeCallback);
            }
            barcodeView.resume();
        }
        private BarcodeCallback barcodeCallback = new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() == null) return;

                barcodeView.pause();
                String scannedData = result.getText();
                String formatName = result.getBarcodeFormat().toString();
                Log.v(TAG, "Scanned Result: " + scannedData);

                AlertDialog.Builder builder = new AlertDialog.Builder(DriverQr.this);
                builder.setTitle("Confirm Start Tracking");
                builder.setMessage("Driver ID: " + scannedData + "\nFormat: " + formatName + "\n\nStart tracking?");
                builder.setPositiveButton("Yes", (dialog, which) -> {
                    dialog.dismiss();
                    frameLayoutCamera.removeAllViews();
                    barcodeView.pause();
                    startTrackingWith(scannedData);
                });
                builder.setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                    barcodeView.resume();  // Allow scanning again
                });
                builder.setCancelable(false);
                builder.create().show();
            }
                @Override
                public void possibleResultPoints(List<ResultPoint> resultPoints) {}
        };


    private void startTrackingWith(String driverId) {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putBoolean(KEY_TRACKING, true)
                .putString(KEY_DRIVER_ID, driverId)
                .apply();
        isTracking = true;

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag("map_fragment");

        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction()
                    .replace(R.id.frame_layout_camera, fragment, "map_fragment")
                    .commit();
        }

        ((SupportMapFragment) fragment).getMapAsync(googleMap -> {
            mMap = googleMap;

            FusedLocationProviderClient fusedClient = LocationServices.getFusedLocationProviderClient(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Zoom to last known location
                fusedClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 17));
                        mMap.addMarker(new MarkerOptions().position(current).title("You"));
                    }
                });

                // Optional: show blue location dot
                mMap.setMyLocationEnabled(true);
            }
        });

        gpsText.setText("Starting location tracking for " + driverId);
        findViewById(R.id.btnStopTracking).setVisibility(View.VISIBLE);

        // Start foreground location service
        Intent serviceIntent = new Intent(this, BackgroundService.class);
        serviceIntent.putExtra("driverId", driverId);
        ContextCompat.startForegroundService(this, serviceIntent);

        // Stop tracking logic
        findViewById(R.id.btnStopTracking).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Stop Tracking?")
                    .setMessage("Are you sure you want to stop tracking?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        dialog.dismiss();
                        stopService(new Intent(this, BackgroundService.class));
                        getSharedPreferences(PREFS, MODE_PRIVATE).edit().clear().apply();
                        isTracking = false;
                        gpsText.setText("Tracking stopped.");
                        Toast.makeText(this, "Tracking stopped.", Toast.LENGTH_SHORT).show();
                        findViewById(R.id.btnStopTracking).setVisibility(View.GONE);
                        frameLayoutCamera.removeAllViews();
                        setupAndStartScanner();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    public static class BusLocation {
            public double latitude, longitude;
            public BusLocation() {}
            public BusLocation(double lat, double lng) {
                this.latitude = lat;
                this.longitude = lng;
            }
        }
        private void startScanner() {
            if (barcodeView != null) {
                barcodeView.resume();
            }
        }
        private void pauseScanner() {
            if (barcodeView != null) {
                barcodeView.pause();
            }
        }
        @Override
        public void onResume() {
            super.onResume();
            if (!isTracking) {
                startScanner();
            }
        }
        @Override
        public void onPause() {
            super.onPause();
            pauseScanner();
        }
        @Override
        protected void onDestroy() {
            super.onDestroy();
            if (barcodeView != null) {
                if (barcodeView.getParent() instanceof ViewGroup) {
                    ((ViewGroup)
                            barcodeView.getParent()).removeView(barcodeView);
                }
            }
        }
        public void onBackPressed() {
            if (isTracking) {
            } else {
                super.onBackPressed();
            }
        }

    }
