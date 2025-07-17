package com.example.projectict;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.content.pm.PackageManager;
import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.journeyapps.barcodescanner.*;

import com.google.zxing.ResultPoint;
import java.util.List;

public class DriverScanActivity extends AppCompatActivity {
    private static final String TAG = "Driver QR";
    private DecoratedBarcodeView barcodeView;
    private FrameLayout frameLayoutCamera;
    private TextView gpsText;
    private GoogleMap mMap;

    private boolean isTracking = false;
    private static final String PREFS = "tracking_prefs";
    private static final String KEY_TRACKING = "isTracking";
    private static final String KEY_DRIVER_ID = "driverId";
    //  camera permission request
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) setupAndStartScanner();
                else {
                    Toast.makeText(this, "Camera permission is required to scan codes.", Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_qr);

        gpsText = findViewById(R.id.gpsText);
        frameLayoutCamera = findViewById(R.id.frame_layout_camera);
        // Create notification channel for foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("tracking_channel", "Bus Tracking", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        // Check if the app was already tracking (to restore after apps close)
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        if (prefs.getBoolean(KEY_TRACKING, false)) {
            String savedDriverId = prefs.getString(KEY_DRIVER_ID, null);
            if (savedDriverId != null) {
                isTracking = true;
                startTrackingWith(savedDriverId);
                return;
            }
        }
        checkCameraPermission();
    }
    // Check and request for camera permission
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            setupAndStartScanner();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    // Initializes the barcode scanner
    private void setupAndStartScanner() {
        if (barcodeView == null) {
            // Inflate custom layout
            View scannerView = getLayoutInflater().inflate(R.layout.custom_barcode_scanner, frameLayoutCamera, false);
            barcodeView = scannerView.findViewById(R.id.zxing_barcode_scanner);

            // Add to frame
            frameLayoutCamera.addView(scannerView);

            // Customize status text (optional)
            TextView statusView = scannerView.findViewById(R.id.zxing_status_view);
            if (statusView != null) {
                statusView.setText("Scan the driver's QR code to start tracking");
            }

            barcodeView.decodeContinuous(barcodeCallback);
        }
        barcodeView.resume();
    }

    // Handles barcode scan results
    private final BarcodeCallback barcodeCallback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() == null) return;
            barcodeView.pause();

            String scannedData = result.getText();
            // Confirmation Dialog after scan QR Code
            new AlertDialog.Builder(DriverScanActivity.this)
                    .setTitle("Confirm Start Tracking")
                    .setMessage("Driver ID: " + scannedData + "\n\nStart tracking?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        dialog.dismiss();
                        frameLayoutCamera.removeAllViews(); //remove qr code and bring out google map
                        barcodeView.pause();
                        startTrackingWith(scannedData);
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss();
                        barcodeView.resume(); //if no = continue show qr code
                    })
                    .setCancelable(false)
                    .show();
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {} //required
    };
    //Tracking Method trigger after driver press ok after scan
    private void startTrackingWith(String driverId) {
        // Save tracking state to SharedPreferences
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putBoolean(KEY_TRACKING, true)
                .putString(KEY_DRIVER_ID, driverId)
                .apply();

        isTracking = true;
        // Load Google Map fragment dynamically
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag("map_fragment");
        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction()
                    .replace(R.id.frame_layout_camera, fragment, "map_fragment")
                    .commit();
        }
        // display driver’s current location when the map is ready to display
        ((SupportMapFragment) fragment).getMapAsync(googleMap -> {
            mMap = googleMap;

            FusedLocationProviderClient fusedClient = LocationServices.getFusedLocationProviderClient(this);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


                fusedClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 17));
                        mMap.addMarker(new MarkerOptions().position(current).title("You"));
                    } else {
                        Toast.makeText(this, "Couldn't get location. Make sure GPS is on.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            }
        });

        // To show the driver that the tracking has been started
        gpsText.setText("Starting location tracking for " + driverId);
        findViewById(R.id.btnStopTracking).setVisibility(View.VISIBLE);
        // Start foreground service to run tracking in background
        Intent serviceIntent = new Intent(this, BackgroundService.class);
        serviceIntent.putExtra("driverId", driverId);
        ContextCompat.startForegroundService(this, serviceIntent);
        // Stop tracking button listener
        findViewById(R.id.btnStopTracking).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Stop Tracking?")
                    .setMessage("Are you sure you want to stop tracking?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Stop background service and reset state
                        stopService(new Intent(this, BackgroundService.class));
                        getSharedPreferences(PREFS, MODE_PRIVATE).edit().clear().apply();
                        isTracking = false;

                        Toast.makeText(this, "Tracking stopped.", Toast.LENGTH_SHORT).show();

                        // Return to DriverMainActivity
                        Intent backToMain = new Intent(this, DriverMainActivity.class);
                        backToMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(backToMain);
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }
    // Resume scanner when activity resumes (only if not tracking)
    @Override
    public void onResume() {
        super.onResume();
        if (!isTracking) startScanner();
    }
    // Pause scanner when activity is paused
    @Override
    public void onPause() {
        super.onPause();
        pauseScanner();
    }
    //QR Scanner
    private void startScanner() {
        if (barcodeView != null) barcodeView.resume();
    }
    private void pauseScanner() {
        if (barcodeView != null) barcodeView.pause();
    }
    // Remove scanner view when tracking start
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (barcodeView != null && barcodeView.getParent() instanceof ViewGroup) {
            ((ViewGroup) barcodeView.getParent()).removeView(barcodeView);
        }
    }
    // Disable back button once tracking starts to prevent accidental exit
    @Override
    public void onBackPressed() {
        if (!isTracking) super.onBackPressed();
    }
}
